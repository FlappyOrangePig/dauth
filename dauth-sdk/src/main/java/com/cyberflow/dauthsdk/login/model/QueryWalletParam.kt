package com.cyberflow.dauthsdk.login.model

import kotlinx.serialization.Serializable


@Serializable
data class QueryWalletParam(
    var access_token: String?,
    val authid: String? = null,
    val sign: String? = null
) {

}