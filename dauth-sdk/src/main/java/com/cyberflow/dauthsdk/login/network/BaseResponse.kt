package com.cyberflow.dauthsdk.login.network

import kotlinx.serialization.Serializable

@Serializable
open class BaseResponse {
    var iRet: Int = 0
    var sMsg: String? = null
}