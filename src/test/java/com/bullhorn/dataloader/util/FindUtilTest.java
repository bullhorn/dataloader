package com.bullhorn.dataloader.util;

import com.google.common.collect.Sets;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class FindUtilTest {

    @SuppressWarnings("InstantiationOfUtilityClass")
    @Test
    public void testConstructor() {
        FindUtil findUtil = new FindUtil();
        Assert.assertNotNull(findUtil);
    }

    @Test
    public void testGetExternalIdSearchValueQuotedSingleSuccess() {
        String externalIdValue = FindUtil.getExternalIdSearchValue(
            "externalID:\"ext 1\"");
        Assert.assertEquals(externalIdValue, "ext 1");
    }

    @Test
    public void testGetExternalIdSearchValueQuotedMultipleFailure() {
        String externalIdValue = FindUtil.getExternalIdSearchValue(
            "firstName:\"Data\" AND externalID:\"ext 1\" AND lastName:\"Loader\"");
        Assert.assertEquals(externalIdValue, "");
    }

    @Test
    public void testGetExternalIdSearchValueUnquotedSingleSuccess() {
        String externalIdValue = FindUtil.getExternalIdSearchValue(
            "externalID: ext-1");
        Assert.assertEquals(externalIdValue, "ext-1");
    }

    @Test
    public void testGetExternalIdSearchValueUnquotedSingleSpaceSuccess() {
        String externalIdValue = FindUtil.getExternalIdSearchValue(
            "externalID: ext 1");
        Assert.assertEquals(externalIdValue, "ext 1");
    }

    @Test
    public void testGetExternalIdSearchValueUnquotedMultipleFailure() {
        String externalIdValue = FindUtil.getExternalIdSearchValue(
            "firstName: Data AND externalID: ext 1 AND lastName: Loader");
        Assert.assertEquals(externalIdValue, "");
    }

    @Test
    public void testGetExternalIdSearchValueMissing() {
        String externalIdValue = FindUtil.getExternalIdSearchValue(
            "firstName:\"Data\" AND lastName:\"Loader\"");
        Assert.assertEquals("", externalIdValue);
    }

    @Test
    public void testGetExternalIdSearchValueEmpty() {
        String externalIdValue = FindUtil.getExternalIdSearchValue("");
        Assert.assertEquals("", externalIdValue);
    }

    @Test
    public void testGetCorrectedFieldSet() {
        Set<String> correctedFieldSet = FindUtil.getCorrectedFieldSet(Sets.newHashSet("id", "firstName", "lastName"));
        Assert.assertEquals(Sets.newHashSet("id", "firstName", "lastName"), correctedFieldSet);
    }

    @Test
    public void testGetCorrectedFieldSetMissingId() {
        Set<String> correctedFieldSet = FindUtil.getCorrectedFieldSet(Sets.newHashSet("firstName", "lastName", "email"));
        Assert.assertEquals(Sets.newHashSet("id", "firstName", "lastName", "email"), correctedFieldSet);
    }
}
