package com.cyberflow.dauthsdk.login.model

class GetSecretKeyParam(
    override var access_token: String,
    override val authid: String,
    val type: Int
) : BaseTokenRequestParam() {
    companion object {
        const val TYPE_KEY = 0
        const val TYPE_MERGE_RESULT = 1
    }
}