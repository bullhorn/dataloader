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
    private ValidationUtil validationUtil;

    @Before
    public void setup() {
        printUtilMock = mock(PrintUtil.class);
        validationUtil = new ValidationUtil(printUtilMock);
    }

    @Test
    public void testIsValidCsvFile() {
        String path = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        Assert.assertTrue(validationUtil.isValidCsvFile(path));
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testIsValidCsvFileBadFile() {
        Assert.assertFalse(validationUtil.isValidCsvFile("bogus/file/path.csv"));
        verify(printUtilMock, times(1)).printAndLog("ERROR: Cannot access: bogus/file/path.csv");
    }

    @Test
    public void testIsValidCsvFileBadFileNoPrint() {
        Assert.assertFalse(validationUtil.isValidCsvFile("bogus/file/path.csv", false));
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testIsValidCsvFileDirectory() {
        String path = TestUtils.getResourceFilePath(".");
        Assert.assertFalse(validationUtil.isValidCsvFile(path));
        verify(printUtilMock, times(1)).printAndLog("ERROR: Expected a file, but a directory was provided.");
    }

    @Test
    public void testIsValidCsvFileDirectoryNoPrint() {
        String path = TestUtils.getResourceFilePath(".");
        Assert.assertFalse(validationUtil.isValidCsvFile(path, false));
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testIsValidCsvFileNonCsvFile() {
        String path = TestUtils.getResourceFilePath("unitTest.properties");
        Assert.assertFalse(validationUtil.isValidCsvFile(path));
        verify(printUtilMock, times(2)).printAndLog(anyString());
    }

    @Test
    public void testIsValidCsvFileNonCsvFileNoPrint() {
        String path = TestUtils.getResourceFilePath("unitTest.properties");
        Assert.assertFalse(validationUtil.isValidCsvFile(path, false));
        verify(printUtilMock, never()).printAndLog(anyString());
    }
}
