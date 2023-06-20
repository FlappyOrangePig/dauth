package com.cyberflow.dauthsdk.mpc.ext

import com.cyberflow.dauthsdk.login.utils.DAuthLogger

internal inline fun <T> runSpending(tag: String, log: String, crossinline block: () -> T): T {
    DAuthLogger.d("$log >>>", tag)
    val start = System.currentTimeMillis()
    val r = block.invoke()
    val spent = System.currentTimeMillis() - start
    DAuthLogger.d("$log <<< spent $spent", tag)
    return r
}