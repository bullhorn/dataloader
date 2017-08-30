package com.bullhorn.dataloader.data;

import org.junit.Assert;
import org.junit.Test;

public class CellTest {

    @Test
    public void testConstructor() {
        Cell cell = new Cell("middleName", "Danger");

        Assert.assertEquals(cell.getName(), "middleName");
        Assert.assertEquals(cell.getValue(), "Danger");
    }

    @Test
    public void testAddressTrue() {
        Cell cell = new Cell("secondaryAddress.state", "MO");

        Assert.assertEquals(cell.isAddress(), true);
    }

    @Test
    public void testAddressFalseNoAssociation() {
        Cell cell = new Cell("state", "MO");

        Assert.assertEquals(cell.isAddress(), false);
    }

    @Test
    public void testAddressFalseNoAddress() {
        Cell cell = new Cell("secondary.state", "MO");

        Assert.assertEquals(cell.isAddress(), false);
    }

    @Test
    public void testIsAssociationTrue() {
        Cell cell = new Cell("candidate.externalID", "1");

        Assert.assertEquals(cell.isAssociation(), true);
    }

    @Test
    public void testIsAssociationFalse() {
        Cell cell = new Cell("externalID", "1");

        Assert.assertEquals(cell.isAssociation(), false);
    }

    @Test
    public void testGetAssociationBaseNameValid() {
        Cell cell = new Cell("candidate.externalID", "1");

        Assert.assertEquals(cell.getAssociationBaseName(), "candidate");
    }

    @Test
    public void testGetAssociationBaseNameInvalid() {
        Cell cell = new Cell("externalID", "1");

        Assert.assertEquals(cell.getAssociationBaseName(), null);
    }

    @Test
    public void testGetAssociationFieldNameValid() {
        Cell cell = new Cell("candidate.externalID", "1");

        Assert.assertEquals(cell.getAssociationFieldName(), "externalID");
    }

    @Test
    public void testGetAssociationFieldNameInvalid() {
        Cell cell = new Cell("externalID", "1");

        Assert.assertEquals(cell.getAssociationFieldName(), null);
    }
}
