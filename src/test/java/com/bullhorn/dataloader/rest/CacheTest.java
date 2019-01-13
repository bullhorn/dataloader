package com.bullhorn.dataloader.rest;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.ClientCorporation;
import com.bullhornsdk.data.model.entity.core.standard.Skill;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joda.time.format.DateTimeFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CacheTest {

    private Cache cache;
    private PropertyFileUtil propertyFileUtilMock;

    @Before
    public void setup() {
        cache = new Cache();
        propertyFileUtilMock = mock(PropertyFileUtil.class);

        String dateFormatString = "yyyy-MM-dd";
        when(propertyFileUtilMock.getDateParser()).thenReturn(DateTimeFormat.forPattern(dateFormatString));
        when(propertyFileUtilMock.getListDelimiter()).thenReturn(";");
        when(propertyFileUtilMock.getProcessEmptyAssociations()).thenReturn(Boolean.FALSE);
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList());
    }

    @Test
    public void testEntryNotPresent() throws Exception {
        EntityInfo entityInfo = EntityInfo.CANDIDATE;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("email"));
        Record record = TestUtils.createRecord(entityInfo, "firstName,lastName,email", "Foo,Bar,foo@bar.com",
            propertyFileUtilMock);

        List<BullhornEntity> actual = cache.getEntry(entityInfo, record.getEntityExistFields(), record.getFieldsParameter());

        Assert.assertNull(actual);
    }

    @Test
    public void testNoMatchDifferentEntities() throws Exception {
        // search/ClientContact?fields=id&query=name:"Foo Bar"
        // search/ClientCorporation?fields=id&query=name:"Foo Bar"
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("name"));
        Record contactRecord = TestUtils.createRecord(EntityInfo.CLIENT_CONTACT, "name", "Foo Bar", propertyFileUtilMock);
        Record companyRecord = TestUtils.createRecord(EntityInfo.CLIENT_CORPORATION, "name", "Foo Bar", propertyFileUtilMock);
        List<BullhornEntity> expectedContact = TestUtils.getConcreteList(ClientContact.class, 1);
        List<BullhornEntity> expectedCompany = TestUtils.getConcreteList(ClientCorporation.class, 1);

        cache.setEntry(EntityInfo.CLIENT_CONTACT, contactRecord.getEntityExistFields(), contactRecord.getFieldsParameter(), expectedContact);
        cache.setEntry(EntityInfo.CLIENT_CORPORATION, companyRecord.getEntityExistFields(), companyRecord.getFieldsParameter(), expectedCompany);
        List<BullhornEntity> actualContact = cache.getEntry(EntityInfo.CLIENT_CONTACT, contactRecord.getEntityExistFields(), contactRecord.getFieldsParameter());
        List<BullhornEntity> actualCompany = cache.getEntry(EntityInfo.CLIENT_CORPORATION, companyRecord.getEntityExistFields(), companyRecord.getFieldsParameter());
        List<BullhornEntity> actualPlacement = cache.getEntry(EntityInfo.PLACEMENT, companyRecord.getEntityExistFields(), companyRecord.getFieldsParameter());

        Assert.assertEquals(expectedContact, actualContact);
        Assert.assertEquals(expectedCompany, actualCompany);
        Assert.assertNull(actualPlacement);
    }

    @Test
    public void testNoMatchDifferentFieldsParameter() throws Exception {
        // search/Candidate?fields=firstName,lastName,email,status&query=status:"Active"
        // search/Candidate?fields=firstName,lastName,primarySkills,status&query=status:"Active"
        EntityInfo entityInfo = EntityInfo.CANDIDATE;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("status"));
        Record recordOne = TestUtils.createRecord(entityInfo, "firstName,lastName,email,status", "Foo,Bar,foo@bar.com,Active",
            propertyFileUtilMock);
        Record recordTwo = TestUtils.createRecord(entityInfo, "firstName,lastName,primarySkills,status", "Foo,Bar,Java,Active",
            propertyFileUtilMock);
        Candidate candidateOne = TestUtils.createEntity(entityInfo, "id,firstName,lastName,email", "101,Foo,Bar,foo@bar.com",
            propertyFileUtilMock);
        Candidate candidateTwo = TestUtils.createEntity(entityInfo, "firstName,lastName,primarySkills,status", "Foo,Bar,Java,Active",
            propertyFileUtilMock);
        List<BullhornEntity> expectedOne = TestUtils.getConcreteList(candidateOne);
        List<BullhornEntity> expectedTwo = TestUtils.getConcreteList(candidateTwo);

        cache.setEntry(entityInfo, recordOne.getEntityExistFields(), recordOne.getFieldsParameter(), expectedOne);
        cache.setEntry(entityInfo, recordTwo.getEntityExistFields(), recordTwo.getFieldsParameter(), expectedTwo);
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, recordOne.getEntityExistFields(), recordOne.getFieldsParameter());
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, recordTwo.getEntityExistFields(), recordTwo.getFieldsParameter());

        Assert.assertEquals(expectedOne, actualOne);
        Assert.assertEquals(expectedTwo, actualTwo);
        Assert.assertNotEquals(expectedOne, expectedTwo);
    }

    @Test
    public void testNoMatchDifferentExistFieldCriteria() throws Exception {
        // search/Candidate?fields=firstName,lastName,email,status&query=email:"foo@bar.com"
        // search/Candidate?fields=firstName,lastName,email,primarySkills&query=email:"foo@bar.com"
        EntityInfo entityInfo = EntityInfo.CANDIDATE;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("email"));
        Record recordOne = TestUtils.createRecord(entityInfo, "firstName,lastName,email,status", "Foo,Bar,foo@bar.com,Active",
            propertyFileUtilMock);
        Record recordTwo = TestUtils.createRecord(entityInfo, "firstName,lastName,email,status", "Foo,Bar,foo@bar.com,Active",
            propertyFileUtilMock);
        Set<String> recordTwoFields = recordTwo.getFieldsParameter();
        recordTwoFields.add("primarySkills");
        Candidate candidateOne = TestUtils.createEntity(entityInfo, "firstName,lastName,email,status", "Foo,Bar,foo@bar.com,Active",
            propertyFileUtilMock);
        Candidate candidateTwo = TestUtils.createEntity(entityInfo, "firstName,lastName,email,primarySkills", "Foo,Bar,foo@bar.com,Java;C++",
            propertyFileUtilMock);
        List<BullhornEntity> expectedOne = TestUtils.getConcreteList(candidateOne);
        List<BullhornEntity> expectedTwo = TestUtils.getConcreteList(candidateTwo);

        cache.setEntry(entityInfo, recordOne.getEntityExistFields(), recordOne.getFieldsParameter(), expectedOne);
        cache.setEntry(entityInfo, recordTwo.getEntityExistFields(), recordTwoFields, expectedTwo);
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, recordOne.getEntityExistFields(), recordOne.getFieldsParameter());
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, recordTwo.getEntityExistFields(), recordTwoFields);

        Assert.assertEquals(expectedOne, actualOne);
        Assert.assertEquals(expectedTwo, actualTwo);
        Assert.assertNotEquals(expectedOne, expectedTwo);
    }

    @Test
    public void testNoMatchDifferentExistFieldValues() throws Exception {
        // search/Candidate?fields=firstName,lastName,email,status&query=email:"foo@bar.com"
        // search/Candidate?fields=firstName,lastName,email,status&query=email:"bar@baz.net"
        EntityInfo entityInfo = EntityInfo.CANDIDATE;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("email"));
        Record recordOne = TestUtils.createRecord(entityInfo, "firstName,lastName,email,status", "Foo,Bar,foo@bar.com,Active",
            propertyFileUtilMock);
        Record recordTwo = TestUtils.createRecord(entityInfo, "firstName,lastName,email,status", "Foo,Bar,bar@baz.net,Active",
            propertyFileUtilMock);
        Candidate candidateOne = TestUtils.createEntity(entityInfo, "firstName,lastName,email,status", "Foo,Bar,foo@bar.com,Active",
            propertyFileUtilMock);
        Candidate candidateTwo = TestUtils.createEntity(entityInfo, "firstName,lastName,email,status", "Foo,Bar,bar@baz.net,Active",
            propertyFileUtilMock);
        List<BullhornEntity> expectedOne = TestUtils.getConcreteList(candidateOne);
        List<BullhornEntity> expectedTwo = TestUtils.getConcreteList(candidateTwo);

        cache.setEntry(entityInfo, recordOne.getEntityExistFields(), recordOne.getFieldsParameter(), expectedOne);
        cache.setEntry(entityInfo, recordTwo.getEntityExistFields(), recordTwo.getFieldsParameter(), expectedTwo);
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, recordOne.getEntityExistFields(), recordOne.getFieldsParameter());
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, recordTwo.getEntityExistFields(), recordTwo.getFieldsParameter());

        Assert.assertEquals(expectedOne, actualOne);
        Assert.assertEquals(expectedTwo, actualTwo);
        Assert.assertNotEquals(expectedOne, expectedTwo);
    }

    @Test
    public void testMatchSingleDirectField() throws Exception {
        // search/Candidate?fields=firstName,lastName,email,status&query=status:"Active"
        // search/Candidate?fields=firstName,lastName,email,status&query=status:"Active"
        EntityInfo entityInfo = EntityInfo.CANDIDATE;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("email"));
        Record recordOne = TestUtils.createRecord(entityInfo, "firstName,lastName,email,status", "Foo,Bar,foo@bar.com,Active",
            propertyFileUtilMock);
        Record recordTwo = TestUtils.createRecord(entityInfo, "firstName,lastName,email,status", "Foo,Bar,foo@bar.com,Active",
            propertyFileUtilMock);
        Candidate candidate = TestUtils.createEntity(entityInfo, "firstName,lastName,email,status", "Foo,Bar,foo@bar.com,Active",
            propertyFileUtilMock);
        List<BullhornEntity> expected = TestUtils.getConcreteList(candidate);

        cache.setEntry(entityInfo, recordOne.getEntityExistFields(), recordOne.getFieldsParameter(), expected);
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, recordOne.getEntityExistFields(), recordOne.getFieldsParameter());
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, recordTwo.getEntityExistFields(), recordTwo.getFieldsParameter());

        Assert.assertEquals(expected, actualOne);
        Assert.assertEquals(expected, actualTwo);
    }

    @Test
    public void testCompoundField() throws Exception {
        // search/Candidate?fields=name,address.city,address.state&query=address.state:"Missouri"
        // search/Candidate?fields=name,address.city,address.state&query=address.state:"Missouri"
        EntityInfo entityInfo = EntityInfo.CANDIDATE;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("address.state"));
        Record recordOne = TestUtils.createRecord(entityInfo, "name,address.city,address.state", "Foo,St. Louis,Missouri",
            propertyFileUtilMock);
        Record recordTwo = TestUtils.createRecord(entityInfo, "name,address.city,address.state", "Bar,Jefferson City,Missouri",
            propertyFileUtilMock);
        Candidate candidateOne = TestUtils.createEntity(entityInfo, "firstName,lastName,email,status", "Foo,Bar,foo@bar.com,Active",
            propertyFileUtilMock);
        List<BullhornEntity> expectedOne = TestUtils.getConcreteList(candidateOne);

        cache.setEntry(entityInfo, recordOne.getEntityExistFields(), recordOne.getFieldsParameter(), expectedOne);
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, recordOne.getEntityExistFields(), recordOne.getFieldsParameter());
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, recordTwo.getEntityExistFields(), recordTwo.getFieldsParameter());

        Assert.assertEquals(expectedOne, actualOne);
        Assert.assertEquals(expectedOne, actualTwo);
    }

    @Test
    public void testToManyFieldSingleInstance() throws Exception {
        // query/Skill?fields=id&query=name:"Java*"
        EntityInfo entityInfo = EntityInfo.SKILL;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("name"));
        Record record = TestUtils.createRecord(entityInfo, "id,name", "1001,Java*", propertyFileUtilMock);
        List<BullhornEntity> expected = TestUtils.getConcreteList(Skill.class, 1);

        cache.setEntry(entityInfo, record.getEntityExistFields(), record.getFieldsParameter(), expected);
        List<BullhornEntity> actual = cache.getEntry(entityInfo, record.getEntityExistFields(), record.getFieldsParameter());

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testToManyFieldMultipleOverlappingInstances() throws Exception {
        // query/Skill?fields=id&query=name:"Skill_1;Skill_2;Skill_3"
        // query/Skill?fields=id&query=name:"Skill_2"
        // query/Skill?fields=id&query=name:"Skill_1;Skill_3"
        EntityInfo entityInfo = EntityInfo.SKILL;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("name"));
        Field fieldOne = new Field(entityInfo, new Cell("name", "Skill_1;Skill_2;Skill_3"), false, propertyFileUtilMock.getDateParser());
        Field fieldTwo = new Field(entityInfo, new Cell("name", "Skill_2"), false, propertyFileUtilMock.getDateParser());
        Field fieldThree = new Field(entityInfo, new Cell("name", "Skill_1;Skill_3"), false, propertyFileUtilMock.getDateParser());
        BullhornEntity skillOne = TestUtils.createEntity(entityInfo, "id,name", "1001,Skill_1", propertyFileUtilMock);
        BullhornEntity skillTwo = TestUtils.createEntity(entityInfo, "id,name", "1002,Skill_2", propertyFileUtilMock);
        BullhornEntity skillThree = TestUtils.createEntity(entityInfo, "id,name", "1003,Skill_3", propertyFileUtilMock);
        List<BullhornEntity> expected = TestUtils.getConcreteList(skillOne, skillTwo, skillThree);

        cache.setEntry(entityInfo, Lists.newArrayList(fieldOne), Sets.newHashSet(StringConsts.ID), expected);
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, Lists.newArrayList(fieldOne), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, Lists.newArrayList(fieldTwo), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualThree = cache.getEntry(entityInfo, Lists.newArrayList(fieldThree), Sets.newHashSet(StringConsts.ID));

        Assert.assertEquals(expected, actualOne);
        // TODO: Split up multiple values in the search field in order to allow these queries to bring back cached values:
        //  Assert.assertEquals(actualTwo, Lists.newArrayList(skillTwo));
        //  Assert.assertEquals(actualThree, Lists.newArrayList(skillOne, skillThree));
    }
}
