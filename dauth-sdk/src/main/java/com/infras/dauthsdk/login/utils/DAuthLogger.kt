package com.infras.dauthsdk.login.utils

import android.util.Log
import com.infras.dauthsdk.api.DAuthLogLevel
import com.infras.dauthsdk.api.DAuthLogLevelEnum
import com.infras.dauthsdk.api.DAuthSDK
import com.infras.dauthsdk.wallet.ext.safeApp
import com.infras.dauthsdk.wallet.impl.manager.Managers

private fun logTagTransform(tag: String): String {
    return "DAuth-$tag"
}

internal object DAuthLogger {
    private val isOpenLog: Boolean get() = DAuthSDK.impl.config.isLogOpen
    private val consoleLogLevel: Int get() = DAuthSDK.impl.config.consoleLogLevel
    private val fileLogLevel: Int get() = DAuthSDK.impl.config.fileLogLevel
    private val logCallback get() = DAuthSDK.impl.config.logCallback

    /**
     * 处理公共逻辑：
     * 1.统一日志tag
     * 2.判断日志开关和级别
     */
    private fun log(
        tag: String,
        log: String,
        @DAuthLogLevelEnum level: Int
    ) {
        // 单元测试
        if (safeApp() == null) {
            println(log)
            return
        }

        // 使用统一的前缀，方便过滤所有的SDK日志
        val finalTag = logTagTransform(tag)

        // 优先级最高
        logCallback?.let {
            it.invoke(level, finalTag, log)
            return
        }

        if (isOpenLog) {
            if (level >= consoleLogLevel) {
                Logcat.log(level, finalTag, log)
            }
            if (level >= fileLogLevel) {
                FileLogger.log(level, finalTag, log)
            }
        }
    }

    fun v(msg: String, tag: String = "") {
        log(tag, msg, DAuthLogLevel.LEVEL_VERBOSE)
    }

    fun d(msg: String, tag: String = "") {
        log(tag, msg, DAuthLogLevel.LEVEL_DEBUG)
    }

    fun i(msg: String, tag: String = "") {
        log(tag, msg, DAuthLogLevel.LEVEL_INFO)
    }

    fun w(msg: String, tag: String = "") {
        log(tag, msg, DAuthLogLevel.LEVEL_WARN)
    }

    fun e(msg: String, tag: String = "") {
        log(tag, msg, DAuthLogLevel.LEVEL_ERROR)
    }

    fun f(msg: String, tag: String = "") {
        log(tag, msg, DAuthLogLevel.LEVEL_FATAL)
    }
}

internal fun String.maskSensitiveData(): String {
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

internal interface ILogger{
    fun log(
        level: Int,
        tag: String,
        log: String,
    )
}

private object Logcat : ILogger {

    override fun log(level: Int, tag: String, log: String) {
        // 日志分批
        val maxLogSize = 1000
        for (i in 0..log.length / maxLogSize) {
            val start = i * maxLogSize
            var end = (i + 1) * maxLogSize
            end = if (end > log.length) log.length else end
            val segment = log.substring(start, end)
            if (i == 0) {
                logcatLog(level, tag, "[tid=${Thread.currentThread().id}]$segment")
            } else {
                logcatLog(level, tag, "        $segment")
            }
        }
    }

    private fun logcatLog(level: Int, t: String, l: String) {
        when (level) {
            DAuthLogLevel.LEVEL_VERBOSE -> Log.v(t, l)
            DAuthLogLevel.LEVEL_DEBUG -> Log.d(t, l)
            DAuthLogLevel.LEVEL_INFO -> Log.i(t, l)
            DAuthLogLevel.LEVEL_ERROR -> Log.e(t, l)
            DAuthLogLevel.LEVEL_WARN -> Log.w(t, l)
            DAuthLogLevel.LEVEL_FATAL -> Log.wtf(t, l)
            else -> {}
        }
    }
}

private object FileLogger : ILogger {
    private val logManager get() = Managers.logManager
    override fun log(level: Int, tag: String, log: String) {
        logManager.log(level, tag, log, true)
    }
}