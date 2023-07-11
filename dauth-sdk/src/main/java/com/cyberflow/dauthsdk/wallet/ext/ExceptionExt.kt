package com.cyberflow.dauthsdk.wallet.ext

import com.cyberflow.dauthsdk.login.utils.DAuthLogger

suspend inline fun <T> runCatchingWithLogSuspend(crossinline block: suspend () -> T) = try {
    block.invoke()
} catch (t: Throwable) {
    DAuthLogger.e(t.stackTraceToString())
    null
}

inline fun <T> runCatchingWithLog(crossinline block: () -> T) = try {
    block.invoke()
} catch (t: Throwable) {
    DAuthLogger.e(t.stackTraceToString())
    null
}