package com.bullhorn.dataloader.util;

import static org.mockito.Mockito.mock;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.joda.time.format.DateTimeFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.enums.EntityInfo;

public class PropertyFileUtilTest {

    private String path;
    private Map<String, String> envVars;
    private Properties systemProperties;
    private String[] emptyArgs;
    private PrintUtil printUtilMock;

    @Before
    public void setup() {
        path = TestUtils.getResourceFilePath("unitTest.properties");
        envVars = new HashMap<>();
        systemProperties = new Properties();
        emptyArgs = new String[]{};
        printUtilMock = mock(PrintUtil.class);
    }

    @Test
    public void testGetConvertedAttachmentFileForCandidate() throws IOException {
        String expected = "convertedAttachments/Candidate/candidate-ext-1.html";

        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, printUtilMock);
        String actual = propertyFileUtil.getConvertedAttachmentFilepath(EntityInfo.CANDIDATE, "candidate-ext-1");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetConvertedAttachmentFileForClientContact() throws IOException {
        String expected = "convertedAttachments/ClientContact/12345.html";

        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, printUtilMock);
        String actual = propertyFileUtil.getConvertedAttachmentFilepath(EntityInfo.CLIENT_CONTACT, "12345");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGettersForPropertyFileValues() throws IOException {
        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, printUtilMock);

        Assert.assertEquals("john.smith", propertyFileUtil.getUsername());
        Assert.assertEquals("password123", propertyFileUtil.getPassword());
        Assert.assertEquals("1234abcd-123a-123a-123a-acbd1234567", propertyFileUtil.getClientId());
        Assert.assertEquals("1234567890abcdefghijklmn", propertyFileUtil.getClientSecret());
        Assert.assertEquals("https://auth.bullhornstaffing.com/oauth/authorize", propertyFileUtil.getAuthorizeUrl());
        Assert.assertEquals("https://auth.bullhornstaffing.com/oauth/token", propertyFileUtil.getTokenUrl());
        Assert.assertEquals("https://rest.bullhornstaffing.com/rest-services/login", propertyFileUtil.getLoginUrl());
        Assert.assertEquals(";", propertyFileUtil.getListDelimiter());
        Assert.assertEquals(DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss.SSS"), propertyFileUtil.getDateParser());
        Assert.assertEquals(Boolean.FALSE, propertyFileUtil.getProcessEmptyAssociations());
        Assert.assertEquals(Boolean.FALSE, propertyFileUtil.getSingleByteEncoding());
        Assert.assertEquals(Boolean.FALSE, propertyFileUtil.getSkipDuplicates());
        Assert.assertEquals(Boolean.FALSE, propertyFileUtil.getExecuteFormTriggers());
        Assert.assertEquals(Boolean.FALSE, propertyFileUtil.getWildcardMatching());
        Assert.assertNull(propertyFileUtil.getEntity());
        Assert.assertEquals(new Integer(10), propertyFileUtil.getNumThreads());
        Assert.assertEquals(Boolean.TRUE, propertyFileUtil.getCaching());
        Assert.assertEquals(new Integer(0), propertyFileUtil.getWaitSecondsBetweenFilesInDirectory());
        Assert.assertEquals(Boolean.TRUE, propertyFileUtil.getVerbose());
    }

    @Test
    public void testGettersForEnvironmentVariableOverrides() throws IOException {
        envVars.put("DATALOADER_USERNAME", "johnny.appleseed");
        envVars.put("DATALOADER_PASSWORD", "password456");
        envVars.put("DATALOADER_CLIENT_ID", "2234abcd-123a-123a-123a-acbd1234567");
        envVars.put("DATALOADER_CLIENT_SECRET", "2234567890abcdefghijklmn");
        envVars.put("DATALOADER_AUTHORIZE_URL", "https://auth.bullhornstaffing.com/oauth/apple");
        envVars.put("DATALOADER_TOKEN_URL", "https://auth.bullhornstaffing.com/oauth/banana");
        envVars.put("DATALOADER_LOGIN_URL", "https://rest.bullhornstaffing.com/rest-services/cherry");
        envVars.put("DATALOADER_LIST_DELIMITER", ",");
        envVars.put("DATALOADER_PROCESS_EMPTY_ASSOCIATIONS", "true");
        envVars.put("DATALOADER_SINGLE_BYTE_ENCODING", "TRUE");
        envVars.put("DATALOADER_SKIP_DUPLICATES", "TRUE");
        envVars.put("DATALOADER_WildCard_Matching", "TRUE");
        envVars.put("DATALOADER_ENTITY", "");
        envVars.put("DATALOADER_NUM_THREADS", "5");
        envVars.put("DATALOADER_CACHING", "false");
        envVars.put("DATALOADER_WAIT_SECONDS_BETWEEN_FILES_IN_DIRECTORY", "15");
        envVars.put("DATALOADER_VERBOSE", "FALSE");

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
        envVars.put("processEmptyAssociations", "false");
        envVars.put("dataloader_numThreads", "bogus");
        envVars.put("numThreads", "bogus");
        envVars.put("waitSecondsBetweenFilesInDirectory", "99999999");

        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, printUtilMock);

