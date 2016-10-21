package com.bullhorn.dataloader;

import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;

/**
 * Utilities used in tests
 */
public class TestUtils {
    static final String CSV = "csv";

    /**
     * Sets a system property to the value of the environment variable, and throws an error if it does not exist
     * @param propertyName the system property to set
     * @param envVarName the environment variable to read from
     */
    public static void setPropertyFromEnvironmentVariable(String propertyName, String envVarName) {
        String envVar = System.getenv(envVarName);
        if (envVar == null) {
            throw new IllegalArgumentException("Test Setup Error: Missing Environment Variable: '" + envVarName + "'");
        } else {
            System.setProperty(propertyName, envVar);
        }
    }

    /**
     * Sets a system property to the value of the environment variable, if it exists
     * @param propertyName the system property to set
     * @param envVarName the environment variable to read from if it exists
     */
    public static void setPropertyFromEnvironmentVariableIfExists(String propertyName, String envVarName) {
        String envVar = System.getenv(envVarName);
        if (envVar != null) {
            System.setProperty(propertyName, envVar);
        }
    }

    /**
     * Returns the full path to the resource file with the given name
     *
     * @param filename The name of the resource file to locate on disk
     * @return The absolute path to the resource file
     * @throws NullPointerException If the file does not exist
     */
    public static String getResourceFilePath(String filename) throws NullPointerException {
        final ClassLoader classLoader = TestUtils.class.getClassLoader();
        return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
    }

    /**
     * Given a directory, this will replace all instances of the given text string with another text string for files with the given extension.
     *
     * @param directory The directory to recurse through
     * @param replaceText The text to find
     * @param findText The text to replace the foundText with
     * @throws IOException In case the directory is null
     */
    public static void replaceTextInFiles(File directory, String replaceText, String findText) throws IOException {
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                String extension = FilenameUtils.getExtension(file.getPath());
                if (extension.equalsIgnoreCase(CSV)) {
                    String content = FileUtils.readFileToString(file, "UTF-8");
                    content = content.replaceAll(findText, replaceText);
                    FileUtils.writeStringToFile(file, content, "UTF-8");
                }
            }
        } else {
            throw new IllegalArgumentException("Integration Test Failure: Cannot access the directory: '" + directory + "' for testing.");
        }
    }

    /**
     * Given a directory, this will check that all of the files in the directory have been successfully processed by
     * checking that success files exist in the results directory and failure files do not.
     *
     * @param directory The directory to recurse through
     * @param command The command used when running DataLoader
     * @throws IOException In case the directory is null
     */
    public static void checkResultsFiles(File directory, Command command) throws IOException {
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                String extension = FilenameUtils.getExtension(file.getPath());
                if (extension.equalsIgnoreCase(CSV)) {
                    checkResultsFile(file, command);
                }
            }
        } else {
            throw new IllegalArgumentException("Integration Test Failure: Cannot access the directory: '" + directory + "' for testing.");
        }
    }

    /**
     * Given a file, this will check that a success and not a failure results file have been created.
     *
     * @param file The file to check
     * @param command The command used when running DataLoader
     * @throws IOException In case the file is null
     */
    public static void checkResultsFile(File file, Command command) throws IOException {
        String successFilePath = CsvFileWriter.getResultsFilePath(file.getPath(), command, Result.Status.SUCCESS);
        String failureFilePath = CsvFileWriter.getResultsFilePath(file.getPath(), command, Result.Status.FAILURE);

        File successFile = new File(successFilePath);
        File failureFile = new File(failureFilePath);

        Assert.assertTrue("Verify " + successFilePath + " Exists Failed!", successFile.exists());
        Assert.assertFalse("Verify " + failureFilePath + " Does not Exist Failed!", failureFile.exists());
    }
}
