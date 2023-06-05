package com.cyberflow.dauthsdk

import com.cyberflow.dauthsdk.login.CyberFlowApplication
import com.twitter.sdk.android.core.Twitter

class MyApplication: CyberFlowApplication() {

    override fun onCreate() {
        super.onCreate()
        Twitter.initialize(this);
    }
}