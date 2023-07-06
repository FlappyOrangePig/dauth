package com.cyberflow.dauthsdk.login.model

class SetSecretKeyParam(
    override var access_token: String,
    override val authid: String,
    val keyshare: String,
    val keyresult: String?,
) : BaseTokenRequestParam()