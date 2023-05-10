package com.cyberflow.dauthsdk

import android.app.Application
import com.cyberflow.dauthsdk.twitter.TwitterLoginManager

class CyberflowApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        TwitterLoginManager.instance.initTwitterSDK(this)
    }
}