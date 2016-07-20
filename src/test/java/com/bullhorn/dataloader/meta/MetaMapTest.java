package com.bullhorn.dataloader.meta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

public class MetaMapTest {
    private final String BAR = "|";

    @Test
    public void testRootFieldNameToEntityName() {
        MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);
        String rootFieldName = "expectedRootFieldName";
        String entityName = "expectedEntityName";

        metaMap.setRootFieldNameToEntityName(rootFieldName, entityName);

        Assert.assertEquals("expectedEntityName", metaMap.getEntityNameByRootFieldName(rootFieldName).get());
    }

    @Test
    public void testFieldNameToDataType() {
        MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);
        String fieldName = "expectedFieldName";
        String dataType = "expectedDataType";

        metaMap.setFieldNameToDataType(fieldName, dataType);

        Assert.assertEquals(dataType, metaMap.getDataTypeByFieldName(fieldName).get());
    }

    @Test
    public void testFieldMapLabelToDataType() {
        MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);
        String fieldMapLabel = "expectedFieldMapLabel";
        String dataType = "expectedDataType";

        metaMap.setFieldMapLabelToDataType(fieldMapLabel, dataType);

        Assert.assertEquals(dataType, metaMap.getDataTypeByFieldMapLabel(fieldMapLabel).get());
    }

    @Test
    public void testFieldNameToAssociationType() {
        MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);
        String fieldName = "expectedFieldName";
        String associationType = "expectedAssociationType";

        metaMap.setFieldNameToAssociationType(fieldName, associationType);

        Assert.assertEquals(associationType, metaMap.getAssociationTypeByFieldName(fieldName));
    }

    @Test
    public void testDetermineDataType_fieldNameExists() {
        String fieldName = "fieldName";
        String dataType = "String";
        MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);
        metaMap.setFieldNameToDataType(fieldName, dataType);

        Optional<String> type = metaMap.determineDataType(fieldName);

        Assert.assertTrue(type.isPresent());
        Assert.assertEquals(dataType, type.get());
    }

    @Test
    public void testDetermineDataType_fieldNameDoesNotExists_fieldMapLabelExists() {
        String fieldMapLabel = "fieldMapLabel";
        String dataType = "Integer";
        MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);
        metaMap.setFieldMapLabelToDataType(fieldMapLabel, dataType);

        Optional<String> type = metaMap.determineDataType(fieldMapLabel);

        Assert.assertTrue(type.isPresent());
        Assert.assertEquals(dataType, type.get());
    }

    @Test
    public void testDetermineDataType_fieldNameDoesNotExists_fieldMapLabelDoesExists() {
        String fieldMapLabel = "fieldMapLabel";
        MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);

        Optional<String> type = metaMap.determineDataType(fieldMapLabel);

        Assert.assertFalse(type.isPresent());
    }

    @Test
    public void testConvertFieldValue_String() {
        String fieldName = "fieldName";
        String dataType = "String";
        MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);
        metaMap.setFieldNameToDataType(fieldName, dataType);

        Object value = metaMap.convertFieldValue(fieldName, "test");

        Assert.assertTrue(value instanceof String);
        Assert.assertEquals("test", (String) value);
    }

    @Test
    public void testConvertFieldValue_Integer() {
        String fieldName = "fieldName";
        String dataType = "Integer";
        MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);
        metaMap.setFieldNameToDataType(fieldName, dataType);

        Object value = metaMap.convertFieldValue(fieldName, "23");

        Assert.assertTrue(value instanceof Integer);
        Assert.assertEquals(23, ((Integer) value).intValue());
    }

    @Test
    public void testConvertFieldValue_Double() {
        String fieldName = "fieldName";
        String dataType = "Double";
        MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);
        metaMap.setFieldNameToDataType(fieldName, dataType);

        Object value = metaMap.convertFieldValue(fieldName, "23");

        Assert.assertTrue(value instanceof Double);
        Assert.assertEquals(23d, ((Double) value).doubleValue(), 0);
    }

    @Test
    public void testConvertFieldValue_Timestamp() {
        String fieldName = "fieldName";
        String dataType = "Timestamp";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        MetaMap metaMap = new MetaMap(simpleDateFormat, BAR);
        metaMap.setFieldNameToDataType(fieldName, dataType);

        Object value = metaMap.convertFieldValue(fieldName, "07/10/96 4:5 PM, PDT");

        Assert.assertTrue(value instanceof Date);
        Assert.assertEquals("7/10/96 4:05 PM", simpleDateFormat.format((Date) value));
    }

    @Test
    public void testConvertFieldValue_Timestamp_HonorsDateFormat() {
        String fieldName = "fieldName";
        String dataType = "Timestamp";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        MetaMap metaMap = new MetaMap(simpleDateFormat, BAR);
        metaMap.setFieldNameToDataType(fieldName, dataType);

        Object value = metaMap.convertFieldValue(fieldName, "07/10/1996");

        Assert.assertTrue(value instanceof Date);
        Assert.assertEquals("07/10/1996", simpleDateFormat.format((Date) value));
    }

    @Test
    public void testConvertFieldValue_fieldNotEnteredInMap_returnsString() {
        String fieldName = "fieldName";
        MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);

        Object value = metaMap.convertFieldValue(fieldName, "testing");

        Assert.assertEquals("testing", value);
    }

    @Test
    public void testConvertFieldValue_isToMany_arrayValues() {
        String fieldName = "categories.id";
        MetaMap metaMap = new MetaMap(new SimpleDateFormat(), BAR);
        metaMap.setFieldNameToAssociationType("categories", "TO_MANY");
        metaMap.setFieldNameToDataType("categories.id", "Integer");

        Object value = metaMap.convertFieldValue(fieldName, "1|2|3");

        List<Integer> expected = new ArrayList<Integer>() {{
            add(1); add(2); add(3);
        }};
        Assert.assertEquals(expected, value);
    }
}
