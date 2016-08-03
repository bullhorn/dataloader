package com.bullhorn.dataloader.util;

import com.google.common.collect.Sets;
import org.joda.time.format.DateTimeFormat;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class PropertyFileUtilTest {

    @Test
    public void testGetters() throws Exception {
        final String path = getFilePath("PropertyFileUtilTest.properties");
        final PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path);

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
        Assert.assertEquals(propertyFileUtil.getCacheSize(), new Integer(10000));
        Assert.assertEquals(propertyFileUtil.getPageSize(), new Integer(500));
    }

    @Test
    public void testExistsFields() throws Exception {
        final String path = getFilePath("PropertyFileUtilTest.properties");
        final PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path);

        Assert.assertEquals(propertyFileUtil.getEntityExistFields("BusinessSector"),
                Optional.ofNullable(Arrays.asList(new String[] {"name"})));
        Assert.assertEquals(propertyFileUtil.getEntityExistFields("Candidate"),
                Optional.ofNullable(Arrays.asList(new String[] {"id"})));
        Assert.assertEquals(propertyFileUtil.getEntityExistFields("Category"),
                Optional.ofNullable(Arrays.asList(new String[] {"occupation"})));
        Assert.assertEquals(propertyFileUtil.getEntityExistFields("JobOrder"),
                Optional.ofNullable(Arrays.asList(new String[] {"title", "name"})));

        Assert.assertFalse(propertyFileUtil.getEntityExistFields("businessSector").isPresent());
        Assert.assertFalse(propertyFileUtil.getEntityExistFields("CandidateName").isPresent());
        Assert.assertFalse(propertyFileUtil.getEntityExistFields("BOGUS").isPresent());
    }

    @Test
    public void testFrontLoadedEntities() throws Exception {
        final String path = getFilePath("PropertyFileUtilTest.properties");
        final PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path);

        Set<String> expected = Sets.newHashSet(new String[] {"BusinessSector", "Skill", "Category"});
        Assert.assertEquals(propertyFileUtil.getFrontLoadedEntities(), expected);

        Assert.assertEquals(propertyFileUtil.shouldFrontLoadEntity("BusinessSector"), true);
        Assert.assertEquals(propertyFileUtil.shouldFrontLoadEntity("businessSector"), false);
        Assert.assertEquals(propertyFileUtil.shouldFrontLoadEntity("BOGUS"), false);
    }

    @Test
    public void testFrontLoadedEntities_Empty() throws Exception {
        final String path = getFilePath("PropertyFileUtilTest_Empty.properties");
        final PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path);

        Assert.assertEquals(0, propertyFileUtil.getFrontLoadedEntities().size());
    }

    private String getFilePath(String filename) {
        final ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
    }
}
