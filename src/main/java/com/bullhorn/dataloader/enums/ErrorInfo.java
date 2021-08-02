package com.bullhorn.dataloader.enums;

import java.text.ParseException;

import com.bullhorn.dataloader.util.DataLoaderException;
import com.bullhornsdk.data.exception.RestApiException;

/**
 * The list of all errors that Data Loader produces, along with additional user-friendly information.
 */
public enum ErrorInfo {

    GENERIC_ERROR(1, "Error", ""),

    // 100's - Setup errors (occurs during setup before interacting with the Bullhorn)
    GENERIC_SETUP(100, "Failure on Setup", "an error occurred in Data Loader before interacting with the Rest API"),
    PROPERTIES_FILE(101, "Cannot read properties file", "Check that the properties file exists in the correct directory."),
    CSV_FILE_NOT_FOUND(102, "Cannot read CSV File", "Verify that the CSV file exists on disk in the correct directory "
        + "and can be read by Data Loader."),
    INVALID_CSV_FILE(110, "Invalid CSV file", "Verify that the CSV file has the correct number of columns "
        + "and is saved in one of the supported formats: UTF-8 (recommended multi-byte format) or ISO-8859-1 (legacy single-byte support)."),
    DUPLICATE_COLUMNS(111, "Invalid CSV file", "Verify that the CSV file has the correct number of columns "
        + "and is saved in one of the supported formats: UTF-8 (recommended multi-byte format) or ISO-8859-1 (legacy single-byte support)."),
    INVALID_NUMBER_OF_COLUMNS(112, "Invalid CSV file", "Verify that the CSV file has the correct number of columns "
        + "and is saved in one of the supported formats: UTF-8 (recommended multi-byte format) or ISO-8859-1 (legacy single-byte support)."),
    INVALID_DATE_FORMAT(120, "Invalid Date Format", ""),
    MISSING_SETTING(120, "Setting is missing", "Check your Data Loader settings and try again"),
    INVALID_SETTING(121, "Setting is invalid", "Check your Data Loader settings and try again"),

    // 200's - Connection errors (occurs during a run of data loader due to internet connectivity issues)
    LOGIN_FAILED(201, "Login Failed", "Check that your credentials are valid and your internet connection is good."),
    CONNECTION_FAILED(202, "Internet connectivity issues",
        "Check your internet connection or try again later."),
    CONNECTION_TIMEOUT(203, "Internet connectivity issues",
        "Check your internet connection or try again later."),
    RUNTIME_FAILURE(210, "Internal Failure",
        "Data Loader encountered an issue internally that caused a failure. Please try again. If issues still occurs, "
            + "report log file as a data loader issue."),

    // 300's - Data lookup errors (occurs during lookup and before data is modified in Bullhorn)
    MISSING_RECORD(301, "Cannot locate entity for updating",
        "Check that this row's value for the duplicate check is valid, and the record exists in Bullhorn."),
    MISSING_TO_ONE_ASSOCIATION(303, "Cannot find associated entity",
        "Invalid or missing associated entity. Check CSV file for invalid or missing associated entity."),
    MISSING_TO_MANY_ASSOCIATION(304, "Cannot find associated entity",
        "Invalid or missing associated entity. Check CSV file for invalid or missing associated entity."),
    TOO_MANY_RECORDS(310, "Too may matching records found for this row",
        "The duplicate check found more than one existing record to update. Each row in the CSV file should correspond "
            + "to a single record in Bullhorn. Narrow the search to only the single record that should be updated for the given row."),
    TOO_MANY_TO_ONE_ASSOCIATIONS(311, "Too may matching records found for this row",
        "The duplicate check found more than one existing record to update. Each row in the CSV file should correspond "
            + "to a single record in Bullhorn. Narrow the search to only the single record that should be updated for the given row."),
    TOO_MANY_TO_MANY_ASSOCIATIONS(312, "Too may matching records found for this row",
        "The duplicate check found more than one existing record to update. Each row in the CSV file should correspond "
            + "to a single record in Bullhorn. Narrow the search to only the single record that should be updated for the given row."),
    INVALID_DUPLICATE_CHECK(320, "Invalid Duplicate Check", ""),

    // 400's - Bad data supplied (data cannot be loaded into Bullhorn because the supplied CSV file has invalid or missing data)
    BAD_REQUEST(400, "Bad Request",
        "The Bullhorn Rest API has responded with an error that indicates that the Data Loader made an invalid request."),
    MISSING_REQUIRED_PROPERTY(401, "Missing Required Property",
        "The Bullhorn Rest API has responded that there are required properties that have not been provided in the CSV file."
            + " Please add these column(s) and try again."),
    DUPLICATE_EFFECTIVE_DATE(410, "Duplicate Effective Date",
        "When creating a new entity with an effective date, it must be unique - there cannot be more than one version on any given day."),

    // 500's - Server issues (Bullhorn has responded with an error indicating that it was unable to process the request internally)
    GENERIC_SERVER_ERROR(500, "Generic Server Error",
        "The Bullhorn Rest API has responded that an internal error has occurred."
            + " Please try again, as this may resolve itself the next time around."),
    ;

    /**
     * Given an exception, determines the best error code to assign to that exception.
     *
     * @param exception any exception thrown from within Data Loader or its dependencies
     * @return the closest matching error info
     */
    public static ErrorInfo fromException(Exception exception) {
        // Check for information from most specific to most generic errors
        if (exception instanceof DataLoaderException) {
            return ((DataLoaderException) exception).getErrorInfo();
        } else if (exception instanceof RestApiException) {
            // TODO: Parse out meaning behind the errors and assign specific error info
            return ErrorInfo.GENERIC_SERVER_ERROR;
        } else if (exception instanceof IllegalArgumentException) {
            return ErrorInfo.INVALID_SETTING;
        } else if (exception instanceof NullPointerException) {
            return ErrorInfo.RUNTIME_FAILURE;
        } else if (exception instanceof ParseException) {
            return ErrorInfo.INVALID_DUPLICATE_CHECK;
        } else if (exception instanceof RuntimeException) {
            return ErrorInfo.CONNECTION_FAILED;
        }

        return ErrorInfo.GENERIC_ERROR;
    }

    private final Integer code;
    private final String title;
    private final String tipsToResolve;

    /**
     * Constructor for enum containing information about each type of error
     */
    ErrorInfo(Integer code, String title, String tipsToResolve) {
        this.code = code;
        this.title = title;
        this.tipsToResolve = tipsToResolve;
    }

    public Integer getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getTipsToResolve() {
        return tipsToResolve;
    }
}
