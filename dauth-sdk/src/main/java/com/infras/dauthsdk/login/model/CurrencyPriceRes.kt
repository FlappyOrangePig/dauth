package com.infras.dauthsdk.login.model

import com.infras.dauthsdk.login.network.BaseResponse
import com.squareup.moshi.Json

class CurrencyPriceRes(
    val data: Data?,
) : BaseResponse() {
    class Data(
        val list: List<PriceInfo>
    )

    class PriceInfo(
        @Json(name = "fiat_code")
        val fiatCode: String?,
        @Json(name = "crypto_code")
        val cryptoCode: String?,
        @Json(name = "price")
        val price: String?,
    )
}