package com.cyberflow.dauthsdk.api

import com.cyberflow.dauthsdk.login.api.ILoginApi
import com.cyberflow.dauthsdk.wallet.api.IWalletApi

interface IDAuthApi : ILoginApi, IWalletApi {
}