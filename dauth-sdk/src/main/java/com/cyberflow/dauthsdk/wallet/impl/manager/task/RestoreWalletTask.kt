package com.cyberflow.dauthsdk.wallet.impl.manager.task

import com.cyberflow.dauthsdk.api.entity.CreateWalletData
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.mpc.MpcKeyStore
import com.cyberflow.dauthsdk.mpc.ext.runSpending
import com.cyberflow.dauthsdk.mpc.util.MergeResultUtil
import com.cyberflow.dauthsdk.wallet.impl.manager.KeysToRestoreResult
import com.cyberflow.dauthsdk.wallet.impl.manager.task.util.WalletTaskUtil
import com.cyberflow.dauthsdk.wallet.util.WalletPrefsV2

private const val TAG = "RestoreWalletTask"

class RestoreWalletTask(
    private val restoreKeyInfo: KeysToRestoreResult.KeyInfo
) {
    suspend fun execute(): DAuthResult<CreateWalletData> {
        DAuthLogger.d("RestoreWalletTask execute", TAG)

        val localKey = runSpending(TAG, "decode") {
            MergeResultUtil.decode(
                restoreKeyInfo.mergeResult,
                arrayOf(restoreKeyInfo.k1, restoreKeyInfo.k2)
            )
        }

        val newKeys = arrayOf(localKey, restoreKeyInfo.k1, restoreKeyInfo.k2)
        val addressResult = WalletTaskUtil.generateAddress(newKeys) ?: return DAuthResult.SdkError()

        MpcKeyStore.setLocalKey(localKey)

        return if (WalletPrefsV2.setAddresses(addressResult.first, addressResult.second)) {
            DAuthResult.Success(CreateWalletData(addressResult.second))
        } else {
            DAuthLogger.e("sp error", TAG)
            DAuthResult.SdkError()
        }
    }
}