package com.cyberflow.dauthsdk.api.entity

sealed class LoginResultData {
    class Success(val code: Int?, val accessToken: String?, val openId: String?) : LoginResultData()
    class Failure(val code: Int?, val accessToken: String? = null, val openId: String? = null) : LoginResultData()
}