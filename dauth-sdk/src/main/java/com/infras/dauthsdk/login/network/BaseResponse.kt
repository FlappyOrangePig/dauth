package com.infras.dauthsdk.login.network

import com.infras.dauthsdk.api.entity.ResponseCode
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class BaseResponse {
    var ret: Int = 0
    var info: String? = null

    fun isSuccess() = ret == ResponseCode.RESPONSE_CORRECT_CODE
}


internal fun BaseResponse?.traceResult(tag: String, log: String): BaseResponse? {
    when {
        this == null -> {
            DAuthLogger.i("$log:network error", tag)
        }

        this.isSuccess() -> {
            DAuthLogger.i("$log:${this.info}", tag)
        }

        else -> {
            DAuthLogger.i("$log:${this.info}", tag)
        }
    }
    return this
}
