package com.infras.dauthsdk.mpc.entity

import androidx.annotation.Keep

@Deprecated("use GG20 instead")
internal const val GG18 = "gg18"
internal const val GG20 = "gg20"

@Keep
class MpcWsHeader(
    val signtype: String = GG20,
    val src: String,
    val openid: String,
    val token: String,
    val bm: String
)