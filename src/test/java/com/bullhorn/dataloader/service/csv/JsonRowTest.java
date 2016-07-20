package com.bullhorn.dataloader.service.csv;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class JsonRowTest {

    @Test
    public void testImmediateAction() {
        JsonRow jsonRow = new JsonRow();
        jsonRow.addImmediateAction(new String[] {"field1", "field2"}, 1);
        jsonRow.addImmediateAction(new String[] {"field1", "field3"}, 2);
        Map<String, Object> actualImmediateAction = jsonRow.getImmediateActions();

        Map<String, Object> expected = new HashMap<String, Object>() {{
            put("field1", new HashMap<String, Object>() {{
                put("field2", 1);
                put("field3", 2);
            }});
        }};
        Assert.assertEquals(expected, actualImmediateAction);
    }

    @Test
    public void testDeferredActions() {
        JsonRow jsonRow = new JsonRow();
        jsonRow.addDeferredAction(new String[] {"field1", "field2"}, 1);
        jsonRow.addDeferredAction(new String[] {"field1", "field3"}, 2);
        Map<String, Object> actualDeferredActions = jsonRow.getDeferredActions();

        Map<String, Object> expected = new HashMap<String, Object>() {{
            put("field1", new HashMap<String, Object>() {{
                put("field2", 1);
                put("field3", 2);
            }});
        }};
        Assert.assertEquals(expected, actualDeferredActions);
    }

    @Test
    public void testPreprocessingActions() {
        JsonRow jsonRow = new JsonRow();
        jsonRow.addPreprocessing(new String[] {"field1", "field2"}, 1);
        jsonRow.addPreprocessing(new String[] {"field1", "field3"}, 2);
        Map<String, Object> actualPreprocessingActions = jsonRow.getPreprocessingActions();

        Map<String, Object> expected = new HashMap<String, Object>() {{
            put("field1", new HashMap<String, Object>() {{
                put("field2", 1);
                put("field3", 2);
            }});
        }};
        Assert.assertEquals(expected, actualPreprocessingActions);
    }

    @Test
    public void testValues() {
        JsonRow jsonRow = new JsonRow();
        String[] values = new String[] {"column_1", "column_2", "column_3"};
        jsonRow.setValues(values);

        String[] expected = new String[] {"column_1", "column_2", "column_3"};
        Assert.assertTrue(Arrays.equals(expected, jsonRow.getValues()));
    }

    @Test
    public void testRowNumber() {
        JsonRow jsonRow = new JsonRow();
        Assert.assertEquals(jsonRow.getRowNumber().intValue(), 0);

        jsonRow.setRowNumber(99);
        Assert.assertEquals(jsonRow.getRowNumber().intValue(), 99);
    }
}
