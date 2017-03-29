package com.bullhorn.dataloader.enums;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class EntityInfoTest {

    @Test
    public void isLoadableEntityTest() throws IOException {
        Assert.assertTrue(EntityInfo.PLACEMENT.isLoadable());
        Assert.assertTrue(EntityInfo.CLIENT_CORPORATION.isLoadable());
        Assert.assertFalse(EntityInfo.SKILL.isLoadable());
        Assert.assertFalse(EntityInfo.BUSINESS_SECTOR.isLoadable());
    }

    @Test
    public void isDeletableEntityTest() throws IOException {
        Assert.assertTrue(EntityInfo.PLACEMENT.isDeletable());
        Assert.assertTrue(EntityInfo.CANDIDATE.isDeletable());
        Assert.assertFalse(EntityInfo.CLIENT_CORPORATION.isDeletable());
        Assert.assertFalse(EntityInfo.BUSINESS_SECTOR.isDeletable());
    }

    @Test
    public void isHardDeletableEntityTest() throws IOException {
        Assert.assertTrue(EntityInfo.PLACEMENT.isHardDeletable());
        Assert.assertTrue(EntityInfo.SENDOUT.isHardDeletable());
        Assert.assertFalse(EntityInfo.SKILL.isHardDeletable());
        Assert.assertFalse(EntityInfo.BUSINESS_SECTOR.isHardDeletable());
    }

    @Test
    public void isSoftDeletableEntityTest() throws IOException {
        Assert.assertTrue(EntityInfo.CLIENT_CONTACT.isSoftDeletable());
        Assert.assertTrue(EntityInfo.CANDIDATE.isSoftDeletable());
        Assert.assertFalse(EntityInfo.SENDOUT.isSoftDeletable());
        Assert.assertFalse(EntityInfo.BUSINESS_SECTOR.isSoftDeletable());
    }

    @Test
    public void isNotDeletableEntityTest() throws IOException {
        Assert.assertFalse(EntityInfo.CLIENT_CORPORATION.isDeletable());
        Assert.assertTrue(EntityInfo.CANDIDATE.isDeletable());
        Assert.assertTrue(EntityInfo.SENDOUT.isDeletable());
    }

    @Test
    public void isReadOnlyEntityTest() throws IOException {
        Assert.assertTrue(EntityInfo.SKILL.isReadOnly());
        Assert.assertTrue(EntityInfo.BUSINESS_SECTOR.isReadOnly());
        Assert.assertFalse(EntityInfo.CANDIDATE.isReadOnly());
    }
}
