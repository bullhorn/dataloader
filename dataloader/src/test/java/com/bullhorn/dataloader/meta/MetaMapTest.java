package com.bullhorn.dataloader.meta;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.junit.Test;

public class MetaMapTest {

    @Test
    public void testRootFieldNameToEntityName() {
        //arrange
        MetaMap metaMap = new MetaMap(new SimpleDateFormat());
        String rootFieldName = "expectedRootFieldName";
        String entityName = "expectedEntityName";

        //act
        metaMap.setRootFieldNameToEntityName(rootFieldName, entityName);

        //assert
        assertEquals("expectedEntityName", metaMap.getEntityNameByRootFieldName(rootFieldName).get());
    }

    @Test
    public void testFieldNameToDataType() {
        //arrange
        MetaMap metaMap = new MetaMap(new SimpleDateFormat());
        String fieldName = "expectedFieldName";
        String dataType = "expectedDataType";

        //act
        metaMap.setFieldNameToDataType(fieldName, dataType);

        //assert
        assertEquals(dataType, metaMap.getDataTypeByFieldName(fieldName).get());
    }

    @Test
    public void testFieldMapLabelToDataType() {
        //arrange
        MetaMap metaMap = new MetaMap(new SimpleDateFormat());
        String fieldMapLabel = "expectedFieldMapLabel";
        String dataType = "expectedDataType";

        //act
        metaMap.setFieldMapLabelToDataType(fieldMapLabel, dataType);

        //assert
        assertEquals(dataType, metaMap.getDataTypeByFieldMapLabel(fieldMapLabel).get());
    }

    @Test
    public void testFieldNameToAssociationType() {
        //arrange
        MetaMap metaMap = new MetaMap(new SimpleDateFormat());
        String fieldName = "expectedFieldName";
        String associationType = "expectedAssociationType";

        //act
        metaMap.setFieldNameToAssociationType(fieldName, associationType);

        //assert
        assertEquals(associationType, metaMap.getAssociationTypeByFieldName(fieldName));
    }

    @Test
    public void testDetermineDataType_fieldNameExists() {
        //arrange
        String fieldName = "fieldName";
        String dataType = "String";
        MetaMap metaMap = new MetaMap(new SimpleDateFormat());
        metaMap.setFieldNameToDataType(fieldName, dataType);

        //act
        Optional<String> type = metaMap.determineDataType(fieldName);

        //assert
        assertTrue(type.isPresent());
        assertEquals(dataType, type.get());
    }

    @Test
    public void testDetermineDataType_fieldNameDoesNotExists_fieldMapLabelExists() {
        //arrange
        String fieldMapLabel = "fieldMapLabel";
        String dataType = "Integer";
        MetaMap metaMap = new MetaMap(new SimpleDateFormat());
        metaMap.setFieldMapLabelToDataType(fieldMapLabel, dataType);

        //act
        Optional<String> type = metaMap.determineDataType(fieldMapLabel);

        //assert
        assertTrue(type.isPresent());
        assertEquals(dataType, type.get());
    }

    @Test
    public void testDetermineDataType_fieldNameDoesNotExists_fieldMapLabelDoesExists() {
        //arrange
        String fieldMapLabel = "fieldMapLabel";
        String dataType = "Integer";
        MetaMap metaMap = new MetaMap(new SimpleDateFormat());

        //act
        Optional<String> type = metaMap.determineDataType(fieldMapLabel);

        //assert
        assertFalse(type.isPresent());
    }

    @Test
    public void testConvertFieldValue_String() {
        //arrange
        String fieldName = "fieldName";
        String dataType = "String";
        MetaMap metaMap = new MetaMap(new SimpleDateFormat());
        metaMap.setFieldNameToDataType(fieldName, dataType);

        //act
        Object value = metaMap.convertFieldValue(fieldName, "test");

        //assert
        assertTrue(value instanceof String);
        assertEquals("test", (String) value);
    }

    @Test
    public void testConvertFieldValue_Integer() {
        //arrange
        String fieldName = "fieldName";
        String dataType = "Integer";
        MetaMap metaMap = new MetaMap(new SimpleDateFormat());
        metaMap.setFieldNameToDataType(fieldName, dataType);

        //act
        Object value = metaMap.convertFieldValue(fieldName, "23");

        //assert
        assertTrue(value instanceof Integer);
        assertEquals(23, ((Integer) value).intValue());
    }

    @Test
    public void testConvertFieldValue_Double() {
        //arrange
        String fieldName = "fieldName";
        String dataType = "Double";
        MetaMap metaMap = new MetaMap(new SimpleDateFormat());
        metaMap.setFieldNameToDataType(fieldName, dataType);

        //act
        Object value = metaMap.convertFieldValue(fieldName, "23");

        //assert
        assertTrue(value instanceof Double);
        assertEquals(23d, ((Double) value).doubleValue());
    }

    @Test
    public void testConvertFieldValue_Timestamp() {
        //arrange
        String fieldName = "fieldName";
        String dataType = "Timestamp";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        MetaMap metaMap = new MetaMap(simpleDateFormat);
        metaMap.setFieldNameToDataType(fieldName, dataType);

        //act
        Object value = metaMap.convertFieldValue(fieldName, "07/10/96 4:5 PM, PDT");

        //assert
        assertTrue(value instanceof Date);
        assertEquals("7/10/96 4:05 PM", simpleDateFormat.format((Date) value));
    }

    @Test
    public void testConvertFieldValue_Timestamp_HonorsDateFormat() {
        //arrange
        String fieldName = "fieldName";
        String dataType = "Timestamp";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        MetaMap metaMap = new MetaMap(simpleDateFormat);
        metaMap.setFieldNameToDataType(fieldName, dataType);

        //act
        Object value = metaMap.convertFieldValue(fieldName, "07/10/1996");

        //assert
        assertTrue(value instanceof Date);
        assertEquals("07/10/1996", simpleDateFormat.format((Date) value));
    }

    @Test
    public void testConvertFieldValue_fieldNotEnteredInMap_returnsString() {
        //arrange
        String fieldName = "fieldName";
        MetaMap metaMap = new MetaMap(new SimpleDateFormat());

        //act
        Object value = metaMap.convertFieldValue(fieldName, "testing");

        //assert
        assertEquals("testing", value);
    }
}
