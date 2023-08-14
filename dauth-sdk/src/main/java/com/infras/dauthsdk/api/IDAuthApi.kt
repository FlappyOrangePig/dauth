package com.infras.dauthsdk.api

import com.infras.dauthsdk.api.annotation.DAuthExperimentalApi

interface IDAuthApi : ILoginApi, IAAWalletApi {

    @DAuthExperimentalApi
    fun getEoaApi(): IEoaWalletApi
}