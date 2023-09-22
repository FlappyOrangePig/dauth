package com.infras.dauthsdk.login.model

import com.infras.dauthsdk.login.network.BaseResponse

class OrderListRes(
    val data: Data?,
) : BaseResponse() {
    class Data(
        val nextId: Long = 0,
        val hasNextPage: Boolean = false,
        val list: List<Order>? = null,
    )

    class Order(
        val order_id: String?,
        val fiat_code: String?,
        val crypto_code: String?,
        val price: String?,
        val amount: String?,
        val quantity: String?,
        val create_time: Long,
        val state: String?,
    )
}