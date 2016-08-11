package com.bullhorn.dataloader.util.validation;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import com.bullhorn.dataloader.util.PrintUtil;

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
     * @param args The user's command line parameters
     * @param expectedNumArgs The expected number of arguments
     * @return true if there are the correct number of parameters returned
     */
    public boolean isNumParametersValid(String[] args, Integer expectedNumArgs, Boolean shouldPrint) {
        if (args.length < expectedNumArgs) {
            if (shouldPrint) {
                printUtil.printAndLog("ERROR: Not enough arguments provided.");
            }
            return false;
        } else if (args.length > expectedNumArgs) {
            if (shouldPrint) {
                printUtil.printAndLog("ERROR: Too many arguments provided.");
            }
            return false;
        }
        return true;
    }

    public boolean isNumParametersValid(String[] args, Integer expectedNumArgs) {
        return isNumParametersValid(args, expectedNumArgs, true);
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

    /**
     * Validates that an entity can be loaded, based on the entityName.
     */
    public boolean isLoadableEntity(String entityName, Boolean shouldPrint) {
        if (EntityValidation.isLoadable(entityName)) {
            return true;
        } else if (EntityValidation.isReadOnly(entityName)) {
            if (shouldPrint) {
                printUtil.printEntityError(entityName, "read only");
            }
            return false;
        }

        if (shouldPrint) {
            printUtil.printUnknownEntityError(entityName);
        }

        return false;
    }

    public boolean isLoadableEntity(String entityName) {
        return isLoadableEntity(entityName, true);
    }

    /**
     * Validates that an entity can be deleted, based on the entityName.
     */
    public boolean isDeletableEntity(String entityName, Boolean shouldPrint) {
        if (EntityValidation.isDeletable(entityName)) {
            return true;
        } else if (EntityValidation.isNotDeletable(entityName) || EntityValidation.isReadOnly(entityName)) {
            if (shouldPrint) {
                printUtil.printEntityError(entityName, "not deletable");
            }
            return false;
        }

        if (shouldPrint) {
            printUtil.printUnknownEntityError(entityName);
        }

        return false;
    }

    public boolean isDeletableEntity(String entityName) {
        return isDeletableEntity(entityName, true);
    }
}