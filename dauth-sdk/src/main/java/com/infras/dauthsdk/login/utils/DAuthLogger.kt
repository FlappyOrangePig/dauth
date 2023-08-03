package com.infras.dauthsdk.login.utils

import android.util.Log
import com.infras.dauthsdk.api.DAuthSDK
import com.infras.dauthsdk.wallet.ext.safeApp
import com.infras.dauthsdk.wallet.impl.manager.Managers

internal const val LEVEL_V = 1
internal const val LEVEL_D = 2
internal const val LEVEL_I = 3
internal const val LEVEL_W = 4
internal const val LEVEL_E = 5
internal const val LEVEL_F = 6

object DAuthLogger {

    private val isOpenLog: Boolean get() = com.infras.dauthsdk.api.DAuthSDK.impl.config.isLogOpen
    private val consoleLogLevel: Int get() = com.infras.dauthsdk.api.DAuthSDK.impl.config.consoleLogLevel
    private val fileLogLevel: Int get() = com.infras.dauthsdk.api.DAuthSDK.impl.config.fileLogLevel
    private val logManager get() = Managers.logManager

    /**
     * 处理公共逻辑：
     * 1.统一日志tag
     * 2.对长日志进行分批
     * 3.判断日志开关
     */
    private inline fun log(
        tag: String,
        log: String,
        level: Int,
        crossinline block: (tag: String, log: String) -> Unit
    ) {
        // 单元测试
        if (safeApp() == null) {
            println(log)
            return
        }

        // 使用统一的前缀，方便过滤所有的SDK日志
        val finalTag = "DAuth-$tag"

        if (isOpenLog) {
            if (level >= consoleLogLevel) {
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
            if (level >= fileLogLevel) {
                logManager.log(level, finalTag, log)
            }
        }
    }

    fun i(msg: String, tag: String = "") {
        log(tag, msg, LEVEL_I) { t, m -> Log.i(t, m) }
    }

    fun d(msg: String, tag: String = "") {
        log(tag, msg, LEVEL_D) { t, m -> Log.d(t, m) }
    }

    fun e(msg: String, tag: String = "") {
        log(tag, msg, LEVEL_E) { t, m -> Log.e(t, m) }
    }

    fun v(msg: String, tag: String = "") {
        log(tag, msg, LEVEL_V) { t, m -> Log.v(t, m) }
    }

    fun w(msg: String, tag: String = "") {
        log(tag, msg, LEVEL_W) { t, m -> Log.w(t, m) }
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