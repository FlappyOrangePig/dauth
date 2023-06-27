package com.cyberflow.dauthsdk.wallet.util

import android.os.Handler
import android.os.Looper
import androidx.viewbinding.BuildConfig
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import java.util.concurrent.Executors

internal object ThreadUtil {

    private val handle by lazy { Handler(Looper.getMainLooper()) }
    private val ioExecutorService by lazy { Executors.newCachedThreadPool() }

    fun isMainThread(): Boolean {
        return Looper.getMainLooper().thread == Thread.currentThread()
    }

    fun assertInMainThread(inOrNot: Boolean = true) {
        val condition = if (inOrNot) {
            !isMainThread()
        } else {
            isMainThread()
        }
        if (condition) {
            val msg = "cannot be invoked in thread ${Thread.currentThread().id}"
            if (BuildConfig.DEBUG) {
                throw java.lang.RuntimeException(msg)
            } else {
                DAuthLogger.e(msg)
            }
        }
    }

    fun runOnMainThread(delayed: Long = 0L, runnable: Runnable) {
        handle.postDelayed(runnable, delayed)
    }

    fun runOnWorkerThread(runnable: Runnable) {
        ioExecutorService.execute(runnable)
    }
}