        Assert.assertEquals("johnny.appleseed", propertyFileUtil.getUsername());
        Assert.assertEquals("password456", propertyFileUtil.getPassword());
        Assert.assertEquals("2234abcd-123a-123a-123a-acbd1234567", propertyFileUtil.getClientId());
        Assert.assertEquals("2234567890abcdefghijklmn", propertyFileUtil.getClientSecret());
        Assert.assertEquals("https://auth.bullhornstaffing.com/oauth/apple", propertyFileUtil.getAuthorizeUrl());
        Assert.assertEquals("https://auth.bullhornstaffing.com/oauth/banana", propertyFileUtil.getTokenUrl());
        Assert.assertEquals("https://rest.bullhornstaffing.com/rest-services/cherry", propertyFileUtil.getLoginUrl());
        Assert.assertEquals(",", propertyFileUtil.getListDelimiter());
        Assert.assertEquals(DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss.SSS"), propertyFileUtil.getDateParser());
        Assert.assertEquals(Boolean.TRUE, propertyFileUtil.getProcessEmptyAssociations());
        Assert.assertEquals(Boolean.TRUE, propertyFileUtil.getSingleByteEncoding());
        Assert.assertEquals(Boolean.TRUE, propertyFileUtil.getSkipDuplicates());
        Assert.assertEquals(Boolean.FALSE, propertyFileUtil.getExecuteFormTriggers());
        Assert.assertEquals(Boolean.TRUE, propertyFileUtil.getWildcardMatching());
        Assert.assertNull(propertyFileUtil.getEntity());
        Assert.assertEquals(new Integer(5), propertyFileUtil.getNumThreads());
        Assert.assertEquals(Boolean.FALSE, propertyFileUtil.getCaching());
        Assert.assertEquals(new Integer(15), propertyFileUtil.getWaitSecondsBetweenFilesInDirectory());
        Assert.assertEquals(Boolean.FALSE, propertyFileUtil.getVerbose());
    }

    @Test
    public void testGettersForSystemPropertyOverrides() throws IOException {
        envVars.put("DATALOADER_USERNAME", "johnny.appleseed");
        envVars.put("DATALOADER_PASSWORD", "password456");
        envVars.put("DATALOADER_CLIENT_ID", "2234abcd-123a-123a-123a-acbd1234567");
        envVars.put("DATALOADER_CLIENT_SECRET", "2234567890abcdefghijklmn");
        envVars.put("DATALOADER_AUTHORIZE_URL", "https://auth.bullhornstaffing.com/oauth/apple");
        envVars.put("DATALOADER_TOKEN_URL", "https://auth.bullhornstaffing.com/oauth/banana");
        envVars.put("DATALOADER_LOGIN_URL", "https://rest.bullhornstaffing.com/rest-services/cherry");
        envVars.put("DATALOADER_LIST_DELIMITER", ",");
        envVars.put("DATALOADER_PROCESS_EMPTY_ASSOCIATIONS", "false");
        envVars.put("DATALOADER_SINGLE_BYTE_ENCODING", "TRUE");
        envVars.put("DATALOADER_wildcard_matching", "FALSE");
        envVars.put("DATALOADER_NUM_THREADS", "5");
        envVars.put("DATALOADER_CACHING", "false");
        envVars.put("DATALOADER_WAIT_SECONDS_BETWEEN_FILES_IN_DIRECTORY", "15");
        envVars.put("DATALOADER_VERBOSE", "FALSE");

        systemProperties.setProperty("username", "johnny.be-good");
        systemProperties.setProperty("password", "password789");
        systemProperties.setProperty("clientId", "2234abcd-123a-123a-123a-acbd1231111");
        systemProperties.setProperty("clientSecret", "2231111890abcdefghijklmn");
        systemProperties.setProperty("authorizeUrl", "https://auth.bullhornstaffing.com/oauth/chipmunk");
        systemProperties.setProperty("tokenUrl", "https://auth.bullhornstaffing.com/oauth/pony");
        systemProperties.setProperty("loginUrl", "https://rest.bullhornstaffing.com/rest-services/wallaby");
        systemProperties.setProperty("listDelimiter", "|");
        systemProperties.setProperty("processEmptyAssociations", "true");
        systemProperties.setProperty("singleByteEncoding", "false");
        systemProperties.setProperty("skipDuplicates", "true");
        systemProperties.setProperty("entity", "");
        systemProperties.setProperty("numThreads", "6");
        systemProperties.setProperty("caching", "true");
        systemProperties.setProperty("waitSecondsBetweenFilesInDirectory", "20");
        systemProperties.setProperty("verbose", "false");

        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, printUtilMock);

