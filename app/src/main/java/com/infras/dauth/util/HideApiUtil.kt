package com.infras.dauth.util

import com.infras.dauth.manager.AccountManager
import com.infras.dauthsdk.api.IEoaWalletApi
import com.infras.dauthsdk.api.annotation.DAuthExperimentalApi

object HideApiUtil {

    @OptIn(DAuthExperimentalApi::class)
    fun getEoaApi(): IEoaWalletApi {
        return AccountManager.sdk.getEoaApi()
    }
}