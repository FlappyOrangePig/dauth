package com.infras.dauthsdk.wallet.impl.manager.task

import com.infras.dauthsdk.api.entity.CreateWalletData
import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.api.entity.traceResult
import com.infras.dauthsdk.api.entity.transformError
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.mpc.DAuthJniInvoker
import com.infras.dauthsdk.mpc.ext.ElapsedContext
import com.infras.dauthsdk.mpc.util.MergeResultUtil
import com.infras.dauthsdk.wallet.impl.manager.KeysToRestoreResult
import com.infras.dauthsdk.wallet.impl.manager.Managers
import com.infras.dauthsdk.wallet.impl.manager.WalletManager.Companion.STATE_INIT
import com.infras.dauthsdk.wallet.util.AssertUtil

private const val TAG = "RestoreWalletTask"

internal class RestoreWalletTask(
    private val context: ElapsedContext,
    private val restoreKeyInfo: KeysToRestoreResult.KeyInfo
) {
    suspend fun execute(): DAuthResult<CreateWalletData> {
        DAuthLogger.d("RestoreWalletTask execute", TAG)
        AssertUtil.assert(Managers.walletPrefsV2.getWalletState() == STATE_INIT)

        val localKey = context.runSpending("decodeKey") {
            MergeResultUtil.decodeKey(
                restoreKeyInfo.mergeResult,
                arrayOf(restoreKeyInfo.k1, restoreKeyInfo.k2)
            )
        }
        DAuthLogger.d("localKey len=${localKey?.length}", TAG)
        if (localKey.isNullOrEmpty()) {
            DAuthLogger.d("localKey calc error", TAG)
            return DAuthResult.SdkError(DAuthResult.SDK_ERROR_RESTORE_KEY_BY_MERGE_RESULT)
        }

        val newKeys = arrayOf(localKey, restoreKeyInfo.k1, restoreKeyInfo.k2)
        // 生成账号
        val signerAddress = context.runSpending("generateEoaAddress") {
            val msg = DAuthJniInvoker.genRandomMsg()
            DAuthJniInvoker.generateEoaAddress(msg.toByteArray(), newKeys).also {
                DAuthLogger.i("generateEoaAddress $it", TAG)
            }
        } ?: return DAuthResult.SdkError(DAuthResult.SDK_ERROR_CANNOT_GENERATE_ADDRESS)
        val aaAddressResult = context.runSpending("generateAAAddress") {
            // 根据EOA地址获取AA地址
            val web3m = Managers.web3m
            web3m.getAaAddressByEoaAddress(signerAddress)
                .also { it.traceResult(TAG, "generateAAAddress") }
        }
        if (aaAddressResult !is DAuthResult.Success) {
            return aaAddressResult.transformError()
        }
        val aaAddress = aaAddressResult.data
        if (aaAddress.length <= 2) {
            DAuthLogger.e("aa address too short")
            return DAuthResult.SdkError(DAuthResult.SDK_ERROR_AA_ADDRESS_INVALID)
        }

        DAuthLogger.d("setLocalKey", TAG)
        context.runSpending("setLocalKey") {
            Managers.mpcKeyStore.setLocalKey(localKey)
        }

        val r = context.runSpending("createResult") {
            if (Managers.walletPrefsV2.setAddresses(signerAddress, aaAddress)) {
                DAuthResult.Success(CreateWalletData(aaAddress))
            } else {
                DAuthLogger.e("sp error", TAG)
                DAuthResult.SdkError(DAuthResult.SDK_ERROR_UNKNOWN)
            }
        }
        DAuthLogger.d("submit result=$r", TAG)
        context.traceElapsedList()
        return r
    }
}