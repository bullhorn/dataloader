package com.bullhorn.dataloader.service.csv;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.bullhorn.dataloader.meta.MetaMap;

import junit.framework.TestCase;

public class CsvFileReaderTest {

    @Test
    public void testImmediateActions() throws IOException {
        // arrange
        MetaMap metaMap = new MetaMap(new SimpleDateFormat("MM/dd/yyyy"), "|");
        String path = getFilePath("CsvToJsonTest_base.csv");

        CsvFileReader csvFileReader = new CsvFileReader(path, metaMap);
        Map<String, Object> onlyRow = new HashMap<String, Object>() {{
            put("a", "1");
            put("b", "2");
            put("c", "3");
        }};

        // act
        int count = 0;
        for (JsonRow jsonRow : csvFileReader) {
            count++;
            TestCase.assertEquals(onlyRow, jsonRow.getImmediateActions());
        }
        TestCase.assertEquals(1, count);
    }

    private String getFilePath(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
    }
}
