package com.cyberflow.dauthsdk.login.model

import com.cyberflow.dauthsdk.login.network.BaseResponse
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class SendEmailVerifyCode : BaseResponse() {

}