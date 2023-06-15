package com.cyberflow.dauthsdk.login.callback

import com.cyberflow.dauthsdk.api.entity.LoginResultData

interface ThirdPartyCallback {
    fun onResult(loginResultData: LoginResultData?)
}