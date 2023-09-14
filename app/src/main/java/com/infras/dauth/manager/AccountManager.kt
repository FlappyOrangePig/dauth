package com.infras.dauth.manager

import android.app.Activity
import com.infras.dauth.ui.login.LoginActivity
import com.infras.dauthsdk.api.IDAuthApi
import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.api.entity.WalletAddressData

internal object AccountManager {

    lateinit var sdk: IDAuthApi
        private set

    fun attachSdk(sdk: IDAuthApi){
        this.sdk = sdk
    }

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

    fun getAuthId(): String {
        return try {
            val dauthLogin = Class.forName("com.infras.dauthsdk.api.DAuthSDK")
                .getDeclaredField("loginApi").also { it.isAccessible = true }
                .get(sdk)

            val prefs = Class.forName("com.infras.dauthsdk.login.impl.DAuthLogin")
                .getDeclaredMethod("getPrefs").also { it.isAccessible = true }
                .invoke(dauthLogin)


            val authId = Class.forName("com.infras.dauthsdk.login.utils.LoginPrefs")
                .getDeclaredMethod("getAuthId").also { it.isAccessible = true }
                .invoke(prefs) as String

            authId
        } catch (t: Throwable) {
            t.printStackTrace()
            ""
        }
    }
}