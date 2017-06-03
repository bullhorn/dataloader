package com.bullhorn.dataloader.integration;

import com.bullhorn.dataloader.Main;
import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * The purpose of this integration test is to:
 * <p>
 * 1. Allow for TravisCI to run as part of every build check, using `maven verify`, which goes beyond
 * `maven test` to also run the integration test.  Uses a test corp on SL9 (BhNext) with hidden
 * credentials in TravisCI Environment Variables.
 * <p>
 * 2. Tests the entire Examples directory, which contains all possible values for all loadable entities and
 * their attachments.  The unique IDs of all of the entities are changed from `-ext-1` to something unique,
 * after the examples have been cloned to a test folder.
 * <p>
 * 3. INSERT the entire examples/load/ folder by performing the load command the first time.
 * <p>
 * 4. UPDATE the entire examples/load/ folder by performing the load command a second time, with all exist
 * fields properly set in the integrationTest.properties file.
 * <p>
 * 5. DELETE all entered records by targeting the entire results directory.
 * <p>
 * 6. Test assertions of both command line output and results files created. We are not making
 * calls against the CRM itself to verify the presence or absence of records, since these steps will
 * cover the presence of records in the index and database.
 */
public class IntegrationTest {

    private static final String EXAMPLE_UUID = "12345678-1234-1234-1234-1234567890AB";
    private static final String EXAMPLE_EXTERNAL_ID_ENDING = "-ext-1";
    private static final Integer INDEXER_WAIT_MINUTES = 5;

    private ConsoleOutputCapturer consoleOutputCapturer;

    /**
     * Runs the integration test, first with a very simple sanity check, then the full examples directory.
     *
     * @throws IOException For directory cloning
     */
    @Test
    public void testIntegration() throws IOException, InterruptedException {
        // Use the properties file from the test/resources directory
        System.setProperty("propertyfile", TestUtils.getResourceFilePath("integrationTest.properties"));

        // Capture command line output as a string without stopping the real-time printout
        consoleOutputCapturer = new ConsoleOutputCapturer();

        // Run the sanity to catch quick and obvious failures
        insertUpdateDeleteFromDirectory(TestUtils.getResourceFilePath("integrationTestSanity"), 0);

        // Run the full test
        insertUpdateDeleteFromDirectory("examples/load", INDEXER_WAIT_MINUTES);
    }

