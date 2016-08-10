package com.bullhorn.dataloader.util;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ActionTotalsTest {

    private ActionTotals actionTotals;

    @Before
    public void setup() throws IOException {
        actionTotals = new ActionTotals();
    }

    @Test
    public void testIncrementTotalInsert() {
        Assert.assertEquals(actionTotals.getTotalInsert(), 0);
        actionTotals.incrementTotalInsert();
        Assert.assertEquals(actionTotals.getTotalInsert(), 1);
    }

    @Test
    public void testIncrementTotalUpdate() {
        Assert.assertEquals(actionTotals.getTotalUpdate(), 0);
        actionTotals.incrementTotalUpdate();
        Assert.assertEquals(actionTotals.getTotalUpdate(), 1);
    }

    @Test
    public void testIncrementTotalError() {
        Assert.assertEquals(actionTotals.getTotalError(), 0);
        actionTotals.incrementTotalError();
        Assert.assertEquals(actionTotals.getTotalError(), 1);
    }

    @Test
    public void testIncrementTotalDelete() {
        Assert.assertEquals(actionTotals.getTotalDelete(), 0);
        actionTotals.incrementTotalDelete();
        Assert.assertEquals(actionTotals.getTotalDelete(), 1);
    }
}
