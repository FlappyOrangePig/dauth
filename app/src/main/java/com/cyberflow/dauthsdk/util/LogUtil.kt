package com.cyberflow.dauthsdk.util

import android.util.Log

object LogUtil {

    fun v(tag: String, log: String) {
        Log.v(tag, log)
    }

    fun d(tag: String, log: String) {
        Log.d(tag, log)
    }

    fun i(tag: String, log: String) {
        Log.i(tag, log)
    }

    fun w(tag: String, log: String) {
        Log.w(tag, log)
    }

    fun e(tag: String, log: String) {
        Log.e(tag, log)
    }
}