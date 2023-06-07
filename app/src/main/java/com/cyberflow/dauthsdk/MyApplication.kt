package com.cyberflow.dauthsdk

import android.app.Application
import com.cyberflow.dauthsdk.login.DAuthSDK
import com.cyberflow.dauthsdk.login.api.bean.SdkConfig

private const val CONSUMER_KEY = "tfCWoaQgJqsbAsYNKFM8r2rI3"
private const val CONSUMER_SECRET = "hUbRMtwQNgyaxRMCDaYRoezV9Z7xGoJk4i3kseFSFP4mfr3b9v"
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DAuthSDK.instance.initSDK(this, SdkConfig(CONSUMER_KEY, CONSUMER_SECRET,""))
    }
}