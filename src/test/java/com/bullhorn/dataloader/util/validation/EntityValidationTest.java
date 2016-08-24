package com.bullhorn.dataloader.util.validation;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class EntityValidationTest {

    @Test
    public void isLoadableEntityTest() throws IOException {
        Assert.assertTrue(EntityValidation.isLoadable("Placement"));
        Assert.assertTrue(EntityValidation.isLoadable("ClientCorporation"));
        Assert.assertFalse(EntityValidation.isLoadable("Skill"));
        Assert.assertFalse(EntityValidation.isLoadable("BusinessSector"));
    }

    @Test
    public void isDeletableEntityTest() throws IOException {
        Assert.assertTrue(EntityValidation.isDeletable("Placement"));
        Assert.assertTrue(EntityValidation.isDeletable("Candidate"));
        Assert.assertFalse(EntityValidation.isDeletable("ClientCorporation"));
        Assert.assertFalse(EntityValidation.isDeletable("BusinessSector"));
    }

    @Test
    public void isHardDeletableEntityTest() throws IOException {
        Assert.assertTrue(EntityValidation.isHardDeletable("Placement"));
        Assert.assertTrue(EntityValidation.isHardDeletable("Sendout"));
        Assert.assertFalse(EntityValidation.isHardDeletable("Skill"));
        Assert.assertFalse(EntityValidation.isHardDeletable("BusinessSector"));
    }

    @Test
    public void isSoftDeletableEntityTest() throws IOException {
        Assert.assertTrue(EntityValidation.isSoftDeletable("ClientContact"));
        Assert.assertTrue(EntityValidation.isSoftDeletable("Candidate"));
        Assert.assertFalse(EntityValidation.isSoftDeletable("Sendout"));
        Assert.assertFalse(EntityValidation.isSoftDeletable("BusinessSector"));
    }

    @Test
    public void isNotDeletableEntityTest() throws IOException {
        Assert.assertTrue(EntityValidation.isNotDeletable("ClientCorporation"));
        Assert.assertFalse(EntityValidation.isNotDeletable("Candidate"));
        Assert.assertFalse(EntityValidation.isNotDeletable("Sendout"));
    }

    @Test
    public void isReadOnlyEntityTest() throws IOException {
        Assert.assertTrue(EntityValidation.isReadOnly("Skill"));
        Assert.assertTrue(EntityValidation.isReadOnly("BusinessSector"));
        Assert.assertFalse(EntityValidation.isReadOnly("Candidate"));
    }
}
