package com.bullhorn.dataloader.util;

import com.bullhornsdk.data.model.entity.embedded.Address;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

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

    @Test
    public void testFindBestMatchExact() throws IOException {
        Set<String> fields = Sets.newSet("name", "firstName", "lastName", "middleName", "last");
        Assert.assertEquals("lastName", MethodUtil.findBestMatch(fields, "lastName"));
        Assert.assertEquals("name", MethodUtil.findBestMatch(fields, "name"));
        Assert.assertEquals("last", MethodUtil.findBestMatch(fields, "last"));
    }

    @Test
    public void testFindBestMatchContains() throws IOException {
        Set<String> fields = Sets.newSet("name", "firstName", "lastName", "middleName");
        Assert.assertEquals("middleName", MethodUtil.findBestMatch(fields, "middle"));
        Assert.assertEquals("firstName", MethodUtil.findBestMatch(fields, "fir"));
    }

    @Test
    public void testFindBestMatchMissing() throws IOException {
        Set<String> fields = Sets.newSet("name", "firstName", "lastName", "middleName");
        Assert.assertEquals(null, MethodUtil.findBestMatch(fields, "initial"));
        Assert.assertEquals(null, MethodUtil.findBestMatch(fields, "names"));
    }
}
