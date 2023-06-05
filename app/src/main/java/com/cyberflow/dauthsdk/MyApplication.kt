package com.cyberflow.dauthsdk

import android.app.Application
import com.cyberflow.dauthsdk.login.DAuthSDK
import com.cyberflow.dauthsdk.login.api.bean.SdkConfig

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DAuthSDK.instance.initSDK(this, SdkConfig("", ""))
    }
}