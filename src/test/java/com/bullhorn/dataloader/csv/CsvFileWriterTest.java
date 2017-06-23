package com.bullhorn.dataloader.csv;

import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.util.StringConsts;
import com.csvreader.CsvReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CsvFileWriterTest {

    private String[] headers = {"name", "quest", "favoriteColor"};
    private String[] success = {"Sir Lancelot of Camelot", "To seek the Holy Grail", "Blue"};
    private String[] failure = {"Sir Galahad of Camelot", "I seek the Grail", " Blue. No, yel..."};

    @Test
    public void testNoRecords() throws IOException {
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.LOAD, "path/to/CandidateTest.csv", headers);

        File resultsDir = new File("results/");
        File successFile = new File("results/CandidateTest_load_" + StringConsts.TIMESTAMP + "_success.csv");
        File failureFile = new File("results/CandidateTest_load_" + StringConsts.TIMESTAMP + "_failure.csv");
        Assert.assertTrue(resultsDir.isDirectory());
        Assert.assertFalse(successFile.exists());
        Assert.assertFalse(failureFile.exists());
    }

    @Test
    public void testSuccessRecordsOnly() throws IOException {
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.DELETE, "path/to/CandidateTest.csv", headers);
        csvFileWriter.writeRow(success, Result.Insert(-1));

        File successFile = new File("results/CandidateTest_delete_" + StringConsts.TIMESTAMP + "_success.csv");
        File failureFile = new File("results/CandidateTest_delete_" + StringConsts.TIMESTAMP + "_failure.csv");
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
    public void testNullBullhornIdRecord() throws IOException {
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.LOAD, "path/to/ClientContactTest.csv", headers);
        csvFileWriter.writeRow(success, Result.Update(null));

        File successFile = new File("results/ClientContactTest_load_" + StringConsts.TIMESTAMP + "_success.csv");
        File failureFile = new File("results/ClientContactTest_load_" + StringConsts.TIMESTAMP + "_failure.csv");
        Assert.assertTrue(successFile.isFile());
        Assert.assertFalse(failureFile.exists());

        // clean up test files
        successFile.deleteOnExit();
    }

    @Test
    public void testFailureRecordsOnly() throws IOException {
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.LOAD, "path/to/CandidateTest.csv", headers);
        csvFileWriter.writeRow(failure, Result.Failure(new Exception("You have chosen poorly")));

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
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.DELETE_ATTACHMENTS, "path/to/CandidateTest.csv", headers);
        csvFileWriter.writeRow(success, Result.Insert(1));
        csvFileWriter.writeRow(failure, Result.Failure(new Exception("You have chosen poorly")));

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
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.LOAD_ATTACHMENTS, "path/to/CandidateTest.csv", headers);
        csvFileWriter.writeRow(success, Result.Insert(1));
        csvFileWriter.writeRow(failure, Result.Failure(new Exception("You have chosen poorly")));

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
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.CONVERT_ATTACHMENTS, "path/to/CandidateTest.csv", headers);
        csvFileWriter.writeRow(success, Result.Insert(1));
        csvFileWriter.writeRow(failure, Result.Failure(new Exception("You have chosen poorly")));

        File successFile = new File("results/CandidateTest_convertAttachments_" + StringConsts.TIMESTAMP + "_success.csv");
        File failureFile = new File("results/CandidateTest_convertAttachments_" + StringConsts.TIMESTAMP + "_failure.csv");
        Assert.assertTrue(successFile.isFile());
        Assert.assertTrue(failureFile.isFile());

        // clean up test files
        successFile.deleteOnExit();
        failureFile.deleteOnExit();
    }

}
