package com.cyberflow.dauthsdk.login.model

import com.cyberflow.dauthsdk.login.network.BaseResponse

class GetSecretKeyRes(
    val ret: Int,
    val info: String?,
    val data: String?,
) : BaseResponse()