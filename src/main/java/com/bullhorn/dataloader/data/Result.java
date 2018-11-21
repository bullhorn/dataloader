package com.bullhorn.dataloader.data;

/**
 * Represents the result of processing a record using the REST API.
 *
 * This class is a data type, not an instance type. Two different results can be considered identical if they contain
 * the same data values. They have no identity in and of themselves.
 */
public class Result {

    private Status status = Status.NOT_SET;
    private Action action = Action.NOT_SET;
    private Integer bullhornId = -1;
    private Integer bullhornParentId = -1;
    private String failureText = "";

    public Result(Status status, Action action, Integer bullhornId, String failureText) {
        this.status = status;
        this.action = action;
        this.bullhornId = bullhornId;
        this.failureText = failureText;
    }

    public Result(Status status, Action action, Integer bullhornId, Integer bullhornParentId, String failureText) {
        this.status = status;
        this.action = action;
        this.bullhornId = bullhornId;
        this.bullhornParentId = bullhornParentId;
        this.failureText = failureText;
    }

    /**
     * Insert convenience constructor
     *
     * @param bullhornId The bullhorn internal ID of the record
     * @return The new Result object
     */
    public static Result insert(Integer bullhornId) {
        return new Result(Status.SUCCESS, Action.INSERT, bullhornId, "");
    }

    /**
     * Insert convenience constructor with bullhorn internal ID of the record and the parent record
     *
     * @param bullhornId       The bullhorn internal ID of the record
     * @param bullhornParentId The bullhorn internal ID of the parent record (for example: file attachments)
     * @return The new Result object
     */
    public static Result insert(Integer bullhornId, Integer bullhornParentId) {
        return new Result(Status.SUCCESS, Action.INSERT, bullhornId, bullhornParentId, "");
    }

    /**
     * Update convenience constructor
     *
     * @param bullhornId The bullhorn internal ID of the record
     * @return The new Result object
     */
    public static Result update(Integer bullhornId) {
        return new Result(Status.SUCCESS, Action.UPDATE, bullhornId, "");
    }

    /**
     * Update convenience constructor with bullhorn internal ID of the record and the parent record
     *
     * @param bullhornId       The bullhorn internal ID of the record
     * @param bullhornParentId The bullhorn internal ID of the parent record (for example: file attachments)
     * @return The new Result object
     */
    public static Result update(Integer bullhornId, Integer bullhornParentId) {
        return new Result(Status.SUCCESS, Action.UPDATE, bullhornId, bullhornParentId, "");
    }

    /**
     * Delete convenience constructor
     *
     * @param bullhornId The bullhorn internal ID of the record
     * @return The new Result object
     */
    public static Result delete(Integer bullhornId) {
        return new Result(Status.SUCCESS, Action.DELETE, bullhornId, "");
    }

    /**
     * Convert convenience constructor
     *
     * @return The new Result object
     */
    public static Result convert() {
        return new Result(Status.SUCCESS, Action.CONVERT, -1, "");
    }

    /**
     * Convert convenience constructor
     *
     * @return The new Result object
     */
    public static Result skip() {
        return new Result(Status.SUCCESS, Action.SKIP, -1, "");
    }

    /**
     * Failure convenience constructor
     *
     * @param exception The exception for this failure result
     * @return The new Result object
     */
    public static Result failure(Exception exception) {
        return new Result(Status.FAILURE, Action.FAILURE, -1, exception.toString());
    }

    /**
     * Failure convenience constructor
     *
     * @param exception  The exception for this failure result
     * @param bullhornId The id of the Bullhorn entity
     * @return The new Result object
     */
    public static Result failure(Exception exception, Integer bullhornId) {
        return new Result(Status.FAILURE, Action.FAILURE, bullhornId, exception.toString());
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
     * Will be set for loading attachments if the record was processed successfully. Valid values are 1 to n.
     *
     * @return -1 if the value is invalid or not present
     */
    public Integer getBullhornParentId() {
        return bullhornParentId;
    }

    public void setBullhornParentId(Integer bullhornParentId) {
        this.bullhornParentId = bullhornParentId;
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Result)) {
            return false;
        }

        Result that = (Result) obj;

        return (getStatus().equals(that.getStatus())
            && getAction().equals(that.getAction())
            && getBullhornId().equals(that.getBullhornId())
            && getBullhornParentId().equals(that.getBullhornParentId())
            && getFailureText().equals(that.getFailureText()));
    }

    @Override
    public int hashCode() {
        int result = getStatus().hashCode();
        result = 31 * result + getAction().hashCode();
        result = 31 * result + getBullhornId().hashCode();
        result = 31 * result + getBullhornParentId().hashCode();
        result = 31 * result + getFailureText().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Result{"
            + "status=" + getStatus()
            + ", action=" + getAction()
            + ", bullhornId=" + getBullhornId()
            + ", bullhornParentId=" + getBullhornParentId()
            + ", failureText='" + getFailureText() + "'"
            + '}';
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
