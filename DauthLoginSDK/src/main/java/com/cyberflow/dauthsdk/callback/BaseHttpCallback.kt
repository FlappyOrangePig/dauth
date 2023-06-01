package com.cyberflow.dauthsdk.callback


interface BaseHttpCallback<T> {
    fun onResult(obj : String?)
    fun onFailed(errorMsg: String)
}