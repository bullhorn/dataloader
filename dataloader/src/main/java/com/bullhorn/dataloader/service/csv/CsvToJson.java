package com.bullhorn.dataloader.service.csv;

import static com.bullhorn.dataloader.util.CaseInsensitiveStringPredicate.isCustomObject;
import static com.bullhorn.dataloader.util.CaseInsensitiveStringPredicate.isToMany;
import static com.bullhorn.dataloader.util.CaseInsensitiveStringPredicate.isToOne;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.meta.MetaMap;
import com.csvreader.CsvReader;

public class CsvToJson implements Iterator<JsonRow>, Iterable<JsonRow> {
    private final Log log = LogFactory.getLog(CsvToJson.class);

    private final MetaMap metaMap;
    private CsvReader csvReader;
    private JsonRow nextRow;
    private boolean hasNextLine = true;
    private String[] columns;

    public CsvToJson(String filePath, MetaMap metaMap) throws IOException {
        this.csvReader = new CsvReader(filePath);
        csvReader.readHeaders();
        columns = csvReader.getHeaders();
        this.metaMap = metaMap;
        readLine();
    }

    private JsonRow mapRow() throws IOException {
        JsonRow jsonRow = new JsonRow();
        for (String column : columns) {
            String[] jsonPath = column.split("\\.");
            Object convertType = metaMap.convertFieldValue(column, get(column));
            String associationName = jsonPath[0];
            if (isToOne(metaMap.getAssociationTypeByFieldName(associationName))) {
                jsonRow.addPreprocessing(jsonPath, convertType);
            } else if (isToMany(metaMap.getAssociationTypeByFieldName(associationName))
                    && !isCustomObject(associationName)) {
                jsonRow.addDeferredAction(jsonPath, convertType);
            } else {
                jsonRow.addImmediateAction(jsonPath, convertType);
            }

        }
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
        return null;
    }

    @Override
    public Iterator<JsonRow> iterator() {
        return this;
    }
}
