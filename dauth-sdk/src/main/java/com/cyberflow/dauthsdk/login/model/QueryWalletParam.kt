package com.cyberflow.dauthsdk.login.model

data class QueryWalletParam(
    var access_token: String?,
    val authid: String? = null,
    val sign: String? = null
) {

}