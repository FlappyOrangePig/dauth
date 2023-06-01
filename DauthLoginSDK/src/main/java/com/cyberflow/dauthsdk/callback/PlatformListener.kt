package com.cyberflow.dauthsdk.callback

import com.cyberflow.dauthsdk.login.DAuthUser

interface PlatformListener {
    //登录成功 hasUserMessage 是否获取到用户信息了
    fun onSuccess(hasUserMessage: Boolean, userBean: DAuthUser?)

    //登录取消
    fun onCancel()

    //登录失败
    fun onError(throwable: Throwable?, errorMsg: String?)
}