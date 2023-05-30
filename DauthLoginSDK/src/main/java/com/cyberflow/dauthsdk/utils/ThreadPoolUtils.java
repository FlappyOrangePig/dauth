package com.cyberflow.dauthsdk.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 自定义线程创建工具类，创建线程池后不需要关闭
 *
 * @author liangxn
 */
public class ThreadPoolUtils {
//    private static final DAuthLogger LOGGER = LoggerFactory.getLogger(ThreadPoolUtils.class);
    private static ThreadPoolExecutor threadPool = null;
    private static final String POOL_NAME = "myPool";
    // 等待队列长度
    private static final int BLOCKING_QUEUE_LENGTH = 1000;
    // 闲置线程存活时间
    private static final int KEEP_ALIVE_TIME = 60 * 1000;

    private ThreadPoolUtils() {
        throw new IllegalStateException("utility class");
    }


    /**
     * 无返回值直接执行
     *
     * @param runnable 需要运行的任务
     */
    public static void execute(Runnable runnable) {
        getThreadPool().execute(runnable);
    }

    /**
     * 有返回值执行
     * 主线程中使用Future.get()获取返回值时，会阻塞主线程，直到任务执行完毕
     *
     * @param callable 需要运行的任务
     */
    public static <T> Future<T> submit(Callable<T> callable) {
        return getThreadPool().submit(callable);
    }

    private static synchronized ThreadPoolExecutor getThreadPool() {
        if (threadPool == null) {
            // 获取处理器数量
            int cpuNum = Runtime.getRuntime().availableProcessors();
            // 根据cpu数量,计算出合理的线程并发数
            int maximumPoolSize = cpuNum * 2 + 1;
            // 核心线程数、最大线程数、闲置线程存活时间、时间单位、线程队列、线程工厂、当前线程数已经超过最大线程数时的异常处理策略
            threadPool = new ThreadPoolExecutor(maximumPoolSize - 1,
                    maximumPoolSize,
                    KEEP_ALIVE_TIME,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingDeque<>(BLOCKING_QUEUE_LENGTH),
                    new ThreadFactoryBuilder().setNameFormat(POOL_NAME + "-%d").build(),
                    new ThreadPoolExecutor.AbortPolicy() {
                        @Override
                        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {

                        }
                    });

        }

        return threadPool;
    }
}
