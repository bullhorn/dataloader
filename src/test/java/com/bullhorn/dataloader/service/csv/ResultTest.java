package com.bullhorn.dataloader.service.csv;

import org.junit.Assert;
import org.junit.Test;

public class ResultTest {

    @Test
    public void testConstructor() {
        //arrange
        Result result = new Result(Result.Status.NOT_SET, Result.Action.NOT_SET, -1, "");

        //act
        result.setStatus(Result.Status.FAILURE);
        result.setFailureText("something good (just kidding!)");

        //assert
        Assert.assertEquals(result.isSuccess(), false);
        Assert.assertEquals(result.getStatus(), Result.Status.FAILURE);
        Assert.assertEquals(result.getAction(), Result.Action.NOT_SET);
        Assert.assertEquals(result.getBullhornId().intValue(), -1);
        Assert.assertEquals(result.getFailureText(), "something good (just kidding!)");
    }

    @Test
    public void testInsert() {
        //arrange

        //act
        Result result = Result.Insert(99);

        //assert
        Assert.assertEquals(result.isSuccess(), true);
        Assert.assertEquals(result.getStatus(), Result.Status.SUCCESS);
        Assert.assertEquals(result.getAction(), Result.Action.INSERT);
        Assert.assertEquals(result.getBullhornId().intValue(), 99);
        Assert.assertEquals(result.getFailureText(), "");
    }

    @Test
    public void testUpdate() {
        //arrange

        //act
        Result result = Result.Update(99);

        //assert
        Assert.assertEquals(result.isSuccess(), true);
        Assert.assertEquals(result.getStatus(), Result.Status.SUCCESS);
        Assert.assertEquals(result.getAction(), Result.Action.UPDATE);
        Assert.assertEquals(result.getBullhornId().intValue(), 99);
        Assert.assertEquals(result.getFailureText(), "");
    }

    @Test
    public void testDelete() {
        //arrange

        //act
        Result result = Result.Delete(99);

        //assert
        Assert.assertEquals(result.isSuccess(), true);
        Assert.assertEquals(result.getStatus(), Result.Status.SUCCESS);
        Assert.assertEquals(result.getAction(), Result.Action.DELETE);
        Assert.assertEquals(result.getBullhornId().intValue(), 99);
        Assert.assertEquals(result.getFailureText(), "");
    }

    @Test
    public void testFailure() {
        //arrange

        //act
        Result result = Result.Failure("something awful");

        //assert
        Assert.assertEquals(result.isSuccess(), false);
        Assert.assertEquals(result.getStatus(), Result.Status.FAILURE);
        Assert.assertEquals(result.getAction(), Result.Action.NOT_SET);
        Assert.assertEquals(result.getBullhornId().intValue(), -1);
        Assert.assertEquals(result.getFailureText(), "something awful");
    }

    @Test
    public void testInsertEnumToString() {
        //arrange

        //act
        Result result = Result.Insert(99);

        //assert
        Assert.assertEquals(result.getAction().toString(), "INSERT");
    }

    @Test
    public void testUpdateEnumToString() {
        //arrange

        //act
        Result result = Result.Update(99);

        //assert
        Assert.assertEquals(result.getAction().toString(), "UPDATE");
    }
}
