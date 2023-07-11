package com.cyberflow.dauthsdk.wallet.impl.manager.task

import com.cyberflow.dauthsdk.api.entity.CreateWalletData
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.ResponseCode
import com.cyberflow.dauthsdk.login.model.BindWalletParam
import com.cyberflow.dauthsdk.login.model.GetParticipantsRes
import com.cyberflow.dauthsdk.login.network.MpcServiceConst.MpcSecretAlreadyBoundError
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.network.RequestApiMpc
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import com.cyberflow.dauthsdk.mpc.DAuthJniInvoker
import com.cyberflow.dauthsdk.mpc.MpcKeyIds
import com.cyberflow.dauthsdk.mpc.ext.runSpending
import com.cyberflow.dauthsdk.mpc.util.MergeResultUtil
import com.cyberflow.dauthsdk.wallet.ext.digest
import com.cyberflow.dauthsdk.wallet.impl.manager.Managers
import com.cyberflow.dauthsdk.wallet.impl.manager.WalletManager
import com.cyberflow.dauthsdk.wallet.impl.manager.task.util.WalletTaskUtil
import com.cyberflow.dauthsdk.wallet.util.AssertUtil
import com.cyberflow.dauthsdk.wallet.util.ThreadUtil

private const val TAG = "CreateWalletTask"

class CreateWalletTask(
    private val loginPrefs: LoginPrefs,
    private val participants: List<GetParticipantsRes.Participant>
) {
    private val mpcApi: RequestApiMpc = RequestApiMpc()
    private val keystore get() = Managers.mpcKeyStore
    private val walletPrefsV2 get() = Managers.walletPrefsV2

    suspend fun execute(): DAuthResult<CreateWalletData> {
        ThreadUtil.assertInMainThread(false)
        val state = walletPrefsV2.getWalletState()
        AssertUtil.assert(state < WalletManager.STATE_OK)

        val allKeys = keystore.getAllKeys()
        val keySize = allKeys.size
        val keys = when (keySize) {
            0 -> {
                // 创建三片
                val generated = runSpending(TAG, "generate keys") {
                    DAuthJniInvoker.generateSignKeys()
                }.toList()
                DAuthLogger.d("save keys:${generated.map { it.length }}", TAG)
                keystore.setAllKeys(generated)
                walletPrefsV2.setWalletState(WalletManager.STATE_KEY_GENERATED)
                generated
            }

            MpcKeyIds.getKeyIds().size -> {
                // 使用未提交的密钥
                DAuthLogger.e("keys to be submit", TAG)
                allKeys
            }

            else -> {
                AssertUtil.assert(false)
                return DAuthResult.SdkError(DAuthResult.SDK_ERROR_UNKNOWN)
            }
        }.toTypedArray()

        // 秘钥求和
        val mergeResult = runSpending(TAG, "merge keys") {
            MergeResultUtil.encodeKey(keys)
        }
        if (mergeResult.isNullOrEmpty()) {
            DAuthLogger.e("mergeResult is empty", TAG)
            return DAuthResult.SdkError(DAuthResult.SDK_ERROR_MERGE_RESULT)
        }

        val mergeResultLen = mergeResult.length
        DAuthLogger.d("key merge len=$mergeResultLen", TAG)
        if (mergeResultLen == 0) {
            return DAuthResult.SdkError(DAuthResult.SDK_ERROR_MERGE_RESULT)
        }
        keystore.setMergeResult(mergeResult)

        // 生成账号
        val addressResult = WalletTaskUtil.generateAddress(keys) ?: return DAuthResult.SdkError()

        return submitWalletAddressAndKeys(
            keys,
            addressResult.first,
            addressResult.second
        )
    }

    private suspend fun submitWalletAddressAndKeys(
        keys: Array<String>,
        eoaAddress: String,
        aaAddress: String,
    ): DAuthResult<CreateWalletData> {
        // 绑定钱包地址
        val accessToken = loginPrefs.getAccessToken()
        val authId = loginPrefs.getAuthId()
        DAuthLogger.d("auth id=$authId")
        val dauthKey = keys[MpcKeyIds.KEY_INDEX_DAUTH_SERVER]
        val mergeResult = Managers.mpcKeyStore.getMergeResult()
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
            it.id > MpcKeyIds.KEY_INDEX_LOCAL && it.id <= MpcKeyIds.KEY_INDEX_APP_SERVER
        }.forEach { each ->
            val keyId = each.id
            DAuthLogger.d("set key $keyId", TAG)
            val participantDAuth = participants[keyId]
            val setKeyUrl = participantDAuth.set_key_url
            val key = keys[keyId]
            val finalMergeResult = mergeResult.takeIf { keyId == MpcKeyIds.KEY_INDEX_DAUTH_SERVER }
            val p = mpcApi.setKey(setKeyUrl, key, finalMergeResult)
            DAuthLogger.d("set key $keyId ${key.digest()} ret=${p?.ret}", TAG)
            if (p == null) {
                DAuthLogger.e("set key $keyId network error", TAG)
                return DAuthResult.SdkError(DAuthResult.SDK_ERROR_SET_KEY)
            }
            when (p.ret) {
                ResponseCode.RESPONSE_CORRECT_CODE -> {
                    DAuthLogger.e("success", TAG)
                }

                MpcSecretAlreadyBoundError -> {
                    DAuthLogger.e("already bound", TAG)
                }

                else -> {
                    DAuthLogger.e("set key $keyId error:${p.sMsg}", TAG)
                    return DAuthResult.SdkError(DAuthResult.SDK_ERROR_SET_KEY)
                }
            }
        }

        DAuthLogger.d("release temp keys", TAG)
        keystore.releaseTempKeys()

        val r = if (walletPrefsV2.setAddresses(eoaAddress, aaAddress)) {
            DAuthResult.Success(CreateWalletData(aaAddress))
        } else {
            DAuthLogger.e("sp error", TAG)
            DAuthResult.SdkError(DAuthResult.SDK_ERROR_UNKNOWN)
        }
        DAuthLogger.d("submit result=$r", TAG)
        return r
    }


}