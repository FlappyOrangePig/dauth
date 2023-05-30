package com.cyberflow.dauthsdk.login

data class DAuthUser(
    var email: String ?= null,
    var nickname: String ?= null,
    var head_img_url: String ?= null,
    var openid: String ?= null
)