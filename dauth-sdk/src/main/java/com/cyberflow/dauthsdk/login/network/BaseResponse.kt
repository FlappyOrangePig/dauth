package com.cyberflow.dauthsdk.login.network

import com.cyberflow.dauthsdk.api.entity.ResponseCode
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class BaseResponse {
    var iRet: Int = 0
    var sMsg: String? = null

    fun isSuccess() = iRet == ResponseCode.RESPONSE_CORRECT_CODE
}