package com.infras.dauthsdk.login.model

import android.os.Parcelable
import com.infras.dauthsdk.login.network.BaseResponse
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

class DigitalCurrencyListRes(
    val data: Data?,
) : BaseResponse() {
    class Data(
        @Json(name = "fiat_list")
        var fiatList: List<FiatInfo>?,
        @Json(name = "crypto_list")
        var cryptoList: List<CryptoInfo>?
    )

    @Parcelize
    data class FiatInfo(
        @Json(name = "fiat_code")
        var fiatCode: String? = null,
        @Json(name = "order_max_limit")
        var orderMaxLimit: String? = null,
        @Json(name = "order_min_limit")
        var orderMinLimit: String? = null,
        @Json(name = "fiat_precision")
        var fiatPrecision: Long = 0,
        @Json(name = "fiat_symbol")
        var fiatSymbol: String? = null,
        @Json(name = "paymethod_info_list")
        var payMethodInfoList: List<PayMethodInfo>? = null,
    ) : Parcelable

    @Parcelize
    data class PayMethodInfo(
        @Json(name = "paymethod_id")
        var payMethodId: String?,
        @Json(name = "paymethod_name")
        var payMethodName: String?,
        var icon: String?,
    ) : Parcelable

    @Parcelize
    data class CryptoInfo(
        @Json(name = "crypto_code")
        var cryptoCode: String? = null,
        @Json(name = "crypto_icon")
        var cryptoIcon: String? = null,
        @Json(name = "order_max_limit")
        var orderMaxLimit: String? = null,
        @Json(name = "order_min_limit")
        var orderMinLimit: String? = null,
        @Json(name = "crypto_precision")
        var cryptoPrecision: Int = 0,
        @Json(name = "crypto_issuer")
        var cryptoIssuer: String? = null,
        var price: String? = null,
    ) : Parcelable
}