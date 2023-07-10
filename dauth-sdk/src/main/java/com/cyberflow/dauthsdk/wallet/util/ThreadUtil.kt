package com.cyberflow.dauthsdk.wallet.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

internal object ThreadUtil {

    private val handle by lazy { Handler(Looper.getMainLooper()) }
    private val ioExecutorService by lazy { Executors.newCachedThreadPool() }

    fun isMainThread(): Boolean {
        return Looper.getMainLooper().thread == Thread.currentThread()
    }

    fun assertInMainThread(inOrNot: Boolean = true) {
        val isMainThread = isMainThread()
        val match = isMainThread == inOrNot
        AssertUtil.assert(match, "cannot be invoked in thread ${Thread.currentThread().id}")
    }

    fun runOnMainThread(delayed: Long = 0L, runnable: Runnable) {
        handle.postDelayed(runnable, delayed)
    }

    fun runOnWorkerThread(runnable: Runnable) {
        ioExecutorService.execute(runnable)
    }
}