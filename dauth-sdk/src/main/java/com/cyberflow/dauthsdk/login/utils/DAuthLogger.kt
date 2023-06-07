package com.cyberflow.dauthsdk.login.utils

import android.util.Log

object DAuthLogger {
    private const val TAG = "DAuthLogger"

    private val isOpenLog: Boolean get() = true

    private inline fun log(log: String, crossinline block: (String) -> Unit) {
        if (isOpenLog) block.invoke("[tid=${Thread.currentThread().id}]$log")
    }

    fun i(msg: String) {
        log(msg) { log -> Log.i(TAG, log) }
    }

    fun d(msg: String) {
        log(msg) { log -> Log.d(TAG, log) }
    }

    fun e(msg: String) {
        log(msg) { log -> Log.e(TAG, log) }
    }

    fun v(msg: String) {
        log(msg) { log -> Log.v(TAG, log) }
    }

    fun w(msg: String) {
        log(msg) { log -> Log.w(TAG, log) }
    }
}