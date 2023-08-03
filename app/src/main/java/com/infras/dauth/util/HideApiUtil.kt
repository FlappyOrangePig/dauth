package com.infras.dauth.util

import com.infras.dauth.manager.AccountManager.sdk
import com.infras.dauthsdk.api.DAuthSDK
import com.infras.dauthsdk.api.IEoaWalletApi


object HideApiUtil {

    fun getEoaApi(): IEoaWalletApi {
        val sdk = sdk() as DAuthSDK
        val f = sdk.javaClass.getDeclaredField("eoaWalletApi")
        f.isAccessible = true
        return f.get(sdk) as IEoaWalletApi
    }
}