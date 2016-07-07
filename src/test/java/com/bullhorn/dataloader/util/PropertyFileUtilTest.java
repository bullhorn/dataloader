package com.bullhorn.dataloader.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

public class PropertyFileUtilTest {

    private PropertyFileUtil propertyFileUtil;

    @Before
    public void setUp() throws Exception {
        String path = getFilePath("PropertyFileUtilTest.properties");
        propertyFileUtil = new PropertyFileUtil(path);
    }

    @Test
    public void testGetters() throws Exception {
        Assert.assertEquals(propertyFileUtil.getUsername(), "john.smith");
        Assert.assertEquals(propertyFileUtil.getPassword(), "password123");
        Assert.assertEquals(propertyFileUtil.getClientId(), "1234abcd-123a-123a-123a-acbd1234567");
        Assert.assertEquals(propertyFileUtil.getClientSecret(), "1234567890abcdefghijklmn");
        Assert.assertEquals(propertyFileUtil.getAuthorizeUrl(), "https://auth.bullhornstaffing.com/oauth/authorize");
        Assert.assertEquals(propertyFileUtil.getTokenUrl(), "https://auth.bullhornstaffing.com/oauth/token");
        Assert.assertEquals(propertyFileUtil.getLoginUrl(), "https://rest.bullhornstaffing.com/rest-services/login");
        Assert.assertEquals(propertyFileUtil.getListDelimiter(), ";");
        Assert.assertEquals(propertyFileUtil.getDateParser(), new SimpleDateFormat("MM/dd/yyyy"));
        Assert.assertEquals(propertyFileUtil.getNumThreads(), new Integer(10));
        Assert.assertEquals(propertyFileUtil.getCacheSize(), new Integer(10000));
        Assert.assertEquals(propertyFileUtil.getPageSize(), new Integer(500));
    }

    @Test
    public void testExistsFields() throws Exception {
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
        Set<String> expected = Sets.newHashSet(new String[] {"BusinessSector", "Skill", "Category"});
        Assert.assertEquals(propertyFileUtil.getFrontLoadedEntities(), expected);

        Assert.assertEquals(propertyFileUtil.shouldFrontLoadEntity("BusinessSector"), true);
        Assert.assertEquals(propertyFileUtil.shouldFrontLoadEntity("businessSector"), false);
        Assert.assertEquals(propertyFileUtil.shouldFrontLoadEntity("BOGUS"), false);
    }

    private String getFilePath(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
    }
}
