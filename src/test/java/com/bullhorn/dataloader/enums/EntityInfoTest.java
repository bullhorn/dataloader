package com.bullhorn.dataloader.enums;

import com.bullhornsdk.data.model.enums.BullhornEntityInfo;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class EntityInfoTest {

    @Test
    public void testGetBullhornEntityInfo() throws IOException {
        Assert.assertEquals(BullhornEntityInfo.PLACEMENT, EntityInfo.PLACEMENT.getBullhornEntityInfo());
        Assert.assertEquals(BullhornEntityInfo.CLIENT_CORPORATION, EntityInfo.CLIENT_CORPORATION.getBullhornEntityInfo());
        Assert.assertEquals(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_1, EntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_1.getBullhornEntityInfo());
        Assert.assertEquals(BullhornEntityInfo.SKILL, EntityInfo.SKILL.getBullhornEntityInfo());
        Assert.assertEquals(BullhornEntityInfo.BUSINESS_SECTOR, EntityInfo.BUSINESS_SECTOR.getBullhornEntityInfo());
    }

    @Test
    public void testIsLoadable() throws IOException {
        Assert.assertTrue(EntityInfo.PLACEMENT.isLoadable());
        Assert.assertTrue(EntityInfo.CLIENT_CORPORATION.isLoadable());
        Assert.assertTrue(EntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_1.isLoadable());
        Assert.assertFalse(EntityInfo.SKILL.isLoadable());
        Assert.assertFalse(EntityInfo.BUSINESS_SECTOR.isLoadable());
    }

    @Test
    public void testIsInsertable() throws IOException {
        Assert.assertTrue(EntityInfo.PLACEMENT.isInsertable());
        Assert.assertTrue(EntityInfo.CLIENT_CORPORATION.isInsertable());
        Assert.assertTrue(EntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_10.isInsertable());
        Assert.assertFalse(EntityInfo.SKILL.isInsertable());
        Assert.assertFalse(EntityInfo.BUSINESS_SECTOR.isInsertable());
    }

    @Test
    public void testIsUpdatable() throws IOException {
        Assert.assertTrue(EntityInfo.PLACEMENT.isUpdatable());
        Assert.assertTrue(EntityInfo.CLIENT_CORPORATION.isUpdatable());
        Assert.assertTrue(EntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_1.isUpdatable());
        Assert.assertFalse(EntityInfo.SKILL.isUpdatable());
        Assert.assertFalse(EntityInfo.BUSINESS_SECTOR.isUpdatable());
    }

    @Test
    public void testIsDeletable() throws IOException {
        Assert.assertTrue(EntityInfo.PLACEMENT.isDeletable());
        Assert.assertTrue(EntityInfo.CANDIDATE.isDeletable());
        Assert.assertTrue(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_3.isDeletable());
        Assert.assertFalse(EntityInfo.CLIENT_CORPORATION.isDeletable());
        Assert.assertFalse(EntityInfo.BUSINESS_SECTOR.isDeletable());
    }

    @Test
    public void testIsHardDeletable() throws IOException {
        Assert.assertTrue(EntityInfo.PLACEMENT.isHardDeletable());
        Assert.assertTrue(EntityInfo.SENDOUT.isHardDeletable());
        Assert.assertTrue(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1.isHardDeletable());
        Assert.assertFalse(EntityInfo.SKILL.isHardDeletable());
        Assert.assertFalse(EntityInfo.BUSINESS_SECTOR.isHardDeletable());
    }

    @Test
    public void testIsSoftDeletable() throws IOException {
        Assert.assertTrue(EntityInfo.CLIENT_CONTACT.isSoftDeletable());
        Assert.assertTrue(EntityInfo.CANDIDATE.isSoftDeletable());
        Assert.assertFalse(EntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_1.isSoftDeletable());
        Assert.assertFalse(EntityInfo.SENDOUT.isSoftDeletable());
        Assert.assertFalse(EntityInfo.BUSINESS_SECTOR.isSoftDeletable());
    }

    @Test
    public void testIsReadOnly() throws IOException {
        Assert.assertTrue(EntityInfo.SKILL.isReadOnly());
        Assert.assertTrue(EntityInfo.BUSINESS_SECTOR.isReadOnly());
        Assert.assertFalse(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_4.isReadOnly());
        Assert.assertFalse(EntityInfo.CANDIDATE.isReadOnly());
    }

    @Test
    public void testIsCustomObject() throws IOException {
        Assert.assertTrue(EntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_5.isCustomObject());
        Assert.assertFalse(EntityInfo.SKILL.isCustomObject());
        Assert.assertFalse(EntityInfo.BUSINESS_SECTOR.isCustomObject());
        Assert.assertFalse(EntityInfo.CANDIDATE.isCustomObject());
    }

    @Test
    public void testIsAttachmentEntity() throws IOException {
        Assert.assertTrue(EntityInfo.CANDIDATE.isAttachmentEntity());
        Assert.assertTrue(EntityInfo.CLIENT_CONTACT.isAttachmentEntity());
        Assert.assertTrue(EntityInfo.CLIENT_CORPORATION.isAttachmentEntity());
        Assert.assertFalse(EntityInfo.JOB_SUBMISSION.isAttachmentEntity());
        Assert.assertFalse(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_4.isAttachmentEntity());
        Assert.assertFalse(EntityInfo.SKILL.isAttachmentEntity());
        Assert.assertFalse(EntityInfo.BUSINESS_SECTOR.isAttachmentEntity());
    }
}
