package com.bullhorn.dataloader.util;

import org.junit.Assert;
import org.junit.Test;

public class FindUtilTest {

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
        Assert.assertEquals(externalIdValue, "");
    }
}
