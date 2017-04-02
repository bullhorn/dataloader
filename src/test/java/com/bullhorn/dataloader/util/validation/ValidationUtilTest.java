package com.bullhorn.dataloader.util.validation;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.util.PrintUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class ValidationUtilTest {

    private PrintUtil printUtilMock;
    private ValidationUtil validationUtil;

    @Before
    public void setup() throws IOException {
        printUtilMock = Mockito.mock(PrintUtil.class);
        validationUtil = new ValidationUtil(printUtilMock);
    }

    @Test
    public void testIsValidCsvFile() {
        String path = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        Boolean actualResult = validationUtil.isValidCsvFile(path);
        Assert.assertTrue(actualResult);
        Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidCsvFile_badFile() {
        Boolean actualResult = validationUtil.isValidCsvFile("bogus/file/path.csv");
        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("ERROR: Cannot access: bogus/file/path.csv");
    }

    @Test
    public void testIsValidCsvFile_badFile_noPrint() {
        Boolean actualResult = validationUtil.isValidCsvFile("bogus/file/path.csv", false);
        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidCsvFile_directory() {
        String path = TestUtils.getResourceFilePath(".");
        Boolean actualResult = validationUtil.isValidCsvFile(path);
        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("ERROR: Expected a file, but a directory was provided.");
    }

    @Test
    public void testIsValidCsvFile_directory_noPrint() {
        String path = TestUtils.getResourceFilePath(".");
        Boolean actualResult = validationUtil.isValidCsvFile(path, false);
        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidCsvFile_nonCsvFile() {
        String path = TestUtils.getResourceFilePath("unitTest.properties");
        Boolean actualResult = validationUtil.isValidCsvFile(path);
        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidCsvFile_nonCsvFile_noPrint() {
        String path = TestUtils.getResourceFilePath("unitTest.properties");
        Boolean actualResult = validationUtil.isValidCsvFile(path, false);
        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
    }
}
