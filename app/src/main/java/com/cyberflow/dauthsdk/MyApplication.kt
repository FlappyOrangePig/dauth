package com.cyberflow.dauthsdk

import android.app.Application
import com.cyberflow.dauthsdk.login.api.bean.SdkConfig

private const val CONSUMER_KEY = "lolei76IdILu5LDW0OQsM3iGZ"
private const val CONSUMER_SECRET = "wMKmwqPoYzDqy8mUumm9RnnOIAHWNe4q7XayYmi7QS32wDOcuo"
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DAuthSDK.instance.initSDK(this, SdkConfig(CONSUMER_KEY, CONSUMER_SECRET,""))
    }
}