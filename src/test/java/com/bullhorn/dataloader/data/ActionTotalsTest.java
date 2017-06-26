package com.bullhorn.dataloader.data;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ActionTotalsTest {

    private ActionTotals actionTotals;

    @Before
    public void setup() throws IOException {
        actionTotals = new ActionTotals();
    }

    @Test
    public void testIncrementTotalInsert1() {
        Result.Action action = Result.Action.UPDATE;
        Assert.assertEquals(actionTotals.getActionTotal(action), 0);
        actionTotals.incrementActionTotal(action);
        Assert.assertEquals(actionTotals.getActionTotal(action), 1);
    }

}
