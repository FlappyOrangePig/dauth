package com.cyberflow.dauthsdk.login.utils

import android.util.Log

object DAuthLogger  {
    // 提供给第三方使用
    private const val TAG = "DAuthLogger"

    // 内部测试
    private const val TAGTEST = "test"

    private const val TAGMSG = " DAuthSDK "


    /**
     * @return 是否打开日志开关
     */
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

    fun t(msg: String) {
        Log.d(TAGTEST, msg)
    }

    var count = 1
    fun warn(count: Int) {
        while (DAuthLogger.count <= count) {
            Log.w(TAG, "$TAGMSG-")
            DAuthLogger.count += 1
        }
        DAuthLogger.count = 1
    }


    var customTagPrefix = "_code" // 自定义Tag的前缀

    var DEBUG_MODES = true


}