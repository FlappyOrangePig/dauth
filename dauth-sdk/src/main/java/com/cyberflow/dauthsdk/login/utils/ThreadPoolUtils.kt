package com.cyberflow.dauthsdk.login.utils

import java.util.concurrent.*

object ThreadPoolUtils {
    private val threadPool: ThreadPoolExecutor

    init {
        val availableProcessors = Runtime.getRuntime().availableProcessors()
        val corePoolSize = availableProcessors.coerceAtLeast(2)
        val maxPoolSize = availableProcessors * 2 + 1
        val keepAliveTime = 10L // 10 seconds

        threadPool = ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            keepAliveTime,
            TimeUnit.SECONDS,
            LinkedBlockingQueue()
        )
    }

    fun execute(task: Runnable) {
        threadPool.execute(task)
    }

    fun <T> submit(callable: Callable<T>?): Future<T> {
        return threadPool.submit(callable)
    }

    fun shutdown() {
        threadPool.shutdown()
    }
}
