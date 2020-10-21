package com.bullhorn.dataloader.util;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("InstantiationOfUtilityClass")
public class StringConstsTest {

    @Test
    public void testConstructor() {
        StringConsts stringConsts = new StringConsts();
        Assert.assertNotNull(stringConsts);
    }
}
