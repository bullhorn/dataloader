package com.bullhorn.dataloader.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class StringConstsTest {

    @Test
    public void testConstructor() throws IOException {
        StringConsts stringConsts = new StringConsts();
        Assert.assertNotNull(stringConsts);
    }
}
