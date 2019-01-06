package com.bullhorn.dataloader.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ThreadPoolUtilTest {

    private ThreadPoolUtil threadPoolUtil;

    @Before
    public void setup() {
        PropertyFileUtil propertyFileUtilMock = mock(PropertyFileUtil.class);
        when(propertyFileUtilMock.getNumThreads()).thenReturn(10);
        threadPoolUtil = new ThreadPoolUtil(propertyFileUtilMock);
    }

    @Test
    public void testGetExecutorService() {
        ExecutorService executorService = threadPoolUtil.getExecutorService();
        Assert.assertNotNull(executorService);
    }

    @Test
    public void testGetTaskPoolSize() {
        Integer taskPoolSize = threadPoolUtil.getTaskPoolSize();
        Assert.assertNotNull(taskPoolSize);
    }
}
