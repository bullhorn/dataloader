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
    public void testSetters() {
        Cell cell = new Cell("middleName", "Danger");
        cell.setName("middleInitial");
        cell.setValue("D");

        Assert.assertEquals(cell.getName(), "middleInitial");
        Assert.assertEquals(cell.getValue(), "D");
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
    public void testGetAssociationNameValid() {
        Cell cell = new Cell("candidate.externalID", "1");

        Assert.assertEquals(cell.getAssociationName(), "candidate");
    }

    @Test
    public void testGetAssociationNameInvalid() {
        Cell cell = new Cell("externalID", "1");

        Assert.assertEquals(cell.getAssociationName(), null);
    }

    @Test
    public void testGetAssociationFieldValid() {
        Cell cell = new Cell("candidate.externalID", "1");

        Assert.assertEquals(cell.getAssociationField(), "externalID");
    }

    @Test
    public void testGetAssociationFieldInvalid() {
        Cell cell = new Cell("externalID", "1");

        Assert.assertEquals(cell.getAssociationField(), null);
    }
}
