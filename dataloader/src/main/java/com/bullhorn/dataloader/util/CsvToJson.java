package com.bullhorn.dataloader.util;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.domain.MetaMap;
import com.bullhorn.dataloader.service.JsonRow;
import com.csvreader.CsvReader;

public class CsvToJson implements Iterator, Iterable<JsonRow> {
    private final Log log = LogFactory.getLog(CsvToJson.class);

    private final MetaMap metaMap;
    private CsvReader csvReader;
    private boolean hasNextLine = true;
    private String[] columns;

    public CsvToJson(String filePath, MetaMap metaMap) throws IOException {
        this.csvReader = new CsvReader(filePath);
        csvReader.readHeaders();
        columns = csvReader.getHeaders();
        this.metaMap = metaMap;
    }

    private JsonRow mapRow() throws IOException {
        JsonRow JsonRow = new JsonRow();
        for (String column : columns) {
            String[] jsonPath = column.split("\\.");
            Object convertType = metaMap.convertType(column, get(column));
            if ("TO_MANY".equalsIgnoreCase(metaMap.getTypeByName(jsonPath[0]))) {
                JsonRow.addDeferredAction(jsonPath, convertType);
            } else {
                JsonRow.addImmediateAction(jsonPath, convertType);
            }

        }
        return JsonRow;
    }

    private String get(String column) throws IOException {
        try {
            return csvReader.get(column);
        } catch (IOException e) {
            log.error("Error getting column: \"" + column + "\" for row: \"" + "\": " + e.toString());
            throw new IOException();
        }
    }

    private void readLine() {
        try {
            hasNextLine = csvReader.readRecord();
        } catch (IOException e) {
            hasNextLine = false;
        }
    }

    @Override
    public boolean hasNext() {
        return hasNextLine;
    }

    @Override
    public JsonRow next() {
        readLine();
        try {
            return mapRow();
        } catch (IOException e) {
            log.error(e);
            return null;
        }
    }

    @Override
    public Iterator<JsonRow> iterator() {
        return this;
    }
}
