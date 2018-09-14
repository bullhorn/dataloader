package com.bullhorn.dataloader.data;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;

public class CsvFileReaderTest {

    private PropertyFileUtil propertyFileUtilMock = mock(PropertyFileUtil.class);

    @Test
    public void duplicateHeaders() throws IOException {
        IllegalStateException expectedException = new IllegalStateException("Provided CSV file contains the following duplicate headers:\n"
            + "\tname\n");

        IllegalStateException actualException = null;
        try {
            new CsvFileReader(TestUtils.getResourceFilePath("ClientCorporation_DuplicateColumns.csv"), propertyFileUtilMock);
        } catch (IllegalStateException e) {
            actualException = e;
        }

        assert actualException != null;
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    public void missingHeader() throws IOException, InterruptedException {
        IOException expectedException = new IOException("Header column count 2 is not equal to row column count 3");
        CsvFileReader csvFileReader = new CsvFileReader(TestUtils.getResourceFilePath("ClientCorporation_MissingHeader.csv"), propertyFileUtilMock);

        IOException actualException = null;
        try {
            csvFileReader.readRecord();
            csvFileReader.getRow();
        } catch (IOException e) {
            actualException = e;
        }

        assert actualException != null;
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }
}
