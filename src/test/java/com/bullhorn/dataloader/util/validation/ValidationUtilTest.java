package com.bullhorn.dataloader.util.validation;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.util.PrintUtil;

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
        String path = getFilePath("Candidate_Valid_File.csv");
        validationUtil.isValidCsvFile(path);
        Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidCsvFile_badFile() {
        validationUtil.isValidCsvFile("bogus/file/path.csv");
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("ERROR: Cannot access: bogus/file/path.csv");
    }

    @Test
    public void testIsValidCsvFile_directory() {
        String path = getFilePath(".");
        validationUtil.isValidCsvFile(path);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("ERROR: Expected a file, but a directory was provided.");
    }

    @Test
    public void testIsValidCsvFile_nonCsvFile() {
        String path = getFilePath("dataloader.properties");
        validationUtil.isValidCsvFile(path);
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsLoadableEntity() {
        Assert.assertTrue(validationUtil.isLoadableEntity("Candidate"));
        Assert.assertTrue(validationUtil.isLoadableEntity("ClientCorporation"));
        Mockito.verify(printUtilMock, Mockito.never()).printEntityError(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(printUtilMock, Mockito.never()).printUnknownEntityError(Mockito.anyString());
    }

    @Test
    public void testIsLoadableEntity_readOnly() {
        Assert.assertFalse(validationUtil.isLoadableEntity("BusinessSector"));
        Mockito.verify(printUtilMock, Mockito.times(1)).printEntityError(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(printUtilMock, Mockito.never()).printUnknownEntityError(Mockito.anyString());
    }

    @Test
    public void testIsLoadableEntity_badEntity() {
        Assert.assertFalse(validationUtil.isLoadableEntity("BusinessSectors"));
        Mockito.verify(printUtilMock, Mockito.never()).printEntityError(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(printUtilMock, Mockito.times(1)).printUnknownEntityError(Mockito.anyString());
    }

    @Test
    public void testIsDeletableEntity() {
        Assert.assertTrue(validationUtil.isDeletableEntity("Candidate"));
        Assert.assertTrue(validationUtil.isDeletableEntity("ClientContact"));
        Mockito.verify(printUtilMock, Mockito.never()).printEntityError(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(printUtilMock, Mockito.never()).printUnknownEntityError(Mockito.anyString());
    }

    @Test
    public void testIsDeletableEntity_notDeletable() {
        Assert.assertFalse(validationUtil.isDeletableEntity("BusinessSector"));
        Mockito.verify(printUtilMock, Mockito.times(1)).printEntityError(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(printUtilMock, Mockito.never()).printUnknownEntityError(Mockito.anyString());
    }

    @Test
    public void testIsDeletableEntity_badEntity() {
        Assert.assertFalse(validationUtil.isDeletableEntity("BusinessSectors"));
        Mockito.verify(printUtilMock, Mockito.never()).printEntityError(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(printUtilMock, Mockito.times(1)).printUnknownEntityError(Mockito.anyString());
    }

    private String getFilePath(String filename) {
        final ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
    }
}
