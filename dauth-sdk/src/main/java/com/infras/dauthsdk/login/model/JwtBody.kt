package com.infras.dauthsdk.login.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class JwtDecodeResponse {
    val account: String? = null
    val nickname: String? = null
    val head_img_url: String? = null
    val iss: String? = null
    //用户id
    val sub: String? = null
    val aud: List<String>? = null
    val exp: Long = 0
}