package com.bullhorn.dataloader.service.csv;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class CsvFileWriterTest {

    @Test
    public void testConstructor() throws IOException {
        //arrange
        String path = getFilePath("CsvToJsonTest_base.csv");
        String[] headers = new String[] {"column_1", "column_2", "column_3"};

        //act
//TODO        CsvFileWriter csvFileWriter = new CsvFileWriter(path, headers);

        //assert
    }

    private String getFilePath(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
    }
}
