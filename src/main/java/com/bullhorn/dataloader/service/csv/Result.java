package com.bullhorn.dataloader.service.csv;

/**
 * Represents the result of processing a record using the REST API.
 */
public class Result {

    public enum Status {
        NOT_SET,
        SUCCESS,
        FAILURE
    }

    public enum Action {
        NOT_SET,
        INSERT,
        UPDATE
    }

    /**
     * Insert convenience constructor
     *
     * @param bullhornId The bullhorn internal ID of the record
     * @return The new Result object
     */
    public static Result Insert(Integer bullhornId) {
        return new Result(Status.SUCCESS, Action.INSERT, bullhornId, "");
    }

    /**
     * Update convenience constructor
     *
     * @param bullhornId The bullhorn internal ID of the record
     * @return The new Result object
     */
    public static Result Update(Integer bullhornId) {
        return new Result(Status.SUCCESS, Action.UPDATE, bullhornId, "");
    }

    /**
     * Failure convenience constructor
     *
     * @param failureText The error text for this failure result
     * @return The new Result object
     */
    public static Result Failure(String failureText) {
        return new Result(Status.FAILURE, Action.NOT_SET, -1, failureText);
    }

    private Status status = Status.NOT_SET;
    private Action action = Action.NOT_SET;
    private Integer bullhornId = -1;
    private String failureText = "";

    public Result (Status status, Action action, Integer bullhornId, String failureText) {
        this.status = status;
        this.action = action;
        this.bullhornId = bullhornId;
        this.failureText = failureText;
    }

    /**
     * Will be set to uninitialized if not set.
     */
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status success) {
        status = success;
    }

    /**
     * Convenience method for determining if the result was successful.
     */
    public Boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    /**
     * Will be set to uninitialized if not set to either INSERT or UPDATE.
     */
    public Action getAction() {
        return action;
    }

    public void setAction(Action success) {
        action = success;
    }

    /**
     * Will be set if the record was processed successfully. Valid values are 1 to n.
     */
    public Integer getBullhornId() {
        return bullhornId;
    }

    public void setBullhornId(Integer bullhornId) {
        this.bullhornId = bullhornId;
    }

    /**
     * Will be set if the record had an error in processing.
     */
    public String getFailureText() {
        return failureText;
    }

    public void setFailureText(String failureText) {
        this.failureText = failureText;
    }
}
