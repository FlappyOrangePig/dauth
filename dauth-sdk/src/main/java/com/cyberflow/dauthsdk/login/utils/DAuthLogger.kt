package com.cyberflow.dauthsdk.login.utils

import android.util.Log

object DAuthLogger {
    private const val TAG = "DAuthLogger"

    private val isOpenLog: Boolean get() = true

    private inline fun log(log: String, crossinline block: (String) -> Unit) {
        if (isOpenLog) {
            val maxLogSize = 1000
            for (i in 0..log.length / maxLogSize) {
                val start = i * maxLogSize
                var end = (i + 1) * maxLogSize
                end = if (end > log.length) log.length else end
                val segment = log.substring(start, end)
                if (i == 0) {
                    block.invoke("[tid=${Thread.currentThread().id}]$segment")
                } else {
                    block.invoke("        $segment")
                }

            }
        }
    }

    fun i(msg: String, tag: String = TAG) {
        log(msg) { log -> Log.i(tag, log) }
    }

    fun d(msg: String, tag: String = TAG) {
        log(msg) { log -> Log.d(tag, log) }
    }

    fun e(msg: String, tag: String = TAG) {
        log(msg) { log -> Log.e(tag, log) }
    }

    fun v(msg: String, tag: String = TAG) {
        log(msg) { log -> Log.v(tag, log) }
    }

    fun w(msg: String, tag: String = TAG) {
        log(msg) { log -> Log.w(tag, log) }
    }
}