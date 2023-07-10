package com.cyberflow.dauthsdk.wallet.ext

import com.cyberflow.dauthsdk.login.utils.DAuthLogger

suspend inline fun runCatchingWithLog(crossinline block: suspend () -> Unit) {
    try {
        block.invoke()
    } catch (t: Throwable) {
        DAuthLogger.e(t.stackTraceToString())
    }
}