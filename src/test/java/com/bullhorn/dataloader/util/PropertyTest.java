package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.enums.Property;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class PropertyTest {

    @Test
    public void testGetName() {
        Property property = Property.USERNAME;
        String propertyName = property.getName();
        Assert.assertEquals("username", propertyName);
    }

    @Test
    public void testFromString() {
        Map<String, Property> testCases = new HashMap<>();
        testCases.put("AUTHORIZE_URL", Property.AUTHORIZE_URL);
        testCases.put("authorize_url", Property.AUTHORIZE_URL);
        testCases.put("-authorize_url", Property.AUTHORIZE_URL);
        testCases.put("--authorize_url", Property.AUTHORIZE_URL);
        testCases.put("Authorize_Url", Property.AUTHORIZE_URL);
        testCases.put("__Authorize_Url__", Property.AUTHORIZE_URL);
        testCases.put("authorizeUrl", Property.AUTHORIZE_URL);
        testCases.put("-authorizeUrl", Property.AUTHORIZE_URL);
        testCases.put("authorizeUr", null);
        testCases.put("USERNAME", Property.USERNAME);
        testCases.put("UserName", Property.USERNAME);
        testCases.put("username", Property.USERNAME);
        testCases.put("-username", Property.USERNAME);
        testCases.put("--username", Property.USERNAME);
        testCases.put("user_name", Property.USERNAME);
        testCases.put("-user_name", Property.USERNAME);
        testCases.put("-user-name", null);
        testCases.put("user-name", null);
        testCases.put("user-name-", null);

        for (Map.Entry<String, Property> entry : testCases.entrySet()) {
            Property actual = Property.fromString(entry.getKey());
            Property expected = entry.getValue();
            Assert.assertEquals(expected, actual);
        }
    }
}
