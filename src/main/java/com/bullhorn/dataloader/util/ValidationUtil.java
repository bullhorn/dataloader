package com.bullhorn.dataloader.util;

import java.io.File;
import java.io.FilenameFilter;

import com.bullhorn.dataloader.service.ParseResumeService;
import org.apache.commons.io.FilenameUtils;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.enums.ErrorInfo;

/**
 * Low level methods for validating the user's input, returning true/false,
 * and also printing/logging any error messages.
 */
public class ValidationUtil {

    /**
     * Validates the number of command line parameters
     *
     * @param args            The user's command line parameters
     * @param expectedNumArgs The expected number of arguments
     * @return true if there are the correct number of parameters returned
     */
    public static boolean validateNumArgs(String[] args, Integer expectedNumArgs, PrintUtil printUtil) {
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
    public static boolean validateCsvFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new DataLoaderException(ErrorInfo.MISSING_CSV_FILE, "Cannot read the CSV file: " + filePath);
        } else if (file.isDirectory()) {
            throw new DataLoaderException(ErrorInfo.CANNOT_PROCESS_DIRECTORY, "Expected a file, but the directory: " + filePath + " was provided.");
        } else if (!FilenameUtils.getExtension(filePath).equalsIgnoreCase(StringConsts.CSV)) {
            throw new DataLoaderException(ErrorInfo.INVALID_FILE_EXTENSION, "Expected a '*.csv' file, but was provided: " + filePath);
        }
        return true;
    }

    /**
     * Validates whether the given filePath is a valid resume file for processing
     */
    public static boolean validateResumeFolder(String filePath) {
        File fileDirectory = new File(filePath);
        if (fileDirectory.isDirectory()) {
            File[] fileList = fileDirectory.listFiles();
            if (fileList != null){
                return fileList.length >= 1;
            }
        }
        return false;
    }

    /**
     * Returns true/false, and also provides the printout for validation.
     */
    public static boolean validateEntityFromFileNameOrProperty(String filePath, PropertyFileUtil propertyFileUtil, PrintUtil printUtil) {
        EntityInfo entityInfo = FileUtil.extractEntityFromFileNameOrProperty(filePath, propertyFileUtil);
        if (entityInfo == null) {
            printUtil.printAndLog("Could not determine entity from file name: " + filePath);
        }
        return entityInfo != null;
    }

    /**
     * Returns true/false, and also provides the printout for validation.
     */
    public static boolean validateEntityName(String entityName, PrintUtil printUtil) {
        EntityInfo entityInfo = FileUtil.extractEntityFromFileName(entityName);
        if (entityInfo == null) {
            printUtil.printAndLog("ERROR: Could not determine entity from the name: \"" + entityName + "\"");
        }
        return entityInfo != null;
    }

    /**
     * Returns true/false, and also provides the printout for validation.
     */
    public static boolean validateLoadableEntity(EntityInfo entityInfo, PrintUtil printUtil) {
        if (!entityInfo.isLoadable()) {
            printUtil.printAndLog("ERROR: " + entityInfo.getEntityName() + " entity is read only.");
        }
        return entityInfo.isLoadable();
    }

    /**
     * Returns true/false, and also provides the printout for validation.
     */
    public static boolean validateDeletableEntity(EntityInfo entityInfo, PrintUtil printUtil) {
        if (!entityInfo.isDeletable()) {
            printUtil.printAndLog("ERROR: " + entityInfo.getEntityName() + " entity is not deletable.");
        }
        return entityInfo.isDeletable();
    }

    /**
     * Returns true/false, and also provides the printout for validation.
     */
    public static boolean validateAttachmentEntity(EntityInfo entityInfo, PrintUtil printUtil) {
        if (!entityInfo.isAttachmentEntity()) {
            printUtil.printAndLog("ERROR: " + entityInfo.getEntityName() + " entity does not support attachments.");
        }
        return entityInfo.isAttachmentEntity();
    }

    static class FilterFiles implements FilenameFilter {

        private final String[] extensions;

        public FilterFiles(String... extensions) {
            this.extensions = extensions;
        }

        @Override
        public boolean accept(File dir, String name) {
            for (String ext: extensions) {
                if (name.endsWith(ext)) {
                    return true;
                }
            }
            return false;
        }
    }
}
