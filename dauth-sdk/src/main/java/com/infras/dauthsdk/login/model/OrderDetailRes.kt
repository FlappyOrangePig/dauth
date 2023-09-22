package com.infras.dauthsdk.login.model

import android.os.Parcelable
import com.infras.dauthsdk.login.network.BaseResponse
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

class OrderDetailRes(
    val data: Data?,
) : BaseResponse() {

    @Parcelize
    class Data(
        @Json(name = "order_id")
        val orderId: String? = null,
        val channel: String? = null,
        val amount: String? = null,
        val quantity: String? = null,
        val price: String? = null,
        val state: String? = null,
        @Json(name = "paymethod_info")
        val payMethodInfo: PayMethodInfo? = null,
        val fee: String? = null,
        @Json(name = "create_time")
        val createTime: Long = 0,
        @Json(name = "update_time")
        val updateTime: Long = 0,
        @Json(name = "pay_time")
        val payTime: Long = 0,
        @Json(name = "relese_crypto_time")
        val releaseCryptoTime: Int = 0,
        @Json(name = "appeal_time")
        val appealTime: Long = 0,
        @Json(name = "withdraw_time")
        val withdrawTime: Long = 0,
        @Json(name = "wallet_address")
        val walletAddress: String? = null,
        @Json(name = "trancation_id")
        val transactionId: String? = null,
        @Json(name = "fiat_code")
        val fiatCode: String? = null,
        @Json(name = "crypto_code")
        val cryptoCode: String? = null,
        @Json(name = "pay_timeout_time")
        val payTimeoutTime: Long = 0,
    ) : Parcelable

    @Parcelize
    class PayMethodValueInfo(
        val type: String? = null,
        val name: String? = null,
        val value: String? = null,
    ) : Parcelable

    @Parcelize
    class PayMethodInfo(
        @Json(name = "paymethod_name")
        val payMethodName: String? = null,
        @Json(name = "paymethod_username")
        val payMethodUsername: String? = null,
        @Json(name = "paymethod_value_info")
        val payMethodValueInfo: List<PayMethodValueInfo>? = null,
    ) : Parcelable
}