package com.infras.dauthsdk.login.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Participants {
    var id = 0
    /* 保存密钥的url */
    var key_url: String? = null
    /* 签名服务的url */
    var sign_url: String? = null
}