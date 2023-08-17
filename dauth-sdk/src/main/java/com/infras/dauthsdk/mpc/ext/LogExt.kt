package com.infras.dauthsdk.mpc.ext

import com.infras.dauthsdk.login.model.LogReportParam
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.wallet.impl.manager.Managers
import java.util.UUID

private const val PREFIX = "elapsed"

internal fun <T> runSpending(
    tag: String,
    log: String,
    block: () -> T,
): T {
    val finalTag = "$PREFIX-$tag"
    DAuthLogger.d("$log >>>", finalTag)
    val start = System.currentTimeMillis()
    val r = block.invoke()
    val spent = System.currentTimeMillis() - start
    DAuthLogger.d("$log <<< spent $spent", finalTag)
    return r
}

internal suspend fun <T> suspendRunSpending(
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
    val elapsedMs: Long,
    val result: Boolean,
)

internal class StatsResultHolder {
    /**
     * Result 如果没有返回结果说明是没有成功或失败这一说，直接返回成功
     */
    var result: Boolean = true
}

internal class ElapsedContext(
    val tag: String,
    private val contextName: String,
) {
    private val elapsedList = mutableListOf<Elapsed>()
    private val logTag = "$tag-$PREFIX"
    private val logWhenHappened = false
    private val contextId = "${Managers.loginPrefs.getAuthId()}-${UUID.randomUUID()}"
    internal suspend fun <T> runSpending(
        log: String,
        block: suspend (rh: StatsResultHolder) -> T,
    ): T {
        if (logWhenHappened) {
            DAuthLogger.d("$log >>>", logTag)
        }
        val rh = StatsResultHolder()
        val start = System.currentTimeMillis()
        val r = block.invoke(rh)
        val spent = System.currentTimeMillis() - start
        if (logWhenHappened) {
            DAuthLogger.d("$log <<< spent $spent", logTag)
        }
        val result = rh.result
        this.elapsedList.add(Elapsed(log, spent, result))
        Managers.statsManager.sendStats(
            LogReportParam.Event(
                contextId = contextId,
                contextName = contextName,
                eventName = log,
                duration = spent,
                result = result.toResultString()
            )
        )
        return r
    }

    internal fun finish() {
        traceElapsedList()
    }

    private fun traceElapsedList() {
        val totalResult = if (elapsedList.isEmpty()) {
            false
        } else {
            elapsedList.last().result
        }

        val sb = StringBuilder()
        sb.appendLine()
        sb.appendLine("**** trace elapsed list **** >>>")
        val totalSpent = this.elapsedList.sumOf { it.elapsedMs }
        sb.appendLine("$contextName total ${totalSpent}ms $totalResult")
        this.elapsedList.forEachIndexed { i, e ->
            sb.appendLine("$i)${e.log}:${e.elapsedMs}ms ${e.result}")
        }
        sb.appendLine("**** trace elapsed list **** <<<")
        DAuthLogger.i(sb.toString(), logTag)

        Managers.statsManager.sendStats(
            LogReportParam.Event(
                contextId = contextId,
                contextName = contextName,
                eventName = "TotalSpent",
                duration = totalSpent,
                result = totalResult.toResultString()
            )
        )
    }

    private fun Boolean.toResultString() = when (this) {
        true -> "success"
        false -> "failure"
    }
}