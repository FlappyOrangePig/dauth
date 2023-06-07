package com.cyberflow.dauthsdk.login.network

import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend inline fun <reified RESPONSE> awaitRequest(crossinline block: () -> RESPONSE?): RESPONSE? {
    return try {
        withContext(Dispatchers.IO) {
            block.invoke()
        }
    } catch (t: Throwable) {
        DAuthLogger.e(t.stackTraceToString())
        null
    }
}