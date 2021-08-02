package com.bullhorn.dataloader.data;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.bullhorn.dataloader.enums.ErrorInfo;
import com.bullhorn.dataloader.util.DataLoaderException;
import com.bullhorn.dataloader.util.Timer;
import com.google.common.collect.Sets;

public class ResultTest {

    @Test
    public void testConstructor() {
        Result result = new Result(Result.Status.NOT_SET, Result.Action.NOT_SET);
        result.setStatus(Result.Status.FAILURE);
        result.setErrorDetails("Message 1");

        Assert.assertEquals(result.isSuccess(), false);
        Assert.assertEquals(result.getStatus(), Result.Status.FAILURE);
        Assert.assertEquals(result.getAction(), Result.Action.NOT_SET);
        Assert.assertEquals(result.getBullhornId().intValue(), -1);
        Assert.assertEquals(result.getErrorDetails(), "Message 1");
    }

    @Test
    public void testInsert() {
        Result result = Result.insert(99);

        Assert.assertEquals(result.isSuccess(), true);
        Assert.assertEquals(result.getStatus(), Result.Status.SUCCESS);
        Assert.assertEquals(result.getAction(), Result.Action.INSERT);
        Assert.assertEquals(result.getBullhornId().intValue(), 99);
        Assert.assertEquals(result.getErrorDetails(), "");
    }

    @Test
    public void testUpdate() {
        Result result = Result.update(99);

        Assert.assertEquals(result.isSuccess(), true);
        Assert.assertEquals(result.getStatus(), Result.Status.SUCCESS);
        Assert.assertEquals(result.getAction(), Result.Action.UPDATE);
        Assert.assertEquals(result.getBullhornId().intValue(), 99);
        Assert.assertEquals(result.getErrorDetails(), "");
    }

    @Test
    public void testDelete() {
        Result result = Result.delete(99);

        Assert.assertEquals(result.isSuccess(), true);
        Assert.assertEquals(result.getStatus(), Result.Status.SUCCESS);
        Assert.assertEquals(result.getAction(), Result.Action.DELETE);
        Assert.assertEquals(result.getBullhornId().intValue(), 99);
        Assert.assertEquals(result.getErrorDetails(), "");
    }

    @Test
    public void testFailure() {
        Result result = Result.failure(new Exception("Message 1"));

        Assert.assertEquals(result.isSuccess(), false);
        Assert.assertEquals(result.getStatus(), Result.Status.FAILURE);
        Assert.assertEquals(result.getAction(), Result.Action.FAILURE);
        Assert.assertEquals(result.getBullhornId().intValue(), -1);
        Assert.assertEquals(result.getErrorDetails(), "Message 1");
    }

    @Test
    public void testNullBullhornId() {
        Result result = Result.update(null);

        Assert.assertEquals(result.isSuccess(), true);
        Assert.assertEquals(result.getStatus(), Result.Status.SUCCESS);
        Assert.assertEquals(result.getAction(), Result.Action.UPDATE);
        Assert.assertEquals(result.getBullhornId().intValue(), -1);
        Assert.assertEquals(result.getErrorDetails(), "");
    }

    @Test
    public void testNullGetterChecks() {
        Result result = new Result(Result.Status.FAILURE, Result.Action.UPDATE);

        Assert.assertEquals(result.getStatus(), Result.Status.FAILURE);
        Assert.assertEquals(result.getAction(), Result.Action.UPDATE);
        Assert.assertEquals(result.getBullhornId().intValue(), -1);
        Assert.assertEquals(result.getBullhornParentId().intValue(), -1);
        Assert.assertEquals(result.getErrorInfo(), ErrorInfo.UNKNOWN_ERROR);
        Assert.assertEquals(result.getErrorDetails(), "");

        // Force data to null to ensure it still provides valid defaults
        result.setStatus(null);
        result.setAction(null);
        result.setBullhornId(null);
        result.setBullhornParentId(null);
        result.setErrorInfo(null);
        result.setErrorDetails(null);

        Assert.assertEquals(result.getStatus(), Result.Status.NOT_SET);
        Assert.assertEquals(result.getAction(), Result.Action.NOT_SET);
        Assert.assertEquals(result.getBullhornId().intValue(), -1);
        Assert.assertEquals(result.getBullhornParentId().intValue(), -1);
        Assert.assertEquals(result.getErrorInfo(), ErrorInfo.UNKNOWN_ERROR);
        Assert.assertEquals(result.getErrorDetails(), "");
    }

