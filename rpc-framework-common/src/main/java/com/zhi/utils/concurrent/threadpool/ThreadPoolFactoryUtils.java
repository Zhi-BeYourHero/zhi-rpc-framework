package com.zhi.utils.concurrent.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-19 14:29
 */
@Slf4j
public final class ThreadPoolFactoryUtils {
    /**
     * 通过 threadNamePrefix 来区分不同线程池（我们可以把相同 threadNamePrefix 的线程池看作是为同一业务场景服务）。
     * TODO :通过信号量机制( {@link Semaphore} 满足条件)限制创建的线程池数量（线程池和线程不是越多越好）
     * key: threadNamePrefix
     * value: threadPool
     */
    private static final Map<String, ExecutorService> THREAD_POOLS = new ConcurrentHashMap<>();
    public static final String PROVIDER_NETTY_SEVER_THREAD_POOL = "PROVIDER_NETTY_SEVER_THREAD_POOL";
    private ThreadPoolFactoryUtils() {
    }
    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix) {
        CustomThreadPoolConfig customThreadPoolConfig = new CustomThreadPoolConfig();
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix, CustomThreadPoolConfig customThreadPoolConfig) {
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    /**
     * 创建特定线程池，如果已经存在对应的线程池，判断状态后进行返回...
     * @param customThreadPoolConfig
     * @param threadPoolName
     * @param daemon
     * @return
     */
    public static ExecutorService createCustomThreadPoolIfAbsent(CustomThreadPoolConfig customThreadPoolConfig, String threadPoolName, Boolean daemon) {
        ExecutorService threadPool = THREAD_POOLS.computeIfAbsent(threadPoolName, k -> createThreadPool(customThreadPoolConfig, threadPoolName, daemon));
        // 如果 threadPool 被 shutdown 的话就重新创建一个
        if (threadPool.isShutdown() || threadPool.isTerminated()) {
            THREAD_POOLS.remove(threadPoolName);
            threadPool = createThreadPool(customThreadPoolConfig, threadPoolName, daemon);
            THREAD_POOLS.put(threadPoolName, threadPool);
        }
        return threadPool;
    }

    /**
     * shutDown 所有线程池
     * 并行关闭，提高性能
     */
    public static void shutDownAllThreadPool() {
        log.info("call shutDownAllThreadPool method");
        THREAD_POOLS.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            //不接收新的，但会执行完旧的任务...
            executorService.shutdown();
            log.info("shut down thread pool [{}] [{}]", entry.getKey(), executorService.isTerminated());
            try {
                //阻塞直至所有的任务执行完成，超时10s
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Thread pool never terminated");
                executorService.shutdownNow();
            }
        });
    }

    private static ExecutorService createThreadPool(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean daemon) {
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor(customThreadPoolConfig.getCorePoolSize(), customThreadPoolConfig.getMaximumPoolSize(),
                customThreadPoolConfig.getKeepAliveTime(), customThreadPoolConfig.getUnit(), customThreadPoolConfig.getWorkQueue(),
                threadFactory);
    }

    /**
     * 创建 ThreadFactory 。如果threadNamePrefix不为空则使用自建ThreadFactory，否则使用defaultThreadFactory
     *
     * @param threadNamePrefix 作为创建的线程名字的前缀
     * @param daemon           指定是否为 Daemon Thread(守护线程)
     * @return ThreadFactory
     */
    private static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if (threadNamePrefix != null) {
            if (daemon != null) {
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix + "-%d")
                        .setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }

    /**
     * 打印线程池的状态
     *
     * @param threadPool 线程池对象
     */
    public static void printThreadPoolStatus(ThreadPoolExecutor threadPool) {
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, createThreadFactory("print-thread-pool-status", false));
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            log.info("============ThreadPool Status=============");
            log.info("ThreadPool Size: [{}]", threadPool.getPoolSize());
            log.info("Active Threads: [{}]", threadPool.getActiveCount());
            log.info("Number of Tasks : [{}]", threadPool.getCompletedTaskCount());
            log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());
            log.info("===========================================");
        }, 0, 1, TimeUnit.SECONDS);
    }
}