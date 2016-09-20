package com.bullhorn.dataloader.integration;

import com.bullhorn.dataloader.Main;
import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * The purpose of this integration test is to:
 *
 *   1. Allow for TravisCI to run as part of every build check, using `maven verify`, which goes beyond
 *      `maven test` to also run the integration test.  Uses a test corp on SL9 (BhNext) with hidden
 *      credentials in TravisCI Environment Variables.
 *
 *   2. Tests the entire Examples directory, which contains all possible values for all loadable entities and
 *      their attachments.  The unique IDs of all of the entities are changed from `-ext-1` to something unique,
 *      after the examples have been cloned to a test folder.
 *
 *   3. INSERT the entire examples/load/ folder by performing the load command the first time.
 *
 *   4. UPDATE the entire examples/load/ folder by performing the load command a second time, with all exist
 *      fields properly set in the integrationTest.properties file.
 *
 *   5. DELETE all entered records by targeting the entire results directory.
 *
 *   6. Test assertions of both command line output and results files created. We are not making
 *      calls against the CRM itself to verify the presence or absence of records, since these steps will
 *      cover the presence of records in the index and database.
 */
public class IntegrationTest {

    private ConsoleOutputCapturer consoleOutputCapturer;

    @Before
    public void setup() throws IOException {
        // Use the properties file from the test/resources directory
        System.setProperty("propertyfile", TestUtils.getFilePath("integrationTest.properties"));

        // Use environment variables to drive system arguments from TravisCI
        System.setProperty("username", TestUtils.getEnvironmentVariable("USERNAME"));
        System.setProperty("password", TestUtils.getEnvironmentVariable("PASSWORD"));
        System.setProperty("clientId", TestUtils.getEnvironmentVariable("CLIENT_ID"));
        System.setProperty("clientSecret", TestUtils.getEnvironmentVariable("CLIENT_SECRET"));

        // Capture command line output as a string without stopping the real-time printout
        consoleOutputCapturer = new ConsoleOutputCapturer();

        // Put a "yes" response into the System.in for accepting the load/delete from directory
        InputStream inputStream = IOUtils.toInputStream("yes", "UTF-8");
        System.setIn(inputStream);
    }

    @Test
    public void testInsertUpdateDelete() throws IOException {
        long secondsSinceEpoch = System.currentTimeMillis() / 1000;
        String resourceDirPath = TestUtils.getFilePath("");
        String examplesDirPath = resourceDirPath + "/integrationTest_" + secondsSinceEpoch;
        String newExternalIdEnding = "-" + secondsSinceEpoch;
        File examplesDirectory = createExampleDirectory("examples/load", examplesDirPath, "-ext-1", newExternalIdEnding);
        FileUtils.deleteDirectory(new File(CsvFileWriter.RESULTS_DIR));

        consoleOutputCapturer.start();
        Main.main(new String[]{"load", examplesDirPath});
        String insertCommandOutput = consoleOutputCapturer.stop();

        Assert.assertFalse("Error messages output during insert step", insertCommandOutput.contains("ERROR"));
        Assert.assertFalse("Failed to process records during insert step", insertCommandOutput.contains("processed: 0"));
        Assert.assertFalse("Update performed during insert step", insertCommandOutput.contains("updated: 1"));
        Assert.assertFalse("Delete performed during insert step", insertCommandOutput.contains("deleted: 1"));
        Assert.assertFalse("Failure reported during insert step", insertCommandOutput.contains("failed: 1"));
        // TODO: Test results files

        consoleOutputCapturer.start();
        Main.main(new String[]{"load", examplesDirPath});
        String updateCommandOutput = consoleOutputCapturer.stop();

        Assert.assertFalse("Error messages output during update step", updateCommandOutput.contains("ERROR"));
        Assert.assertFalse("Failed to process records during update step", updateCommandOutput.contains("processed: 0"));
        Assert.assertFalse("Insert performed during update step", updateCommandOutput.contains("inserted: 1"));
        Assert.assertFalse("Delete performed during update step", updateCommandOutput.contains("deleted: 1"));
        Assert.assertFalse("Failure reported during update step", updateCommandOutput.contains("failed: 1"));

        consoleOutputCapturer.start();
        Main.main(new String[]{"delete", CsvFileWriter.RESULTS_DIR});
        String deleteCommandOutput = consoleOutputCapturer.stop();

        Assert.assertFalse("Error messages output during delete step", deleteCommandOutput.contains("ERROR"));
        Assert.assertFalse("Failed to process records during delete step", deleteCommandOutput.contains("processed: 0"));
        Assert.assertFalse("Insert performed during delete step", deleteCommandOutput.contains("inserted: 1"));
        Assert.assertFalse("Update performed during delete step", deleteCommandOutput.contains("updated: 1"));
        Assert.assertFalse("Failure reported during delete step", deleteCommandOutput.contains("failed: 1"));

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
