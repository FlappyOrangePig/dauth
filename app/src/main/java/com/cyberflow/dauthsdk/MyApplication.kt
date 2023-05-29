package com.cyberflow.dauthsdk

import com.cyberflow.dauthsdk.twitter.TwitterLoginManager

class MyApplication: CyberFlowApplication() {

    override fun onCreate() {
        super.onCreate()
        TwitterLoginManager.instance.initTwitterSDK(this)
    }
}