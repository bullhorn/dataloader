package com.bullhorn.dataloader.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.domain.MetaMap;
import com.csvreader.CsvReader;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CsvToJson implements Iterator, Iterable<Map<String, Object>> {
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

    private Map<String, Object> mapRow() {
        Map<String, Object> map = Maps.newHashMap();
        for(String column : columns) {
            String[] nestedProperties = column.split("\\.");
            Map<String, Object> tmpMap = map;
            for(int i = 0; i < nestedProperties.length - 1; i++) {
                String nestedProperty = nestedProperties[i];
                if(!tmpMap.containsKey(nestedProperty)) {
                    if("TO_MANY".equalsIgnoreCase(metaMap.getTypeByName(nestedProperty))) {
                        List<Map<String, Object>> list = Lists.newArrayList();
                        Map<String, Object> m = Maps.newHashMap();
                        list.add(m);
                        tmpMap.put(nestedProperty, list);
                        tmpMap = m;
                    } else {
                        Map<String, Object> m = Maps.newHashMap();
                        tmpMap.put(nestedProperty, m);
                        tmpMap = m;
                    }
                } else {
                    if("TO_MANY".equalsIgnoreCase(metaMap.getTypeByName(nestedProperty))) {
                        List<Map<String, Object>> list = (List<Map<String, Object>>)tmpMap.get(nestedProperty);
                        tmpMap = list.get(0);
                    } else {
                        tmpMap = (Map<String, Object>)tmpMap.get(nestedProperty);
                    }
                }
            }
            Object val;
            try {
                val = metaMap.convertType(column, get(column));
            } catch (Exception e) {
                log.error("Error reading column: \"" + column + "\" at row: " + csvReader.getCurrentRecord() + ". " + e.toString());
                continue;
            }
            String subPropertyName = nestedProperties[nestedProperties.length - 1];
            tmpMap.put(subPropertyName, val);
        }
        return map;
    }

    private String get(String column) {
        String val = null;
        try {
            val = csvReader.get(column);
        } catch (IOException e) {
            log.error("Error getting column: \"" + column + "\" for row: \"" + "\": " + e.toString());
        }
        return val;
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
    public Object next() {
        readLine();
        return mapRow();
    }

    @Override
    public Iterator<Map<String, Object>> iterator() {
        return this;
    }
}
