package com.infras.dauthsdk.login.model

import com.infras.dauthsdk.login.network.BaseResponse

class DigitalCurrencyListRes(
    val data: Data?,
) : BaseResponse() {
    class Data {
        var fiat_list: List<Fiat_list>? = null
        var crypto_list: List<Crypto_list>? = null
    }

    data class Fiat_list(
        var fiat_code: String? = null,
        var order_max_limit: Int = 0,
        var order_min_limit: Int = 0,
        var fiat_precision: Int = 0,
        var fiat_symbol: String? = null,
        var paymethod_info_list: List<Paymethod_info_list>? = null,
    )

    data class Paymethod_info_list(
        var paymethod_id: String? = null,
        var paymethod_name: String? = null,
        var icon: String? = null,
    )

    data class Crypto_list(
        var crypto_code: String? = null,
        var crypto_icon: String? = null,
        var order_max_limit: Int = 0,
        var order_min_limit: Int = 0,
        var crypto_precision: Int = 0,
    )
}