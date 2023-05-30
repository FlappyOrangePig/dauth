package com.cyberflow.dauthsdk.login

import android.app.Application
import com.cyberflow.dauthsdk.login.utils.DAuthLogger

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