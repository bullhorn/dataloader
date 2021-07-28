package com.bullhorn.dataloader.util;

/**
 * Extends the basic exception to include a Data Loader specific error code that can be used to surface
 * human readable titles and tips to resolve.
 */
public class DataLoaderException extends RuntimeException {
    private final Integer errorCode;

    public DataLoaderException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public DataLoaderException(Integer errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
