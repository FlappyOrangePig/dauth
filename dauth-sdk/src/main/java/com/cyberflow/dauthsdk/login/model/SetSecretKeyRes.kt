package com.cyberflow.dauthsdk.login.model

import com.cyberflow.dauthsdk.login.network.BaseResponse

class SetSecretKeyRes(
    val ret: Int,
    val info: String?,
    val data: String?,
) : BaseResponse()