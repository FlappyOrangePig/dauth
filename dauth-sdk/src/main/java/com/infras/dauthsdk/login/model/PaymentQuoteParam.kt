package com.infras.dauthsdk.login.model

class PaymentQuoteParam(
    val fiat_code: String,
    val crypto_code: String,
    val crypto_amount: String,
    val fiat_amount: String,
) : IAccessTokenRequest, IAuthorizationRequest