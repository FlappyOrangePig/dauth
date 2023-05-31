package com.cyberflow.dauthsdk.`interface`


interface DAuthCallback<T> {
    fun onResult(obj : T)
    fun onFailed(errorMsg: String)
}