package com.bullhorn.dataloader.util;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class StringConstsTest {

    @Test
    public void testGetTimestamp() throws IOException {
        String originalTimestamp = StringConsts.getTimestamp();
        String newTimestamp = StringConsts.getTimestamp();

        Assert.assertEquals(originalTimestamp, newTimestamp);
    }

    @Test
    public void isHardDeletableEntityTest() throws IOException {
        Assert.assertTrue(StringConsts.isHardDeletable("Placement"));
        Assert.assertTrue(StringConsts.isHardDeletable("Sendout"));
        Assert.assertFalse(StringConsts.isHardDeletable("Skill"));
        Assert.assertFalse(StringConsts.isHardDeletable("BusinessSector"));
    }

    @Test
    public void isSoftDeletableEntityTest() throws IOException {
        Assert.assertTrue(StringConsts.isSoftDeletable("ClientContact"));
        Assert.assertTrue(StringConsts.isSoftDeletable("Candidate"));
        Assert.assertFalse(StringConsts.isSoftDeletable("Sendout"));
        Assert.assertFalse(StringConsts.isSoftDeletable("BusinessSector"));
    }

    @Test
    public void isImmutableEntityTest() throws IOException {
        Assert.assertTrue(StringConsts.isImmutable("Skill"));
        Assert.assertTrue(StringConsts.isImmutable("BusinessSector"));
        Assert.assertFalse(StringConsts.isImmutable("Candidate"));
        Assert.assertFalse(StringConsts.isImmutable("BOGUS"));
    }
}
