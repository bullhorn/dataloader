package com.bullhorn.dataloader.util;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.enums.ErrorInfo;

import com.bullhornsdk.data.model.entity.embedded.Address;

public class MethodUtilTest {

    @SuppressWarnings("InstantiationOfUtilityClass")
    @Test
    public void testConstructor() {
        MethodUtil methodUtil = new MethodUtil();
        Assert.assertNotNull(methodUtil);
    }

    @Test
    public void testGetSetterMethodMapAddress() {
        Map<String, Method> setterMethodMap = MethodUtil.getSetterMethodMap(Address.class);
        Assert.assertEquals(setterMethodMap.size(), 8);
    }

    @Test
    public void testGetSetterMethodId() {
        Method setterMethod = MethodUtil.getSetterMethod(EntityInfo.CANDIDATE, "id");
        Assert.assertEquals(setterMethod.getName(), "setId");
        Assert.assertEquals(setterMethod.getReturnType(), void.class);
        Assert.assertEquals(setterMethod.getParameterTypes()[0], Integer.class);
    }

    @Test
    public void testGetSetterMethodExternalID() {
        Method setterMethod = MethodUtil.getSetterMethod(EntityInfo.CANDIDATE, "externalID");
        Assert.assertEquals(setterMethod.getName(), "setExternalID");
        Assert.assertEquals(setterMethod.getReturnType(), void.class);
        Assert.assertEquals(setterMethod.getParameterTypes()[0], String.class);
    }

    @Test
    public void testGetSetterMethodAddress1() {
        Method setterMethod = MethodUtil.getSetterMethod(EntityInfo.ADDRESS, "address1");
        Assert.assertEquals(setterMethod.getName(), "setAddress1");
        Assert.assertEquals(setterMethod.getReturnType(), void.class);
        Assert.assertEquals(setterMethod.getParameterTypes()[0], String.class);
    }

    @Test
    public void testGetGetterMethodFailure() {
        DataLoaderException expectedException = new DataLoaderException(ErrorInfo.INCORRECT_COLUMN_NAME,
            "'workFromHome' does not exist on Placement");
        DataLoaderException actualException = null;

        try {
            MethodUtil.getGetterMethod(EntityInfo.PLACEMENT, "workFromHome");
        } catch (DataLoaderException e) {
            actualException = e;
        }

        Assert.assertNotNull(actualException);
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    public void testGetSetterMethodFailure() {
        DataLoaderException expectedException = new DataLoaderException(ErrorInfo.INCORRECT_COLUMN_NAME,
            "'workFromHome' does not exist on Placement");
        DataLoaderException actualException = null;

        try {
            MethodUtil.getSetterMethod(EntityInfo.PLACEMENT, "workFromHome");
        } catch (DataLoaderException e) {
            actualException = e;
        }

        Assert.assertNotNull(actualException);
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    public void testGetGetterMethodMalformedAddressFailure() {
        DataLoaderException expectedException = new DataLoaderException(ErrorInfo.INCORRECT_COLUMN_NAME,
            "Invalid address field format: 'address1'. Must use: 'address.address1' to set an address field.");
        DataLoaderException actualException = null;

        try {
            MethodUtil.getSetterMethod(EntityInfo.CANDIDATE, "address1");
        } catch (DataLoaderException e) {
            actualException = e;
        }

        Assert.assertNotNull(actualException);
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    public void testGetSetterMethodAddressFailure() {
        DataLoaderException expectedException = new DataLoaderException(ErrorInfo.INCORRECT_COLUMN_NAME,
            "Invalid address field format: 'address1'. Must use: 'address.address1' to set an address field.");
        DataLoaderException actualException = null;

        try {
            MethodUtil.getSetterMethod(EntityInfo.CANDIDATE, "address1");
        } catch (DataLoaderException e) {
            actualException = e;
        }

        Assert.assertNotNull(actualException);
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    public void testFindBestMatchExact() {
        Set<String> fields = Sets.newSet("name", "firstName", "lastName", "middleName", "last");
        Assert.assertEquals("lastName", MethodUtil.findBestMatch(fields, "lastName"));
        Assert.assertEquals("name", MethodUtil.findBestMatch(fields, "name"));
        Assert.assertEquals("last", MethodUtil.findBestMatch(fields, "last"));
    }

    @Test
    public void testFindBestMatchCaseInsensitive() {
        Set<String> fields = Sets.newSet("name", "firstName", "lastName", "middleName", "last");
        Assert.assertEquals("lastName", MethodUtil.findBestMatch(fields, "LastName"));
        Assert.assertEquals("name", MethodUtil.findBestMatch(fields, "Name"));
        Assert.assertEquals("last", MethodUtil.findBestMatch(fields, "LAST"));
    }

    @Test
    public void testFindBestMatchContains() {
        Set<String> fields = Sets.newSet("name", "firstName", "lastName", "middleName");
        Assert.assertEquals("middleName", MethodUtil.findBestMatch(fields, "middle"));
        Assert.assertEquals("firstName", MethodUtil.findBestMatch(fields, "fir"));
    }

    @Test
    public void testFindBestMatchMissing() {
        Set<String> fields = Sets.newSet("name", "firstName", "lastName", "middleName");
        Assert.assertNull(MethodUtil.findBestMatch(fields, "initial"));
        Assert.assertNull(MethodUtil.findBestMatch(fields, "names"));
    }

    @Test
    public void testConvertStringToObjectReturnsNull() throws ParseException {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("MM/dd/yyyy");
        Object actual = MethodUtil.convertStringToObject("bogus", MethodUtil.class, dateTimeFormatter);
        Assert.assertNull(actual);
    }

    @Test
    public void testAlternativeNameGetterMethods() {
        // The getter is named without the word is: getWorkFromHome()
        Method methodNameMismatch = MethodUtil.getGetterMethod(EntityInfo.PLACEMENT, "isWorkFromHome");
        Assert.assertNotNull(methodNameMismatch);

        Method clientContact = MethodUtil.getGetterMethod(EntityInfo.OPPORTUNITY, "clientContact");
        Assert.assertNotNull(clientContact);

        Method isClientContact = MethodUtil.getGetterMethod(EntityInfo.OPPORTUNITY, "isClientContact");
        Assert.assertNotNull(isClientContact);
    }

    @Test
    public void testAlternativeNameSetterMethods() {
        // The setter is named without the word is: getWorkFromHome()
        Method methodNameMismatch = MethodUtil.getSetterMethod(EntityInfo.PLACEMENT, "isWorkFromHome");
        Assert.assertNotNull(methodNameMismatch);

        Method clientContact = MethodUtil.getSetterMethod(EntityInfo.OPPORTUNITY, "clientContact");
        Assert.assertNotNull(clientContact);

        Method isClientContact = MethodUtil.getSetterMethod(EntityInfo.OPPORTUNITY, "isClientContact");
        Assert.assertNotNull(isClientContact);
    }
}
