package com.cyberflow.dauthsdk.wallet.util

import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.wallet.ext.safeApp

internal object AssertUtil {

    fun assert(condition: Boolean, message: String = "") {
        if (!condition) {
            val app = safeApp()
            when {
                app == null -> {
                    println(message)
                }

                DebugUtil.isAppDebuggable(app) -> {
                    throw java.lang.RuntimeException(message)
                }

                else -> {
                    DAuthLogger.e("assert error $message")
                }
            }
        }
    }
}