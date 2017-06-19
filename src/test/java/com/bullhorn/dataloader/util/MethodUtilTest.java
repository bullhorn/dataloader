package com.bullhorn.dataloader.util;

import com.bullhornsdk.data.model.entity.embedded.Address;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

public class MethodUtilTest {

    @Test
    public void testConstructor() throws IOException {
        MethodUtil methodUtil = new MethodUtil();
        Assert.assertNotNull(methodUtil);
    }

    @Test
    public void testGetSetterMethodMap() throws IOException {
        Map<String, Method> setterMethodMap = MethodUtil.getSetterMethodMap(Address.class);
        Assert.assertEquals(setterMethodMap.size(), 6);
    }
}
