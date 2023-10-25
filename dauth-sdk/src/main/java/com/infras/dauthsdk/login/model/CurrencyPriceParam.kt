package com.infras.dauthsdk.login.model

class CurrencyPriceParam(
    val fiat_list: String,
    val crypto_list: String,
) : IAccessTokenRequest, IAuthorizationRequest