package com.bullhorn.dataloader.meta;

import static org.mockito.Mockito.verify;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class MetaMapTest {
    private final String BAR = "|";

    @Test
    public void testRootFieldNameToEntityName() {
        final MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);
        final String rootFieldName = "expectedRootFieldName";
        final String entityName = "expectedEntityName";

        metaMap.setRootFieldNameToEntityName(rootFieldName, entityName);

        Assert.assertEquals("expectedEntityName", metaMap.getEntityNameByRootFieldName(rootFieldName).get());
    }

    @Test
    public void testFieldNameToDataType() {
        final MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);
        final String fieldName = "expectedFieldName";
        final String dataType = "expectedDataType";

        metaMap.setFieldNameToDataType(fieldName, dataType);

        Assert.assertEquals(dataType, metaMap.getDataTypeByFieldName(fieldName).get());
    }

    @Test
    public void testFieldMapLabelToDataType() {
        final MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);
        final String fieldMapLabel = "expectedFieldMapLabel";
        final String dataType = "expectedDataType";

        metaMap.setFieldMapLabelToDataType(fieldMapLabel, dataType);

        Assert.assertEquals(dataType, metaMap.getDataTypeByFieldMapLabel(fieldMapLabel).get());
    }

    @Test
    public void testFieldNameToAssociationType() {
        final MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);
        final String fieldName = "expectedFieldName";
        final String associationType = "expectedAssociationType";

        metaMap.setFieldNameToAssociationType(fieldName, associationType);

        Assert.assertEquals(associationType, metaMap.getAssociationTypeByFieldName(fieldName));
    }

    @Test
    public void testDetermineDataType_fieldNameExists() {
        final String fieldName = "fieldName";
        final String dataType = "String";
        final MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);

        metaMap.setFieldNameToDataType(fieldName, dataType);

        final Optional<String> type = metaMap.determineDataType(fieldName);

        Assert.assertTrue(type.isPresent());
        Assert.assertEquals(dataType, type.get());
    }

    @Test
    public void testDetermineDataType_fieldNameDoesNotExists_fieldMapLabelExists() {
        final String fieldMapLabel = "fieldMapLabel";
        final String dataType = "Integer";
        final MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);

        metaMap.setFieldMapLabelToDataType(fieldMapLabel, dataType);

        final Optional<String> type = metaMap.determineDataType(fieldMapLabel);

        Assert.assertTrue(type.isPresent());
        Assert.assertEquals(dataType, type.get());
    }

    @Test
    public void testDetermineDataType_fieldNameDoesNotExists_fieldMapLabelDoesExists() {
        final String fieldMapLabel = "fieldMapLabel";
        final MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);
        final Optional<String> type = metaMap.determineDataType(fieldMapLabel);

        Assert.assertFalse(type.isPresent());
    }

    @Test
    public void testConvertFieldValue_String() {
        final String fieldName = "fieldName";
        final String dataType = "String";
        final MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);

        metaMap.setFieldNameToDataType(fieldName, dataType);

        final Object value = metaMap.convertFieldValue(fieldName, "test");

        Assert.assertTrue(value instanceof String);
        Assert.assertEquals("test", (String) value);
    }

    @Test
    public void testConvertFieldValue_Integer() {
        final String fieldName = "fieldName";
        final String dataType = "Integer";
        final MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);

        metaMap.setFieldNameToDataType(fieldName, dataType);

        final Object value = metaMap.convertFieldValue(fieldName, "23");

        Assert.assertTrue(value instanceof Integer);
        Assert.assertEquals(23, ((Integer) value).intValue());
    }

    @Test
    public void testConvertFieldValue_Double() {
        final String fieldName = "fieldName";
        final String dataType = "Double";
        final MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);

        metaMap.setFieldNameToDataType(fieldName, dataType);

        final Object value = metaMap.convertFieldValue(fieldName, "23");

        Assert.assertTrue(value instanceof Double);
        Assert.assertEquals(23d, ((Double) value).doubleValue(), 0);
    }

    @Test
    public void testConvertFieldValue_Timestamp() throws ParseException {
        final String fieldName = "fieldName";
        final String dataType = "Timestamp";
        final SimpleDateFormat simpleDateFormat = Mockito.mock(SimpleDateFormat.class);
        final MetaMap metaMap = new MetaMap(simpleDateFormat, BAR);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

        metaMap.setFieldNameToDataType(fieldName, dataType);
        metaMap.convertFieldValue(fieldName, "07/10/96 4:5 PM, PDT");

        verify(simpleDateFormat).parse(stringArgumentCaptor.capture());
        String actualValue = stringArgumentCaptor.getValue();
        Assert.assertEquals("07/10/96 4:5 PM, PDT", actualValue);
    }

    @Test
    public void testConvertFieldValue_fieldNotEnteredInMap_returnsString() {
        final String fieldName = "fieldName";
        final MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);

        final Object value = metaMap.convertFieldValue(fieldName, "testing");

        Assert.assertEquals("testing", value);
    }

    @Test
    public void testConvertFieldValue_isToMany_arrayValues() {
        final String fieldName = "categories.id";
        final MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);

        metaMap.setFieldNameToAssociationType("categories", "TO_MANY");
        metaMap.setFieldNameToDataType("categories.id", "Integer");

        final Object value = metaMap.convertFieldValue(fieldName, "1|2|3");

        final List<Integer> expected = new ArrayList<Integer>() {{
            add(1); add(2); add(3);
        }};

        Assert.assertEquals(expected, value);
    }
}
