package com.infras.dauthsdk.login.model

import com.infras.dauthsdk.login.network.BaseResponse
import com.squareup.moshi.Json

class PaymentQuoteRes(
    val data: Data?,
) : BaseResponse() {
    class Data(
        @Json(name = "FiatAmount")
        val fiatAmount: String,
        @Json(name = "CryptoAmount")
        val cryptoAmount: String,
    )
}