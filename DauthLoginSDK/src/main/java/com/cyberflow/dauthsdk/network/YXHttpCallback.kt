package com.cyberflow.dauthsdk.network

interface YXHttpCallback<RESPONSE> {
    fun onResult(result: HttpResult<RESPONSE>)
}

sealed class HttpResult<RESPONSE> {
    class Success<RESPONSE>(val response: RESPONSE) : HttpResult<RESPONSE>()
    class Failure<RESPONSE>(val exception: java.lang.Exception) : HttpResult<RESPONSE>()
    class HttpError<RESPONSE>(val code: Int, val message: String) : HttpResult<RESPONSE>()
    class Exception<RESPONSE>(val exception: java.lang.Exception) : HttpResult<RESPONSE>()
}

