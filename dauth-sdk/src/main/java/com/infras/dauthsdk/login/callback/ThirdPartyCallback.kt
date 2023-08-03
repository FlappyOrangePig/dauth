package com.infras.dauthsdk.login.callback

import com.infras.dauthsdk.api.entity.LoginResultData

interface ThirdPartyCallback {
    fun onResult(loginResultData: LoginResultData?)
}