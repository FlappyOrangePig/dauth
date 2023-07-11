package com.cyberflow.dauthsdk.wallet.impl.manager.task

import com.cyberflow.dauthsdk.api.entity.CreateWalletData
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.mpc.ext.runSpending
import com.cyberflow.dauthsdk.mpc.util.MergeResultUtil
import com.cyberflow.dauthsdk.wallet.impl.manager.KeysToRestoreResult
import com.cyberflow.dauthsdk.wallet.impl.manager.Managers
import com.cyberflow.dauthsdk.wallet.impl.manager.WalletManager.Companion.STATE_INIT
import com.cyberflow.dauthsdk.wallet.impl.manager.task.util.WalletTaskUtil
import com.cyberflow.dauthsdk.wallet.util.AssertUtil

private const val TAG = "RestoreWalletTask"

class RestoreWalletTask(
    private val restoreKeyInfo: KeysToRestoreResult.KeyInfo
) {
    suspend fun execute(): DAuthResult<CreateWalletData> {
        DAuthLogger.d("RestoreWalletTask execute", TAG)
        AssertUtil.assert(Managers.walletPrefsV2.getWalletState() == STATE_INIT)

        val localKey = runSpending(TAG, "decode") {
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
        val addressResult = WalletTaskUtil.generateAddress(newKeys)
        DAuthLogger.d("addressResult=$addressResult", TAG)
        if (addressResult == null) {
            DAuthLogger.d("address error", TAG)
            return DAuthResult.SdkError(DAuthResult.SDK_ERROR_CANNOT_GENERATE_ADDRESS)
        }

        DAuthLogger.d("setLocalKey", TAG)
        Managers.mpcKeyStore.setLocalKey(localKey)

        val r = if (Managers.walletPrefsV2.setAddresses(addressResult.first, addressResult.second)) {
            DAuthResult.Success(CreateWalletData(addressResult.second))
        } else {
            DAuthLogger.e("sp error", TAG)
            DAuthResult.SdkError(DAuthResult.SDK_ERROR_UNKNOWN)
        }
        DAuthLogger.d("submit result=$r", TAG)
        return r
    }
}