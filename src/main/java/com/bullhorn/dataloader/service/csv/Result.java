package com.bullhorn.dataloader.service.csv;

/**
 * Represents the result of processing a record using the REST API.
 */
public class Result {

    /**
     * Success convenience constructor
     *
     * @param bullhornId The bullhorn internal ID of the record
     */
    public static Result Success(Integer bullhornId) {
        return new Result(true, bullhornId, "");
    }

    /**
     * Failure convenience constructor
     *
     * @param failureText The error text for this failure result
     */
    public static Result Failure(String failureText) {
        return new Result(false, -1, failureText);
    }

    private Boolean isSuccess = false;
    private Integer bullhornId = -1;
    private String failureText = "";

    public Result (Boolean isSuccess, Integer bullhornId, String failureText) {
        this.isSuccess = isSuccess;
        this.bullhornId = bullhornId;
        this.failureText = failureText;
    }

    public Boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(Boolean success) {
        isSuccess = success;
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
