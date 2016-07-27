package com.bullhorn.dataloader.meta;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Test;

public class MetaDataTypeTest {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/mm/yy");

    private static class TestOption {
        final private String type;
        final private String testValue;
        final private Object expectedResult;
        final private SimpleDateFormat simpleDateFormat = MetaDataTypeTest.simpleDateFormat;

        public TestOption(String type, String testValue, Object expectedResult) {
            Assert.assertNotNull(type);
            Assert.assertNotNull(testValue);
            this.type = type;
            this.testValue = testValue;
            this.expectedResult = expectedResult;
        }

        public Object getExpectedResult() {
            return expectedResult;
        }

        public String getTestValue() {
            return testValue;
        }

        public String getType() {
            return type;
        }

        public SimpleDateFormat getSimpleDateFormat() {
            return simpleDateFormat;
        }
    }

    private static TestOption[] TEST_OPTIONS;
    private static TestOption[] TEST_OPTIONS_EMPTY;

    static {
        try {
            TEST_OPTIONS = new TestOption[]{
                    new TestOption("String", "testString", "testString"),
                    new TestOption("Boolean", "true", true),
                    new TestOption("Integer", "42", 42),
                    new TestOption("Double", "42.2", 42.2),
                    new TestOption("Timestamp", "14/10/92", simpleDateFormat.parse("14/10/92"))
            };

            TEST_OPTIONS_EMPTY = new TestOption[]{
                    new TestOption("String", "", ""),
                    new TestOption("Boolean", "", false),
                    new TestOption("Integer", "", 0),
                    new TestOption("Double", "", 0.0d),
                    new TestOption("Timestamp", "", null)
            };
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSize() {
        Assert.assertEquals(TEST_OPTIONS.length, MetaDataType.values().length);
        Assert.assertEquals(TEST_OPTIONS_EMPTY.length, MetaDataType.values().length);
    }

    @Test
    public void testFromName() {
        for (MetaDataType metaDataType : MetaDataType.values()) {
            final String type = metaDataType.name();
            final MetaDataType myMetaDataType = MetaDataType.fromName(type);

            Assert.assertEquals(metaDataType, myMetaDataType);
        }
    }

    @Test
    public void testFromName_null() {
        final MetaDataType metaDataType = MetaDataType.fromName(null);
        Assert.assertEquals(null, metaDataType);
    }

    @Test
    public void testConvertFromValue() {
        for (TestOption testOption : TEST_OPTIONS) {
            final MetaDataType metaDataType = MetaDataType.fromName(testOption.getType());
            final Object o = metaDataType.convertFieldValue(testOption.getTestValue(), testOption.getSimpleDateFormat());
            Assert.assertEquals(testOption.getExpectedResult(), o);
        }
    }

    @Test
    public void testConvertFromValue_defaults() {
        for (TestOption testOption : TEST_OPTIONS_EMPTY) {
            final MetaDataType metaDataType = MetaDataType.fromName(testOption.getType());
            final Object o = metaDataType.convertFieldValue(testOption.getTestValue(), testOption.getSimpleDateFormat());
            Assert.assertEquals(testOption.getExpectedResult(), o);
        }
    }
}
