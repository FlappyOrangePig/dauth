package com.infras.dauthsdk.wallet.impl.manager.task

import com.infras.dauthsdk.api.DAuthSDK
import com.infras.dauthsdk.api.entity.CreateWalletData
import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.api.entity.DAuthResult.Companion.SDK_ERROR_AA_ADDRESS_INVALID
import com.infras.dauthsdk.api.entity.DAuthResult.Companion.SDK_ERROR_CANNOT_GENERATE_ADDRESS
import com.infras.dauthsdk.api.entity.ResponseCode
import com.infras.dauthsdk.api.entity.traceResult
import com.infras.dauthsdk.api.entity.transformError
import com.infras.dauthsdk.login.model.BindWalletParam
import com.infras.dauthsdk.login.model.GetParticipantsRes
import com.infras.dauthsdk.login.network.MpcServiceConst.MpcSecretAlreadyBoundError
import com.infras.dauthsdk.login.network.RequestApiMpc
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.login.utils.LoginPrefs
import com.infras.dauthsdk.mpc.DAuthJniInvoker
import com.infras.dauthsdk.mpc.MpcKeyIds
import com.infras.dauthsdk.mpc.ext.ElapsedContext
import com.infras.dauthsdk.mpc.util.MergeResultUtil
import com.infras.dauthsdk.wallet.ext.digest
import com.infras.dauthsdk.wallet.impl.ConfigurationManager
import com.infras.dauthsdk.wallet.impl.manager.Managers
import com.infras.dauthsdk.wallet.impl.manager.WalletManager
import com.infras.dauthsdk.wallet.util.AssertUtil
import com.infras.dauthsdk.wallet.util.ThreadUtil
import com.infras.dauthsdk.wallet.util.sha3
import com.infras.dauthsdk.wallet.util.toHexString

private const val TAG = "CreateWalletTask"

