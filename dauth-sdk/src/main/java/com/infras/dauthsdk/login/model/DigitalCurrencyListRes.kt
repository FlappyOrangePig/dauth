package com.infras.dauthsdk.login.model

import android.os.Parcelable
import com.infras.dauthsdk.login.network.BaseResponse
import kotlinx.parcelize.Parcelize

class DigitalCurrencyListRes(
    val data: Data?,
) : BaseResponse() {
    class Data {
        var fiat_info: List<FiatInfo>? = null
        var crypto_info: List<CryptoInfo>? = null
    }

    @Parcelize
    data class FiatInfo(
        var fiat_code: String? = null,
        var order_max_limit: String? = null,
        var order_min_limit: String? = null,
        var fiat_precision: Long = 0,
        var fiat_symbol: String? = null,
        var paymethod_info: List<PayMethodInfo>? = null,
    ) : Parcelable

    @Parcelize
    data class PayMethodInfo(
        var paymethod_id: String? = null,
        var paymethod_name: String? = null,
        var icon: String? = null,
    ) : Parcelable

    @Parcelize
    data class CryptoInfo(
        var crypto_code: String? = null,
        var crypto_icon: String? = null,
        var order_max_limit: String? = null,
        var order_min_limit: String? = null,
        var crypto_precision: Int = 0,
    ) : Parcelable
}