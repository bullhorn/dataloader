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
        propertyFileUtilMock = mock(PropertyFileUtil.class);
        cache = new Cache(propertyFileUtilMock);

        String dateFormatString = "yyyy-MM-dd";
        when(propertyFileUtilMock.getDateParser()).thenReturn(DateTimeFormat.forPattern(dateFormatString));
        when(propertyFileUtilMock.getListDelimiter()).thenReturn(";");
        when(propertyFileUtilMock.getProcessEmptyAssociations()).thenReturn(Boolean.FALSE);
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList());
        when(propertyFileUtilMock.getWildcardMatching()).thenReturn(false);
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
    public void testCandidateByExternalID() throws Exception {
        // search/Candidate?fields=id&query=externalID:"candidate-ext-101"
        // search/Candidate?fields=id&query=externalID:"candidate-ext-102"
        // search/Candidate?fields=id&query=externalID:"candidate-ext-101"
        EntityInfo entityInfo = EntityInfo.CANDIDATE;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("externalID"));
        Record recordOne = TestUtils.createRecord(entityInfo, "externalID", "candidate-ext-101", propertyFileUtilMock);
        Record recordTwo = TestUtils.createRecord(entityInfo, "externalID", "candidate-ext-102", propertyFileUtilMock);
        Candidate candidateOne = TestUtils.createEntity(entityInfo, "id,externalID", "101,candidate-ext-101", propertyFileUtilMock);
        Candidate candidateTwo = TestUtils.createEntity(entityInfo, "id,externalID", "102,candidate-ext-102", propertyFileUtilMock);
        List<BullhornEntity> expectedOne = TestUtils.getConcreteList(candidateOne);
        List<BullhornEntity> expectedTwo = TestUtils.getConcreteList(candidateTwo);

        cache.setEntry(entityInfo, recordOne.getEntityExistFields(), Sets.newHashSet(StringConsts.ID), expectedOne);
        cache.setEntry(entityInfo, recordTwo.getEntityExistFields(), Sets.newHashSet(StringConsts.ID), expectedTwo);
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, recordOne.getEntityExistFields(), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, recordTwo.getEntityExistFields(), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualThree = cache.getEntry(entityInfo, recordOne.getEntityExistFields(), Sets.newHashSet(StringConsts.ID));

        Assert.assertEquals(expectedOne, actualOne);
        Assert.assertEquals(expectedTwo, actualTwo);
        Assert.assertEquals(expectedOne, actualThree);
        Assert.assertNotEquals(actualOne, actualTwo);
    }

    @Test
    public void testCandidateByFirstNameLastNameEmail() throws Exception {
        // search/Candidate?fields=id&query=firstName:(+bill) AND lastName:(+horn) AND email:(+bill@b.com)
        // search/Candidate?fields=id&query=firstName:(+bob) AND lastName:(+horn) AND email:(+bob@b.com)
        // search/Candidate?fields=id&query=firstName:(+bill) AND lastName:(+horn) AND email:(+bill@b.com)
        EntityInfo entityInfo = EntityInfo.CANDIDATE;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("firstName", "lastName", "email"));
        Record recordOne = TestUtils.createRecord(entityInfo, "firstName,lastName,email", "bill,horn,bill@b.com", propertyFileUtilMock);
        Record recordTwo = TestUtils.createRecord(entityInfo, "firstName,lastName,email", "bob,horn,bob@b.com", propertyFileUtilMock);
        Candidate candidateOne = TestUtils.createEntity(entityInfo, "id,firstName,lastName,email", "1,bill,horn,bill@b.com", propertyFileUtilMock);
        Candidate candidateTwo = TestUtils.createEntity(entityInfo, "id,firstName,lastName,email", "2,bob,horn,bob@b.com", propertyFileUtilMock);
        List<BullhornEntity> expectedOne = TestUtils.getConcreteList(candidateOne);
        List<BullhornEntity> expectedTwo = TestUtils.getConcreteList(candidateTwo);

        cache.setEntry(entityInfo, recordOne.getEntityExistFields(), Sets.newHashSet(StringConsts.ID), expectedOne);
        cache.setEntry(entityInfo, recordTwo.getEntityExistFields(), Sets.newHashSet(StringConsts.ID), expectedTwo);
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, recordOne.getEntityExistFields(), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, recordTwo.getEntityExistFields(), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualThree = cache.getEntry(entityInfo, recordOne.getEntityExistFields(), Sets.newHashSet(StringConsts.ID));

        Assert.assertEquals(expectedOne, actualOne);
        Assert.assertEquals(expectedTwo, actualTwo);
        Assert.assertEquals(expectedOne, actualThree);
        Assert.assertNotEquals(actualOne, actualTwo);
    }

    @Test
    public void testBadExistField() throws Exception {
        // search/Candidate?fields=id&query=externalID:"candidate-ext-101"
        // search/Candidate?fields=id&query=externalID:"candidate-ext-101"
        EntityInfo entityInfo = EntityInfo.CANDIDATE;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("bogus"));
        Record record = TestUtils.createRecord(entityInfo, "externalID", "candidate-ext-101", propertyFileUtilMock);
        Candidate candidate = TestUtils.createEntity(entityInfo, "id,externalID", "101,candidate-ext-101", propertyFileUtilMock);

        cache.setEntry(entityInfo, record.getEntityExistFields(), Sets.newHashSet(StringConsts.ID), TestUtils.getConcreteList(candidate));
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, record.getEntityExistFields(), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, record.getEntityExistFields(), Sets.newHashSet(StringConsts.ID));

        Assert.assertNull(actualOne);
        Assert.assertNull(actualTwo);
    }

    @Test
    public void testNoExistField() throws Exception {
        // search/Candidate?fields=id&query=externalID:"candidate-ext-101"
        // search/Candidate?fields=id&query=externalID:"candidate-ext-101"
        EntityInfo entityInfo = EntityInfo.CANDIDATE;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList());
        Record record = TestUtils.createRecord(entityInfo, "externalID", "candidate-ext-101", propertyFileUtilMock);
        Candidate candidate = TestUtils.createEntity(entityInfo, "id,externalID", "101,candidate-ext-101", propertyFileUtilMock);

        cache.setEntry(entityInfo, record.getEntityExistFields(), Sets.newHashSet(StringConsts.ID), TestUtils.getConcreteList(candidate));
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, record.getEntityExistFields(), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, record.getEntityExistFields(), Sets.newHashSet(StringConsts.ID));

        Assert.assertNull(actualOne);
        Assert.assertNull(actualTwo);
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
    public void testNoMatchDifferentExistFieldValuesMultipleExistFields() throws Exception {
        // search/Candidate?fields=firstName,lastName,email,status&query=email:"foo@bar.com"
        // search/Candidate?fields=firstName,lastName,email,status&query=email:"bar@baz.net"
        EntityInfo entityInfo = EntityInfo.CANDIDATE;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("lastName", "email"));
        Record recordOne = TestUtils.createRecord(entityInfo, "firstName,lastName,email,status", "Foo,Bar,foo@bar.com,Active",
            propertyFileUtilMock);
        Record recordTwo = TestUtils.createRecord(entityInfo, "firstName,lastName,email,status", "Foo,Bar,bar@baz.net,Active",
            propertyFileUtilMock);
        Candidate candidateOne = TestUtils.createEntity(entityInfo, "firstName,lastName,email,status", "Foo,Bar,foo@bar.com,Active",
            propertyFileUtilMock);
        Candidate candidateTwo = TestUtils.createEntity(entityInfo, "firstName,lastName,email,status", "Foo,Bar,bar@baz.net,Active",
            propertyFileUtilMock);
        List<BullhornEntity> expectedOne = TestUtils.getConcreteList(candidateOne);

        cache.setEntry(entityInfo, recordOne.getEntityExistFields(), recordOne.getFieldsParameter(), expectedOne);
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, recordOne.getEntityExistFields(), recordOne.getFieldsParameter());
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, recordTwo.getEntityExistFields(), recordTwo.getFieldsParameter());

        Assert.assertEquals(expectedOne, actualOne);
        Assert.assertNull(actualTwo);
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
    public void testToManyFieldReorder() throws Exception {
        // query/Skill?fields=id&query=name:"Skill_1;Skill_2;Skill_3" -> makes call
        // query/Skill?fields=id&query=name:"Skill_2;Skill_1;Skill_3" -> should reuse results
        // query/Skill?fields=id&query=name:"Skill_3;Skill_2;Skill_1" -> should reuse results
        EntityInfo entityInfo = EntityInfo.SKILL;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("name"));
        Field fieldOne = new Field(entityInfo, new Cell("name", "Skill_1;Skill_2;Skill_3"), false, propertyFileUtilMock.getDateParser());
        Field fieldTwo = new Field(entityInfo, new Cell("name", "Skill_2;Skill_3;Skill_1"), false, propertyFileUtilMock.getDateParser());
        Field fieldThree = new Field(entityInfo, new Cell("name", "Skill_3;Skill_2;Skill_1"), false, propertyFileUtilMock.getDateParser());
        BullhornEntity skillOne = TestUtils.createEntity(entityInfo, "id,name", "1001,Skill_1", propertyFileUtilMock);
        BullhornEntity skillTwo = TestUtils.createEntity(entityInfo, "id,name", "1002,Skill_2", propertyFileUtilMock);
        BullhornEntity skillThree = TestUtils.createEntity(entityInfo, "id,name", "1003,Skill_3", propertyFileUtilMock);

        cache.setEntry(entityInfo, Lists.newArrayList(fieldOne), Sets.newHashSet(StringConsts.ID),
            TestUtils.getConcreteList(skillOne, skillTwo, skillThree));
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, Lists.newArrayList(fieldOne), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, Lists.newArrayList(fieldTwo), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualThree = cache.getEntry(entityInfo, Lists.newArrayList(fieldThree), Sets.newHashSet(StringConsts.ID));

        Assert.assertEquals(Lists.newArrayList(skillOne, skillTwo, skillThree), actualOne);
        Assert.assertEquals(Lists.newArrayList(skillOne, skillTwo, skillThree), actualTwo);
        Assert.assertEquals(Lists.newArrayList(skillOne, skillTwo, skillThree), actualThree);
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

        cache.setEntry(entityInfo, Lists.newArrayList(fieldOne), Sets.newHashSet(StringConsts.ID),
            TestUtils.getConcreteList(skillOne, skillTwo, skillThree));
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, Lists.newArrayList(fieldOne), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, Lists.newArrayList(fieldTwo), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualThree = cache.getEntry(entityInfo, Lists.newArrayList(fieldThree), Sets.newHashSet(StringConsts.ID));

        Assert.assertEquals(Lists.newArrayList(skillOne, skillTwo, skillThree), actualOne);
        Assert.assertEquals(Lists.newArrayList(skillTwo), actualTwo);
        Assert.assertEquals(Lists.newArrayList(skillOne, skillThree), actualThree);
    }

    @Test
    public void testToManyFieldMultipleOverlappingInstancesMissingSearchValues() throws Exception {
        // query/Skill?fields=id&query=name:"Skill_1;Skill_2;Skill_3" -> Name is not returned, only id, so we cannot determine 1:1 map to entity
        // query/Skill?fields=id&query=name:"Skill_2"                 -> We do not have information to map to individual value
        // query/Skill?fields=id&query=name:"Skill_1;Skill_3"         -> We do not have information to map to individual value
        EntityInfo entityInfo = EntityInfo.SKILL;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("name"));
        Field fieldOne = new Field(entityInfo, new Cell("name", "Skill_1;Skill_2;Skill_3"), false, propertyFileUtilMock.getDateParser());
        Field fieldTwo = new Field(entityInfo, new Cell("name", "Skill_2"), false, propertyFileUtilMock.getDateParser());
        Field fieldThree = new Field(entityInfo, new Cell("name", "Skill_1;Skill_3"), false, propertyFileUtilMock.getDateParser());
        BullhornEntity skillOne = TestUtils.createEntity(entityInfo, "id", "1001", propertyFileUtilMock);
        BullhornEntity skillTwo = TestUtils.createEntity(entityInfo, "id", "1002", propertyFileUtilMock);
        BullhornEntity skillThree = TestUtils.createEntity(entityInfo, "id", "1003", propertyFileUtilMock);

        cache.setEntry(entityInfo, Lists.newArrayList(fieldOne), Sets.newHashSet(StringConsts.ID),
            TestUtils.getConcreteList(skillOne, skillTwo, skillThree));
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, Lists.newArrayList(fieldOne), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, Lists.newArrayList(fieldTwo), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualThree = cache.getEntry(entityInfo, Lists.newArrayList(fieldThree), Sets.newHashSet(StringConsts.ID));

        Assert.assertEquals(Lists.newArrayList(skillOne, skillTwo, skillThree), actualOne);
        Assert.assertNull(actualTwo);
        Assert.assertNull(actualThree);
    }

    @Test
    public void testToManyFieldAggregateInstances() throws Exception {
        // query/Skill?fields=id&query=name:"Skill_1"
        // query/Skill?fields=id&query=name:"Skill_2"
        // query/Skill?fields=id&query=name:"Skill_1;Skill_2"
        EntityInfo entityInfo = EntityInfo.SKILL;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("name"));
        Field fieldOne = new Field(entityInfo, new Cell("name", "Skill_1"), false, propertyFileUtilMock.getDateParser());
        Field fieldTwo = new Field(entityInfo, new Cell("name", "Skill_2"), false, propertyFileUtilMock.getDateParser());
        Field fieldThree = new Field(entityInfo, new Cell("name", "Skill_1;Skill_2"), false, propertyFileUtilMock.getDateParser());
        BullhornEntity skillOne = TestUtils.createEntity(entityInfo, "id,name", "1001,Skill_1", propertyFileUtilMock);
        BullhornEntity skillTwo = TestUtils.createEntity(entityInfo, "id,name", "1002,Skill_2", propertyFileUtilMock);

        cache.setEntry(entityInfo, Lists.newArrayList(fieldOne), Sets.newHashSet(StringConsts.ID), TestUtils.getConcreteList(skillOne));
        cache.setEntry(entityInfo, Lists.newArrayList(fieldTwo), Sets.newHashSet(StringConsts.ID), TestUtils.getConcreteList(skillTwo));
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, Lists.newArrayList(fieldOne), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, Lists.newArrayList(fieldTwo), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualThree = cache.getEntry(entityInfo, Lists.newArrayList(fieldThree), Sets.newHashSet(StringConsts.ID));

        Assert.assertEquals(Lists.newArrayList(skillOne), actualOne);
        Assert.assertEquals(Lists.newArrayList(skillTwo), actualTwo);
        Assert.assertEquals(Lists.newArrayList(skillOne, skillTwo), actualThree);
    }

    @Test
    public void testToManyFieldMultipleOverlappingInstancesWithDuplicate() throws Exception {
        // query/Skill?fields=id&query=name:"Skill_1"
        // query/Skill?fields=id&query=name:"Skill_1;Skill_2"
        // query/Skill?fields=id&query=name:"Skill_2"
        EntityInfo entityInfo = EntityInfo.SKILL;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("name"));
        Field fieldOne = new Field(entityInfo, new Cell("name", "Skill_1"), false, propertyFileUtilMock.getDateParser());
        Field fieldTwo = new Field(entityInfo, new Cell("name", "Skill_1;Skill_2"), false, propertyFileUtilMock.getDateParser());
        Field fieldThree = new Field(entityInfo, new Cell("name", "Skill_2"), false, propertyFileUtilMock.getDateParser());
        BullhornEntity skillOne = TestUtils.createEntity(entityInfo, "id,name", "1001,Skill_1", propertyFileUtilMock);
        BullhornEntity skillOneDupe = TestUtils.createEntity(entityInfo, "id,name", "9999,Skill_1", propertyFileUtilMock);
        BullhornEntity skillTwo = TestUtils.createEntity(entityInfo, "id,name", "1002,Skill_2", propertyFileUtilMock);

        cache.setEntry(entityInfo, Lists.newArrayList(fieldOne), Sets.newHashSet(StringConsts.ID),
            TestUtils.getConcreteList(skillOne, skillOneDupe));
        cache.setEntry(entityInfo, Lists.newArrayList(fieldTwo), Sets.newHashSet(StringConsts.ID),
            TestUtils.getConcreteList(skillOne, skillOneDupe, skillTwo));
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, Lists.newArrayList(fieldOne), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, Lists.newArrayList(fieldTwo), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualThree = cache.getEntry(entityInfo, Lists.newArrayList(fieldThree), Sets.newHashSet(StringConsts.ID));

        Assert.assertEquals(Lists.newArrayList(skillOne, skillOneDupe), actualOne);
        Assert.assertEquals(Lists.newArrayList(skillOne, skillOneDupe, skillTwo), actualTwo);
        Assert.assertEquals(Lists.newArrayList(skillTwo), actualThree);
    }

    @Test
    public void testToManyFieldMultipleOverlappingInstancesMissingRecords() throws Exception {
        // query/Skill?fields=id&query=name:"Skill_1;Skill_2;Skill_3" -> Skill2 is not returned
        // query/Skill?fields=id&query=name:"Skill_1;Skill_2"         -> Return is null, since Skill2 does not exist
        // query/Skill?fields=id&query=name:"Skill_1"                 -> Skill1 is returned
        EntityInfo entityInfo = EntityInfo.SKILL;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("name"));
        Field fieldOne = new Field(entityInfo, new Cell("name", "Skill_1;Skill_2;Skill_3"), false, propertyFileUtilMock.getDateParser());
        Field fieldTwo = new Field(entityInfo, new Cell("name", "Skill_1;Skill_2"), false, propertyFileUtilMock.getDateParser());
        Field fieldThree = new Field(entityInfo, new Cell("name", "Skill_1"), false, propertyFileUtilMock.getDateParser());
        BullhornEntity skillOne = TestUtils.createEntity(entityInfo, "id,name", "1001,Skill_1", propertyFileUtilMock);
        BullhornEntity skillThree = TestUtils.createEntity(entityInfo, "id,name", "1003,Skill_3", propertyFileUtilMock);

        cache.setEntry(entityInfo, Lists.newArrayList(fieldOne), Sets.newHashSet(StringConsts.ID), TestUtils.getConcreteList(skillOne, skillThree));
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, Lists.newArrayList(fieldOne), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, Lists.newArrayList(fieldTwo), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualThree = cache.getEntry(entityInfo, Lists.newArrayList(fieldThree), Sets.newHashSet(StringConsts.ID));

        Assert.assertEquals(Lists.newArrayList(skillOne, skillThree), actualOne);
        Assert.assertNull(actualTwo);
        Assert.assertEquals(Lists.newArrayList(skillOne), actualThree);
    }

    @Test
    public void testAsteriskNotWildcard() throws Exception {
        // search/Candidate?fields=id&query=externalID:"candidate-ext-101*"
        // search/Candidate?fields=id&query=externalID:"candidate-ext-102*"
        // search/Candidate?fields=id&query=externalID:"candidate-ext-101*"
        EntityInfo entityInfo = EntityInfo.CANDIDATE;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("externalID"));
        when(propertyFileUtilMock.getWildcardMatching()).thenReturn(false);
        Record recordOne = TestUtils.createRecord(entityInfo, "externalID", "candidate-ext-101*", propertyFileUtilMock);
        Record recordTwo = TestUtils.createRecord(entityInfo, "externalID", "candidate-ext-102*", propertyFileUtilMock);
        Candidate candidateOne = TestUtils.createEntity(entityInfo, "id,externalID", "101,candidate-ext-101*", propertyFileUtilMock);
        Candidate candidateTwo = TestUtils.createEntity(entityInfo, "id,externalID", "102,candidate-ext-102*", propertyFileUtilMock);
        List<BullhornEntity> expectedOne = TestUtils.getConcreteList(candidateOne);
        List<BullhornEntity> expectedTwo = TestUtils.getConcreteList(candidateTwo);

        cache.setEntry(entityInfo, recordOne.getEntityExistFields(), Sets.newHashSet(StringConsts.ID), expectedOne);
        cache.setEntry(entityInfo, recordTwo.getEntityExistFields(), Sets.newHashSet(StringConsts.ID), expectedTwo);
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, recordOne.getEntityExistFields(), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, recordTwo.getEntityExistFields(), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualThree = cache.getEntry(entityInfo, recordOne.getEntityExistFields(), Sets.newHashSet(StringConsts.ID));

        Assert.assertEquals(expectedOne, actualOne);
        Assert.assertEquals(expectedTwo, actualTwo);
        Assert.assertEquals(expectedOne, actualThree);
        Assert.assertNotEquals(actualOne, actualTwo);
    }

    @Test
    public void testWildcardExclusion() throws Exception {
        // query/Skill?fields=id&query=name:"Java*;Angular*"
        // query/Skill?fields=id&query=name:"Javascript"
        // query/Skill?fields=id&query=name:"AngularJS"
        // query/Skill?fields=id&query=name:"Java*"
        EntityInfo entityInfo = EntityInfo.SKILL;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("name"));
        when(propertyFileUtilMock.getWildcardMatching()).thenReturn(true);
        Field fieldOne = new Field(entityInfo, new Cell("name", "Java*;Angular*"), false, propertyFileUtilMock.getDateParser());
        Field fieldTwo = new Field(entityInfo, new Cell("name", "Javascript"), false, propertyFileUtilMock.getDateParser());
        Field fieldThree = new Field(entityInfo, new Cell("name", "AngularJS"), false, propertyFileUtilMock.getDateParser());
        Field fieldFour = new Field(entityInfo, new Cell("name", "Java*"), false, propertyFileUtilMock.getDateParser());
        BullhornEntity skillJava = TestUtils.createEntity(entityInfo, "id,name", "1001,Java", propertyFileUtilMock);
        BullhornEntity skillJavascript = TestUtils.createEntity(entityInfo, "id,name", "1002,Javascript", propertyFileUtilMock);
        BullhornEntity skillAngular = TestUtils.createEntity(entityInfo, "id,name", "1003,Angular", propertyFileUtilMock);
        BullhornEntity skillAngularJS = TestUtils.createEntity(entityInfo, "id,name", "1004,AngularJS", propertyFileUtilMock);

        cache.setEntry(entityInfo, Lists.newArrayList(fieldOne), Sets.newHashSet(StringConsts.ID),
            TestUtils.getConcreteList(skillJavascript, skillJava, skillAngular, skillAngularJS));
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, Lists.newArrayList(fieldOne), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, Lists.newArrayList(fieldTwo), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualThree = cache.getEntry(entityInfo, Lists.newArrayList(fieldThree), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualFour = cache.getEntry(entityInfo, Lists.newArrayList(fieldFour), Sets.newHashSet(StringConsts.ID));

        Assert.assertEquals(Lists.newArrayList(skillJavascript, skillJava, skillAngular, skillAngularJS), actualOne);
        Assert.assertNull(actualTwo);
        Assert.assertNull(actualThree);
        Assert.assertNull(actualFour);
    }

    @Test
    public void testToManyFieldFallbackForBadData() throws Exception {
        // query/Skill?fields=id&query=name:"Skill_1;Skill_2" -> Bad Skill 2 data - wrong name
        // query/Skill?fields=id&query=name:"Skill_1"         -> Still works
        // query/Skill?fields=id&query=name:"Skill_2"         -> Does not find Skill_2, since it was not indexed individually (no matching name)
        EntityInfo entityInfo = EntityInfo.SKILL;
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList("name"));
        Field fieldOne = new Field(entityInfo, new Cell("name", "Skill_1;Skill_2"), false, propertyFileUtilMock.getDateParser());
        Field fieldTwo = new Field(entityInfo, new Cell("name", "Skill_1"), false, propertyFileUtilMock.getDateParser());
        Field fieldThree = new Field(entityInfo, new Cell("name", "Skill_2"), false, propertyFileUtilMock.getDateParser());
        BullhornEntity skillOne = TestUtils.createEntity(entityInfo, "id,name", "1001,Skill_1", propertyFileUtilMock);
        BullhornEntity skillTwo = TestUtils.createEntity(entityInfo, "id,name", "1002,Skill_000002", propertyFileUtilMock);

        cache.setEntry(entityInfo, Lists.newArrayList(fieldOne), Sets.newHashSet(StringConsts.ID), TestUtils.getConcreteList(skillOne, skillTwo));
        List<BullhornEntity> actualOne = cache.getEntry(entityInfo, Lists.newArrayList(fieldOne), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualTwo = cache.getEntry(entityInfo, Lists.newArrayList(fieldTwo), Sets.newHashSet(StringConsts.ID));
        List<BullhornEntity> actualThree = cache.getEntry(entityInfo, Lists.newArrayList(fieldThree), Sets.newHashSet(StringConsts.ID));

        Assert.assertEquals(Lists.newArrayList(skillOne, skillTwo), actualOne);
        Assert.assertEquals(Lists.newArrayList(skillOne), actualTwo);
        Assert.assertNull(actualThree);
    }
}
