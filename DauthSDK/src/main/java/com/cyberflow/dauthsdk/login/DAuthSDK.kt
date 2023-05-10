package com.cyberflow.dauthsdk.login

import android.app.Activity
import com.google.android.gms.common.GoogleApiAvailability

class DAuthSDK {

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            DAuthSDK()
        }
    }

    fun initSDK(appId: String, appKey: String) {

    }

  fun login() {

  }


}