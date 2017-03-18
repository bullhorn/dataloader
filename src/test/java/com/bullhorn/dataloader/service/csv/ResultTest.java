package com.bullhorn.dataloader.service.csv;

import com.bullhorn.dataloader.util.Timer;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class ResultTest {

    @Test
    public void testConstructor() {
        final Result result = new Result(Result.Status.NOT_SET, Result.Action.NOT_SET, -1, "");
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
        final Result result = Result.Insert(99);

        Assert.assertEquals(result.isSuccess(), true);
        Assert.assertEquals(result.getStatus(), Result.Status.SUCCESS);
        Assert.assertEquals(result.getAction(), Result.Action.INSERT);
        Assert.assertEquals(result.getBullhornId().intValue(), 99);
        Assert.assertEquals(result.getFailureText(), "");
    }

    @Test
    public void testUpdate() {
        final Result result = Result.Update(99);

        Assert.assertEquals(result.isSuccess(), true);
        Assert.assertEquals(result.getStatus(), Result.Status.SUCCESS);
        Assert.assertEquals(result.getAction(), Result.Action.UPDATE);
        Assert.assertEquals(result.getBullhornId().intValue(), 99);
        Assert.assertEquals(result.getFailureText(), "");
    }

    @Test
    public void testDelete() {
        final Result result = Result.Delete(99);

        Assert.assertEquals(result.isSuccess(), true);
        Assert.assertEquals(result.getStatus(), Result.Status.SUCCESS);
        Assert.assertEquals(result.getAction(), Result.Action.DELETE);
        Assert.assertEquals(result.getBullhornId().intValue(), 99);
        Assert.assertEquals(result.getFailureText(), "");
    }

    @Test
    public void testFailure() {
        final Result result = Result.Failure(new Exception("Message 1"));

        Assert.assertEquals(result.isSuccess(), false);
        Assert.assertEquals(result.getStatus(), Result.Status.FAILURE);
        Assert.assertEquals(result.getAction(), Result.Action.FAILURE);
        Assert.assertEquals(result.getBullhornId().intValue(), -1);
        Assert.assertEquals(result.getFailureText(), "java.lang.Exception: Message 1");
    }

    @Test
    public void testInsertEnumToString() {
        final Result result = Result.Insert(99);

        Assert.assertEquals(result.getAction().toString(), "INSERT");
    }

    @Test
    public void testUpdateEnumToString() {
        final Result result = Result.Update(99);

        Assert.assertEquals(result.getAction().toString(), "UPDATE");
    }

    @Test
    public void testToString() {
        final Result result = new Result(Result.Status.NOT_SET, Result.Action.NOT_SET, -1, "");

        Assert.assertEquals(result.toString(), "Result{status=NOT_SET, action=NOT_SET, bullhornId=-1, failureText=''}");
    }

    @Test
    public void testEquals_identity() {
        final Result result1 = Result.Insert(99);
        final Result result2 = result1;

        Assert.assertEquals(result1, result2);
    }

    @Test
    public void testEquals_type() {
        final Result result = Result.Insert(99);
        final Timer timer = new Timer();

        Assert.assertNotEquals(result, timer);
    }

    @Test
    public void testEquals_status() {
        final Result result1 = Result.Insert(99);
        final Result result2 = Result.Insert(99);
        final Result different = Result.Failure(new Exception());

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testEquals_action() {
        final Result result1 = Result.Insert(1);
        final Result result2 = Result.Insert(1);
        final Result different = Result.Update(1);

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testEquals_action_setter() {
        final Result result1 = Result.Insert(1);
        final Result result2 = Result.Insert(1);
        final Result different = Result.Insert(1);
        different.setAction(Result.Action.UPDATE);

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testEquals_bullhornId() {
        final Result result1 = Result.Insert(1);
        final Result result2 = Result.Insert(1);
        final Result different = Result.Insert(2);

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testEquals_bullhornId_setter() {
        final Result result1 = Result.Insert(1);
        final Result result2 = Result.Insert(1);
        final Result different = Result.Insert(1);
        different.setBullhornId(2);

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testEquals_failureText() {
        final Result result1 = Result.Failure(new Exception("Message 1"));
        final Result result2 = Result.Failure(new Exception("Message 1"));
        final Result different = Result.Failure(new Exception("Message 2"));

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testHashCode_status() {
        final Set<Result> results = Sets.newHashSet();
        results.add(Result.Insert(99));
        results.add(Result.Failure(new Exception("")));
        results.add(Result.Failure(new Exception("")));

        Assert.assertEquals(2, results.size());
    }

    @Test
    public void testHashCode_action() {
        final Set<Result> results = Sets.newHashSet();
        results.add(Result.Insert(99));
        results.add(Result.Insert(99));
        results.add(Result.Update(99));
        results.add(Result.Delete(99));

        Assert.assertEquals(3, results.size());
    }

    @Test
    public void testHashCode_bullhornId() {
        final Set<Result> results = Sets.newHashSet();
        results.add(Result.Insert(99));
        results.add(Result.Insert(99));
        results.add(Result.Insert(100));

        Assert.assertEquals(2, results.size());
    }

    @Test
    public void testHashCode_failureMessage() {
        final Set<Result> results = Sets.newHashSet();
        results.add(Result.Failure(new Exception("Message 1")));
        results.add(Result.Failure(new Exception("Message 1")));
        results.add(Result.Failure(new Exception("Message 2")));

        Assert.assertEquals(2, results.size());
    }
}
