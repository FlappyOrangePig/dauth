package com.cyberflow.dauthsdk.login.callback

interface ThirdPartyCallback {
    fun onResult(code: Int?)
}