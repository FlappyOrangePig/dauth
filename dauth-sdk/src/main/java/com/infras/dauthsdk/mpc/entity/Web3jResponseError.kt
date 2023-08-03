package com.infras.dauthsdk.mpc.entity

import androidx.annotation.Keep

@Keep
class Web3jResponseError(
    val code: Int,
    val message: String,
    val data: String,
)