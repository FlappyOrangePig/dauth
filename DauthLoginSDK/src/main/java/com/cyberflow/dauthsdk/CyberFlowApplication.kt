package com.cyberflow.dauthsdk

import android.app.Application
import com.cyberflow.dauthsdk.twitter.TwitterLoginManager
import com.cyberflow.dauthsdk.utils.DAuthLogger

open class CyberFlowApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        DAuthLogger.d("CyberFlowApplication onCreate()")
    }

    companion object {
        internal lateinit var instance: CyberFlowApplication
    }
}