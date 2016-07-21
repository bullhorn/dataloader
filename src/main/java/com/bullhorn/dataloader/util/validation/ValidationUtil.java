package com.bullhorn.dataloader.util.validation;

import java.io.File;

import com.bullhorn.dataloader.util.PrintUtil;
import org.apache.commons.io.FilenameUtils;

/**
 * Validation methods for validating user input on the command line.  Prints results and returns errors, does
 * not log errors because this should happen before the log file is created.
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
     * @return true if there are the correct number of parameters returned
     */
    public boolean isValidParameters(String[] args) {
        if (args.length < 2) {
            System.out.println("ERROR: Not enough arguments provided.");
            printUtil.printUsage();
            return false;
        } else if (args.length > 3) {
            System.out.println("ERROR: Too many arguments provided.");
            printUtil.printUsage();
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
            System.out.println("ERROR: Cannot access: " + filePath);
            System.out.println("       Ensure path is correct.");
            printUtil.printUsage();
            return false;
        } else if (file.isDirectory()) {
            System.out.println("ERROR: Expected a file, but a directory was provided.");
            printUtil.printUsage();
            return false;
        } else if (!FilenameUtils.getExtension(filePath).equalsIgnoreCase("csv")) {
            System.out.println("ERROR: Expected a '*.csv' file, but was provided: " + filePath);
            System.out.println("       Provide a csv file to load/update");
            printUtil.printUsage();
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