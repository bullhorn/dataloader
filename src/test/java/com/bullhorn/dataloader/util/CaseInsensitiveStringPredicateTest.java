package com.bullhorn.dataloader.util;

import org.junit.Assert;
import org.junit.Test;

public class CaseInsensitiveStringPredicateTest {
    @Test
    public void testIsCustomObject() {
        Assert.assertTrue(AssociationFilter.isCustomObject("customObject"));
        Assert.assertTrue(AssociationFilter.isCustomObject("CustomObject"));
        Assert.assertTrue(AssociationFilter.isCustomObject("PersonCustomObject1s"));
        Assert.assertTrue(AssociationFilter.isCustomObject("CustomObject1s"));

        Assert.assertFalse(AssociationFilter.isCustomObject("CustomObjec"));
        Assert.assertFalse(AssociationFilter.isCustomObject("customobject"));
    }

    @Test
    public void testIsToMany() {
        Assert.assertTrue(AssociationFilter.isToMany("TO_MANY"));
        Assert.assertTrue(AssociationFilter.isToMany("to_many"));
        Assert.assertFalse(AssociationFilter.isToMany("TO_FEW"));
    }

    @Test
    public void testIsToOne() {
        Assert.assertTrue(AssociationFilter.isToOne("TO_ONE"));
        Assert.assertTrue(AssociationFilter.isToOne("to_one"));
        Assert.assertFalse(AssociationFilter.isToOne("TO_FEW"));
    }

    @Test
    public void testIsPut() {
        Assert.assertTrue(AssociationFilter.isPut("put"));
        Assert.assertTrue(AssociationFilter.isPut("PuT"));
        Assert.assertFalse(AssociationFilter.isPut("POST"));
    }
}
