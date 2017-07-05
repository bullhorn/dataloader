package com.bullhorn.dataloader.rest;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class SearchCriteriaTest {

    @Test
    public void testConstructor() throws IOException {
        SearchCriteria SearchCriteria = new SearchCriteria();
        Assert.assertNotNull(SearchCriteria);
    }

    @Test
    public void testGetSetterMethodMap() throws IOException {
        String externalIdValue = SearchCriteria.getExternalIdValue(
            "firstName:\"Data\" AND externalID:\"ext 1\" AND lastName:\"Loader\"");
        Assert.assertEquals(externalIdValue, "ext 1");
    }
}
