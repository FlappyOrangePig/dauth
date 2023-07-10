package com.cyberflow.dauthsdk.wallet.util

import androidx.viewbinding.BuildConfig
import com.cyberflow.dauthsdk.login.utils.DAuthLogger

internal object AssertUtil {

    fun assert(condition: Boolean, message: String = "") {
        if (!condition) {
            if (BuildConfig.DEBUG) {
                throw java.lang.RuntimeException(message)
            } else {
                DAuthLogger.e("assert error $message")
            }
        }
    }
}