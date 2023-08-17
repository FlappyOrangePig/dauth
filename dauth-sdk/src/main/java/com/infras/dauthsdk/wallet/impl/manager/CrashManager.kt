package com.infras.dauthsdk.wallet.impl.manager

import com.infras.dauthsdk.api.annotation.DAuthLogLevel
import com.infras.dauthsdk.wallet.util.ThreadUtil

private const val TAG = "CrashManager"

internal class CrashManager internal constructor(
    private val logManager: DLogManager
) {
    private var initialized = false

    init {
        initCrash()
    }

    private fun initCrash() {
        ThreadUtil.assertInMainThread(true)
        safeInitCrash()
    }

    @Synchronized
    private fun safeInitCrash() {
        if (!initialized) {
            initialized = true
            initCrashInner()
        }
    }

    private fun initCrashInner() {
        val h = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(DAuthExceptionHandler(h, logManager))
    }
}

private class DAuthExceptionHandler(
    private val h: Thread.UncaughtExceptionHandler?,
    private val logManager: DLogManager
) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        logManager.log(DAuthLogLevel.LEVEL_FATAL, TAG, "----------DAuth CRASH----------", false)
        logManager.log(DAuthLogLevel.LEVEL_FATAL, TAG, "thread=$t", false)
        logManager.log(DAuthLogLevel.LEVEL_FATAL, TAG, e.stackTraceToString(), false)
        h?.uncaughtException(t, e)
    }
}