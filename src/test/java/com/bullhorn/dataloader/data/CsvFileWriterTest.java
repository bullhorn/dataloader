package com.bullhorn.dataloader.data;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.util.StringConsts;
import com.csvreader.CsvReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CsvFileWriterTest {

    private Row successRow;
    private Row failureRow;

    @Before
    public void setup() throws IOException {
        successRow = TestUtils.createRow("name,quest,favoriteColor", "Sir Lancelot of Camelot,To seek the Holy Grail,Blue");
        failureRow = TestUtils.createRow("name,quest,favoriteColor", "Sir Galahad of Camelot,I seek the Grail,Blue. No - yel...");
    }

    @Test
    public void testNoRecords() {
        new CsvFileWriter(Command.LOAD, "path/to/CandidateTestNoRecords.csv", successRow.getNames().toArray(new String[0]));
        File resultsDir = new File("results/");
        File successFile = new File("results/CandidateTestNoRecords_load_" + StringConsts.TIMESTAMP + "_success.csv");
        File failureFile = new File("results/CandidateTestNoRecords_load_" + StringConsts.TIMESTAMP + "_failure.csv");
        Assert.assertTrue(resultsDir.isDirectory());
        Assert.assertFalse(successFile.exists());
        Assert.assertFalse(failureFile.exists());
    }

    @Test
    public void testLoadSuccessRecordsOnly() throws IOException {
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.LOAD, "path/to/CandidateTestLoadSuccessRecordsOnly.csv",
            successRow.getNames().toArray(new String[0]));
        csvFileWriter.writeRow(successRow, Result.insert(-1));

        File successFile = new File("results/CandidateTestLoadSuccessRecordsOnly_load_" + StringConsts.TIMESTAMP + "_success.csv");
        File failureFile = new File("results/CandidateTestLoadSuccessRecordsOnly_load_" + StringConsts.TIMESTAMP + "_failure.csv");
        Assert.assertTrue(successFile.isFile());
        Assert.assertFalse(failureFile.exists());

        FileReader fileReader = new FileReader(successFile);
        CsvReader csvReader = new CsvReader(fileReader);
        csvReader.readHeaders();
        String[] expectedHeaders = new String[]{"id", "dataloader_action", "name", "quest", "favoriteColor"};
        String[] actualHeaders = csvReader.getHeaders();
        Assert.assertArrayEquals(expectedHeaders, actualHeaders);

        // clean up test files
        successFile.deleteOnExit();
    }

    @Test
    public void testDeleteSuccessRecordsOnly() throws IOException {
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.DELETE, "path/to/CandidateTest.csv", successRow.getNames().toArray(new String[0]));
        csvFileWriter.writeRow(successRow, Result.insert(-1));

        File successFile = new File("results/CandidateTest_delete_" + StringConsts.TIMESTAMP + "_success.csv");
        File failureFile = new File("results/CandidateTest_delete_" + StringConsts.TIMESTAMP + "_failure.csv");
        Assert.assertTrue(successFile.isFile());
        Assert.assertFalse(failureFile.exists());

        FileReader fileReader = new FileReader(successFile);
        CsvReader csvReader = new CsvReader(fileReader);
        csvReader.readHeaders();
        String[] expectedHeaders = new String[]{"name", "quest", "favoriteColor"};
        String[] actualHeaders = csvReader.getHeaders();
        Assert.assertArrayEquals(expectedHeaders, actualHeaders);

        // clean up test files
        successFile.deleteOnExit();
    }

    @Test
    public void testNullBullhornIdRecord() throws IOException {
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.LOAD, "path/to/ClientContactTest.csv", successRow.getNames().toArray(new String[0]));
        csvFileWriter.writeRow(successRow, Result.update(null));

        File successFile = new File("results/ClientContactTest_load_" + StringConsts.TIMESTAMP + "_success.csv");
        File failureFile = new File("results/ClientContactTest_load_" + StringConsts.TIMESTAMP + "_failure.csv");
        Assert.assertTrue(successFile.isFile());
        Assert.assertFalse(failureFile.exists());

        // clean up test files
        successFile.deleteOnExit();
    }

    @Test
    public void testFailureRecordsOnly() throws IOException {
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.LOAD, "path/to/CandidateTest.csv", successRow.getNames().toArray(new String[0]));
        csvFileWriter.writeRow(failureRow, Result.failure(new Exception("You have chosen poorly")));

        File successFile = new File("results/CandidateTest_load_" + StringConsts.TIMESTAMP + "_success.csv");
        File failureFile = new File("results/CandidateTest_load_" + StringConsts.TIMESTAMP + "_failure.csv");
        Assert.assertFalse(successFile.exists());
        Assert.assertTrue(failureFile.isFile());

        FileReader fileReader = new FileReader(failureFile);
        CsvReader csvReader = new CsvReader(fileReader);
        csvReader.readHeaders();
        String[] expectedHeaders = new String[]{"failure_reason", "name", "quest", "favoriteColor"};
        String[] actualHeaders = csvReader.getHeaders();
        Assert.assertArrayEquals(expectedHeaders, actualHeaders);

        // clean up test files
        failureFile.deleteOnExit();
    }

    @Test
    public void testSuccessAndFailureRecords() throws IOException {
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.DELETE_ATTACHMENTS, "path/to/CandidateTest.csv", successRow.getNames().toArray(new String[0]));
        csvFileWriter.writeRow(successRow, Result.insert(1));
        csvFileWriter.writeRow(failureRow, Result.failure(new Exception("You have chosen poorly")));

        File successFile = new File("results/CandidateTest_deleteAttachments_" + StringConsts.TIMESTAMP + "_success.csv");
        File failureFile = new File("results/CandidateTest_deleteAttachments_" + StringConsts.TIMESTAMP + "_failure.csv");
        Assert.assertTrue(successFile.isFile());
        Assert.assertTrue(failureFile.isFile());

        // clean up test files
        successFile.deleteOnExit();
        failureFile.deleteOnExit();
    }

    @Test
    public void testLoadAttachments() throws IOException {
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.LOAD_ATTACHMENTS, "path/to/CandidateTest.csv", successRow.getNames().toArray(new String[0]));
        csvFileWriter.writeRow(successRow, Result.insert(1));
        csvFileWriter.writeRow(failureRow, Result.failure(new Exception("You have chosen poorly")));

        File successFile = new File("results/CandidateTest_loadAttachments_" + StringConsts.TIMESTAMP + "_success.csv");
        File failureFile = new File("results/CandidateTest_loadAttachments_" + StringConsts.TIMESTAMP + "_failure.csv");
        Assert.assertTrue(successFile.isFile());
        Assert.assertTrue(failureFile.isFile());

        // clean up test files
        successFile.deleteOnExit();
        failureFile.deleteOnExit();
    }

    @Test
    public void testConvertAttachments() throws IOException {
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.CONVERT_ATTACHMENTS, "path/to/CandidateTest.csv", successRow.getNames().toArray(new String[0]));
        csvFileWriter.writeRow(successRow, Result.insert(1));
        csvFileWriter.writeRow(failureRow, Result.failure(new Exception("You have chosen poorly")));

        File successFile = new File("results/CandidateTest_convertAttachments_" + StringConsts.TIMESTAMP + "_success.csv");
        File failureFile = new File("results/CandidateTest_convertAttachments_" + StringConsts.TIMESTAMP + "_failure.csv");
        Assert.assertTrue(successFile.isFile());
        Assert.assertTrue(failureFile.isFile());

        // clean up test files
        successFile.deleteOnExit();
        failureFile.deleteOnExit();
    }
}
