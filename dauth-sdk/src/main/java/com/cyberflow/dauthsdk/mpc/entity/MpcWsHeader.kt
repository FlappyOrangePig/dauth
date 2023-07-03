package com.cyberflow.dauthsdk.mpc.entity

import androidx.annotation.Keep

@Keep
class MpcWsHeader(
    val signtype: String = "gg18",
    val src: String,
    val openid: String,
    val token: String,
    val bm: String
)