        Assert.assertEquals("johnny.be-good", propertyFileUtil.getUsername());
        Assert.assertEquals("password789", propertyFileUtil.getPassword());
        Assert.assertEquals("2234abcd-123a-123a-123a-acbd1231111", propertyFileUtil.getClientId());
        Assert.assertEquals("2231111890abcdefghijklmn", propertyFileUtil.getClientSecret());
        Assert.assertEquals("https://auth.bullhornstaffing.com/oauth/chipmunk", propertyFileUtil.getAuthorizeUrl());
        Assert.assertEquals("https://auth.bullhornstaffing.com/oauth/pony", propertyFileUtil.getTokenUrl());
        Assert.assertEquals("https://rest.bullhornstaffing.com/rest-services/wallaby", propertyFileUtil.getLoginUrl());
        Assert.assertEquals("|", propertyFileUtil.getListDelimiter());
        Assert.assertEquals(DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss.SSS"), propertyFileUtil.getDateParser());
        Assert.assertEquals(Boolean.TRUE, propertyFileUtil.getProcessEmptyAssociations());
        Assert.assertEquals(Boolean.FALSE, propertyFileUtil.getSingleByteEncoding());
        Assert.assertEquals(Boolean.TRUE, propertyFileUtil.getSkipDuplicates());
        Assert.assertEquals(Boolean.FALSE, propertyFileUtil.getExecuteFormTriggers());
        Assert.assertEquals(Boolean.FALSE, propertyFileUtil.getWildcardMatching());
        Assert.assertNull(propertyFileUtil.getEntity());
        Assert.assertEquals(new Integer(6), propertyFileUtil.getNumThreads());
        Assert.assertEquals(Boolean.TRUE, propertyFileUtil.getCaching());
        Assert.assertEquals(new Integer(20), propertyFileUtil.getWaitSecondsBetweenFilesInDirectory());
        Assert.assertEquals(Boolean.FALSE, propertyFileUtil.getResultsFileEnabled());
        Assert.assertEquals("./results.json", propertyFileUtil.getResultsFilePath());
        Assert.assertEquals(new Integer(500), propertyFileUtil.getResultsFileWriteIntervalMsec());
        Assert.assertEquals(Boolean.FALSE, propertyFileUtil.getVerbose());
    }

