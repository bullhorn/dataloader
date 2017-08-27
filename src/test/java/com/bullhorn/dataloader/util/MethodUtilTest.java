package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.embedded.Address;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;

public class MethodUtilTest {

    @Test
    public void testConstructor() throws IOException {
        MethodUtil methodUtil = new MethodUtil();
        Assert.assertNotNull(methodUtil);
    }

    @Test
    public void testGetSetterMethodMapAddress() throws IOException {
        Map<String, Method> setterMethodMap = MethodUtil.getSetterMethodMap(Address.class);
        Assert.assertEquals(setterMethodMap.size(), 6);
    }

    @Test
    public void testGetSetterMethodId() throws IOException {
        Method setterMethod = MethodUtil.getSetterMethod(EntityInfo.CANDIDATE, "id");
        Assert.assertEquals(setterMethod.getName(), "setId");
        Assert.assertEquals(setterMethod.getReturnType(), void.class);
        Assert.assertEquals(setterMethod.getParameterTypes()[0], Integer.class);
    }

    @Test
    public void testGetSetterMethodExternalID() throws IOException {
        Method setterMethod = MethodUtil.getSetterMethod(EntityInfo.CANDIDATE, "externalID");
        Assert.assertEquals(setterMethod.getName(), "setExternalID");
        Assert.assertEquals(setterMethod.getReturnType(), void.class);
        Assert.assertEquals(setterMethod.getParameterTypes()[0], String.class);
    }

    @Test
    public void testGetSetterMethodAddress1() throws IOException {
        Method setterMethod = MethodUtil.getSetterMethod(EntityInfo.ADDRESS, "address1");
        Assert.assertEquals(setterMethod.getName(), "setAddress1");
        Assert.assertEquals(setterMethod.getReturnType(), void.class);
        Assert.assertEquals(setterMethod.getParameterTypes()[0], String.class);
    }

    @Test
    public void testGetSetterMethodFailure() throws IOException {
        RestApiException expectedException = new RestApiException(
            "Invalid address field format: 'address1'. Must use: 'address.address1' to set an address field.");
        RestApiException actualException = null;

        try {
            MethodUtil.getSetterMethod(EntityInfo.CANDIDATE, "address1");
        } catch (RestApiException e) {
            actualException = e;
        }

        Assert.assertNotNull(actualException);
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    public void testFindBestMatchExact() throws IOException {
        Set<String> fields = Sets.newSet("name", "firstName", "lastName", "middleName", "last");
        Assert.assertEquals("lastName", MethodUtil.findBestMatch(fields, "lastName"));
        Assert.assertEquals("name", MethodUtil.findBestMatch(fields, "name"));
        Assert.assertEquals("last", MethodUtil.findBestMatch(fields, "last"));
    }

    @Test
    public void testFindBestMatchCaseInsensitive() throws IOException {
        Set<String> fields = Sets.newSet("name", "firstName", "lastName", "middleName", "last");
        Assert.assertEquals("lastName", MethodUtil.findBestMatch(fields, "LastName"));
        Assert.assertEquals("name", MethodUtil.findBestMatch(fields, "Name"));
        Assert.assertEquals("last", MethodUtil.findBestMatch(fields, "LAST"));
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

    @Test
    public void testConvertStringToObjectReturnsNull() throws ParseException {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("MM/dd/yyyy");
        Object actual = MethodUtil.convertStringToObject("bogus", MethodUtil.class, dateTimeFormatter);
        Assert.assertEquals(null, actual);
    }
}
