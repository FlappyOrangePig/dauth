package com.cyberflow.dauthsdk.login.callback


interface BaseHttpCallback<T> {
    fun onResult(obj : String?)
    fun onFailed(errorMsg: String)
}