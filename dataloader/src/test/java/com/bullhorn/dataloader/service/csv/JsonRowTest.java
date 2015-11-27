package com.bullhorn.dataloader.service.csv;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import junit.framework.TestCase;

public class JsonRowTest {
    @Test
    public void testImmediateAction() {
        //arrange
        JsonRow jsonRow = new JsonRow();

        //act
        jsonRow.addImmediateAction(new String[] {"field1", "field2"}, 1);
        jsonRow.addImmediateAction(new String[] {"field1", "field3"}, 2);
        Map<String, Object> actualImmediateAction = jsonRow.getImmediateActions();

        //assert
        Map<String, Object> expected = new HashMap<String, Object>() {{
            put("field1", new HashMap<String, Object>() {{
                put("field2", 1);
                put("field3", 2);
            }});
        }};
        TestCase.assertEquals(expected, actualImmediateAction);
    }

    @Test
    public void testDeferredActions() {
        //arrange
        JsonRow jsonRow = new JsonRow();

        //act
        jsonRow.addDeferredAction(new String[] {"field1", "field2"}, 1);
        jsonRow.addDeferredAction(new String[] {"field1", "field3"}, 2);
        Map<String, Object> actualDeferredActions = jsonRow.getDeferredActions();

        //assert
        Map<String, Object> expected = new HashMap<String, Object>() {{
            put("field1", new HashMap<String, Object>() {{
                put("field2", 1);
                put("field3", 2);
            }});
        }};
        TestCase.assertEquals(expected, actualDeferredActions);
    }

    @Test
    public void testPreprocessingActions() {
        //arrange
        JsonRow jsonRow = new JsonRow();

        //act
        jsonRow.addPreprocessing(new String[] {"field1", "field2"}, 1);
        jsonRow.addPreprocessing(new String[] {"field1", "field3"}, 2);
        Map<String, Object> actualPreprocessingActions = jsonRow.getPreprocessingActions();

        //assert
        Map<String, Object> expected = new HashMap<String, Object>() {{
            put("field1", new HashMap<String, Object>() {{
                put("field2", 1);
                put("field3", 2);
            }});
        }};
        TestCase.assertEquals(expected, actualPreprocessingActions);
    }
}
