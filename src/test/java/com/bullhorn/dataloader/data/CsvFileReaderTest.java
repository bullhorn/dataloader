package com.bullhorn.dataloader.data;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.enums.ErrorInfo;
import com.bullhorn.dataloader.util.DataLoaderException;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;

public class CsvFileReaderTest {

    private PropertyFileUtil propertyFileUtil;
    private PrintUtil printUtilMock;

    @Before
    public void setup() throws IOException {
        printUtilMock = mock(PrintUtil.class);

        // Normally, we would mock out these low level dependencies, but it is in fact easier and more
        // straightforward to use the real objects when testing.
        String path = TestUtils.getResourceFilePath("unitTest.properties");
        Map<String, String> envVars = new HashMap<>();
        Properties systemProperties = new Properties();
        String[] emptyArgs = new String[]{};
        propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, printUtilMock);
    }

    @Test
    public void testByteOrderMarkRemoval() throws IOException {
        CsvFileReader csvFileReader = new CsvFileReader(TestUtils.getResourceFilePath("CandidateByteOrderMark.csv"), propertyFileUtil, printUtilMock);
        Assert.assertArrayEquals(new String[]{"externalID", "name", "firstName", "lastName", "email"}, csvFileReader.getHeaders());
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
        DataLoaderException expectedException = new DataLoaderException(ErrorInfo.DUPLICATE_COLUMNS_PROVIDED,
            "Provided CSV file contains the following duplicate headers:\n\tname\n\tfirstName\n");

        DataLoaderException actualException = null;
        try {
            new CsvFileReader(TestUtils.getResourceFilePath("ClientCorporation_DuplicateColumns.csv"), propertyFileUtil, printUtilMock);
        } catch (DataLoaderException e) {
            actualException = e;
        }

        assert actualException != null;
        Assert.assertEquals(expectedException.getErrorInfo(), actualException.getErrorInfo());
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    public void testMissingHeader() throws IOException {
        DataLoaderException expectedException = new DataLoaderException(ErrorInfo.INVALID_NUMBER_OF_COLUMNS,
            "Row 1: Header column count 2 does not match row column count 3");
        CsvFileReader csvFileReader = new CsvFileReader(TestUtils.getResourceFilePath("ClientCorporation_MissingHeader.csv"),
            propertyFileUtil, printUtilMock);

        DataLoaderException actualException = null;
        try {
            csvFileReader.readRecord();
            csvFileReader.getRow();
        } catch (DataLoaderException e) {
            actualException = e;
        }

        assert actualException != null;
        Assert.assertEquals(expectedException.getErrorInfo(), actualException.getErrorInfo());
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }
}
