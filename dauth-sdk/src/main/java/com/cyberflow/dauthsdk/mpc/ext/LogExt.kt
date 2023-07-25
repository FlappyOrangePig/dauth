package com.cyberflow.dauthsdk.mpc.ext

import com.cyberflow.dauthsdk.login.utils.DAuthLogger

private const val PREFIX = "elapsed"

internal suspend fun <T> runSpending(
    tag: String,
    log: String,
    block: suspend () -> T,
): T {
    val finalTag = "$PREFIX-$tag"
    DAuthLogger.d("$log >>>", finalTag)
    val start = System.currentTimeMillis()
    val r = block.invoke()
    val spent = System.currentTimeMillis() - start
    DAuthLogger.d("$log <<< spent $spent", finalTag)
    return r
}

private class Elapsed(
    val log: String,
    val elapsedMs: Long
)

internal class ElapsedContext(
    val tag: String
) {
    private val elapsedList = mutableListOf<Elapsed>()
    private val logTag = "$tag-$PREFIX"
    private val logWhenHappened = false

    internal suspend fun <T> runSpending(
        log: String,
        block: suspend () -> T,
    ): T {
        if (logWhenHappened) {
            DAuthLogger.d("$log >>>", logTag)
        }
        val start = System.currentTimeMillis()
        val r = block.invoke()
        val spent = System.currentTimeMillis() - start
        if (logWhenHappened) {
            DAuthLogger.d("$log <<< spent $spent", logTag)
        }
        this.elapsedList.add(Elapsed(log, spent))
        return r
    }

    internal fun traceElapsedList() {
        DAuthLogger.d("**** trace elapsed list **** >>>", logTag)
        val totalSpent = this.elapsedList.sumOf { it.elapsedMs }
        DAuthLogger.d("total spent $totalSpent", logTag)
        this.elapsedList.forEachIndexed { i, e ->
            DAuthLogger.d("$i) ${e.log} spent ${e.elapsedMs}ms", logTag)
        }
        DAuthLogger.d("**** trace elapsed list **** <<<", logTag)
    }
}