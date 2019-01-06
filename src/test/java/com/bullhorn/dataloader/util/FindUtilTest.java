package com.bullhorn.dataloader.util;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class FindUtilTest {

    @Test
    public void testConstructor() {
        FindUtil findUtil = new FindUtil();
        Assert.assertNotNull(findUtil);
    }

    @Test
    public void testGetExternalIdValueSuccess() {
        String externalIdValue = FindUtil.getExternalIdValue(
            "firstName:\"Data\" AND externalID:\"ext 1\" AND lastName:\"Loader\"");
        Assert.assertEquals(externalIdValue, "ext 1");
    }

    @Test
    public void testGetExternalIdValueMissing() {
        String externalIdValue = FindUtil.getExternalIdValue(
            "firstName:\"Data\" AND lastName:\"Loader\"");
        Assert.assertEquals("", externalIdValue);
    }

    @Test
    public void testGetCorrectedFieldSet() {
        Set<String> correctedFieldSet = FindUtil.getCorrectedFieldSet(Sets.newHashSet("id", "firstName", "lastName"));
        Assert.assertEquals(Sets.newHashSet("id", "firstName", "lastName"), correctedFieldSet);
    }

    @Test
    public void testGetCorrectedFieldSetTooManyFields() {
        Set<String> originalFieldSet = Sets.newHashSet();
        for (int i = 0; i < 50; i++) {
            originalFieldSet.add("customText" + i);
        }
        Set<String> correctedFieldSet = FindUtil.getCorrectedFieldSet(originalFieldSet);
        Assert.assertEquals(Sets.newHashSet("*"), correctedFieldSet);
    }

    @Test
    public void testGetCorrectedFieldSetMissingId() {
        Set<String> correctedFieldSet = FindUtil.getCorrectedFieldSet(Sets.newHashSet("firstName", "lastName", "email"));
        Assert.assertEquals(Sets.newHashSet("id", "firstName", "lastName", "email"), correctedFieldSet);
    }
}
