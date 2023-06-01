package com.cyberflow.dauthsdk.callback

import android.content.Intent


interface Platform {

    fun login()


    fun loginOut()


    fun isLogin(): Boolean


    fun setPlatformListener(platformListener: PlatformListener)


    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}