package com.bullhorn.dataloader.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Additional user friendly data for each error code that Data Loader produces.
 */
public class ErrorConsts {

    // (Error Code, Title, Tips to Resolve)
    public static final List<ErrorDetails> errorDetailsList = Arrays.asList(
        new ErrorDetails(101, "Failure to login",
            "Check that your credentials are valid"),
        new ErrorDetails(110, "Invalid CSV file",
            "Verify that the CSV file has the correct number of columns and is saved in one of the supported formats: "
                + "UTF-8 (recommended multi-byte format) or ISO-8859-1 (legacy single-byte support)."),
        new ErrorDetails(120, "Missing Configuration",
            "Check your Data Loader settings and try again"),
        new ErrorDetails(201, "Cannot locate entity for updating",
            "Check that this row's value for the duplicate check is valid, and the record exists in Bullhorn."),
        new ErrorDetails(202, "Too may matching records found for this row",
            "The duplicate check found more than one existing record to update. Each row in the CSV file should correspond "
                + "to a single record in Bullhorn. Narrow the search to only the single record that should be updated for the given row."),
        new ErrorDetails(203, "Cannot find associated entity",
            "TODO"),
        new ErrorDetails(301, "Internet connectivity issues",
            "TODO"),
        new ErrorDetails(400, "Bad Request",
            "The Bullhorn Rest API has responded with an error that indicates that the Data Loader made an invalid request."),
        new ErrorDetails(401, "Missing Required Property",
            "The Bullhorn Rest API has responded that there are required properties that have not been provided in the CSV file."
                + " Please add these column(s) and try again."),
        new ErrorDetails(410, "Duplicate Effective Date",
            "When creating a new entity with an effective date, it must be unique - there cannot be more than one version on any given day."),
        new ErrorDetails(500, "Generic Server Error",
            "The Bullhorn Rest API has responded that an internal error has occurred."
                + " Please try again, as this may resolve itself the next time around.")
    );

    /**
     * Given an error code, returns the user friendly title for that error
     *
     * @return null if not found
     */
    public static String getTitle(Integer code) {
        if (codeToTitleMap.isEmpty()) {
            for (ErrorDetails errorDetails : errorDetailsList) {
                codeToTitleMap.put(errorDetails.code, errorDetails.title);
            }
        }
        return codeToTitleMap.get(code);
    }

    /**
     * Given an error code, returns the user friendly tips to resolve the error
     *
     * @return null if not found
     */
    public static String getTipsToResolve(Integer code) {
        if (codeToTipsToResolveMap.isEmpty()) {
            for (ErrorDetails errorDetails : errorDetailsList) {
                codeToTipsToResolveMap.put(errorDetails.code, errorDetails.tipsToResolve);
            }
        }
        return codeToTipsToResolveMap.get(code);
    }

    // Lazy loaded lookups by error code
    private static final Map<Integer, String> codeToTitleMap = Maps.newHashMap();
    private static final Map<Integer, String> codeToTipsToResolveMap = Maps.newHashMap();

    // Inner private class for storing details about each error
    private static class ErrorDetails {
        public Integer code;
        public String title;
        public String tipsToResolve;

        public ErrorDetails(Integer code, String title, String tipsToResolve) {
            this.code = code;
            this.title = title;
            this.tipsToResolve = tipsToResolve;
        }
    }
}
