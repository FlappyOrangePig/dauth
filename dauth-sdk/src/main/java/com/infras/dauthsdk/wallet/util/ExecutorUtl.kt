package com.infras.dauthsdk.wallet.util

import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.internal.ExecutorUtils
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

object ExecutorUtl {

    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    private val CORE_POOL_SIZE = CPU_COUNT + 1
    private val MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1
    private const val KEEP_ALIVE = 1L
    private const val DEFAULT_TERMINATION_TIMEOUT = 1L

    fun buildThreadPoolExecutorService(name: String): ExecutorService {
        val threadFactory = getNamedThreadFactory(name)
        val executor: ExecutorService = ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
            LinkedBlockingQueue(), threadFactory
        )
        addDelayedShutdownHook(name, executor)
        return executor
    }

    fun buildSingleThreadScheduledExecutorService(name: String): ScheduledExecutorService {
        val threadFactory = getNamedThreadFactory(name)
        val executor = Executors.newSingleThreadScheduledExecutor(threadFactory)
        addDelayedShutdownHook(name, executor)
        return executor
    }

    fun getNamedThreadFactory(threadNameTemplate: String): ThreadFactory {
        val count = AtomicLong(1)
        return ThreadFactory { runnable: Runnable? ->
            val thread =
                Executors.defaultThreadFactory().newThread(runnable)
            thread.name = threadNameTemplate + count.getAndIncrement()
            thread
        }
    }

    fun addDelayedShutdownHook(serviceName: String, service: ExecutorService) {
        addDelayedShutdownHook(
            serviceName,
            service,
            DEFAULT_TERMINATION_TIMEOUT,
            TimeUnit.SECONDS
        )
    }

    fun addDelayedShutdownHook(
        serviceName: String,
        service: ExecutorService, terminationTimeout: Long, timeUnit: TimeUnit?
    ) {
        Runtime.getRuntime().addShutdownHook(Thread({
            try {
                service.shutdown()
                if (!service.awaitTermination(terminationTimeout, timeUnit)) {
                    Twitter.getLogger().d(
                        Twitter.TAG, serviceName + " did not shutdown in the"
                                + " allocated time. Requesting immediate shutdown."
                    )
                    service.shutdownNow()
                }
            } catch (e: InterruptedException) {
                Twitter.getLogger().d(
                    Twitter.TAG, String.format(
                        Locale.US,
                        "Interrupted while waiting for %s to shut down." +
                                " Requesting immediate shutdown.",
                        serviceName
                    )
                )
                service.shutdownNow()
            }
        }, "Twitter Shutdown Hook for $serviceName"))
    }
}