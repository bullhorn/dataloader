package com.bullhorn.dataloader.csv;

import com.bullhorn.dataloader.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class CsvFileReaderTest {

    @Test
    public void duplicateHeaders() throws IOException, InterruptedException {
        IllegalStateException expectedException = new IllegalStateException("Provided CSV file contains the following duplicate headers:\n" +
            "\tname\n");

        IllegalStateException actualException = null;
        try {
            new CsvFileReader(TestUtils.getResourceFilePath("ClientCorporation_DuplicateColumns.csv"));
        } catch (IllegalStateException e) {
            actualException = e;
        }

        assert actualException != null;
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    public void missingHeader() throws IOException, InterruptedException {
        IOException expectedException = new IOException("Header column count 2 is not equal to row column count 3");
        CsvFileReader csvFileReader = new CsvFileReader(TestUtils.getResourceFilePath("ClientCorporation_MissingHeader.csv"));

        IOException actualException = null;
        try {
            csvFileReader.readRecord();
            csvFileReader.getRecordDataMap();
        } catch (IOException e) {
            actualException = e;
        }

        assert actualException != null;
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }
}
