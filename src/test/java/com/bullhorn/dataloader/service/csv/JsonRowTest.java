package com.bullhorn.dataloader.service.csv;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class JsonRowTest {

    @Test
    public void testImmediateAction() {
        final JsonRow jsonRow = new JsonRow();
        jsonRow.addImmediateAction(new String[] {"field1", "field2"}, 1);
        jsonRow.addImmediateAction(new String[] {"field1", "field3"}, 2);
        final Map<String, Object> actualImmediateAction = jsonRow.getImmediateActions();

        final Map<String, Object> expected = new HashMap<String, Object>() {{
            put("field1", new HashMap<String, Object>() {{
                put("field2", 1);
                put("field3", 2);
            }});
        }};
        Assert.assertEquals(expected, actualImmediateAction);
    }

    @Test
    public void testDeferredActions() {
        final JsonRow jsonRow = new JsonRow();
        jsonRow.addDeferredAction(new String[] {"field1", "field2"}, 1);
        jsonRow.addDeferredAction(new String[] {"field1", "field3"}, 2);
        final Map<String, Object> actualDeferredActions = jsonRow.getDeferredActions();

        final Map<String, Object> expected = new HashMap<String, Object>() {{
            put("field1", new HashMap<String, Object>() {{
                put("field2", 1);
                put("field3", 2);
            }});
        }};
        Assert.assertEquals(expected, actualDeferredActions);
    }

    @Test
    public void testPreprocessingActions() {
        final JsonRow jsonRow = new JsonRow();
        jsonRow.addPreprocessing(new String[] {"field1", "field2"}, 1);
        jsonRow.addPreprocessing(new String[] {"field1", "field3"}, 2);
        final Map<String, Object> actualPreprocessingActions = jsonRow.getPreprocessingActions();

        final Map<String, Object> expected = new HashMap<String, Object>() {{
            put("field1", new HashMap<String, Object>() {{
                put("field2", 1);
                put("field3", 2);
            }});
        }};
        Assert.assertEquals(expected, actualPreprocessingActions);
    }

    @Test
    public void testValues() {
        final JsonRow jsonRow = new JsonRow();
        final String[] values = new String[] {"column_1", "column_2", "column_3"};
        jsonRow.setValues(values);

        final String[] expected = new String[] {"column_1", "column_2", "column_3"};
        Assert.assertTrue(Arrays.equals(expected, jsonRow.getValues()));
    }

    @Test
    public void testRowNumber() {
        final JsonRow jsonRow = new JsonRow();
        Assert.assertEquals(jsonRow.getRowNumber().intValue(), 0);

        jsonRow.setRowNumber(99);
        Assert.assertEquals(jsonRow.getRowNumber().intValue(), 99);
    }
}
