package com.bullhorn.dataloader.util;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ActionTotalsTest {

    private ActionTotals actionTotals1;
    private ActionTotals actionTotals2;

    @Before
    public void setup() throws IOException {
        actionTotals1 = new ActionTotals();
        actionTotals2 = new ActionTotals();
    }

    @Test
    public void testIncrementTotalInsert1() {
        Assert.assertEquals(actionTotals1.getTotalInsert(), 0);
        actionTotals1.incrementTotalInsert();
        Assert.assertEquals(actionTotals1.getTotalInsert(), 1);
    }

    @Test
    public void testIncrementTotalInsert2() {
        Assert.assertEquals(actionTotals2.getTotalInsert(), 0);
        actionTotals2.incrementTotalInsert();
        Assert.assertEquals(actionTotals2.getTotalInsert(), 1);
    }

    @Test
    public void testIncrementTotalUpdate1() {
        Assert.assertEquals(actionTotals1.getTotalUpdate(), 0);
        actionTotals1.incrementTotalUpdate();
        Assert.assertEquals(actionTotals1.getTotalUpdate(), 1);
    }

    @Test
    public void testIncrementTotalUpdate2() {
        Assert.assertEquals(actionTotals2.getTotalUpdate(), 0);
        actionTotals2.incrementTotalUpdate();
        Assert.assertEquals(actionTotals2.getTotalUpdate(), 1);
    }

    @Test
    public void testIncrementTotalError1() {
        Assert.assertEquals(actionTotals1.getTotalError(), 0);
        actionTotals1.incrementTotalError();
        Assert.assertEquals(actionTotals1.getTotalError(), 1);
    }

    @Test
    public void testIncrementTotalError2() {
        Assert.assertEquals(actionTotals2.getTotalError(), 0);
        actionTotals2.incrementTotalError();
        Assert.assertEquals(actionTotals2.getTotalError(), 1);
    }

    @Test
    public void testIncrementTotalDelete1() {
        Assert.assertEquals(actionTotals1.getTotalDelete(), 0);
        actionTotals1.incrementTotalDelete();
        Assert.assertEquals(actionTotals1.getTotalDelete(), 1);
    }

    @Test
    public void testIncrementTotalDelete2() {
        Assert.assertEquals(actionTotals2.getTotalDelete(), 0);
        actionTotals2.incrementTotalDelete();
        Assert.assertEquals(actionTotals2.getTotalDelete(), 1);
    }
}
