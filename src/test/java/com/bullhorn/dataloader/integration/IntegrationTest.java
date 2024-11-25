package com.bullhorn.dataloader.integration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.bullhorn.dataloader.Main;
import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.enums.Command;

/**
 * TravisCI runs this as part of every build, using `maven verify`, which goes beyond `maven test` to run the integration test.
 * Uses a test corp on SL9 (BhNext) with hidden credentials in TravisCI Environment Variables.
 */
public class IntegrationTest {

    private static final String EXAMPLE_UUID = "12345678-1234-1234-1234-1234567890AB";
    private static final String EXAMPLE_EXTERNAL_ID_ENDING = "-ext-1";
    private static final long SECONDS_SINCE_EPOCH = System.currentTimeMillis() / 1000;

    private ConsoleOutputCapturer consoleOutputCapturer;

    // Used to turn off integration test features for a specific directory
    private Boolean skipDuplicates = false;
    private Boolean skipInserts = false;
    private Boolean skipExports = false;
    private Boolean skipUpdates = false;
    private Boolean skipDeletes = false;

    /**
     * Tests all directories under: src/test/java/resources/integrationTest for specific use cases. Also tests the entire examples
     * directory, which contains all possible values for all loadable entities and their attachments (See examples/README.md).
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
        runAllCommandsAgainstDirectory(TestUtils.getResourceFilePath("sanity"));

        // Special character test to ensure that we are supporting them in query/search calls
        skipDeletes = true;
        runAllCommandsAgainstDirectory(TestUtils.getResourceFilePath("specialCharacters"));
        skipDeletes = false;

        // Test using more than 100,000 characters in a field
        runAllCommandsAgainstDirectory(TestUtils.getResourceFilePath("longFields"));

        // Test using more than 500 associations in a To-Many field - requires that wildcard matching is enabled
        System.setProperty("wildcardMatching", "true");
        skipExports = true;
        skipUpdates = true;
        runAllCommandsAgainstDirectory(TestUtils.getResourceFilePath("associationsOver500"));
        skipUpdates = false;
        skipExports = false;
        System.setProperty("wildcardMatching", "false");

        // Test for ignoring soft deleted entities
        skipDeletes = true;
        runAllCommandsAgainstDirectory(TestUtils.getResourceFilePath("softDeletes"));
        skipDeletes = false;

        // Test that column header name mapping is working properly
        runAllCommandsAgainstDirectory(TestUtils.getResourceFilePath("columnMapping"));

        // Test that incorrect capitalization will be fixed instead of cause errors
        runAllCommandsAgainstDirectory(TestUtils.getResourceFilePath("capitalization"));

        // Test that the byte order mark is ignored when it is present in the input file as the first (hidden) character
        runAllCommandsAgainstDirectory(TestUtils.getResourceFilePath("byteOrderMark"));

        // Test that country names are case-insensitive
        runAllCommandsAgainstDirectory(TestUtils.getResourceFilePath("countryNames"));

        // Run a test for skipping updates (with the setting turned on)
        System.setProperty("skipDuplicates", "true");
        skipDuplicates = true;
        runAllCommandsAgainstDirectory(TestUtils.getResourceFilePath("skipDuplicates"));
        skipDuplicates = false;
        System.setProperty("skipDuplicates", "false");

        // Test for wildcard associations for candidates in a note
        System.setProperty("wildcardMatching", "true");
        runAllCommandsAgainstDirectory(TestUtils.getResourceFilePath("wildcardMatching"));
        System.setProperty("wildcardMatching", "false");

        // Run a test for processing empty association fields (with the setting turned on)
        System.setProperty("processEmptyAssociations", "true");
        runAllCommandsAgainstDirectory(TestUtils.getResourceFilePath("processEmptyFields"));
        System.setProperty("processEmptyAssociations", "false");

        // Run a test where the difference when caching is significant (manual inspection of timing for now)
        System.setProperty("caching", "false");
        runAllCommandsAgainstDirectory(TestUtils.getResourceFilePath("cacheOff"));
        System.setProperty("caching", "true");
        runAllCommandsAgainstDirectory(TestUtils.getResourceFilePath("cacheOn"));

        // Test for derived entities that are not independent but require that a parent entity exists.
        // Insert the parent entities during part 1 and do not delete them.
        // Update the derived entities that require the parent entity without performing inserts (since insert is not allowed).
        skipDeletes = true;
        runAllCommandsAgainstDirectory(TestUtils.getResourceFilePath("derivedEntitiesPart1"));
        skipInserts = true;
        runAllCommandsAgainstDirectory(TestUtils.getResourceFilePath("derivedEntitiesPart2"));
        skipInserts = false;
        skipDeletes = false;

        skipDeletes = true;
        runAllCommandsAgainstDirectory(TestUtils.getResourceFilePath("skipUpdatesEntitiesPart1"));
        skipUpdates = true;
        skipExports = true;
        runAllCommandsAgainstDirectory(TestUtils.getResourceFilePath("skipUpdatesEntitiesPart2"));
        skipUpdates = false;
        skipDeletes = false;
        skipExports = false;

        // Run the full test of all example files
        runAllCommandsAgainstDirectory("examples/load");
    }

    /**
     * Given a directory path, this method will attempt to run all commands against CSV input files in that directory:
     * <p> <p>
     * 1. Load - Insert
     * 2. Convert Attachments
     * 3. Load Attachments - Insert
     * 4. Load Attachments - Update
     * 5. Delete Attachments
     * 6. Export
     * 7. Load - Update
     * 8. Delete
     * <p> <p>
     * The unique IDs of entities are changed from `-ext-1` to something unique, after the examples have been
     * cloned to a test folder.
     * <p> <p>
     * Test assertions of both command line output and results files created. These steps cover the presence of records
     * in the index and database, so if indexing is lagging behind in production, it will cause the build to fail.
     *
     * @param directoryPath The path to the directory to load
     * @throws IOException For directory cloning
     */
    @SuppressWarnings("ConstantConditions")
    private void runAllCommandsAgainstDirectory(String directoryPath) throws IOException {
        // region SETUP
        // Copy example files to a temp directory located at: 'dataloader/target/test-classes/integrationTest_1234567890'
        File resultsDir = new File(CsvFileWriter.RESULTS_DIR);
        String tempDirPath = TestUtils.getResourceFilePath("") + "/integrationTest_" + SECONDS_SINCE_EPOCH;
        File tempDirectory = new File(tempDirPath);
        FileUtils.copyDirectory(new File(directoryPath), tempDirectory);

        // Replace all external ID endings with unique ones based on the current timestamp
        String newExternalIdEnding = "-" + SECONDS_SINCE_EPOCH;
        TestUtils.replaceTextInFiles(tempDirectory, EXAMPLE_EXTERNAL_ID_ENDING, newExternalIdEnding);

        // Replace all UUIDs in with unique ones
        String uuid = UUID.randomUUID().toString();
        TestUtils.replaceTextInFiles(tempDirectory, EXAMPLE_UUID, uuid);
        // endregion

        // region LOAD - INSERT
        if (!skipInserts) {
            FileUtils.deleteQuietly(new File(CsvFileWriter.RESULTS_DIR)); // Cleanup from previous runs
            System.setIn(IOUtils.toInputStream("yes", StandardCharsets.UTF_8)); // Accepts command for entire directory
            consoleOutputCapturer.start();
            Main.main(new String[]{"load", tempDirPath});
            TestUtils.checkCommandLineOutput(consoleOutputCapturer.stop(), Result.Action.INSERT);
            TestUtils.checkResultsFiles(tempDirectory, Command.LOAD);
        }
        // endregion

        // region ATTACHMENTS
        File tempAttachmentsDirectory = new File(tempDirectory + "/attachments");
        if (tempAttachmentsDirectory.exists()) {
            // region CONVERT ATTACHMENTS
            FileUtils.deleteQuietly(new File(CsvFileWriter.RESULTS_DIR)); // Cleanup from previous runs
            consoleOutputCapturer.start();
            Main.main(new String[]{"convertAttachments", tempAttachmentsDirectory.getPath() + "/Candidate.csv"});
            Main.main(new String[]{"convertAttachments", tempAttachmentsDirectory.getPath() + "/CandidateUpdate.csv"});
            TestUtils.checkCommandLineOutput(consoleOutputCapturer.stop(), Result.Action.CONVERT);
            TestUtils.checkResultsFiles(tempAttachmentsDirectory, Command.CONVERT_ATTACHMENTS);
            // endregion

            // region LOAD ATTACHMENTS - INSERT
            FileUtils.deleteQuietly(new File(CsvFileWriter.RESULTS_DIR)); // Cleanup from previous runs
            consoleOutputCapturer.start();
            Main.main(new String[]{"loadAttachments", tempAttachmentsDirectory.getPath() + "/Candidate.csv"});
            TestUtils.checkCommandLineOutput(consoleOutputCapturer.stop(), Result.Action.INSERT);
            // endregion

            // region LOAD ATTACHMENTS - UPDATE
            // Do not clean up after previous run here - both Candidate and CandidateUpdate need to be present for delete step
            consoleOutputCapturer.start();
            Main.main(new String[]{"loadAttachments", tempAttachmentsDirectory.getPath() + "/CandidateUpdate.csv"});
            TestUtils.checkCommandLineOutput(consoleOutputCapturer.stop(), Result.Action.UPDATE);
            TestUtils.checkResultsFiles(tempAttachmentsDirectory, Command.LOAD_ATTACHMENTS);
            // endregion

            // region DELETE ATTACHMENTS
            consoleOutputCapturer.start();
            File successResultsFile = resultsDir.listFiles()[0];
            Main.main(new String[]{"deleteAttachments", successResultsFile.getPath()});
            TestUtils.checkCommandLineOutput(consoleOutputCapturer.stop(), Result.Action.DELETE);
            TestUtils.checkResultsFile(successResultsFile, Command.DELETE_ATTACHMENTS);
            // endregion
        }
        // endregion

        // region EXPORT
        if (!skipExports) {
            FileUtils.deleteQuietly(new File(CsvFileWriter.RESULTS_DIR)); // Cleanup from previous runs
            System.setIn(IOUtils.toInputStream("yes", StandardCharsets.UTF_8)); // Accepts command for entire directory
            consoleOutputCapturer.start();
            Main.main(new String[]{"export", tempDirPath});
            TestUtils.checkCommandLineOutput(consoleOutputCapturer.stop(), Result.Action.EXPORT);
            TestUtils.checkResultsFiles(tempDirectory, Command.EXPORT);
        }
        // endregion

        // region LOAD - UPDATE
        if (!skipUpdates) {
            // Update the effectiveDate to allow effective dated entities to be updated
            TestUtils.replaceTextInFiles(tempDirectory, "2001-01-01", "2002-02-02");

            FileUtils.deleteQuietly(new File(CsvFileWriter.RESULTS_DIR)); // Cleanup from previous runs
            System.setIn(IOUtils.toInputStream("yes", StandardCharsets.UTF_8)); // Accepts command for entire directory
            consoleOutputCapturer.start();
            Main.main(new String[]{"load", tempDirPath});
            Result.Action expectedAction = skipDuplicates ? Result.Action.SKIP : Result.Action.UPDATE;
            TestUtils.checkCommandLineOutput(consoleOutputCapturer.stop(), expectedAction);
            TestUtils.checkResultsFiles(tempDirectory, Command.LOAD);
        }
        // endregion

        // region DELETE
        if (!skipDeletes) {
            // Capture results file directory state
            File[] resultsFiles = resultsDir.listFiles();

            System.setIn(IOUtils.toInputStream("yes", StandardCharsets.UTF_8)); // Accepts command for entire directory
            consoleOutputCapturer.start();
            Main.main(new String[]{"delete", CsvFileWriter.RESULTS_DIR});
            TestUtils.checkCommandLineOutput(consoleOutputCapturer.stop(), Result.Action.DELETE);

            // Test that we deleted the results files that were there previously (not the results of our delete)
            for (File file : resultsFiles) {
                if (TestUtils.isResultsFileDeletable(file)) {
                    TestUtils.checkResultsFile(file, Command.DELETE);
                }
            }
        }
        // endregion

        // region TEARDOWN
        // Cleanup our temporary directory
        FileUtils.deleteQuietly(tempDirectory);
        // endregion
    }
}
