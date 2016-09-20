package com.bullhorn.dataloader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

/**
 * Utilities used in tests
 */
public class TestUtils {
    /**
     * Returns the value of the environment variable with the given name. Throws an error if it does not exist.
     * @param name The name of the environment variable
     * @return The value of the environment variable
     */
    public static String getEnvironmentVariable(String name) {
        String envVar = System.getenv(name);
        if (envVar == null) {
            throw new IllegalArgumentException("Test Setup Error: Missing Environment Variable: '" + name + "'");
        }
        return envVar;
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
     * @param fileExtension Only files that match this extension will be modified
     * @param replaceText The text to find
     * @param findText The text to replace the foundText with
     * @throws IOException In case the directory is null
     */
    public static void ReplaceTextInFiles(File directory, String fileExtension, String replaceText, String findText) throws IOException {
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                String extension = FilenameUtils.getExtension(file.getPath());
                if (extension.equalsIgnoreCase(fileExtension)) {
                    String content = FileUtils.readFileToString(file, "UTF-8");
                    content = content.replaceAll(findText, replaceText);
                    FileUtils.writeStringToFile(file, content, "UTF-8");
                }
            }
        } else {
            throw new IllegalArgumentException("Integration Test Failure: Cannot clone the directory: '" + directory + "' for testing.");
        }
    }
}
