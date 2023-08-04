package com.infras.dauthsdk.wallet.ext

import com.infras.dauthsdk.login.utils.DAuthLogger

internal suspend inline fun <T> runCatchingWithLogSuspend(crossinline block: suspend () -> T) = try {
    block.invoke()
} catch (t: Throwable) {
    DAuthLogger.e(t.stackTraceToString())
    null
}

internal inline fun <T> runCatchingWithLog(crossinline block: () -> T) = try {
    block.invoke()
} catch (t: Throwable) {
    DAuthLogger.e(t.stackTraceToString())
    null
}