package com.cyberflow.dauthsdk.login.network

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
open class BaseResponse {
    var iRet: Int = 0
    var sMsg: String? = null
}