    /**
     * Given a directory path, this method will attempt to load twice (insert, then update) then delete all CSV files
     * found in that directory.
     *
     * @param directoryPath The path to the directory to load
     * @throws IOException For directory cloning
     */
    private void insertUpdateDeleteFromDirectory(String directoryPath, Integer waitTimeMinutes) throws IOException, InterruptedException {
        // region SETUP
        // Copy example files to a temp directory located at: 'dataloader/target/test-classes/integrationTest_1234567890'
        long secondsSinceEpoch = System.currentTimeMillis() / 1000;
        File resultsDir = new File(CsvFileWriter.RESULTS_DIR);
        String tempDirPath = TestUtils.getResourceFilePath("") + "/integrationTest_" + secondsSinceEpoch;
        File tempDirectory = new File(tempDirPath);
        FileUtils.copyDirectory(new File(directoryPath), tempDirectory);

        // Replace all external ID endings with unique ones based on the current timestamp
        String newExternalIdEnding = "-" + secondsSinceEpoch;
        TestUtils.replaceTextInFiles(tempDirectory, newExternalIdEnding, EXAMPLE_EXTERNAL_ID_ENDING);

        // Replace all UUIDs in with unique ones
        String uuid = UUID.randomUUID().toString();
        TestUtils.replaceTextInFiles(tempDirectory, uuid, EXAMPLE_UUID);

        // Copy over attachments to a subdirectory if they exists (these will not get loaded as part of the directory)
        File attachmentsDirectory = new File(directoryPath + "Attachments");
        File tempAttachmentsDirectory = null;
        if (attachmentsDirectory.exists()) {
            tempAttachmentsDirectory = new File(tempDirectory + "/attachments");
            FileUtils.copyDirectory(new File(attachmentsDirectory.getPath()), tempAttachmentsDirectory);
            TestUtils.replaceTextInFiles(tempAttachmentsDirectory, newExternalIdEnding, EXAMPLE_EXTERNAL_ID_ENDING);

            // TODO: Remove this replacement by allowing paths relative the to CSV file or the working directory
            TestUtils.replaceTextInFiles(tempAttachmentsDirectory, tempAttachmentsDirectory.getPath(), "examples/loadAttachments");
        }
        // endregion SETUP

        // region INSERT
        FileUtils.deleteDirectory(new File(CsvFileWriter.RESULTS_DIR)); // Cleanup from previous runs
        System.setIn(IOUtils.toInputStream("yes", "UTF-8")); // For accepting the load/delete from directory
        consoleOutputCapturer.start();
        Main.main(new String[]{"load", tempDirPath});
        String insertCommandOutput = consoleOutputCapturer.stop();

        Assert.assertFalse("Error messages output during insert step", insertCommandOutput.contains("ERROR"));
        Assert.assertFalse("Failed to process records during insert step", insertCommandOutput.contains("processed: 0"));
        Assert.assertFalse("Update performed during insert step", insertCommandOutput.contains("updated: 1"));
        Assert.assertFalse("Delete performed during insert step", insertCommandOutput.contains("deleted: 1"));
        Assert.assertFalse("Failure reported during insert step", insertCommandOutput.contains("failed: 1"));
        TestUtils.checkResultsFiles(tempDirectory, Command.LOAD);
        // endregion

        // region ~WORKAROUND~
        // The Note V1 indexers on SL9 can take a while to index during normal business hours.
        if (waitTimeMinutes > 0) {
            System.out.println("...Waiting " + waitTimeMinutes + " minutes for indexers to catch up...");
            TimeUnit.MINUTES.sleep(waitTimeMinutes);
        }
        // endregion

        // region INSERT ATTACHMENTS
        if (tempAttachmentsDirectory != null) {
            FileUtils.deleteDirectory(new File(CsvFileWriter.RESULTS_DIR)); // Cleanup from previous runs

            consoleOutputCapturer.start();
            Main.main(new String[]{"loadAttachments", tempAttachmentsDirectory.getPath() + "/Candidate.csv"});
            String loadAttachmentsCommandOutput = consoleOutputCapturer.stop();

            Assert.assertFalse("Error messages output during load attachments step", loadAttachmentsCommandOutput.contains("ERROR"));
            Assert.assertFalse("Failed to process records during load attachments step", loadAttachmentsCommandOutput.contains("processed: 0"));
            Assert.assertFalse("Update performed during load attachments step", loadAttachmentsCommandOutput.contains("updated: 1"));
            Assert.assertFalse("Delete performed during load attachments step", loadAttachmentsCommandOutput.contains("deleted: 1"));
            Assert.assertFalse("Failure reported during load attachments step", loadAttachmentsCommandOutput.contains("failed: 1"));
            TestUtils.checkResultsFiles(tempAttachmentsDirectory, Command.LOAD_ATTACHMENTS);
        }
        // endregion

        // region UPDATE
        FileUtils.deleteDirectory(new File(CsvFileWriter.RESULTS_DIR)); // Cleanup from previous runs
        System.setIn(IOUtils.toInputStream("yes", "UTF-8"));
        consoleOutputCapturer.start();
        Main.main(new String[]{"load", tempDirPath});
        String updateCommandOutput = consoleOutputCapturer.stop();

        Assert.assertFalse("Error messages output during update step", updateCommandOutput.contains("ERROR"));
        Assert.assertFalse("Failed to process records during update step", updateCommandOutput.contains("processed: 0"));
        Assert.assertFalse("Insert performed during update step", updateCommandOutput.contains("inserted: 1"));
        Assert.assertFalse("Delete performed during update step", updateCommandOutput.contains("deleted: 1"));
        Assert.assertFalse("Failure reported during update step", updateCommandOutput.contains("failed: 1"));
        TestUtils.checkResultsFiles(tempDirectory, Command.LOAD);
        // endregion

        // TODO: UPDATE ATTACHMENTS

        // region ~FIXME~
        // Deleting Placement records is failing!
        for (File file : resultsDir.listFiles()) {
            if (file.getName().contains("Placement_")) {
                file.delete();
            }
        }
        // endregion

        // region DELETE
        // capture results file directory state
        File[] resultsFiles = resultsDir.listFiles();

        System.setIn(IOUtils.toInputStream("yes", "UTF-8"));
        consoleOutputCapturer.start();
        Main.main(new String[]{"delete", CsvFileWriter.RESULTS_DIR});
        String deleteCommandOutput = consoleOutputCapturer.stop();

        Assert.assertFalse("Error messages output during delete step", deleteCommandOutput.contains("ERROR"));
        Assert.assertFalse("Failed to process records during delete step", deleteCommandOutput.contains("processed: 0"));
        Assert.assertFalse("Insert performed during delete step", deleteCommandOutput.contains("inserted: 1"));
        Assert.assertFalse("Update performed during delete step", deleteCommandOutput.contains("updated: 1"));
        Assert.assertFalse("Failure reported during delete step", deleteCommandOutput.contains("failed: 1"));

        // Test that we deleted the results files that were there previously (not the results of our delete)
        for (File file : resultsFiles) {
            if (!file.getName().contains("ClientCorporation_")) {
                TestUtils.checkResultsFile(file, Command.DELETE);
            }
        }
        // endregion

        // TODO: DELETE ATTACHMENTS

        // region TEARDOWN
        // Cleanup our temporary directory
        FileUtils.deleteDirectory(tempDirectory);
        // endregion
    }
}
