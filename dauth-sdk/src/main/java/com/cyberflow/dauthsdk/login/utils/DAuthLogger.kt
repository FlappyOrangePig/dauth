package com.cyberflow.dauthsdk.login.utils

import android.app.Application
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.cyberflow.dauthsdk.api.DAuthSDK

object DAuthLogger {

    private val isOpenLog: Boolean get() = DAuthSDK.impl.config.isLogOpen

    /**
     * 处理公共逻辑：
     * 1.统一日志tag
     * 2.对长日志进行分批
     * 3.判断日志开关
     */
    private inline fun log(
        tag: String,
        log: String,
        crossinline block: (tag: String, log: String) -> Unit
    ) {
        // 单元测试
        if (DAuthSDK.impl.context !is Application) {
            println(log)
            return
        }

        if (isOpenLog) {
            // 使用统一的前缀，方便过滤所有的SDK日志
            val finalTag = "DAuth-$tag"
            val maxLogSize = 1000
            for (i in 0..log.length / maxLogSize) {
                val start = i * maxLogSize
                var end = (i + 1) * maxLogSize
                end = if (end > log.length) log.length else end
                val segment = log.substring(start, end)
                if (i == 0) {
                    block.invoke(finalTag, "[tid=${Thread.currentThread().id}]$segment")
                } else {
                    block.invoke(finalTag, "        $segment")
                }
            }
        }
    }

    fun i(msg: String, tag: String = "") {
        log(tag, msg) { t, m -> Log.i(t, m) }
    }

    fun d(msg: String, tag: String = "") {
        log(tag, msg) { t, m -> Log.d(t, m) }
    }

    fun e(msg: String, tag: String = "") {
        log(tag, msg) { t, m -> Log.e(t, m) }
    }

    fun v(msg: String, tag: String = "") {
        log(tag, msg) { t, m -> Log.v(t, m) }
    }

    fun w(msg: String, tag: String = "") {
        log(tag, msg) { t, m -> Log.w(t, m) }
    }
}

fun String.maskSensitiveData(): String {
    val str = this
    val maskBytes = 3
    return if (length <= maskBytes * 2) {
        "*".repeat(length)
    } else {
        val maskedString =
            str.substring(0, maskBytes) + "*".repeat(str.length - maskBytes * 2) + str.substring(
                str.length - maskBytes
            )
        maskedString
    }
}