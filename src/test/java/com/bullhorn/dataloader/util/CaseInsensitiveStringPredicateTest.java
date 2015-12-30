package com.bullhorn.dataloader.util;

import org.junit.Assert;
import org.junit.Test;

public class CaseInsensitiveStringPredicateTest {
    @Test
    public void testIsCustomObject() {
        Assert.assertTrue(CaseInsensitiveStringPredicate.isCustomObject("customObject"));
        Assert.assertTrue(CaseInsensitiveStringPredicate.isCustomObject("CustomObject"));
        Assert.assertTrue(CaseInsensitiveStringPredicate.isCustomObject("PersonCustomObject1s"));
        Assert.assertTrue(CaseInsensitiveStringPredicate.isCustomObject("CustomObject1s"));

        Assert.assertFalse(CaseInsensitiveStringPredicate.isCustomObject("CustomObjec"));
        Assert.assertFalse(CaseInsensitiveStringPredicate.isCustomObject("customobject"));
    }

    @Test
    public void testIsToMany() {
        Assert.assertTrue(CaseInsensitiveStringPredicate.isToMany("TO_MANY"));
        Assert.assertTrue(CaseInsensitiveStringPredicate.isToMany("to_many"));
        Assert.assertFalse(CaseInsensitiveStringPredicate.isToMany("TO_FEW"));
    }

    @Test
    public void testIsToOne() {
        Assert.assertTrue(CaseInsensitiveStringPredicate.isToOne("TO_ONE"));
        Assert.assertTrue(CaseInsensitiveStringPredicate.isToOne("to_one"));
        Assert.assertFalse(CaseInsensitiveStringPredicate.isToOne("TO_FEW"));
    }

    @Test
    public void testIsPut() {
        Assert.assertTrue(CaseInsensitiveStringPredicate.isPut("put"));
        Assert.assertTrue(CaseInsensitiveStringPredicate.isPut("PuT"));
        Assert.assertFalse(CaseInsensitiveStringPredicate.isPut("POST"));
    }
}
