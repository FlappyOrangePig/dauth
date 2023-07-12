package com.cyberflow.dauthsdk.wallet.util

import android.content.Context
import android.content.pm.ApplicationInfo
import com.cyberflow.dauthsdk.login.utils.DAuthLogger

object DebugUtil {
    fun isAppDebuggable(context: Context) = try {
        val ai = context.applicationInfo
        (ai.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0)
    } catch (e: java.lang.Exception) {
        DAuthLogger.e(e.stackTraceToString())
        false
    }
}