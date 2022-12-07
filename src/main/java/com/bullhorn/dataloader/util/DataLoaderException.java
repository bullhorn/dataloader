package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.enums.ErrorInfo;

/**
 * Extends the basic exception to include a Data Loader specific error code that can be used to surface
 * human readable titles and tips to resolve.
 */
public class DataLoaderException extends RuntimeException {
    private final ErrorInfo errorInfo;

    public DataLoaderException(ErrorInfo errorInfo, String message) {
        super(message);
        this.errorInfo = errorInfo;
    }

    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }
}
