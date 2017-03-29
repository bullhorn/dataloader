package com.bullhorn.dataloader.util.validation;

import com.bullhorn.dataloader.util.PrintUtil;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

/**
 * Validation methods for validating user input on the command line.
 */
public class ValidationUtil {

    final private PrintUtil printUtil;

    public ValidationUtil(PrintUtil printUtil) {
        this.printUtil = printUtil;
    }

    /**
     * Validates the number of command line parameters
     *
     * @param args            The user's command line parameters
     * @param expectedNumArgs The expected number of arguments
     * @return true if there are the correct number of parameters returned
     */
    public boolean isNumParametersValid(String[] args, Integer expectedNumArgs) {
        if (args.length < expectedNumArgs) {
            printUtil.printAndLog("ERROR: Not enough arguments provided.");
            return false;
        } else if (args.length > expectedNumArgs) {
            printUtil.printAndLog("ERROR: Too many arguments provided.");
            return false;
        }
        return true;
    }

    /**
     * Validates whether the given filePath is a valid CSV file for processing.
     */
    public boolean isValidCsvFile(String filePath, Boolean shouldPrint) {
        File file = new File(filePath);
        if (!file.exists()) {
            if (shouldPrint) {
                printUtil.printAndLog("ERROR: Cannot access: " + filePath);
                printUtil.printAndLog("       Ensure path is correct.");
            }
            return false;
        } else if (file.isDirectory()) {
            if (shouldPrint) {
                printUtil.printAndLog("ERROR: Expected a file, but a directory was provided.");
            }
            return false;
        } else if (!FilenameUtils.getExtension(filePath).equalsIgnoreCase("csv")) {
            if (shouldPrint) {
                printUtil.printAndLog("ERROR: Expected a '*.csv' file, but was provided: " + filePath);
                printUtil.printAndLog("       Provide a csv file to load/update");
            }
            return false;
        }
        return true;
    }

    public boolean isValidCsvFile(String filePath) {
        return isValidCsvFile(filePath, true);
    }
}
