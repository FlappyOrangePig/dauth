package com.cyberflow.dauthsdk.login.model

abstract class BaseTokenRequestParam {

    abstract var access_token: String

    abstract val authid: String
}