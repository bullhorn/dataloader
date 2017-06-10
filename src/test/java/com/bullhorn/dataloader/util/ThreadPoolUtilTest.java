package com.bullhorn.dataloader.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class ThreadPoolUtilTest {

    @Test
    public void testConstructor() throws IOException {
        ThreadPoolUtil threadPoolUtil = new ThreadPoolUtil();
        Assert.assertNotNull(threadPoolUtil);
    }

    @Test
    public void testGetExecutorService() throws IOException {
        ExecutorService executorService = ThreadPoolUtil.getExecutorService(10);
        Assert.assertNotNull(executorService);
    }

    @Test
    public void testGetTaskPoolSize() throws IOException {
        Integer taskPoolSize = ThreadPoolUtil.getTaskPoolSize();
        Assert.assertNotNull(taskPoolSize);
    }
}
