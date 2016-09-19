package com.bullhorn.dataloader.integration;

import com.bullhorn.dataloader.Main;
import com.bullhorn.dataloader.TestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class IntegrationTest {

    private ConsoleOutputCapturer consoleOutputCapturer;

    @Before
    public void setup() throws IOException {
        // Use the properties file from the test/resources directory
        System.setProperty("propertyfile", TestUtils.getFilePath("integrationTest.properties"));

        // Use environment variables to drive the login DArgs from TravisCI
        System.setProperty("username", TestUtils.getEnvironmentVariable("USERNAME"));
        System.setProperty("password", TestUtils.getEnvironmentVariable("PASSWORD"));
        System.setProperty("clientId", TestUtils.getEnvironmentVariable("CLIENT_ID"));
        System.setProperty("clientSecret", TestUtils.getEnvironmentVariable("CLIENT_SECRET"));

        // Capture command line output as a string without stopping the real-time printout
        consoleOutputCapturer = new ConsoleOutputCapturer();
        consoleOutputCapturer.start();

        // Put a "yes" response into the System.in for accepting the load/delete from directory
        InputStream inputStream = IOUtils.toInputStream("yes", "UTF-8");
        System.setIn(inputStream);
    }

    // TODO: 1. Allow for session creation from here, then TravisCI
    // TODO: 2. Allow for changing of `-ext-1` to something unique
    // TODO: 3. Enable upload of entire examples/load/ folder
    // TODO: 4. Enable delete from entire results/ folder
    // TODO: 5. Allow for success/failure assertions

    @Test
    public void loadFromFolder() throws IOException {
        long secondsSinceEpoch = System.currentTimeMillis() / 1000;
        String resourceDirPath = TestUtils.getFilePath("");
        String examplesDirPath = resourceDirPath + "/integrationTestExamples_" + secondsSinceEpoch;
        String newExternalIdEnding = "-" + secondsSinceEpoch;
        File examplesDirectory = createExampleDirectory("examples/load", examplesDirPath, "-ext-1", newExternalIdEnding);
        String[] args = {"load", examplesDirPath};

        Main.main(args);

        String cmdLineOutput = consoleOutputCapturer.stop();
        Assert.assertFalse(cmdLineOutput.contains("ERROR"));
        FileUtils.deleteDirectory(examplesDirectory);
    }

    /**
     * Given a directory with CSV files in it, this method will move all of those files to a new directory
     * and will string replace all instances in all files of the given text.
     *
     * @param originalDirPath The source directory path
     * @param newDirPath The path to the new directory to create
     * @param findText The text to find in the files
     * @param replaceText The text to replace all instances of findText with
     * @return The new directory that was created
     */
    private File createExampleDirectory(String originalDirPath, String newDirPath, String findText, String replaceText) throws IOException {
        File originalDirectory = new File(originalDirPath);
        File newDirectory = new File(newDirPath);
        FileUtils.copyDirectory(originalDirectory, newDirectory);

        File[] directoryListing = newDirectory.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                String fileExtension = FilenameUtils.getExtension(file.getPath());
                if (fileExtension.equalsIgnoreCase("csv")) {
                    String content = FileUtils.readFileToString(file, "UTF-8");
                    content = content.replaceAll(findText, replaceText);
                    FileUtils.writeStringToFile(file, content, "UTF-8");
                }
            }
        } else {
            throw new IllegalArgumentException("Integration Test Failure: Cannot clone the directory: '" + originalDirPath + "' for testing.");
        }

        return newDirectory;
    }
}
