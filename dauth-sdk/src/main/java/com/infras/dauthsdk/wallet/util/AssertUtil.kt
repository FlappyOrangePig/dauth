package com.infras.dauthsdk.wallet.util

import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.wallet.ext.safeApp

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