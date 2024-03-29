package com.bullhorn.dataloader.util;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.enums.ErrorInfo;
import com.bullhorn.dataloader.rest.Field;
import com.bullhornsdk.data.model.entity.association.AssociationFactory;
import com.bullhornsdk.data.model.entity.association.AssociationField;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;

@SuppressWarnings("InstantiationOfUtilityClass")
public class AssociationUtilTest {

    @Test
    public void testConstructor() {
        AssociationUtil associationUtil = new AssociationUtil();
        Assert.assertNotNull(associationUtil);
    }

    @Test
    public void testGetToManyFields() {
        Assert.assertEquals(AssociationUtil.getToManyFields(EntityInfo.CATEGORY), AssociationFactory.categoryAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getToManyFields(EntityInfo.CATEGORY), AssociationFactory.categoryAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getToManyFields(EntityInfo.CLIENT_CONTACT), AssociationFactory.clientContactAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getToManyFields(EntityInfo.CORPORATE_USER), AssociationFactory.corporateUserAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getToManyFields(EntityInfo.JOB_ORDER), AssociationFactory.jobOrderAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getToManyFields(EntityInfo.PLACEMENT), AssociationFactory.placementAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getToManyFields(EntityInfo.OPPORTUNITY), AssociationFactory.opportunityAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getToManyFields(EntityInfo.LEAD), AssociationFactory.leadAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getToManyFields(EntityInfo.TEARSHEET), AssociationFactory.tearsheetAssociations().allAssociations());
    }

    @Test
    public void testGetToManyFieldsEmpty() {
        List<AssociationField<AssociationEntity, BullhornEntity>> associationFields =
            AssociationUtil.getToManyFields(EntityInfo.CANDIDATE_WORK_HISTORY);
        Assert.assertTrue(associationFields.isEmpty());
    }

    @Test
    public void testGetToManyField() {
        Cell cell = new Cell("clientContacts.id", "1");
        Field field = new Field(EntityInfo.NOTE, cell, false, null);
        AssociationField actual = AssociationUtil.getToManyField(field);
        Assert.assertEquals(ClientContact.class, actual.getAssociationType());
        Assert.assertEquals("clientContacts", actual.getAssociationFieldName());
    }

    @Test
    public void testGetFieldEntityException() {
        DataLoaderException expectedException = new DataLoaderException(ErrorInfo.INCORRECT_COLUMN_NAME, "'trolls' does not exist on Candidate");
        DataLoaderException actualException = null;

        try {
            AssociationUtil.getToManyField(EntityInfo.CANDIDATE, "trolls");
        } catch (DataLoaderException e) {
            actualException = e;
        }

        Assert.assertNotNull(actualException);
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    public void testGetFieldEntityDirect() {
        Cell cell = new Cell("externalID", "note-ext-1");
        EntityInfo entityInfo = AssociationUtil.getFieldEntity(EntityInfo.NOTE, cell);
        Assert.assertEquals(EntityInfo.NOTE, entityInfo);
    }

    @Test
    public void testGetFieldEntityToOne() {
        Cell cell = new Cell("commentingPerson.email", "jsmith@example.com");
        EntityInfo entityInfo = AssociationUtil.getFieldEntity(EntityInfo.NOTE, cell);
        Assert.assertEquals(EntityInfo.PERSON, entityInfo);
    }

    @Test
    public void testGetFieldEntityCompound() {
        Cell cell = new Cell("address.city", "St. Louis");
        EntityInfo entityInfo = AssociationUtil.getFieldEntity(EntityInfo.CANDIDATE, cell);
        Assert.assertEquals(EntityInfo.ADDRESS, entityInfo);
    }

    @Test
    public void testGetFieldEntityToMany() {
        Cell cell = new Cell("candidates.externalID", "1;2;3;4");
        EntityInfo entityInfo = AssociationUtil.getFieldEntity(EntityInfo.NOTE, cell);
        Assert.assertEquals(EntityInfo.CANDIDATE, entityInfo);
    }

    @Test
    public void testGetFieldEntityEmpty() {
        Cell cell = new Cell("", "");
        EntityInfo entityInfo = AssociationUtil.getFieldEntity(EntityInfo.CANDIDATE, cell);
        Assert.assertEquals(EntityInfo.CANDIDATE, entityInfo);
    }

    @Test(expected = DataLoaderException.class)
    public void testGetFieldEntityInvalidAssociation() {
        Cell cell = new Cell("name.first", "bill");
        AssociationUtil.getFieldEntity(EntityInfo.CANDIDATE, cell);
    }
}
