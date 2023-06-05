package com.cyberflow.dauthsdk.login.response

data class BaseResponseBody<T>(val code: Int, val msg: String, val data: T) {
    val isSuccessful get() = code == 0
}