    @Test
    public void testInsertEnumToString() {
        Result result = Result.insert(99);

        Assert.assertEquals(result.getAction().toString(), "INSERT");
    }

    @Test
    public void testUpdateEnumToString() {
        Result result = Result.update(99);

        Assert.assertEquals(result.getAction().toString(), "UPDATE");
    }

    @Test
    public void testToString() {
        Result result = new Result(Result.Status.NOT_SET, Result.Action.NOT_SET);

        Assert.assertEquals(result.toString(),
            "Result{status=NOT_SET, action=NOT_SET, bullhornId=-1, bullhornParentId=-1, errorInfo='UNKNOWN_ERROR', errorDetails=''}");
    }

    @Test
    public void testEquals_identity() {
        Result result1 = Result.insert(99);

        Assert.assertEquals(result1, result1);
    }

    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    @Test
    public void testEquals_type() {
        Result result = Result.insert(99);
        Timer timer = new Timer();

        Assert.assertNotEquals(result, timer);
    }

    @Test
    public void testEquals_status() {
        Result result1 = Result.insert(99);
        Result result2 = Result.insert(99);
        Result different = Result.failure(new Exception());

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testEquals_action() {
        Result result1 = Result.insert(1);
        Result result2 = Result.insert(1);
        Result different = Result.update(1);

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testEquals_action_setter() {
        Result result1 = Result.insert(1);
        Result result2 = Result.insert(1);
        Result different = Result.insert(1);
        different.setAction(Result.Action.UPDATE);

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testEquals_bullhornId() {
        Result result1 = Result.insert(1);
        Result result2 = Result.insert(1);
        Result different = Result.insert(2);

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testEquals_bullhornId_setter() {
        Result result1 = Result.insert(1);
        Result result2 = Result.insert(1);
        Result different = Result.insert(1);
        different.setBullhornId(2);

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testEquals_bullhornParentId_setter() {
        Result result1 = Result.insert(1);
        Result result2 = Result.insert(1);
        Result different = Result.insert(1);
        different.setBullhornParentId(2);

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testEquals_errorInfo() {
        Result result1 = Result.failure(new DataLoaderException(ErrorInfo.INVALID_CSV_FILE, "Generic Message"));
        Result result2 = Result.failure(new DataLoaderException(ErrorInfo.INVALID_CSV_FILE, "Generic Message"));
        Result different = Result.failure(new DataLoaderException(ErrorInfo.INVALID_SETTING, "Generic Message"));

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testEquals_errorDetails() {
        Result result1 = Result.failure(new Exception("Message 1"));
        Result result2 = Result.failure(new Exception("Message 1"));
        Result different = Result.failure(new Exception("Message 2"));

        Assert.assertEquals(result1, result2);
        Assert.assertNotEquals(result1, different);
    }

    @Test
    public void testHashCode_status() {
        Set<Result> results = Sets.newHashSet();
        results.add(Result.insert(99));
        results.add(Result.failure(new Exception("")));
        results.add(Result.failure(new Exception("")));

        Assert.assertEquals(2, results.size());
    }

    @Test
    public void testHashCode_action() {
        Set<Result> results = Sets.newHashSet();
        results.add(Result.insert(99));
        results.add(Result.insert(99));
        results.add(Result.update(99));
        results.add(Result.delete(99));

        Assert.assertEquals(3, results.size());
    }

    @Test
    public void testHashCode_bullhornId() {
        Set<Result> results = Sets.newHashSet();
        results.add(Result.insert(99));
        results.add(Result.insert(99));
        results.add(Result.insert(100));

        Assert.assertEquals(2, results.size());
    }

    @Test
    public void testHashCode_failureMessage() {
        Set<Result> results = Sets.newHashSet();
        results.add(Result.failure(new Exception("Message 1")));
        results.add(Result.failure(new Exception("Message 1")));
        results.add(Result.failure(new Exception("Message 2")));

        Assert.assertEquals(2, results.size());
    }
}
