package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ValidationUtilTest {

    private PrintUtil printUtilMock;

    @Before
    public void setup() {
        printUtilMock = mock(PrintUtil.class);
    }

    @Test
    public void testValidateCsvFile() {
        String path = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        Assert.assertTrue(ValidationUtil.validateCsvFile(path, printUtilMock));
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testValidateCsvFileBadFile() {
        Assert.assertFalse(ValidationUtil.validateCsvFile("bogus/file/path.csv", printUtilMock));
        verify(printUtilMock, times(1)).printAndLog("ERROR: Cannot access: bogus/file/path.csv");
    }

    @Test
    public void testValidateCsvFileBadFileNoPrint() {
        Assert.assertFalse(ValidationUtil.validateCsvFile("bogus/file/path.csv"));
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testValidateCsvFileDirectory() {
        String path = TestUtils.getResourceFilePath(".");
        Assert.assertFalse(ValidationUtil.validateCsvFile(path, printUtilMock));
        verify(printUtilMock, times(1)).printAndLog("ERROR: Expected a file, but a directory was provided.");
    }

    @Test
    public void testValidateCsvFileDirectoryNoPrint() {
        String path = TestUtils.getResourceFilePath(".");
        Assert.assertFalse(ValidationUtil.validateCsvFile(path));
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testValidateCsvFileNonCsvFile() {
        String path = TestUtils.getResourceFilePath("unitTest.properties");
        Assert.assertFalse(ValidationUtil.validateCsvFile(path, printUtilMock));
        verify(printUtilMock, times(2)).printAndLog(anyString());
    }

    @Test
    public void testValidateCsvFileNonCsvFileNoPrint() {
        String path = TestUtils.getResourceFilePath("unitTest.properties");
        Assert.assertFalse(ValidationUtil.validateCsvFile(path));
        verify(printUtilMock, never()).printAndLog(anyString());
    }
}
