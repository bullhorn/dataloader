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
    public void testIsAddressTrue() {
        Cell cell = new Cell("secondaryAddress.state", "MO");

        Assert.assertEquals(cell.isAddress(), true);
    }

    @Test
    public void testIsAddressFalseNoAssociation() {
        Cell cell = new Cell("state", "MO");

        Assert.assertEquals(cell.isAddress(), false);
    }

    @Test
    public void testIsAddressFalseNoAddress() {
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

        Assert.assertNull(cell.getAssociationBaseName());
    }

    @Test
    public void testGetAssociationFieldNameValid() {
        Cell cell = new Cell("candidate.externalID", "1");

        Assert.assertEquals(cell.getAssociationFieldName(), "externalID");
    }

    @Test
    public void testGetAssociationFieldNameInvalid() {
        Cell cell = new Cell("externalID", "1");

        Assert.assertNull(cell.getAssociationFieldName());
    }

    @Test
    public void testSetAssociationNames() {
        Cell cell = new Cell("candidate.BadCAPiTALiZATioN", "test");

        Assert.assertEquals(cell.getAssociationBaseName(), "candidate");
        Assert.assertEquals(cell.getAssociationFieldName(), "BadCAPiTALiZATioN");

        cell.setAssociationNames("clientContact", "firstName");

        Assert.assertEquals(cell.getAssociationBaseName(), "clientContact");
        Assert.assertEquals(cell.getAssociationFieldName(), "firstName");
        Assert.assertEquals(cell.getValue(), "test");
    }
}
