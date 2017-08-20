package com.bullhorn.dataloader.rest;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.google.common.collect.Lists;
import org.joda.time.format.DateTimeFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecordTest {

    private PropertyFileUtil propertyFileUtilMock;

    @Before
    public void setup() {
        propertyFileUtilMock = mock(PropertyFileUtil.class);

        String dateFormatString = "yyyy-MM-dd";
        when(propertyFileUtilMock.getDateParser()).thenReturn(DateTimeFormat.forPattern(dateFormatString));
        when(propertyFileUtilMock.getListDelimiter()).thenReturn(";");
    }

    @Test
    public void testGetters() throws IOException {
        Row row = TestUtils.createRow(
            "externalID,firstName,lastName,name,email,primarySkills.id",
            "ext-1,Data,Loader,Data Loader,dloader@example.com,1;2;3");

        Record record = new Record(EntityInfo.CANDIDATE, row, propertyFileUtilMock);

        Assert.assertEquals(EntityInfo.CANDIDATE, record.getEntityInfo());
        Assert.assertEquals(new Integer(1), record.getNumber());
        Assert.assertEquals(6, record.getFields().size());
    }

    @Test
    public void testGetExistFieldsValid() throws IOException {
        Row row = TestUtils.createRow(
            "externalID,firstName,lastName,name,email,primarySkills.id",
            "ext-1,Data,Loader,Data Loader,dloader@example.com,1;2;3");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE))
            .thenReturn(Arrays.asList("firstName", "lastName", "email"));

        Record record = new Record(EntityInfo.CANDIDATE, row, propertyFileUtilMock);

        Assert.assertEquals(6, record.getFields().size());
        Assert.assertEquals(3, record.getEntityExistFields().size());
    }

    @Test
    public void testGetExistFieldsMissing() throws IOException {
        Row row = TestUtils.createRow(
            "externalID,firstName,lastName,name,email,primarySkills.id",
            "ext-1,Data,Loader,Data Loader,dloader@example.com,1;2;3");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE))
            .thenReturn(Arrays.asList("customText1", "customInt1", "customDate1"));

        Record record = new Record(EntityInfo.CANDIDATE, row, propertyFileUtilMock);

        Assert.assertEquals(6, record.getFields().size());
        Assert.assertEquals(0, record.getEntityExistFields().size());
    }

    @Test
    public void testGetExistFieldsNotConfigured() throws IOException {
        Row row = TestUtils.createRow(
            "externalID,firstName,lastName,name,email,primarySkills.id",
            "ext-1,Data,Loader,Data Loader,dloader@example.com,1;2;3");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE)).thenReturn(Lists.newArrayList());

        Record record = new Record(EntityInfo.CANDIDATE, row, propertyFileUtilMock);

        Assert.assertEquals(6, record.getFields().size());
        Assert.assertEquals(0, record.getEntityExistFields().size());
    }
}