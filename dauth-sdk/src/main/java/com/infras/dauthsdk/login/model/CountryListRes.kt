package com.infras.dauthsdk.login.model

import com.infras.dauthsdk.login.network.BaseResponse
import com.squareup.moshi.Json

class CountryListRes(
    val data: Data?,
) : BaseResponse() {
    class Data(
        val list: List<CountryInfo>
    )

    class CountryInfo(
        @Json(name = "country_name")
        val countryName: String,
        @Json(name = "country_code")
        val countryCode: String,
        @Json(name = "phone_area_code")
        val phoneAreaCode: String,
        @Json(name = "is_support")
        val isSupport: Boolean,
        @Transient
        val useFullName: Boolean = false,
    )
}