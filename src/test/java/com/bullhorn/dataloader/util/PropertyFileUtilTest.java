package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.util.validation.PropertyValidation;
import org.joda.time.format.DateTimeFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class PropertyFileUtilTest {

    private PropertyValidation propertyValidation;
    private PrintUtil printUtilMock;
    private PropertyFileUtil propertyFileUtil;

    @Before
    public void setup() throws IOException {
        propertyValidation = new PropertyValidation();
        printUtilMock = Mockito.mock(PrintUtil.class);

        String path = getFilePath("dataloader.properties");
        propertyFileUtil = new PropertyFileUtil(path, propertyValidation, printUtilMock);
    }

    @Test
    public void testGetters() {
        Assert.assertEquals(propertyFileUtil.getUsername(), "john.smith");
        Assert.assertEquals(propertyFileUtil.getPassword(), "password123");
        Assert.assertEquals(propertyFileUtil.getClientId(), "1234abcd-123a-123a-123a-acbd1234567");
        Assert.assertEquals(propertyFileUtil.getClientSecret(), "1234567890abcdefghijklmn");
        Assert.assertEquals(propertyFileUtil.getAuthorizeUrl(), "https://auth.bullhornstaffing.com/oauth/authorize");
        Assert.assertEquals(propertyFileUtil.getTokenUrl(), "https://auth.bullhornstaffing.com/oauth/token");
        Assert.assertEquals(propertyFileUtil.getLoginUrl(), "https://rest.bullhornstaffing.com/rest-services/login");
        Assert.assertEquals(propertyFileUtil.getListDelimiter(), ";");
        Assert.assertEquals(propertyFileUtil.getDateParser(), DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss.SSS"));
        Assert.assertEquals(propertyFileUtil.getNumThreads(), new Integer(10));
    }

    @Test
    public void testExistsFields() throws IOException {
        Assert.assertEquals(propertyFileUtil.getEntityExistFields("BusinessSector"),
            Optional.ofNullable(Arrays.asList(new String[]{"name"})));
        Assert.assertEquals(propertyFileUtil.getEntityExistFields("Candidate"),
            Optional.ofNullable(Arrays.asList(new String[]{"id"})));
        Assert.assertEquals(propertyFileUtil.getEntityExistFields("Category"),
            Optional.ofNullable(Arrays.asList(new String[]{"occupation"})));
        Assert.assertEquals(propertyFileUtil.getEntityExistFields("JobOrder"),
            Optional.ofNullable(Arrays.asList(new String[]{"title", "name"})));

        Assert.assertFalse(propertyFileUtil.getEntityExistFields("businessSector").isPresent());
        Assert.assertFalse(propertyFileUtil.getEntityExistFields("CandidateName").isPresent());
        Assert.assertFalse(propertyFileUtil.getEntityExistFields("BOGUS").isPresent());
    }

    private String getFilePath(String filename) {
        final ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
    }
}
