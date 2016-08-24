package com.bullhorn.dataloader.util;

import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.meta.Field;
import com.bullhornsdk.data.model.entity.meta.StandardMetaData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TemplateUtilTest {

    TemplateUtil templateUtil;
    private BullhornData bullhornData;
    private Set<Field> metaFieldSet;
    private Set<Field> associationFields;
    private ArrayList<String> headers;
    private ArrayList<String> dataTypes;

    @Before
    public void setup() throws Exception {
        templateUtil = Mockito.spy(new TemplateUtil(bullhornData));

        headers = new ArrayList<>();
        dataTypes = new ArrayList<>();
        metaFieldSet = new HashSet<>();

        bullhornData = Mockito.mock(BullhornData.class);

        setUpMetaFieldSet();
        setUpAssociationFields();
    }

    private void setUpMetaFieldSet() {
        Field addressField = getAddressField();

        StandardMetaData clientCorporationMetaData = new StandardMetaData();
        clientCorporationMetaData.setEntity("ClientCorporation");
        Field idField = new Field();
        idField.setName("id");
        Field nameField = new Field();
        nameField.setName("name");
        clientCorporationMetaData.setFields(Arrays.asList(idField, nameField));

        Field clientCorporationField = new Field();
        clientCorporationField.setName("clientCorporation");
        clientCorporationField.setType("TO_ONE");
        clientCorporationField.setAssociatedEntity(clientCorporationMetaData);
        clientCorporationField.setOptionsType("ClientCorporation");

        Field fax = new Field();
        fax.setName("fax");
        fax.setType(null);
        fax.setDataType(null);

        metaFieldSet.add(fax);
        metaFieldSet.add(addressField);
        metaFieldSet.add(clientCorporationField);
    }

    private Field getAddressField() {
        Field addressField = new Field();
        addressField.setName("secondaryAddress");
        addressField.setType("COMPOSITE");
        addressField.setDataType("Address");
        return addressField;
    }

    private void setUpAssociationFields() {
        associationFields = metaFieldSet.stream().filter(n -> n.getAssociatedEntity() != null).collect(Collectors.toSet());
    }

    @Test
    public void populateDataTypesTestAddress() throws ClassNotFoundException {

        templateUtil.populateDataTypes("Candidate", metaFieldSet, headers, dataTypes);

        Assert.assertTrue(headers.stream().anyMatch(n -> n.equalsIgnoreCase("secondaryAddress.state")));
        Assert.assertTrue(headers.stream().anyMatch(n -> n.equalsIgnoreCase("secondaryAddress.address1")));
        Assert.assertTrue(headers.stream().anyMatch(n -> n.equalsIgnoreCase("secondaryAddress.address2")));
        Assert.assertTrue(headers.stream().anyMatch(n -> n.equalsIgnoreCase("secondaryAddress.zip")));
        Assert.assertTrue(headers.stream().anyMatch(n -> n.equalsIgnoreCase("secondaryAddress.countryID")));
        Assert.assertTrue(headers.stream().anyMatch(n -> n.equalsIgnoreCase("secondaryAddress.city")));
    }

    @Test
    public void addAssociatedFieldsTest() {

        templateUtil.addAssociatedFields(metaFieldSet, associationFields);

        Assert.assertTrue(metaFieldSet.stream().anyMatch(n -> n.getName().equalsIgnoreCase("clientCorporation.id")));
        Assert.assertTrue(metaFieldSet.stream().anyMatch(n -> n.getName().equalsIgnoreCase("clientCorporation.name")));
        Assert.assertTrue(metaFieldSet.stream().anyMatch(n -> n.getName().equalsIgnoreCase("clientCorporation.externalID")));
    }

    @Test
    public void hasIdTest_true() {
        templateUtil.addAssociatedFields(metaFieldSet, associationFields);

        boolean result = templateUtil.hasId(metaFieldSet, "clientCorporation");

        Assert.assertTrue(result);
    }

    @Test
    public void hasIdTest_false() {
        templateUtil.addAssociatedFields(metaFieldSet, associationFields);

        boolean result = templateUtil.hasId(metaFieldSet, "clientCorporation.id");

        Assert.assertFalse(result);
    }

    @Test
    public void isCompositeTypeTest() {

        boolean result = templateUtil.isCompositeType(getAddressField());

        Assert.assertTrue(result);
    }

    @Test(expected=ClassNotFoundException.class)
    public void testPopulateDataTypesIncorrectEntity() throws ClassNotFoundException {
        final String entity = "Cornidate";
        templateUtil.populateDataTypes(entity, metaFieldSet, headers, dataTypes);
    }

    @Test
    public void testIsNotCompositeType() {
        final Field random = new Field();
        Assert.assertTrue(!templateUtil.isCompositeType(random));
    }

    @Test
    public void testDataTypeIsNull() throws ClassNotFoundException {
        templateUtil.populateDataTypes("ClientCorporation", metaFieldSet, headers, dataTypes);
    }
}
