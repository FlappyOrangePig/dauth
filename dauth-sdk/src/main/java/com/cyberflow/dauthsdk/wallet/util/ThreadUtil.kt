package com.cyberflow.dauthsdk.wallet.util

import android.os.Looper
import androidx.viewbinding.BuildConfig
import com.cyberflow.dauthsdk.wallet.const.WalletConst.LOG_TAG

object ThreadUtil {

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
                LogUtil.e(LOG_TAG, msg)
            }
        }
    }
}