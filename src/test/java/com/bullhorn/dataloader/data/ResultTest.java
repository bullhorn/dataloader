package com.bullhorn.dataloader.data;

import com.bullhorn.dataloader.util.Timer;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class ResultTest {

    @Test
    public void testConstructor() {
        Result result = new Result(Result.Status.NOT_SET, Result.Action.NOT_SET, -1, "");
        result.setStatus(Result.Status.FAILURE);
        result.setFailureText("Message 1");

        Assert.assertEquals(result.isSuccess(), false);
        Assert.assertEquals(result.getStatus(), Result.Status.FAILURE);
        Assert.assertEquals(result.getAction(), Result.Action.NOT_SET);
        Assert.assertEquals(result.getBullhornId().intValue(), -1);
        Assert.assertEquals(result.getFailureText(), "Message 1");
    }

    @Test
    public void testInsert() {
        Result result = Result.Insert(99);

        Assert.assertEquals(result.isSuccess(), true);
        Assert.assertEquals(result.getStatus(), Result.Status.SUCCESS);
        Assert.assertEquals(result.getAction(), Result.Action.INSERT);
        Assert.assertEquals(result.getBullhornId().intValue(), 99);
        Assert.assertEquals(result.getFailureText(), "");
    }

    @Test
    public void testUpdate() {
        Result result = Result.Update(99);

        Assert.assertEquals(result.isSuccess(), true);
        Assert.assertEquals(result.getStatus(), Result.Status.SUCCESS);
        Assert.assertEquals(result.getAction(), Result.Action.UPDATE);
        Assert.assertEquals(result.getBullhornId().intValue(), 99);
        Assert.assertEquals(result.getFailureText(), "");
    }

    @Test
    public void testDelete() {
        Result result = Result.Delete(99);

        Assert.assertEquals(result.isSuccess(), true);
        Assert.assertEquals(result.getStatus(), Result.Status.SUCCESS);
        Assert.assertEquals(result.getAction(), Result.Action.DELETE);
        Assert.assertEquals(result.getBullhornId().intValue(), 99);
        Assert.assertEquals(result.getFailureText(), "");
    }

    @Test
    public void testFailure() {
        Result result = Result.Failure(new Exception("Message 1"));

        Assert.assertEquals(result.isSuccess(), false);
        Assert.assertEquals(result.getStatus(), Result.Status.FAILURE);
        Assert.assertEquals(result.getAction(), Result.Action.FAILURE);
        Assert.assertEquals(result.getBullhornId().intValue(), -1);
        Assert.assertEquals(result.getFailureText(), "java.lang.Exception: Message 1");
    }

    @Test
    public void testNullBullhornId() {
        Result result = Result.Update(null);

        Assert.assertEquals(result.isSuccess(), true);
        Assert.assertEquals(result.getStatus(), Result.Status.SUCCESS);
        Assert.assertEquals(result.getAction(), Result.Action.UPDATE);
        Assert.assertEquals(result.getBullhornId().intValue(), -1);
        Assert.assertEquals(result.getFailureText(), "");
    }

    @Test
    public void testInsertEnumToString() {
        Result result = Result.Insert(99);

        Assert.assertEquals(result.getAction().toString(), "INSERT");
    }

    @Test
    public void testUpdateEnumToString() {
        Result result = Result.Update(99);

        Assert.assertEquals(result.getAction().toString(), "UPDATE");
    }

    @Test
    public void testToString() {
        Result result = new Result(Result.Status.NOT_SET, Result.Action.NOT_SET, -1, "");

        Assert.assertEquals(result.toString(), "Result{status=NOT_SET, action=NOT_SET, bullhornId=-1, failureText=''}");
    }

    @Test
    public void testEquals_identity() {
        Result result1 = Result.Insert(99);
        Result result2 = result1;

        Assert.assertEquals(result1, result2);
    }

    @Test
    public void testEquals_type() {
        Result result = Result.Insert(99);
        Timer timer = new Timer();

        Assert.assertNotEquals(result, timer);
    }

    @Test
    public void testEquals_status() {
        Result result1 = Result.Insert(99);
        Result result2 = Result.Insert(99);
        Result different = Result.Failure(new Exception());

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testEquals_action() {
        Result result1 = Result.Insert(1);
        Result result2 = Result.Insert(1);
        Result different = Result.Update(1);

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testEquals_action_setter() {
        Result result1 = Result.Insert(1);
        Result result2 = Result.Insert(1);
        Result different = Result.Insert(1);
        different.setAction(Result.Action.UPDATE);

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testEquals_bullhornId() {
        Result result1 = Result.Insert(1);
        Result result2 = Result.Insert(1);
        Result different = Result.Insert(2);

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testEquals_bullhornId_setter() {
        Result result1 = Result.Insert(1);
        Result result2 = Result.Insert(1);
        Result different = Result.Insert(1);
        different.setBullhornId(2);

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testEquals_failureText() {
        Result result1 = Result.Failure(new Exception("Message 1"));
        Result result2 = Result.Failure(new Exception("Message 1"));
        Result different = Result.Failure(new Exception("Message 2"));

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testHashCode_status() {
        Set<Result> results = Sets.newHashSet();
        results.add(Result.Insert(99));
        results.add(Result.Failure(new Exception("")));
        results.add(Result.Failure(new Exception("")));

        Assert.assertEquals(2, results.size());
    }

    @Test
    public void testHashCode_action() {
        Set<Result> results = Sets.newHashSet();
        results.add(Result.Insert(99));
        results.add(Result.Insert(99));
        results.add(Result.Update(99));
        results.add(Result.Delete(99));

        Assert.assertEquals(3, results.size());
    }

    @Test
    public void testHashCode_bullhornId() {
        Set<Result> results = Sets.newHashSet();
        results.add(Result.Insert(99));
        results.add(Result.Insert(99));
        results.add(Result.Insert(100));

        Assert.assertEquals(2, results.size());
    }

    @Test
    public void testHashCode_failureMessage() {
        Set<Result> results = Sets.newHashSet();
        results.add(Result.Failure(new Exception("Message 1")));
        results.add(Result.Failure(new Exception("Message 1")));
        results.add(Result.Failure(new Exception("Message 2")));

        Assert.assertEquals(2, results.size());
    }
}