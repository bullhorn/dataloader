package com.bullhorn.dataloader.util;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Utility methods for creating the thread pool that is used for running tasks.
 */
public class ThreadPoolUtil {

    static final int KEEP_ALIVE_TIME = 10;
    static final long SIXTEEN_GIGABYTES = 16456252;

    /**
     * Creates a thread pool executor service for running parallel tasks
     *
     * @param numThreads The number of parallel tasks to allow at one time
     * @return The service for executing tasks as a pool of threads
     */
    public static ExecutorService getExecutorService(Integer numThreads) {
        final BlockingQueue taskPoolSize = new ArrayBlockingQueue(getTaskPoolSize());

        return new ThreadPoolExecutor(numThreads, numThreads, KEEP_ALIVE_TIME, TimeUnit.SECONDS, taskPoolSize, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * Gets task pool size limit on basis of system memory
     *
     * @return task pool size limit
     */
    public static int getTaskPoolSize() {
        final long memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize() / 1024;

        if (memorySize < SIXTEEN_GIGABYTES) {
            return 1000;
        }
        return 10000;
    }
}
