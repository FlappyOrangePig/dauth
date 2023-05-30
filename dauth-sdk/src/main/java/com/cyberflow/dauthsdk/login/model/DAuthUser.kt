package com.cyberflow.dauthsdk.login.model

data class DAuthUser(
    var email: String ?= null,
    var nickname: String ?= null,
    var head_img_url: String ?= null,
    var openid: String ?= null      //钱包授权登录  参数钱包地址也传这个
)