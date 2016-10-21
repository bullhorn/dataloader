package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.util.validation.PropertyValidation;
import org.joda.time.format.DateTimeFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;

public class PropertyFileUtilTest {

    private PropertyValidation propertyValidation;
    private PrintUtil printUtilMock;
    private PropertyFileUtil propertyFileUtil;
    private Properties systemPropertiesMock;
    private String path;

    @Before
    public void setup() {
        propertyValidation = new PropertyValidation();
        printUtilMock = Mockito.mock(PrintUtil.class);
        systemPropertiesMock = Mockito.mock(Properties.class);
        path = TestUtils.getResourceFilePath("unitTest.properties");
    }

    @Test
    public void testPropertyFileGetters() throws IOException {
        propertyFileUtil = new PropertyFileUtil(path, systemPropertiesMock, propertyValidation, printUtilMock);

        Assert.assertEquals("john.smith", propertyFileUtil.getUsername());
        Assert.assertEquals("password123", propertyFileUtil.getPassword());
        Assert.assertEquals("1234abcd-123a-123a-123a-acbd1234567", propertyFileUtil.getClientId());
        Assert.assertEquals("1234567890abcdefghijklmn", propertyFileUtil.getClientSecret());
        Assert.assertEquals("https://auth.bullhornstaffing.com/oauth/authorize", propertyFileUtil.getAuthorizeUrl());
        Assert.assertEquals("https://auth.bullhornstaffing.com/oauth/token", propertyFileUtil.getTokenUrl());
        Assert.assertEquals("https://rest.bullhornstaffing.com/rest-services/login", propertyFileUtil.getLoginUrl());
        Assert.assertEquals(";", propertyFileUtil.getListDelimiter());
        Assert.assertEquals(DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss.SSS"), propertyFileUtil.getDateParser());
        Assert.assertEquals(new Integer(10), propertyFileUtil.getNumThreads());
    }

    @Test
    public void testGetterSystemPropertyOverrides() throws IOException {
        Mockito.doReturn("johnny.appleseed").when(systemPropertiesMock).getProperty("username");
        Mockito.doReturn("password456").when(systemPropertiesMock).getProperty("password");
        Mockito.doReturn("2234abcd-123a-123a-123a-acbd1234567").when(systemPropertiesMock).getProperty("clientId");
        Mockito.doReturn("2234567890abcdefghijklmn").when(systemPropertiesMock).getProperty("clientSecret");
        Mockito.doReturn("https://auth.bullhornstaffing.com/oauth/apple").when(systemPropertiesMock).getProperty("authorizeUrl");
        Mockito.doReturn("https://auth.bullhornstaffing.com/oauth/banana").when(systemPropertiesMock).getProperty("tokenUrl");
        Mockito.doReturn("https://rest.bullhornstaffing.com/rest-services/cherry").when(systemPropertiesMock).getProperty("loginUrl");
        Mockito.doReturn(",").when(systemPropertiesMock).getProperty("listDelimiter");
        Mockito.doReturn("5").when(systemPropertiesMock).getProperty("numThreads");

        propertyFileUtil = new PropertyFileUtil(path, systemPropertiesMock, propertyValidation, printUtilMock);

        Assert.assertEquals("johnny.appleseed", propertyFileUtil.getUsername());
        Assert.assertEquals("password456", propertyFileUtil.getPassword());
        Assert.assertEquals("2234abcd-123a-123a-123a-acbd1234567", propertyFileUtil.getClientId());
        Assert.assertEquals("2234567890abcdefghijklmn", propertyFileUtil.getClientSecret());
        Assert.assertEquals("https://auth.bullhornstaffing.com/oauth/apple", propertyFileUtil.getAuthorizeUrl());
        Assert.assertEquals("https://auth.bullhornstaffing.com/oauth/banana", propertyFileUtil.getTokenUrl());
        Assert.assertEquals("https://rest.bullhornstaffing.com/rest-services/cherry", propertyFileUtil.getLoginUrl());
        Assert.assertEquals(",", propertyFileUtil.getListDelimiter());
        Assert.assertEquals(DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss.SSS"), propertyFileUtil.getDateParser());
        Assert.assertEquals(new Integer(5), propertyFileUtil.getNumThreads());
    }

    @Test
    public void testExistsFields() throws IOException {
        propertyFileUtil = new PropertyFileUtil(path, systemPropertiesMock, propertyValidation, printUtilMock);

        Assert.assertEquals(Optional.ofNullable(Arrays.asList(new String[]{"externalID"})),
            propertyFileUtil.getEntityExistFields("Candidate"));
        Assert.assertEquals(Optional.ofNullable(Arrays.asList(new String[]{"externalID"})),
            propertyFileUtil.getEntityExistFields("ClientContact"));
        Assert.assertEquals(Optional.ofNullable(Arrays.asList(new String[]{"customText1"})),
            propertyFileUtil.getEntityExistFields("Lead"));
        Assert.assertEquals(Optional.ofNullable(Arrays.asList(new String[]{"title", "name"})),
            propertyFileUtil.getEntityExistFields("JobOrder"));

        Assert.assertFalse(propertyFileUtil.getEntityExistFields("businessSector").isPresent());
        Assert.assertFalse(propertyFileUtil.getEntityExistFields("CandidateName").isPresent());
        Assert.assertFalse(propertyFileUtil.getEntityExistFields("BOGUS").isPresent());
    }

    @Test
    public void testExistsFieldSystemPropertyOverrides() throws IOException {
        Mockito.doReturn("customTextField4,customTextField5").when(systemPropertiesMock).getProperty("candidateExistField");
        Mockito.doReturn(new HashSet<>(Arrays.asList("candidateExistField", "clientContactExistField"))).when(systemPropertiesMock).stringPropertyNames();

        propertyFileUtil = new PropertyFileUtil(path, systemPropertiesMock, propertyValidation, printUtilMock);

        Assert.assertEquals(Optional.ofNullable(Arrays.asList(new String[]{"customTextField4", "customTextField5"})),
            propertyFileUtil.getEntityExistFields("Candidate"));
    }

    @Test(expected = FileNotFoundException.class)
    public void testPropertyFileSystemPropertyOverride() throws IOException {
        Mockito.doReturn("bogus/file/path/to/dataloader.properties").when(systemPropertiesMock).getProperty("propertyfile");
        propertyFileUtil = new PropertyFileUtil(path, systemPropertiesMock, propertyValidation, printUtilMock);
    }
}
