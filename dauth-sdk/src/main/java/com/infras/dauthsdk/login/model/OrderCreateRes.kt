package com.infras.dauthsdk.login.model

import com.infras.dauthsdk.login.network.BaseResponse
import com.squareup.moshi.Json

class OrderCreateRes(
    val data: Data?,
) : BaseResponse() {
    class Data(
        @Json(name = "order_id")
        val orderId: String?
    )
}