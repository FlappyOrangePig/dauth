package com.infras.dauthsdk.login.model

import com.infras.dauthsdk.login.network.BaseResponse
import com.squareup.moshi.Json

class PaymentQuoteRes(
    val data: Data?,
) : BaseResponse() {
    class Data(
        @Json(name = "fiat_amount")
        val fiatAmount: String,
        @Json(name = "crypto_amount")
        val cryptoAmount: String,
    )
}