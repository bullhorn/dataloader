package com.bullhorn.dataloader.service.csv;

import com.bullhorn.dataloader.meta.MetaMap;
import com.csvreader.CsvReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.bullhorn.dataloader.util.AssociationFilter.isCustomObject;
import static com.bullhorn.dataloader.util.AssociationFilter.isToMany;
import static com.bullhorn.dataloader.util.AssociationFilter.isToOne;

public class CsvFileReader implements Iterator<JsonRow>, Iterable<JsonRow> {
    private final Logger log = LogManager.getLogger(CsvFileReader.class);

    private final MetaMap metaMap;
    private CsvReader csvReader;
    private JsonRow nextRow;
    private boolean hasNextLine = true;
    private String[] headers;
    private Integer rowCount = 0;

    public CsvFileReader(String filePath, MetaMap metaMap) throws IOException {
        this.csvReader = new CsvReader(filePath);
        csvReader.readHeaders();
        headers = csvReader.getHeaders();
        this.metaMap = metaMap;
        readLine();
    }

    public String[] getHeaders() {
        return headers;
    }

    private JsonRow mapRow() throws IOException {
        JsonRow jsonRow = new JsonRow();
        for (String column : headers) {
            String[] jsonPath = column.split("\\.");
            Object convertType = metaMap.convertFieldValue(column, get(column));
            String associationName = jsonPath[0];
            String associationType = metaMap.getAssociationTypeByFieldName(associationName);
            if (isToOne(associationType)) {
                jsonRow.addPreprocessing(jsonPath, convertType);
            } else if (isToMany(associationType) && !isCustomObject(associationName)) {
                jsonRow.addDeferredAction(jsonPath, convertType);
            } else {
                jsonRow.addImmediateAction(jsonPath, convertType);
            }
        }

        jsonRow.setValues(csvReader.getValues());
        jsonRow.setRowNumber(++rowCount);
        return jsonRow;
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
            nextRow = mapRow();
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
        if (hasNextLine) {
            JsonRow currentRow = nextRow;
            readLine();
            return currentRow;
        }
        throw new NoSuchElementException();
    }

    @Override
    public Iterator<JsonRow> iterator() {
        return this;
    }
}
