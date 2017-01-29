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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class PropertyFileUtilTest {

    private String path;
    Map<String, String> envVars;
    private Properties systemProperties;
    private String[] emptyArgs;
    private PropertyValidation propertyValidation;
    private PrintUtil printUtilMock;

    @Before
    public void setup() {
        path = TestUtils.getResourceFilePath("unitTest.properties");
        emptyArgs = new String[] {};
        envVars = new HashMap<>();
        systemProperties = new Properties();
        propertyValidation = new PropertyValidation();
        printUtilMock = Mockito.mock(PrintUtil.class);
    }

    @Test
    public void testGetMethods_PropertyFileValues() throws IOException {
        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, propertyValidation, printUtilMock);

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
        Assert.assertEquals(new Integer(0), propertyFileUtil.getWaitTimeMsecBetweenFilesInDirectory());
    }

    @Test
    public void testGetMethods_EnvironmentVariableOverrides() throws IOException {
        envVars.put("DATALOADER_USERNAME", "johnny.appleseed");
        envVars.put("DATALOADER_PASSWORD", "password456");
        envVars.put("DATALOADER_CLIENT_ID", "2234abcd-123a-123a-123a-acbd1234567");
        envVars.put("DATALOADER_CLIENT_SECRET", "2234567890abcdefghijklmn");
        envVars.put("DATALOADER_AUTHORIZE_URL", "https://auth.bullhornstaffing.com/oauth/apple");
        envVars.put("DATALOADER_TOKEN_URL", "https://auth.bullhornstaffing.com/oauth/banana");
        envVars.put("DATALOADER_LOGIN_URL", "https://rest.bullhornstaffing.com/rest-services/cherry");
        envVars.put("DATALOADER_LIST_DELIMITER", ",");
        envVars.put("DATALOADER_NUM_THREADS", "5");
        envVars.put("DATALOADER_WAIT_TIME_MSEC_BETWEEN_FILES_IN_DIRECTORY", "15");

        // Ensure that values that do not begin with "DATALOADER_" do not get used
        envVars.put("dataloader_username", "bogus");
        envVars.put("username", "bogus");
        envVars.put("password", "bogus");
        envVars.put("dataloader_password", "bogus");
        envVars.put("clientId", "bogus");
        envVars.put("clientSecret", "bogus");
        envVars.put("authorizeUrl", "bogus");
        envVars.put("tokenUrl", "bogus");
        envVars.put("loginUrl", "bogus");
        envVars.put("listDelimiter", "bogus");
        envVars.put("dataloader_numThreads", "bogus");
        envVars.put("numThreads", "bogus");
        envVars.put("waitTimeMSecBetweenFilesInDirectory", "99999999");

        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, propertyValidation, printUtilMock);

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
        Assert.assertEquals(new Integer(15), propertyFileUtil.getWaitTimeMsecBetweenFilesInDirectory());
    }

    @Test
    public void testGetMethods_SystemPropertyOverrides() throws IOException {
        envVars.put("DATALOADER_USERNAME", "johnny.appleseed");
        envVars.put("DATALOADER_PASSWORD", "password456");
        envVars.put("DATALOADER_CLIENT_ID", "2234abcd-123a-123a-123a-acbd1234567");
        envVars.put("DATALOADER_CLIENT_SECRET", "2234567890abcdefghijklmn");
        envVars.put("DATALOADER_AUTHORIZE_URL", "https://auth.bullhornstaffing.com/oauth/apple");
        envVars.put("DATALOADER_TOKEN_URL", "https://auth.bullhornstaffing.com/oauth/banana");
        envVars.put("DATALOADER_LOGIN_URL", "https://rest.bullhornstaffing.com/rest-services/cherry");
        envVars.put("DATALOADER_LIST_DELIMITER", ",");
        envVars.put("DATALOADER_NUM_THREADS", "5");
        envVars.put("DATALOADER_WAIT_TIME_MSEC_BETWEEN_FILES_IN_DIRECTORY", "15");

        systemProperties.setProperty("username", "johnny.be-good");
        systemProperties.setProperty("password", "password789");
        systemProperties.setProperty("clientId", "2234abcd-123a-123a-123a-acbd1231111");
        systemProperties.setProperty("clientSecret", "2231111890abcdefghijklmn");
        systemProperties.setProperty("authorizeUrl", "https://auth.bullhornstaffing.com/oauth/chipmunk");
        systemProperties.setProperty("tokenUrl", "https://auth.bullhornstaffing.com/oauth/pony");
        systemProperties.setProperty("loginUrl", "https://rest.bullhornstaffing.com/rest-services/wallaby");
        systemProperties.setProperty("listDelimiter", "|");
        systemProperties.setProperty("numThreads", "6");
        systemProperties.setProperty("waitTimeMSecBetweenFilesInDirectory", "20");

        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, propertyValidation, printUtilMock);

        Assert.assertEquals("johnny.be-good", propertyFileUtil.getUsername());
        Assert.assertEquals("password789", propertyFileUtil.getPassword());
        Assert.assertEquals("2234abcd-123a-123a-123a-acbd1231111", propertyFileUtil.getClientId());
        Assert.assertEquals("2231111890abcdefghijklmn", propertyFileUtil.getClientSecret());
        Assert.assertEquals("https://auth.bullhornstaffing.com/oauth/chipmunk", propertyFileUtil.getAuthorizeUrl());
        Assert.assertEquals("https://auth.bullhornstaffing.com/oauth/pony", propertyFileUtil.getTokenUrl());
        Assert.assertEquals("https://rest.bullhornstaffing.com/rest-services/wallaby", propertyFileUtil.getLoginUrl());
        Assert.assertEquals("|", propertyFileUtil.getListDelimiter());
        Assert.assertEquals(DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss.SSS"), propertyFileUtil.getDateParser());
        Assert.assertEquals(new Integer(6), propertyFileUtil.getNumThreads());
        Assert.assertEquals(new Integer(20), propertyFileUtil.getWaitTimeMsecBetweenFilesInDirectory());
    }

    @Test
    public void testGetMethods_ArgumentPropertyOverrides() throws IOException {
        envVars.put("DATALOADER_USERNAME", "johnny.appleseed");
        envVars.put("DATALOADER_PASSWORD", "password456");
        envVars.put("DATALOADER_CLIENT_ID", "2234abcd-123a-123a-123a-acbd1234567");
        envVars.put("DATALOADER_CLIENT_SECRET", "2234567890abcdefghijklmn");
        envVars.put("DATALOADER_AUTHORIZE_URL", "https://auth.bullhornstaffing.com/oauth/apple");
        envVars.put("DATALOADER_TOKEN_URL", "https://auth.bullhornstaffing.com/oauth/banana");
        envVars.put("DATALOADER_LOGIN_URL", "https://rest.bullhornstaffing.com/rest-services/cherry");
        envVars.put("DATALOADER_LIST_DELIMITER", ",");
        envVars.put("DATALOADER_NUM_THREADS", "5");
        envVars.put("DATALOADER_WAIT_TIME_MSEC_BETWEEN_FILES_IN_DIRECTORY", "15");

        systemProperties.setProperty("username", "johnny.be-good");
        systemProperties.setProperty("password", "password789");
        systemProperties.setProperty("clientId", "2234abcd-123a-123a-123a-acbd1231111");
        systemProperties.setProperty("clientSecret", "2231111890abcdefghijklmn");
        systemProperties.setProperty("authorizeUrl", "https://auth.bullhornstaffing.com/oauth/chipmunk");
        systemProperties.setProperty("tokenUrl", "https://auth.bullhornstaffing.com/oauth/pony");
        systemProperties.setProperty("loginUrl", "https://rest.bullhornstaffing.com/rest-services/wallaby");
        systemProperties.setProperty("listDelimiter", "|");
        systemProperties.setProperty("numThreads", "6");
        systemProperties.setProperty("waitTimeMSecBetweenFilesInDirectory", "20");

        ArrayList<String> args = new ArrayList<>();
        args.add("username");
        args.add("johnny.mnemonic");
        args.add("-password");
        args.add("password000");
        args.add("--clientId");
        args.add("2234abcd-999a-999a-999a-acbd9999999");
        args.add("-clientSecret");
        args.add("2239999890abcdefghijklmn");
        args.add("AUTHORIZEURL");
        args.add("https://auth.bullhornstaffing.com/oauth/paris");
        args.add("TokenUrl");
        args.add("https://auth.bullhornstaffing.com/oauth/london");
        args.add("LOGIN_URL");
        args.add("https://rest.bullhornstaffing.com/rest-services/sydney");
        args.add("listdelimiter");
        args.add("&");
        args.add("-NUM_THREADS");
        args.add("7");
        args.add("-waitTimeMSecBetweenFilesInDirectory");
        args.add("25");
        String[] argsArray = args.toArray(new String[] {});

        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, argsArray, propertyValidation, printUtilMock);

        Assert.assertEquals("johnny.mnemonic", propertyFileUtil.getUsername());
        Assert.assertEquals("password000", propertyFileUtil.getPassword());
        Assert.assertEquals("2234abcd-999a-999a-999a-acbd9999999", propertyFileUtil.getClientId());
        Assert.assertEquals("2239999890abcdefghijklmn", propertyFileUtil.getClientSecret());
        Assert.assertEquals("https://auth.bullhornstaffing.com/oauth/paris", propertyFileUtil.getAuthorizeUrl());
        Assert.assertEquals("https://auth.bullhornstaffing.com/oauth/london", propertyFileUtil.getTokenUrl());
        Assert.assertEquals("https://rest.bullhornstaffing.com/rest-services/sydney", propertyFileUtil.getLoginUrl());
        Assert.assertEquals("&", propertyFileUtil.getListDelimiter());
        Assert.assertEquals(DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss.SSS"), propertyFileUtil.getDateParser());
        Assert.assertEquals(new Integer(7), propertyFileUtil.getNumThreads());
        Assert.assertEquals(new Integer(25), propertyFileUtil.getWaitTimeMsecBetweenFilesInDirectory());
    }

    @Test
    public void testGetEntityExistFields_PropertyFileValues() throws IOException {
        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, propertyValidation, printUtilMock);

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
    public void testGetEntityExistFields_EnvironmentVariableOverrides() throws IOException {
        envVars.put("DATALOADER_CANDIDATE_EXIST_FIELD", "customTextField4,customTextField5");
        envVars.put("dataloader_candidateExistField", "bogus");
        envVars.put("candidateExistField", "bogus");

        envVars.put("DATALOADER_Lead_Exist_Field", "customText99");
        envVars.put("Dataloader_Lead_Exist_Field", "bogus");

        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, propertyValidation, printUtilMock);

        Assert.assertEquals(Optional.ofNullable(Arrays.asList(new String[]{"customTextField4", "customTextField5"})),
            propertyFileUtil.getEntityExistFields("Candidate"));
        Assert.assertEquals(Optional.ofNullable(Arrays.asList(new String[]{"customText99"})),
            propertyFileUtil.getEntityExistFields("Lead"));
    }

    @Test
    public void testGetEntityExistFields_SystemPropertyOverrides() throws IOException {
        envVars.put("candidateExistField", "customTextField4,customTextField5");
        systemProperties.setProperty("candidateExistField", "one,two,buckle,shoe");

        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, propertyValidation, printUtilMock);

        Assert.assertEquals(Optional.ofNullable(Arrays.asList(new String[]{"one", "two", "buckle", "shoe"})),
            propertyFileUtil.getEntityExistFields("Candidate"));
    }

    @Test
    public void testGetEntityExistFields_ArgumentOverrides() throws IOException {
        envVars.put("candidateExistField", "customTextField4,customTextField5");
        systemProperties.setProperty("clientContactExistField", "one,two,buckle,shoe");
        ArrayList<String> args = new ArrayList<>();
        args.add("candidateExistField");
        args.add("externalID");
        String[] argsArray = args.toArray(new String[] {});

        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, argsArray, propertyValidation, printUtilMock);

        Assert.assertEquals(Optional.ofNullable(Arrays.asList(new String[]{"externalID"})),
            propertyFileUtil.getEntityExistFields("Candidate"));
    }

    @Test(expected = FileNotFoundException.class)
    public void testPropertyFileSystemPropertyOverride() throws IOException {
        systemProperties.setProperty("propertyfile", "bogus/file/path/to/dataloader.properties");
        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, propertyValidation, printUtilMock);
    }
}
