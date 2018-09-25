package com.bullhorn.dataloader.data;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.PropertyValidationUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.mock;

public class CsvFileReaderTest {

    private PropertyFileUtil propertyFileUtil;
    private PrintUtil printUtilMock;

    @Before
    public void setup() throws IOException {
        printUtilMock = mock(PrintUtil.class);

        // Normally, we would mock out these low level dependencies, but it's in fact easier and more
        // straightforward to use the real objects when testing.
        String path = TestUtils.getResourceFilePath("unitTest.properties");
        Map<String, String> envVars = new HashMap<>();
        Properties systemProperties = new Properties();
        String[] emptyArgs = new String[]{};
        PropertyValidationUtil propertyValidationUtil = new PropertyValidationUtil();
        propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs,
            propertyValidationUtil, printUtilMock);
    }

    @Test
    public void testMappedColumns() throws IOException {
        CsvFileReader csvFileReader = new CsvFileReader(TestUtils.getResourceFilePath("Candidate_MappedColumns.csv"), propertyFileUtil, printUtilMock);
        Assert.assertArrayEquals(new String[]{"id", "firstName", "lastName", "email", "owner.id"}, csvFileReader.getHeaders());
    }

    @Test
    public void testGetRecord_RemovingColumn() throws IOException {
        CsvFileReader csvFileReader = new CsvFileReader(TestUtils.getResourceFilePath("Candidate_MappedColumns.csv"), propertyFileUtil, printUtilMock);

        boolean hasRecord = csvFileReader.readRecord();
        Assert.assertTrue(hasRecord);

        Row row = csvFileReader.getRow();
        Assert.assertEquals(Arrays.asList("1", "John", "Smith", "j.smith@example.com", "101"), row.getValues());
    }

    @Test
    public void testGetRecord_ReadingRows() throws IOException {
        CsvFileReader csvFileReader = new CsvFileReader(TestUtils.getResourceFilePath("Candidate_MappedColumns.csv"), propertyFileUtil, printUtilMock);

        boolean hasRecord = csvFileReader.readRecord();
        Assert.assertTrue(hasRecord);

        hasRecord = csvFileReader.readRecord();
        Assert.assertFalse(hasRecord);
    }

    @Test
    public void testDuplicateHeaders() throws IOException {
        IllegalStateException expectedException = new IllegalStateException("Provided CSV file contains the following duplicate headers:\n"
            + "\tname\n"
            + "\tfirstName\n");

        IllegalStateException actualException = null;
        try {
            new CsvFileReader(TestUtils.getResourceFilePath("ClientCorporation_DuplicateColumns.csv"), propertyFileUtil, printUtilMock);
        } catch (IllegalStateException e) {
            actualException = e;
        }

        assert actualException != null;
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    public void testMissingHeader() throws IOException {
        IOException expectedException = new IOException("Row 1: Header column count 2 is not equal to row column count 3");
        CsvFileReader csvFileReader = new CsvFileReader(TestUtils.getResourceFilePath("ClientCorporation_MissingHeader.csv"), propertyFileUtil, printUtilMock);

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
