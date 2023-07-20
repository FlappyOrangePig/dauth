package com.cyberflow.dauthsdk.login.network

import com.cyberflow.dauthsdk.api.entity.ResponseCode
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class BaseResponse {
    var ret: Int = 0
    var info: String? = null

    fun isSuccess() = ret == ResponseCode.RESPONSE_CORRECT_CODE
}