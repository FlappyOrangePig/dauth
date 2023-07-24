package com.cyberflow.dauthsdk.manager

import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.IDAuthApi
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.WalletAddressData

object AccountManager {

    private var sdk: IDAuthApi = DAuthSDK.instance

    private suspend fun getAccountAddressResult(): DAuthResult<WalletAddressData> {
        return sdk.queryWalletAddress()
    }

    suspend fun getAccountAddress(): String? {
        return when (val addressResult = getAccountAddressResult()) {
            is DAuthResult.Success -> addressResult.data.aaAddress
            else -> null
        }
    }

    suspend fun isWalletExists(): Boolean {
        return !getAccountAddress().isNullOrEmpty()
    }
}