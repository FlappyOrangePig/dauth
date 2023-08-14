package com.infras.dauthsdk.login.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class JwtDecodeResponse(
    val account: String?,
    val nickname: String?,
    val head_img_url: String?,
    val iss: String?,

    //用户id
    var sub: String,
    val aud: List<String>?,
    val exp: Long = 0,
) {
    companion object {
        fun createInstance(): JwtDecodeResponse {
            return JwtDecodeResponse(
                "", "", "", "", "", null, 0
            )
        }
    }
}