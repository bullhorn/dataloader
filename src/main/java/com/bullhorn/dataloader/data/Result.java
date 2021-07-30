package com.bullhorn.dataloader.data;

import com.bullhorn.dataloader.enums.ErrorInfo;

/**
 * Represents the result of processing a record using the REST API.
 *
 * This class is a data type, not an instance type. Two different results can be considered identical if they contain
 * the same data values. They have no identity in and of themselves.
 */
public class Result {

    private Status status;
    private Action action;
    private Integer bullhornId = -1;
    private Integer bullhornParentId = -1;
    private ErrorInfo errorInfo = ErrorInfo.GENERIC_ERROR;
    private String errorDetails = "";

    /**
     * Successful state constructor arguments
     */
    public Result(Status status, Action action) {
        this.status = status;
        this.action = action;
    }

    /**
     * Successful state constructor with bullhorn internal ID
     */
    public Result(Status status, Action action, Integer bullhornId) {
        this.status = status;
        this.action = action;
        this.bullhornId = bullhornId;
    }

    /**
     * Failure state constructor arguments
     */
    public Result(Status status, Action action, ErrorInfo errorInfo, String errorDetails) {
        this.status = status;
        this.action = action;
        this.errorInfo = errorInfo;
        this.errorDetails = errorDetails;
    }

    /**
     * Failure state constructor arguments with bullhorn Internal ID
     */
    public Result(Status status, Action action, Integer bullhornId, ErrorInfo errorInfo, String errorDetails) {
        this.status = status;
        this.action = action;
        this.bullhornId = bullhornId;
        this.errorInfo = errorInfo;
        this.errorDetails = errorDetails;
    }

    /**
     * Insert convenience constructor
     *
     * @param bullhornId The bullhorn internal ID of the record
     * @return The new Result object
     */
    public static Result insert(Integer bullhornId) {
        return new Result(Status.SUCCESS, Action.INSERT, bullhornId);
    }

    /**
     * Insert convenience constructor with bullhorn internal ID of the record and the parent record
     *
     * @param bullhornId       The bullhorn internal ID of the record
     * @param bullhornParentId The bullhorn internal ID of the parent record (for example: file attachments)
     * @return The new Result object
     */
    public static Result insert(Integer bullhornId, Integer bullhornParentId) {
        Result result = new Result(Status.SUCCESS, Action.INSERT, bullhornId);
        result.bullhornParentId = bullhornParentId;
        return result;
    }

    /**
     * Update convenience constructor
     *
     * @param bullhornId The bullhorn internal ID of the record
     * @return The new Result object
     */
    public static Result update(Integer bullhornId) {
        return new Result(Status.SUCCESS, Action.UPDATE, bullhornId);
    }

    /**
     * Update convenience constructor with bullhorn internal ID of the record and the parent record
     *
     * @param bullhornId       The bullhorn internal ID of the record
     * @param bullhornParentId The bullhorn internal ID of the parent record (for example: file attachments)
     * @return The new Result object
     */
    public static Result update(Integer bullhornId, Integer bullhornParentId) {
        Result result = new Result(Status.SUCCESS, Action.UPDATE, bullhornId);
        result.bullhornParentId = bullhornParentId;
        return result;
    }

    /**
     * Delete convenience constructor
     *
     * @param bullhornId The bullhorn internal ID of the record
     * @return The new Result object
     */
    public static Result delete(Integer bullhornId) {
        return new Result(Status.SUCCESS, Action.DELETE, bullhornId);
    }

    /**
     * Convert convenience constructor
     *
     * @return The new Result object
     */
    public static Result convert() {
        return new Result(Status.SUCCESS, Action.CONVERT);
    }

    /**
     * Skip convenience constructor
     *
     * @return The new Result object
     */
    public static Result skip() {
        return new Result(Status.SUCCESS, Action.SKIP);
    }

    /**
     * Skip convenience constructor
     *
     * @param bullhornId The bullhorn internal ID of the record
     * @return The new Result object
     */
    public static Result skip(Integer bullhornId) {
        return new Result(Status.SUCCESS, Action.SKIP, bullhornId);
    }

    /**
     * Convert convenience constructor
     *
     * @return The new Result object
     */
    public static Result export(Integer bullhornId) {
        return new Result(Status.SUCCESS, Action.EXPORT, bullhornId);
    }

    /**
     * Failure convenience constructor that converts any exception to the correct error info and details
     *
     * @param exception  The exception for this failure result
     * @param bullhornId The id of the Bullhorn entity
     * @return The new Result object
     */
    public static Result failure(Exception exception, Integer bullhornId) {
        return new Result(Status.FAILURE, Action.FAILURE, bullhornId, ErrorInfo.fromException(exception), exception.getMessage());
    }

    /**
     * Failure convenience constructor
     *
     * @param exception The exception for this failure result
     * @return The new Result object
     */
    public static Result failure(Exception exception) {
        return failure(exception, -1);
    }

    /**
     * Will be set to uninitialized if not set.
     *
     * @return NOT_SET if the value is invalid or not present
     */
    public Status getStatus() {
        return status != null ? status : Status.NOT_SET;
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
     *
     * @return NOT_SET if the value is invalid or not present
     */
    public Action getAction() {
        return action != null ? action : Action.NOT_SET;
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
        return bullhornId != null ? bullhornId : -1;
    }

    void setBullhornId(Integer bullhornId) {
        this.bullhornId = bullhornId;
    }

    /**
     * Will be set for loading attachments if the record was processed successfully. Valid values are 1 to n.
     *
     * @return -1 if the value is invalid or not present
     */
    Integer getBullhornParentId() {
        return bullhornParentId != null ? bullhornParentId : -1;
    }

    void setBullhornParentId(Integer bullhornParentId) {
        this.bullhornParentId = bullhornParentId;
    }

    /**
     * Will be set if the record had an error in processing.
     *
     * @return Generic error info if not previously set
     */
    public ErrorInfo getErrorInfo() {
        return errorInfo != null ? errorInfo : ErrorInfo.GENERIC_ERROR;
    }

    public void setErrorInfo(ErrorInfo errorInfo) {
        this.errorInfo = errorInfo;
    }

    /**
     * Will be set if the record had an error in processing.
     *
     * @return Empty string if not previously set
     */
    public String getErrorDetails() {
        return errorDetails != null ? errorDetails : "";
    }

    void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
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
            && getErrorInfo().equals(that.getErrorInfo())
            && getErrorDetails().equals(that.getErrorDetails()));
    }

    @Override
    public int hashCode() {
        int result = getStatus().hashCode();
        result = 31 * result + getAction().hashCode();
        result = 31 * result + getBullhornId().hashCode();
        result = 31 * result + getBullhornParentId().hashCode();
        result = 31 * result + getErrorInfo().hashCode();
        result = 31 * result + getErrorDetails().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Result{"
            + "status=" + getStatus()
            + ", action=" + getAction()
            + ", bullhornId=" + getBullhornId()
            + ", bullhornParentId=" + getBullhornParentId()
            + ", errorInfo='" + getErrorInfo() + "'"
            + ", errorDetails='" + getErrorDetails() + "'"
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
        EXPORT,
        FAILURE
    }
}
