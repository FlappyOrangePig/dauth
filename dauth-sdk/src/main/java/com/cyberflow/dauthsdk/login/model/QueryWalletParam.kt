package com.cyberflow.dauthsdk.login.model

data class QueryWalletParam(
    val access_token: String?,
    val authid: String? = null,
    val sign: String? = null
) {

}