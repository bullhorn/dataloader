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

    private static final int KEEP_ALIVE_TIME = 10;
    private static final long SIXTEEN_GIGABYTES = 16456252;

    private PropertyFileUtil propertyFileUtil;

    public ThreadPoolUtil(PropertyFileUtil propertyFileUtil) {
        this.propertyFileUtil = propertyFileUtil;
    }

    /**
     * Creates a thread pool executor service for running parallel tasks
     *
     * @return The service for executing tasks as a pool of threads
     */
    public ExecutorService getExecutorService() {
        final BlockingQueue<Runnable> taskPoolSize = new ArrayBlockingQueue<>(getTaskPoolSize());

        return new ThreadPoolExecutor(propertyFileUtil.getNumThreads(), propertyFileUtil.getNumThreads(),
            KEEP_ALIVE_TIME, TimeUnit.SECONDS, taskPoolSize, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * Gets task pool size limit on basis of system memory
     *
     * @return task pool size limit
     */
    int getTaskPoolSize() {
        final long memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize() / 1024;

        if (memorySize < SIXTEEN_GIGABYTES) {
            return 1000;
        }
        return 10000;
    }
}
