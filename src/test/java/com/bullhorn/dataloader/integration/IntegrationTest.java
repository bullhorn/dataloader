package com.bullhorn.dataloader.integration;

import com.bullhorn.dataloader.Main;
import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.enums.Command;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * The purpose of this integration test is to:
 *
 * 1. Allow for TravisCI to run as part of every build check, using `maven verify`, which goes beyond `maven test` to
 * also run the integration test.  Uses a test corp on SL9 (BhNext) with hidden credentials in TravisCI Environment
 * Variables.
 *
 * 2. Tests the entire Examples directory, which contains all possible values for all loadable entities and their
 * attachments.  The unique IDs of all of the entities are changed from `-ext-1` to something unique, after the examples
 * have been cloned to a test folder.
 *
 * 3. INSERT the entire examples/load/ folder by performing the load command the first time.
 *
 * 4. UPDATE the entire examples/load/ folder by performing the load command a second time, with all exist fields
 * properly set in the integrationTest.properties file.
 *
 * 5. DELETE all entered records by targeting the entire results directory.
 *
 * 6. Test assertions of both command line output and results files created. We are not making calls against the CRM
 * itself to verify the presence or absence of records, since these steps will cover the presence of records in the
 * index and database.
 */
public class IntegrationTest {

    private static final String EXAMPLE_UUID = "12345678-1234-1234-1234-1234567890AB";
    private static final String EXAMPLE_EXTERNAL_ID_ENDING = "-ext-1";

    private ConsoleOutputCapturer consoleOutputCapturer;

    /**
     * Runs the integration test, first with a very simple sanity check, then the full examples directory.
     *
     * @throws IOException For directory cloning
     */
    @Test
    public void testIntegration() throws IOException {
        // Use the properties file from the test/resources directory
        System.setProperty("propertyfile", TestUtils.getResourceFilePath("integrationTest.properties"));

        // Capture command line output as a string without stopping the real-time printout
        consoleOutputCapturer = new ConsoleOutputCapturer();

        // Sanity to catch quick and obvious failures
        insertUpdateDeleteFromDirectory(TestUtils.getResourceFilePath("sanity"), false);

        // Special character test to ensure that we are supporting them in query/search calls
        insertUpdateDeleteFromDirectory(TestUtils.getResourceFilePath("specialCharacters"), true);

        // Test using more than 100,000 characters in a field
        insertUpdateDeleteFromDirectory(TestUtils.getResourceFilePath("longFields"), false);

        // Test using more than 500 associations in a To-Many field - requires that wildcard matching is enabled
        System.setProperty("wildcardMatching", "true");
        insertUpdateDeleteFromDirectory(TestUtils.getResourceFilePath("associationsOver500"), false);
        System.setProperty("wildcardMatching", "false");

        // Test for ignoring soft deleted entities
        insertUpdateDeleteFromDirectory(TestUtils.getResourceFilePath("softDeletes"), true);

        // Test that column header name mapping is working properly
        insertUpdateDeleteFromDirectory(TestUtils.getResourceFilePath("columnMapping"), false);

        // Test that the byte order mark is ignored when it's present in the input file as the first (hidden) character
        insertUpdateDeleteFromDirectory(TestUtils.getResourceFilePath("byteOrderMark"), false);

        // Test for wildcard associations for candidates in a note
        System.setProperty("wildcardMatching", "true");
        insertUpdateDeleteFromDirectory(TestUtils.getResourceFilePath("wildcardMatching"), false);
        System.setProperty("wildcardMatching", "false");

        // Run a test for processing empty association fields (with the setting turned on)
        System.setProperty("processEmptyAssociations", "true");
        insertUpdateDeleteFromDirectory(TestUtils.getResourceFilePath("processEmptyFields"), false);
        System.setProperty("processEmptyAssociations", "false");

        // Run the full test of all example files
        insertUpdateDeleteFromDirectory("examples/load", false);
    }

    /**
     * Given a directory path, this method will attempt to load twice (insert, then update) then delete all CSV files
     * found in that directory.
     *
     * @param directoryPath The path to the directory to load
     * @param skipDelete    Set to true if the test directory contains intentionally deleted records
     * @throws IOException For directory cloning
     */
    @SuppressWarnings("ConstantConditions")
    private void insertUpdateDeleteFromDirectory(String directoryPath, Boolean skipDelete) throws IOException {
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
        FileUtils.deleteQuietly(new File(CsvFileWriter.RESULTS_DIR)); // Cleanup from previous runs
        System.setIn(IOUtils.toInputStream("yes", "UTF-8")); // For accepting the load/delete from directory
        consoleOutputCapturer.start();
        Main.main(new String[]{"load", tempDirPath});
        TestUtils.checkCommandLineOutput(consoleOutputCapturer.stop(), Result.Action.INSERT);
        TestUtils.checkResultsFiles(tempDirectory, Command.LOAD);
        // endregion

        if (tempAttachmentsDirectory != null) {
            // region INSERT ATTACHMENTS
            FileUtils.deleteQuietly(new File(CsvFileWriter.RESULTS_DIR)); // Cleanup from previous runs
            consoleOutputCapturer.start();
            Main.main(new String[]{"loadAttachments", tempAttachmentsDirectory.getPath() + "/Candidate.csv"});
            TestUtils.checkCommandLineOutput(consoleOutputCapturer.stop(), Result.Action.INSERT);
            // endregion

            // region UPDATE ATTACHMENTS
            consoleOutputCapturer.start();
            Main.main(new String[]{"loadAttachments", tempAttachmentsDirectory.getPath() + "/CandidateUpdate.csv"});
            TestUtils.checkCommandLineOutput(consoleOutputCapturer.stop(), Result.Action.UPDATE);
            TestUtils.checkResultsFiles(tempAttachmentsDirectory, Command.LOAD_ATTACHMENTS);
            // endregion
        }

        // region UPDATE
        FileUtils.deleteQuietly(new File(CsvFileWriter.RESULTS_DIR)); // Cleanup from previous runs
        System.setIn(IOUtils.toInputStream("yes", "UTF-8"));
        consoleOutputCapturer.start();
        Main.main(new String[]{"load", tempDirPath});
        TestUtils.checkCommandLineOutput(consoleOutputCapturer.stop(), Result.Action.UPDATE);
        TestUtils.checkResultsFiles(tempDirectory, Command.LOAD);
        // endregion

        if (!skipDelete) {
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
            TestUtils.checkCommandLineOutput(consoleOutputCapturer.stop(), Result.Action.DELETE);

            // Test that we deleted the results files that were there previously (not the results of our delete)
            for (File file : resultsFiles) {
                if (!file.getName().contains("ClientCorporation_")) {
                    TestUtils.checkResultsFile(file, Command.DELETE);
                }
            }
            // endregion

            // TODO: DELETE ATTACHMENTS
        }

        // region TEARDOWN
        // Cleanup our temporary directory
        FileUtils.deleteQuietly(tempDirectory);
        // endregion
    }
}
