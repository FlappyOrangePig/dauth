package com.cyberflow.dauthsdk.util

import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.IEoaWalletApi
import com.cyberflow.dauthsdk.manager.sdk

object HideApiUtil {

    fun getEoaApi(): IEoaWalletApi {
        val sdk = sdk() as DAuthSDK
        val f = sdk.javaClass.getDeclaredField("eoaWalletApi")
        f.isAccessible = true
        return f.get(sdk) as IEoaWalletApi
    }
}