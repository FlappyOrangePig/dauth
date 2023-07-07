package com.cyberflow.dauthsdk.wallet.impl.manager.task

import com.cyberflow.dauthsdk.api.entity.CreateWalletData
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.login.model.BindWalletParam
import com.cyberflow.dauthsdk.login.model.GetParticipantsRes
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.network.RequestApiMpc
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import com.cyberflow.dauthsdk.mpc.DAuthJniInvoker
import com.cyberflow.dauthsdk.mpc.MpcKeyIds
import com.cyberflow.dauthsdk.mpc.MpcKeyStore
import com.cyberflow.dauthsdk.mpc.util.MergeResultUtil
import com.cyberflow.dauthsdk.wallet.impl.manager.WalletManager
import com.cyberflow.dauthsdk.wallet.impl.manager.task.util.WalletTaskUtil
import com.cyberflow.dauthsdk.wallet.util.WalletPrefsV2

private const val TAG = "CreateWalletTask"

class CreateWalletTask(
    private val loginPrefs: LoginPrefs,
    private val participants: Array<GetParticipantsRes.Participant>
) {
    private val mpcApi: RequestApiMpc = RequestApiMpc(loginPrefs)
    private val keystore get() = MpcKeyStore
    private val skipGenerateMergeResult get() = false

    suspend fun execute(): DAuthResult<CreateWalletData> {
        val keys = DAuthJniInvoker.generateSignKeys()

        // 第一片存本地
        DAuthLogger.d("save 1st key", TAG)
        keystore.setAllKeys(keys.toList())
        WalletPrefsV2.setWalletState(WalletManager.STATE_KEY_GENERATED)

        // 秘钥求和
        val mergeResult = if (skipGenerateMergeResult) {
            "hahahaha"
        } else {
            MergeResultUtil.encode(keys)
        }
        val mergeResultLen = mergeResult.length
        DAuthLogger.d("key merge len=$mergeResultLen", TAG)
        if (mergeResultLen == 0) {
            return DAuthResult.SdkError(DAuthResult.SDK_ERROR_MERGE_RESULT)
        }
        keystore.setMergeResult(mergeResult)
        WalletPrefsV2.setWalletState(WalletManager.STATE_MERGE_RESULT_GENERATED)

        // 生成账号
        val addressResult = WalletTaskUtil.generateAddress(keys) ?: return DAuthResult.SdkError()

        return submitWalletAddressAndKeys(
            keys,
            addressResult.first,
            addressResult.second,
            mergeResult
        )
    }

    private suspend fun submitWalletAddressAndKeys(
        keys: Array<String>,
        eoaAddress: String,
        aaAddress: String,
        mergeResult: String,
    ): DAuthResult<CreateWalletData> {
        // 绑定钱包地址
        val accessToken = loginPrefs.getAccessToken()
        val authId = loginPrefs.getAuthId()
        DAuthLogger.d("auth id=$authId")
        val dauthKey = keys[MpcKeyIds.KEY_INDEX_DAUTH_SERVER]
        val bindWalletParam = BindWalletParam(
            accessToken, authId, aaAddress, BindWalletParam.WALLET_TYPE_AA,
            dauthKey,
            mergeResult
        )
        val bindResponse = RequestApi().bindWallet(bindWalletParam)
        if (bindResponse == null) {
            DAuthLogger.e("bind network error", TAG)
            return DAuthResult.SdkError(DAuthResult.SDK_ERROR_BIND_WALLET)
        }
        if (!bindResponse.isSuccess()) {
            DAuthLogger.e("bind error:${bindResponse.sMsg}", TAG)
            return DAuthResult.SdkError(DAuthResult.SDK_ERROR_BIND_WALLET)
        }

        // 把密钥分发给远端
        DAuthLogger.d("dispatch keys", TAG)
        participants.filter {
            it.id > MpcKeyIds.KEY_INDEX_LOCAL
                    && it.id <= MpcKeyIds.KEY_INDEX_APP_SERVER
                    && it.set_key_url.isNotEmpty()
        }.forEach { each ->
            val keyId = each.id
            DAuthLogger.d("dispatch key $keyId", TAG)
            val participantDAuth = participants[keyId]
            val setKeyUrl = participantDAuth.set_key_url
            val key = keys[keyId]
            val finalMergeResult = mergeResult.takeIf { keyId == MpcKeyIds.KEY_INDEX_DAUTH_SERVER }
            val p = mpcApi.setKey(setKeyUrl, key, finalMergeResult)
            if (p == null) {
                DAuthLogger.e("set key $keyId network error", TAG)
                return DAuthResult.SdkError()
            }
            if (!p.isSuccess()) {
                DAuthLogger.e("set key $keyId error:${p.sMsg}", TAG)
                return DAuthResult.SdkError()
            }
        }

        keystore.releaseTempKeys()

        return if (WalletPrefsV2.setAddresses(eoaAddress, aaAddress)) {
            DAuthResult.Success(CreateWalletData(aaAddress))
        } else {
            DAuthLogger.e("sp error", TAG)
            DAuthResult.SdkError()
        }
    }


}