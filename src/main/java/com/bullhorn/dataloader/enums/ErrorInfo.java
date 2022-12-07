package com.bullhorn.dataloader.enums;

import java.text.ParseException;

import com.bullhorn.dataloader.util.DataLoaderException;
import com.bullhornsdk.data.exception.RestApiException;

/**
 * The list of all errors that Data Loader produces, along with additional user-friendly information.
 */
public enum ErrorInfo {

    // 0-99 - Internal Errors (Unexpected errors during execution in Data Loader)
    UNKNOWN_ERROR(1, "Error", ""),
    NULL_POINTER_EXCEPTION(10, "Internal Error", "Restart and try again."),

    // 100's - Setup Errors (Errors during setup before interacting with the Bullhorn API)
    MISSING_PROPERTIES_FILE(101, "Missing Properties File", "Check that the properties file exists in the correct directory."),
    MISSING_SETTING(102, "Missing Setting", "Fill in the required setting value in the properties file."),
    INVALID_SETTING(103, "Invalid Setting", "Adjust the setting value."),
    MISSING_CSV_FILE(110, "Cannot Read CSV File", "Check that the CSV file exists in the correct directory."),
    CANNOT_PROCESS_DIRECTORY(111, "Cannot Process Directory", "Provide a CSV file instead of a directory."),
    INVALID_FILE_EXTENSION(112, "Invalid File Extension", "Save the spreadsheet to load as a CSV file first."),
    INVALID_CSV_FILE(113, "Invalid CSV file", "Save CSV file as either: UTF-8 (recommended multi-byte format) or "
        + "ISO-8859-1 (legacy single-byte support)."),
    DUPLICATE_COLUMNS_PROVIDED(114, "Duplicate Columns Provided", "Remove duplicate columns from CSV file."),
    INVALID_NUMBER_OF_COLUMNS(115, "Invalid Number of Columns", "Ensure all rows have the same number of columns."),
    MISSING_REQUIRED_COLUMN(116, "Missing Required Column", "Add the required column to the CSV file and try again."),
    MISSING_ATTACHMENT_FILE(120, "Cannot Read Attachment File", "Check that the file exists in the correct directory relative to the CSV file."),
    CANNOT_PERFORM_DELETE(130, "Cannot Perform Delete", "This entity is not deletable in Bullhorn."),

    // 200's - Connection Errors (Errors connecting to the Bullhorn API)
    LOGIN_FAILED(201, "Login Failed", "Check that your credentials are valid and your internet connection is good."),
    CONNECTION_TIMEOUT(202, "Internet Connectivity Issues", "Check your internet connection or try again later."),

    // 300's - Lookup Errors (Errors finding existing data in Bullhorn)
    MISSING_RECORD(301, "Record Not Found", "Update duplicate check settings or remove row from file."),
    MISSING_OR_DELETED_RECORD(302, "Record Not Found", "Check that data exists in Bullhorn or remove row."),
    MISSING_TO_ONE_ASSOCIATION(303, "Record Not Found", "Check that data exists in Bullhorn or remove association."),
    MISSING_TO_MANY_ASSOCIATION(304, "Record Not Found", "Check that data exists in Bullhorn or remove association."),
    MISSING_PARENT_ENTITY_FOR_ATTACHMENT(305, "Record Not Found", "Update duplicate check settings or remove row from file."),
    MISSING_PARENT_ENTITY_FOR_CUSTOM_OBJECT(306, "Record Not Found",
        "Ensure that the parent entity exists and is found by the duplicate check settings."),
    DUPLICATE_RECORDS(310, "Duplicate Records Found", "Remove duplicates in Bullhorn or change duplicate check settings."),
    DUPLICATE_TO_ONE_ASSOCIATIONS(311, "Duplicate Records Found", "Remove duplicates in Bullhorn or change association field."),
    DUPLICATE_TO_MANY_ASSOCIATIONS(312, "Duplicate Records Found", "Remove duplicates in Bullhorn or change association field."),

    // 400's - Bad Data Provided (Bullhorn responded that there is missing or invalid data provided)
    BAD_REQUEST(400, "Bad Request", "Correct the issue and try again."),
    INCORRECT_COLUMN_NAME(401, "Incorrect Column Name", "Check csv column names."),
    DUPLICATE_EFFECTIVE_DATE(410, "Duplicate Effective Date", "Use a different effective date or update the current one."),
    INVALID_DUPLICATE_SEARCH(420, "Invalid Duplicate Check", "Update duplicate check to use different fields."),
    INVALID_DUPLICATE_QUERY(421, "Invalid Duplicate Check", "Update duplicate check to use different fields."),
    INVALID_DATE_FORMAT(430, "Invalid Date Format", "Adjust the Date Format in settings to match the dates provided in the file. "
        + "The most common formats are: \n\t1. US Short Date with Time:      MM/dd/yy HH:mm\n"
        + "2. UK Short Date with Time:      dd/MM/yy HH:mm\n"
        + "3. US Short Date:                         MM/dd/yyyy\n"
        + "4. UK Short Date:                         dd/MM/yyyy\n"
        + "For more options, see: https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html"),

    // 500's - Server Errors (Bullhorn responded that there was an internal error)
    INTERNAL_SERVER_ERROR(500, "Internal Server Error",
        "Oops, something went wrong after the data was uploaded to Bullhorn that prevented the data from saving. Please try again."),
    ;

    /**
     * Given an exception, determines the best error code to assign to that exception.
     *
     * @param exception any exception thrown from within Data Loader or its dependencies
     * @return the closest matching error info
     */
    public static ErrorInfo fromException(Exception exception) {
        if (exception != null) {
            // Check for information from most specific to most generic errors
            if (exception instanceof DataLoaderException) {
                return ((DataLoaderException) exception).getErrorInfo();
            } else if (exception instanceof RestApiException) {
                // Parse out meaning behind SDK-REST errors and assign specific error info
                if (exception.getMessage().startsWith("Error getting")
                    || exception.getMessage().startsWith("Error posting")) {
                    return ErrorInfo.CONNECTION_TIMEOUT;
                }
                return ErrorInfo.INTERNAL_SERVER_ERROR;
            } else if (exception instanceof IllegalArgumentException) {
                return ErrorInfo.INVALID_SETTING;
            } else if (exception instanceof NullPointerException) {
                return ErrorInfo.NULL_POINTER_EXCEPTION;
            } else if (exception instanceof ParseException) {
                return ErrorInfo.INVALID_DUPLICATE_QUERY;
            }
        }

        return ErrorInfo.UNKNOWN_ERROR;
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