    @Test
    public void testGettersForArgumentPropertyOverrides() throws IOException {
        envVars.put("DATALOADER_USERNAME", "johnny.appleseed");
        envVars.put("DATALOADER_PASSWORD", "password456");
        envVars.put("DATALOADER_CLIENT_ID", "2234abcd-123a-123a-123a-acbd1234567");
        envVars.put("DATALOADER_CLIENT_SECRET", "2234567890abcdefghijklmn");
        envVars.put("DATALOADER_AUTHORIZE_URL", "https://auth.bullhornstaffing.com/oauth/apple");
        envVars.put("DATALOADER_TOKEN_URL", "https://auth.bullhornstaffing.com/oauth/banana");
        envVars.put("DATALOADER_LOGIN_URL", "https://rest.bullhornstaffing.com/rest-services/cherry");
        envVars.put("DATALOADER_LIST_DELIMITER", ",");
        envVars.put("DATALOADER_PROCESS_EMPTY_ASSOCIATIONS", "false");
        envVars.put("DATALOADER_SINGLE_BYTE_ENCODING", "TRUE");
        envVars.put("DATALOADER_wildcard_MATCHING", "TRUE");
        envVars.put("DATALOADER_NUM_THREADS", "5");
        envVars.put("DATALOADER_CACHING", "false");
        envVars.put("DATALOADER_WAIT_SECONDS_BETWEEN_FILES_IN_DIRECTORY", "15");
        envVars.put("DATALOADER_VERBOSE", "FALSE");

        systemProperties.setProperty("username", "johnny.be-good");
        systemProperties.setProperty("password", "password789");
        systemProperties.setProperty("clientId", "2234abcd-123a-123a-123a-acbd1231111");
        systemProperties.setProperty("clientSecret", "2231111890abcdefghijklmn");
        systemProperties.setProperty("authorizeUrl", "https://auth.bullhornstaffing.com/oauth/chipmunk");
        systemProperties.setProperty("tokenUrl", "https://auth.bullhornstaffing.com/oauth/pony");
        systemProperties.setProperty("loginUrl", "https://rest.bullhornstaffing.com/rest-services/wallaby");
        systemProperties.setProperty("listDelimiter", "|");
        systemProperties.setProperty("processEmptyAssociations", "true");
        systemProperties.setProperty("singleByteEncoding", "false");
        systemProperties.setProperty("numThreads", "6");
        systemProperties.setProperty("caching", "true");
        systemProperties.setProperty("waitSecondsBetweenFilesInDirectory", "20");
        systemProperties.setProperty("verbose", "false");

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
        args.add("processEmptyAssociations");
        args.add("false");
        args.add("singleByteEncoding");
        args.add("true");
        args.add("executeFormTriggers");
        args.add("true");
        args.add("entity");
        args.add("candidate");
        args.add("-NUM_THREADS");
        args.add("7");
        args.add("-caching");
        args.add("false");
        args.add("-waitSecondsBetweenFilesInDirectory");
        args.add("25");
        args.add("-resultsFileEnabled");
        args.add("true");
        args.add("-resultsFilepath");
        args.add("../results/output.json");
        args.add("-resultsFileWriteIntervalMSEC");
        args.add("100");
        args.add("--verbose");
        args.add("true");
        String[] argsArray = args.toArray(new String[]{});

        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, argsArray, printUtilMock);

        Assert.assertEquals("johnny.mnemonic", propertyFileUtil.getUsername());
        Assert.assertEquals("password000", propertyFileUtil.getPassword());
        Assert.assertEquals("2234abcd-999a-999a-999a-acbd9999999", propertyFileUtil.getClientId());
        Assert.assertEquals("2239999890abcdefghijklmn", propertyFileUtil.getClientSecret());
        Assert.assertEquals("https://auth.bullhornstaffing.com/oauth/paris", propertyFileUtil.getAuthorizeUrl());
        Assert.assertEquals("https://auth.bullhornstaffing.com/oauth/london", propertyFileUtil.getTokenUrl());
        Assert.assertEquals("https://rest.bullhornstaffing.com/rest-services/sydney", propertyFileUtil.getLoginUrl());
        Assert.assertEquals("&", propertyFileUtil.getListDelimiter());
        Assert.assertEquals(DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss.SSS"), propertyFileUtil.getDateParser());
        Assert.assertEquals(Boolean.FALSE, propertyFileUtil.getProcessEmptyAssociations());
        Assert.assertEquals(Boolean.TRUE, propertyFileUtil.getSingleByteEncoding());
        Assert.assertEquals(Boolean.TRUE, propertyFileUtil.getExecuteFormTriggers());
        Assert.assertEquals(Boolean.TRUE, propertyFileUtil.getWildcardMatching());
        Assert.assertEquals(EntityInfo.CANDIDATE, propertyFileUtil.getEntity());
        Assert.assertEquals(new Integer(7), propertyFileUtil.getNumThreads());
        Assert.assertEquals(Boolean.FALSE, propertyFileUtil.getCaching());
        Assert.assertEquals(new Integer(25), propertyFileUtil.getWaitSecondsBetweenFilesInDirectory());
        Assert.assertEquals(Boolean.TRUE, propertyFileUtil.getResultsFileEnabled());
        Assert.assertEquals("../results/output.json", propertyFileUtil.getResultsFilePath());
        Assert.assertEquals(new Integer(100), propertyFileUtil.getResultsFileWriteIntervalMsec());
        Assert.assertEquals(Boolean.TRUE, propertyFileUtil.getVerbose());
    }

