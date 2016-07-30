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
    public boolean isValidCsvFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            printUtil.printAndLog("ERROR: Cannot access: " + filePath);
            printUtil.printAndLog("       Ensure path is correct.");
            return false;
        } else if (file.isDirectory()) {
            printUtil.printAndLog("ERROR: Expected a file, but a directory was provided.");
            return false;
        } else if (!FilenameUtils.getExtension(filePath).equalsIgnoreCase("csv")) {
            printUtil.printAndLog("ERROR: Expected a '*.csv' file, but was provided: " + filePath);
            printUtil.printAndLog("       Provide a csv file to load/update");
            return false;
        }
        return true;
    }

    /**
     * Validates that an entity can be loaded, based on the entityName.
     */
    public boolean isLoadableEntity(String entityName) {
        if (EntityValidation.isLoadable(entityName)) {
            return true;
        } else if (EntityValidation.isReadOnly(entityName)) {
            printUtil.printEntityError(entityName, "read only");
            return false;
        }
        printUtil.printUnknownEntityError(entityName);
        return false;
    }

    /**
     * Validates that an entity can be deleted, based on the entityName.
     */
    public boolean isDeletableEntity(String entityName) {
        if (EntityValidation.isDeletable(entityName)) {
            return true;
        } else if (EntityValidation.isNotDeletable(entityName) || EntityValidation.isReadOnly(entityName)) {
            printUtil.printEntityError(entityName, "not deletable");
            return false;
        }
        printUtil.printUnknownEntityError(entityName);
        return false;
    }
}