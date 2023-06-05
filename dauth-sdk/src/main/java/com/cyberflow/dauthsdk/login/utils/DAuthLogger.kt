package com.cyberflow.dauthsdk.login.utils

import android.util.Log

object DAuthLogger  {
    private const val TAG = "DAuthLogger"

    private val isOpenLog: Boolean get() = DEBUG_MODES

    fun i(msg: String) {
        if (isOpenLog) Log.i(TAG, msg)
    }

    fun d(msg: String) {
        if (isOpenLog) {
            Log.d(TAG, msg)
        }
    }

    fun e(msg: String) {
        Log.e(TAG, msg)
    }

    fun v(msg: String) {
        if (isOpenLog) Log.v(TAG, msg)
    }

    fun w(msg: String) {
        if (isOpenLog) Log.w(TAG, msg)
    }

    var DEBUG_MODES = true
}