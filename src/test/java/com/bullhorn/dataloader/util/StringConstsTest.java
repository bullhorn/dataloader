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
    public void isLoadableEntityTest() throws IOException {
        Assert.assertTrue(StringConsts.isLoadable("Placement"));
        Assert.assertTrue(StringConsts.isLoadable("ClientCorporation"));
        Assert.assertFalse(StringConsts.isLoadable("Skill"));
        Assert.assertFalse(StringConsts.isLoadable("BusinessSector"));
    }

    @Test
    public void isDeletableEntityTest() throws IOException {
        Assert.assertTrue(StringConsts.isDeletable("Placement"));
        Assert.assertTrue(StringConsts.isDeletable("Candidate"));
        Assert.assertFalse(StringConsts.isDeletable("ClientCorporation"));
        Assert.assertFalse(StringConsts.isDeletable("BusinessSector"));
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
    public void isNotDeletableEntityTest() throws IOException {
        Assert.assertTrue(StringConsts.isNotDeletable("ClientCorporation"));
        Assert.assertFalse(StringConsts.isNotDeletable("Candidate"));
        Assert.assertFalse(StringConsts.isNotDeletable("Sendout"));
    }

    @Test
    public void isReadOnlyEntityTest() throws IOException {
        Assert.assertTrue(StringConsts.isReadOnly("Skill"));
        Assert.assertTrue(StringConsts.isReadOnly("BusinessSector"));
        Assert.assertFalse(StringConsts.isReadOnly("Candidate"));
        Assert.assertFalse(StringConsts.isReadOnly("BOGUS"));
    }
}
