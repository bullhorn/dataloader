package com.bullhorn.dataloader.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ThreadPoolUtilTest {

    PropertyFileUtil propertyFileUtilMock;
    ThreadPoolUtil threadPoolUtil;

    @Before
    public void setup() {
        propertyFileUtilMock = mock(PropertyFileUtil.class);
        when(propertyFileUtilMock.getNumThreads()).thenReturn(10);
        threadPoolUtil = new ThreadPoolUtil(propertyFileUtilMock);
    }

    @Test
    public void testGetExecutorService() throws IOException {
        ExecutorService executorService = threadPoolUtil.getExecutorService();
        Assert.assertNotNull(executorService);
    }

    @Test
    public void testGetTaskPoolSize() throws IOException {
        Integer taskPoolSize = threadPoolUtil.getTaskPoolSize();
        Assert.assertNotNull(taskPoolSize);
    }
}