internal class CreateWalletTask(
    private val context: ElapsedContext,
    private val loginPrefs: LoginPrefs,
    private val participants: List<GetParticipantsRes.Participant>,
) {
    private val mpcApi: RequestApiMpc = Managers.requestApiMpc
    private val keystore get() = Managers.mpcKeyStore
    private val walletPrefsV2 get() = Managers.walletPrefsV2
    private val requestApi = Managers.requestApi

    suspend fun execute(): DAuthResult<CreateWalletData> {
        ThreadUtil.assertInMainThread(false)
        val state = context.runSpending("getWalletState") {
            walletPrefsV2.getWalletState()
        }
        AssertUtil.assert(state < WalletManager.STATE_OK)

        val allKeys = context.runSpending("getAllKeys") {
            keystore.getAllKeys()
        }
        val keySize = allKeys.size
        val keys = when (keySize) {
            MpcKeyIds.getKeyIds().size -> {
                // 使用未提交的密钥
                DAuthLogger.e("keys to be submit", TAG)
                allKeys
            }
            else -> {
                val serverGenerateKey = ConfigurationManager.innerConfig.serverGenerateKey
                val generated = if (serverGenerateKey) {
                    val generateResult = context.runSpending("generate keys") { rh ->
                        val msgHashHex =
                            DAuthJniInvoker.genRandomMsg().toByteArray().sha3().toHexString()
                        val r = mpcApi.generateKeys(msgHashHex)
                        val list = if (r != null && r.isSuccess()) {
                            listOf(r.data.s0, r.data.s1, r.data.s2).filter { it.isNotEmpty() }
                        } else {
                            null
                        }
                        if (list == null) {
                            DAuthLogger.e("gen key network error", TAG)
                            DAuthResult.NetworkError()
                        } else if (list.size != MpcKeyIds.getKeyIds().size) {
                            DAuthLogger.e("gen key error", TAG)
                            DAuthResult.SdkError(DAuthResult.SDK_ERROR_GENERATE_KEY)
                        } else {
                            DAuthResult.Success(list)
                        }.also { rh.result = it.isSuccess() }
                    }
                    if (generateResult !is DAuthResult.Success) {
                        return DAuthResult.NetworkError()
                    }
                    generateResult.data
                } else {
                    // 创建三片
                    context.runSpending("generate keys") { rh ->
                        val preGenerateKeyManager = Managers.preGenerateKeyManager
                        val popped = preGenerateKeyManager.popKeys()
                        if (popped.size == MpcKeyIds.getKeyIds().size) {
                            DAuthLogger.d("use pre-generated", TAG)
                            popped
                        } else {
                            DAuthLogger.d("generate now", TAG)
                            DAuthJniInvoker.generateSignKeys().toList()
                        }.also {
                            rh.result = it.size == MpcKeyIds.getKeyIds().size
                        }
                    }
                }

                context.runSpending("save keys") {
                    DAuthLogger.d("save keys:${generated.map { it.length }}", TAG)
                    keystore.setAllKeys(generated)
                    walletPrefsV2.setWalletState(WalletManager.STATE_KEY_GENERATED)
                }

                generated
            }
        }.toTypedArray()

        // 秘钥求和
        val mergeResult = context.runSpending("merge keys") {
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

        // 生成账号
        val signerAddress = context.runSpending("generateEoaAddress") { rh ->
            val msg = DAuthJniInvoker.genRandomMsg()
            DAuthJniInvoker.generateEoaAddress(msg.toByteArray(), keys).also {
                DAuthLogger.i("generateEoaAddress $it", TAG)
            }.also {
                rh.result = !it.isNullOrEmpty()
            }
        } ?: return DAuthResult.SdkError(SDK_ERROR_CANNOT_GENERATE_ADDRESS)
        val aaAddressResult = context.runSpending("generateAAAddress") { rh ->
            // 根据EOA地址获取AA地址
            val web3m = Managers.web3m
            web3m.getAaAddressByEoaAddress(signerAddress)
                .also {
                    it.traceResult(TAG, "generateAAAddress")
                    rh.result = it.isSuccess()
                }
        }
        if (aaAddressResult !is DAuthResult.Success) {
            return aaAddressResult.transformError()
        }
        val aaAddress = aaAddressResult.data
        if (aaAddress.length <= 2) {
            DAuthLogger.e("aa address too short")
            return DAuthResult.SdkError(SDK_ERROR_AA_ADDRESS_INVALID)
        }

        val r = submitWalletAddressAndKeys(
            keys,
            signerAddress,
            aaAddress,
            context,
            mergeResult,
        )

        return r
    }

    private suspend fun submitWalletAddressAndKeys(
        keys: Array<String>,
        eoaAddress: String,
        aaAddress: String,
        context: ElapsedContext,
        mergeResult: String,
    ): DAuthResult<CreateWalletData> {
        // 绑定钱包地址
        val accessToken = loginPrefs.getAccessToken()
        val authId = loginPrefs.getAuthId()
        DAuthLogger.d("auth id=${authId}")
        val dauthKey = keys[MpcKeyIds.KEY_INDEX_DAUTH_SERVER]
        val bindWalletParam = BindWalletParam(
            accessToken, authId, aaAddress, BindWalletParam.WALLET_TYPE_AA,
            dauthKey,
            mergeResult
        )
        val bindResponse = context.runSpending("bindWallet") { rh ->
            requestApi.bindWallet(bindWalletParam).also {
                rh.result = (it?.isSuccess() == true)
            }
        }
        if (bindResponse == null) {
            DAuthLogger.e("bind network error", TAG)
            return DAuthResult.SdkError(DAuthResult.SDK_ERROR_BIND_WALLET)
        }
        if (!bindResponse.isSuccess()) {
            DAuthLogger.e("bind error:${bindResponse.info}", TAG)
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
            val p = context.runSpending("dispatch keys $keyId") { rh ->
                mpcApi.setKey(setKeyUrl, key, finalMergeResult).also {
                    rh.result = (it != null)
                }
            }
            DAuthLogger.d("set key $keyId ${key.digest()} ret=${p?.ret}", TAG)
            if (p == null) {
                DAuthLogger.e("set key $keyId network error", TAG)
                return DAuthResult.SdkError(DAuthResult.SDK_ERROR_SET_KEY)
            }
            when (p.ret) {
                ResponseCode.RESPONSE_CORRECT_CODE -> {
                    DAuthLogger.d("success", TAG)
                }

                MpcSecretAlreadyBoundError -> {
                    DAuthLogger.e("already bound", TAG)
                }

                else -> {
                    DAuthLogger.e("set key $keyId error:${p.info}", TAG)
                    return DAuthResult.SdkError(DAuthResult.SDK_ERROR_SET_KEY)
                }
            }
        }

        DAuthLogger.d("release temp keys", TAG)
        context.runSpending("releaseTempKeys") {
            keystore.releaseTempKeys()
        }

        val r = context.runSpending("save address") { rh ->
            if (walletPrefsV2.setAddresses(eoaAddress, aaAddress)) {
                DAuthResult.Success(CreateWalletData(aaAddress))
            } else {
                DAuthLogger.e("sp error", TAG)
                DAuthResult.SdkError(DAuthResult.SDK_ERROR_UNKNOWN)
            }.also {
                rh.result = it.isSuccess()
            }
        }
        DAuthLogger.d("submit result=$r", TAG)
        return r
    }


}