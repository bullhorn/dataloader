package com.bullhorn.dataloader.util;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhornsdk.data.model.entity.meta.Field;
import com.bullhornsdk.data.model.entity.meta.StandardMetaData;
import com.google.common.collect.Sets;

public class TemplateUtilTest {

    private ArrayList<String> dataTypes;
    private ArrayList<String> headers;
    private Set<Field> associationFields;
    private Set<Field> metaFieldSet;
    private TemplateUtil templateUtil;

    @Before
    public void setup() {
        RestApi restApiMock = mock(RestApi.class);
        PropertyFileUtil propertyFileUtilMock = mock(PropertyFileUtil.class);
        PrintUtil printUtilMock = mock(PrintUtil.class);

        templateUtil = new TemplateUtil(restApiMock, propertyFileUtilMock, printUtilMock);

        headers = new ArrayList<>();
        dataTypes = new ArrayList<>();
        metaFieldSet = new HashSet<>();

        setupMetaFieldSet();
        setupAssociationFields();
    }

    private void setupMetaFieldSet() {
        StandardMetaData clientCorporationMetaData = new StandardMetaData();
        clientCorporationMetaData.setEntity("ClientCorporation");
        Field idField = TestUtils.createField("id", null, null, null, "SCALAR", "Integer");
        Field nameField = TestUtils.createField("name", "Name", "", "", "SCALAR", "String");
        clientCorporationMetaData.setFields(Arrays.asList(idField, nameField));

        Field clientCorporationField = TestUtils.createField("clientCorporation", "", "", null, "TO_ONE", null);
        clientCorporationField.setAssociatedEntity(clientCorporationMetaData);
        clientCorporationField.setOptionsType("ClientCorporation");

        Field faxField = TestUtils.createField("fax", "Fax", null, null, null, null);
        Field leadsField = TestUtils.createField("leads", "Leads", null, null, "TO_MANY", null);
        Field departmentField = TestUtils.createField("department", "Department", null, null, "TO_ONE", null);
        Field addressField = TestUtils.createField("secondaryAddress", "Address 2", null, null, "COMPOSITE", "Address");

        metaFieldSet.add(departmentField);
        metaFieldSet.add(leadsField);
        metaFieldSet.add(faxField);
        metaFieldSet.add(addressField);
        metaFieldSet.add(clientCorporationField);
    }

    private void setupAssociationFields() {
        associationFields = metaFieldSet.stream().filter(n -> n.getAssociatedEntity() != null).collect(Collectors.toSet());
    }

    @Test
    public void testPopulateDataTypesAddress() {
        templateUtil.populateDataTypes(EntityInfo.CANDIDATE, metaFieldSet, headers, dataTypes);

        Assert.assertTrue(headers.stream().anyMatch(n -> n.equalsIgnoreCase("secondaryAddress.state")));
        Assert.assertTrue(headers.stream().anyMatch(n -> n.equalsIgnoreCase("secondaryAddress.address1")));
        Assert.assertTrue(headers.stream().anyMatch(n -> n.equalsIgnoreCase("secondaryAddress.address2")));
        Assert.assertTrue(headers.stream().anyMatch(n -> n.equalsIgnoreCase("secondaryAddress.zip")));
        Assert.assertTrue(headers.stream().anyMatch(n -> n.equalsIgnoreCase("secondaryAddress.countryID")));
        Assert.assertTrue(headers.stream().anyMatch(n -> n.equalsIgnoreCase("secondaryAddress.city")));
    }

    @Test
    public void testAddAssociatedFields() {
        templateUtil.addAssociatedFields(metaFieldSet, associationFields);

        Assert.assertTrue(metaFieldSet.stream().anyMatch(n -> n.getName().equalsIgnoreCase("clientCorporation.id")));
        Assert.assertTrue(metaFieldSet.stream().anyMatch(n -> n.getName().equalsIgnoreCase("clientCorporation.name")));
        Assert.assertTrue(metaFieldSet.stream().anyMatch(n -> n.getName().equalsIgnoreCase("clientCorporation.externalID")));
    }

    @Test
    public void testAssociatedFieldsTestNoExternalId() {
        StandardMetaData skillMeta = new StandardMetaData();
        skillMeta.setEntity("Skill");
        Field idField = TestUtils.createField("id", null, null, null, "SCALAR", "Integer");
        Field nameField = TestUtils.createField("name", "Name", "", "", "SCALAR", "String");
        skillMeta.setFields(Arrays.asList(idField, nameField));

        Field primarySkillsField = TestUtils.createField("primarySkills", "Skills", null, null, "TO_MANY", "Skill");
        primarySkillsField.setOptionsType("Skill");
        primarySkillsField.setAssociatedEntity(skillMeta);

        Set<Field> fieldSet = Sets.newHashSet();

        templateUtil.addAssociatedFields(fieldSet, Sets.newHashSet(primarySkillsField));

        Assert.assertTrue(fieldSet.stream().anyMatch(n -> n.getName().equalsIgnoreCase("primarySkills.id")));
        Assert.assertTrue(fieldSet.stream().anyMatch(n -> n.getName().equalsIgnoreCase("primarySkills.name")));
        Assert.assertFalse(fieldSet.stream().anyMatch(n -> n.getName().equalsIgnoreCase("primarySkills.externalID")));
    }

    @Test
    public void testIsCompositeType() {
        Field addressField = TestUtils.createField("secondaryAddress", "Address 2", null, null, "COMPOSITE", "Address");
        boolean result = templateUtil.isCompositeType(addressField);
        Assert.assertTrue(result);
    }

    @Test
    public void testIsNotCompositeType() {
        final Field random = new Field();
        Assert.assertTrue(!templateUtil.isCompositeType(random));
    }

    @Test
    public void testDataTypeIsNull() {
        templateUtil.populateDataTypes(EntityInfo.CLIENT_CORPORATION, metaFieldSet, headers, dataTypes);
    }

    @Test
    public void testIsToManyNonReadOnly() {
        templateUtil.populateDataTypes(EntityInfo.CLIENT_CORPORATION, metaFieldSet, headers, dataTypes);
        Assert.assertTrue(headers.contains("leads.id"));
    }

    @Test
    public void testIsToManyReadOnly() {
        templateUtil.populateDataTypes(EntityInfo.CLIENT_CORPORATION, metaFieldSet, headers, dataTypes);
        Assert.assertFalse(headers.contains("clientContact.id"));
    }

    @Test
    public void testIsToOne() {
        templateUtil.populateDataTypes(EntityInfo.CLIENT_CORPORATION, metaFieldSet, headers, dataTypes);
        Assert.assertTrue(headers.contains("department.id"));
    }
}
