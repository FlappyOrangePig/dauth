package com.infras.dauth.manager

import android.app.Activity
import com.infras.dauth.ui.login.LoginActivity
import com.infras.dauthsdk.api.IDAuthApi
import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.api.entity.WalletAddressData

internal object AccountManager {

    private lateinit var sdk: IDAuthApi

    fun attachSdk(sdk: IDAuthApi){
        this.sdk = sdk
    }

    internal fun sdk() = sdk

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

    fun logout(activity: Activity) {
        sdk.logout()
        activity.finishAffinity()
        LoginActivity.launch(activity)
    }
}

internal fun sdk() = AccountManager.sdk()