package com.bullhorn.dataloader.csv;

/**
 * Represents the result of processing a record using the REST API.
 * <p>
 * This class is a data type, not an instance type. Two different results can be considered identical if they
 * contain the same data values. They have no identity in and of themselves.
 */
public class Result {

    private Status status = Status.NOT_SET;
    private Action action = Action.NOT_SET;
    private Integer bullhornId = -1;
    private String failureText = "";

    public Result(Status status, Action action, Integer bullhornId, String failureText) {
        this.status = status;
        this.action = action;
        this.bullhornId = bullhornId;
        this.failureText = failureText;
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
     * Delete convenience constructor
     *
     * @param bullhornId The bullhorn internal ID of the record
     * @return The new Result object
     */
    public static Result Delete(Integer bullhornId) {
        return new Result(Status.SUCCESS, Action.DELETE, bullhornId, "");
    }

    /**
     * Convert convenience constructor
     *
     * @return The new Result object
     */
    public static Result Convert() {
        return new Result(Status.SUCCESS, Action.CONVERT, -1, "");
    }

    /**
     * Convert convenience constructor
     *
     * @return The new Result object
     */
    public static Result Skip() {
        return new Result(Status.SUCCESS, Action.SKIP, -1, "");
    }

    /**
     * Failure convenience constructor
     *
     * @param exception The exception for this failure result
     * @return The new Result object
     */
    public static Result Failure(Exception exception) {
        return new Result(Status.FAILURE, Action.FAILURE, -1, exception.toString());
    }

    /**
     * Failure convenience constructor
     *
     * @param exception  The exception for this failure result
     * @param bullhornID The id of the Bullhorn entity
     * @return The new Result object
     */
    public static Result Failure(Exception exception, Integer bullhornID) {
        return new Result(Status.FAILURE, Action.FAILURE, bullhornID, exception.toString());
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
     *
     * @return -1 if the value is invalid or not present
     */
    public Integer getBullhornId() {
        if (bullhornId == null) {
            return -1;
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Result)) return false;

        Result that = (Result) o;

        return (getStatus().equals(that.getStatus()) &&
            getAction().equals(that.getAction()) &&
            getBullhornId().equals(that.getBullhornId()) &&
            getFailureText().equals(that.getFailureText()));
    }

    @Override
    public int hashCode() {
        int result = getStatus().hashCode();
        result = 31 * result + getAction().hashCode();
        result = 31 * result + getBullhornId().hashCode();
        result = 31 * result + getFailureText().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Result{" +
            "status=" + getStatus() +
            ", action=" + getAction() +
            ", bullhornId=" + getBullhornId() +
            ", failureText='" + getFailureText() + "'" +
            '}';
    }

    public enum Status {
        NOT_SET,
        SUCCESS,
        FAILURE
    }

    public enum Action {
        NOT_SET,
        INSERT,
        UPDATE,
        DELETE,
        CONVERT,
        SKIP,
        FAILURE
    }
}
