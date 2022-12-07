package com.bullhorn.dataloader.util;

import org.junit.Assert;
import org.junit.Test;

import com.bullhorn.dataloader.TestUtils;

@SuppressWarnings("InstantiationOfUtilityClass")
public class ValidationUtilTest {

    @Test
    public void testConstructor() {
        ValidationUtil validationUtil = new ValidationUtil();
        Assert.assertNotNull(validationUtil);
    }

    @Test
    public void testValidateCsvFile() {
        String path = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        Assert.assertTrue(ValidationUtil.validateCsvFile(path));
    }

    @Test(expected = DataLoaderException.class)
    public void testValidateCsvFileBadFile() {
        Assert.assertFalse(ValidationUtil.validateCsvFile("bogus/file/path.csv"));
    }

    @Test(expected = DataLoaderException.class)
    public void testValidateCsvFileDirectory() {
        String path = TestUtils.getResourceFilePath(".");
        Assert.assertFalse(ValidationUtil.validateCsvFile(path));
    }

    @Test(expected = DataLoaderException.class)
    public void testValidateCsvFileNonCsvFile() {
        String path = TestUtils.getResourceFilePath("unitTest.properties");
        Assert.assertFalse(ValidationUtil.validateCsvFile(path));
    }
}
