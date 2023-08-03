package com.infras.dauthsdk.wallet.impl.manager

import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.wallet.util.ThreadUtil

private const val TAG = "CrashManager"

internal class CrashManager internal constructor() {

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
        Thread.setDefaultUncaughtExceptionHandler(DAuthExceptionHandler(h))
    }
}

private class DAuthExceptionHandler(
    private val h: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        DAuthLogger.e("----------CRASH----------", TAG)
        h?.uncaughtException(t, e)
    }
}