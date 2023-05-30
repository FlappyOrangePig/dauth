package com.cyberflow.dauthsdk.wallet.util

import android.util.Log

private fun String.wrap(): String {
    return "[${Thread.currentThread().id}] $this"
}

object LogUtil {

    @JvmStatic
    fun v(tag: String, log: String) {
        Log.v(tag, log.wrap())
    }

    @JvmStatic
    fun d(tag: String, log: String) {
        Log.d(tag, log.wrap())
    }

    @JvmStatic
    fun i(tag: String, log: String) {
        Log.i(tag, log.wrap())
    }

    @JvmStatic
    fun w(tag: String, log: String) {
        Log.w(tag, log.wrap())
    }

    @JvmStatic
    fun e(tag: String, log: String) {
        Log.e(tag, log.wrap())
    }

    @JvmStatic
    fun f(tag: String, log: String) {
        Log.wtf(tag, log.wrap())
    }
}