    @Test
    public void testGetEntityExistFieldsPropertyFileValues() throws IOException {
        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, printUtilMock);

        Assert.assertEquals(Collections.singletonList("externalID"),
            propertyFileUtil.getEntityExistFields(EntityInfo.CANDIDATE));
        Assert.assertEquals(Collections.singletonList("externalID"),
            propertyFileUtil.getEntityExistFields(EntityInfo.CLIENT_CONTACT));
        Assert.assertEquals(Collections.singletonList("customText1"),
            propertyFileUtil.getEntityExistFields(EntityInfo.LEAD));
        Assert.assertEquals(Arrays.asList("title", "name"),
            propertyFileUtil.getEntityExistFields(EntityInfo.JOB_ORDER));

        Assert.assertTrue(propertyFileUtil.getEntityExistFields(EntityInfo.BUSINESS_SECTOR).isEmpty());
        Assert.assertTrue(propertyFileUtil.getEntityExistFields(EntityInfo.CATEGORY).isEmpty());
        Assert.assertTrue(propertyFileUtil.getEntityExistFields(EntityInfo.JOB_SUBMISSION_HISTORY).isEmpty());
    }

    @Test
    public void testGetEntityExistFieldsEnvironmentVariableOverrides() throws IOException {
        envVars.put("DATALOADER_CANDIDATE_EXIST_FIELD", "customTextField4,customTextField5");
        envVars.put("dataloader_candidateExistField", "bogus");
        envVars.put("candidateExistField", "bogus");

        envVars.put("DATALOADER_Lead_Exist_Field", "customText99");
        envVars.put("Dataloader_Lead_Exist_Field", "bogus");

        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, printUtilMock);

        Assert.assertEquals(Arrays.asList("customTextField4", "customTextField5"),
            propertyFileUtil.getEntityExistFields(EntityInfo.CANDIDATE));
        Assert.assertEquals(Collections.singletonList("customText99"),
            propertyFileUtil.getEntityExistFields(EntityInfo.LEAD));
    }

    @Test
    public void testGetEntityExistFieldsSystemPropertyOverrides() throws IOException {
        envVars.put("candidateExistField", "customTextField4,customTextField5");
        systemProperties.setProperty("candidateExistField", "one,two,buckle,shoe");

        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, printUtilMock);

        Assert.assertEquals(Arrays.asList("one", "two", "buckle", "shoe"),
            propertyFileUtil.getEntityExistFields(EntityInfo.CANDIDATE));
    }

    @Test
    public void testGetEntityExistFieldsArgumentOverrides() throws IOException {
        envVars.put("candidateExistField", "customTextField4,customTextField5");
        systemProperties.setProperty("clientContactExistField", "one,two,buckle,shoe");
        ArrayList<String> args = new ArrayList<>();
        args.add("candidateExistField");
        args.add("externalID");
        String[] argsArray = args.toArray(new String[]{});

        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, argsArray, printUtilMock);

        Assert.assertEquals(Collections.singletonList("externalID"),
            propertyFileUtil.getEntityExistFields(EntityInfo.CANDIDATE));
    }

    @Test
    public void testGetColumnNameMappingPropertyFileValues() throws IOException {
        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, printUtilMock);

        Assert.assertEquals("owner.id", propertyFileUtil.getColumnNameMapping("recruiterUserID"));
        Assert.assertEquals("owner.id", propertyFileUtil.getColumnNameMapping("userID"));
        Assert.assertEquals("clientContact.id", propertyFileUtil.getColumnNameMapping("clientUserID"));

        Assert.assertEquals("owner.id", propertyFileUtil.getColumnNameMapping("owner.id"));
        Assert.assertEquals("primarySkills.name", propertyFileUtil.getColumnNameMapping("primarySkills.name"));
        Assert.assertEquals("firstName", propertyFileUtil.getColumnNameMapping("firstName"));
    }

    @Test
    public void testGetColumnNameMappingEnvironmentVariableOverrides() throws IOException {
        envVars.put("DATALOADER_billingUserID_COLUMN", "billingUser.id");
        envVars.put("DATALOADER_statementUserID_Column", "billingUser.id");
        envVars.put("DATALOADER_sendingOwnerNameColumn", "reference.name");
        envVars.put("DATALOADER_City Location_Column", "address.city");
        envVars.put("Dataloader_Bogus_Column", "address.zip");
        envVars.put("bogus_COLUMN", "reference.name");

        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, printUtilMock);

        Assert.assertEquals("billingUser.id", propertyFileUtil.getColumnNameMapping("billingUserID"));
        Assert.assertEquals("billingUser.id", propertyFileUtil.getColumnNameMapping("statementUserID"));
        Assert.assertEquals("clientContact.id", propertyFileUtil.getColumnNameMapping("clientUserID"));
        Assert.assertEquals("owner.id", propertyFileUtil.getColumnNameMapping("userID"));
        Assert.assertEquals("address.city", propertyFileUtil.getColumnNameMapping("City Location"));
        Assert.assertEquals("bogus", propertyFileUtil.getColumnNameMapping("bogus"));
    }

    @Test
    public void testGetColumnNameMappingSystemPropertyOverrides() throws IOException {
        envVars.put("DATALOADER_billingUserID_COLUMN", "billingUser.id");
        envVars.put("DATALOADER_statementUserID_COLUMN", "billingUser.id");
        systemProperties.setProperty("oneColumn", "billingUser.id");
        systemProperties.setProperty("userIDColumn", "recruiter.name");

        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, printUtilMock);

        Assert.assertEquals("billingUser.id", propertyFileUtil.getColumnNameMapping("billingUserID"));
        Assert.assertEquals("billingUser.id", propertyFileUtil.getColumnNameMapping("one"));
        Assert.assertEquals("recruiter.name", propertyFileUtil.getColumnNameMapping("userID"));
    }

    @Test
    public void testGetColumnNameMappingArgumentOverrides() throws IOException {
        envVars.put("DATALOADER_billingUserID_COLUMN", "billingUser.id");
        systemProperties.setProperty("columnOneColumn", "job.title");
        ArrayList<String> args = new ArrayList<>();
        args.add("columnTwoColumn");
        args.add("address.state");
        args.add("userIDColumn");
        args.add("person.username");
        String[] argsArray = args.toArray(new String[]{});

        PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path, envVars, systemProperties, argsArray, printUtilMock);

        Assert.assertEquals("billingUser.id", propertyFileUtil.getColumnNameMapping("billingUserID"));
        Assert.assertEquals("job.title", propertyFileUtil.getColumnNameMapping("columnOne"));
        Assert.assertEquals("address.state", propertyFileUtil.getColumnNameMapping("columnTwo"));
        Assert.assertEquals("person.username", propertyFileUtil.getColumnNameMapping("userID"));
        Assert.assertEquals("address.address1", propertyFileUtil.getColumnNameMapping("address.address1"));
    }

    @Test(expected = FileNotFoundException.class)
    public void testPropertyFileSystemPropertyOverride() throws IOException {
        systemProperties.setProperty("propertyfile", "bogus/file/path/to/dataloader.properties");
        new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, printUtilMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadEntityOverride() throws IOException {
        systemProperties.setProperty("entity", "Cornidate");
        new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, printUtilMock);
    }

    @Test
    public void testInvalidDateFormat() throws IOException {
        IllegalArgumentException expectedException = new IllegalArgumentException(
            "Provided dateFormat is invalid: cannot convert: 'MM/dd/bogus HH:mm' to a valid date format. "
                + "Valid formats are specified here: http://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html");

        systemProperties.setProperty("dateFormat", "MM/dd/bogus HH:mm");

        IllegalArgumentException actualException = null;
        try {
            new PropertyFileUtil(path, envVars, systemProperties, emptyArgs, printUtilMock);
        } catch (IllegalArgumentException e) {
            actualException = e;
        }

        assert actualException != null;
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }
}
