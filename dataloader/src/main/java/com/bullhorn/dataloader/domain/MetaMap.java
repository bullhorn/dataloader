package com.bullhorn.dataloader.domain;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;

public class MetaMap {
    private Map<String, String> nameToDataType = Maps.newHashMap();
    private Map<String, String> labelToDataType = Maps.newHashMap();

    public void addMeta(String name, String label, String dataType) {
        nameToDataType.put(name, dataType);
        labelToDataType.put(label, dataType);
    }

    public Optional<String> getDataTypeByName(String name) {
        return Optional.ofNullable(nameToDataType.get(name));
    }

    public Optional<String> getDataTypeByLabel(String label) {
        return Optional.ofNullable(labelToDataType.get(label));
    }

    public Object convertType(String columnName, String value) throws Exception {
        Optional<String> dataType = getDataTypeByName(columnName);
        if(!dataType.isPresent()) {
            dataType = getDataTypeByLabel(columnName);
        }

        if(dataType.isPresent()) {
            String type = dataType.get();
            switch (type) {
                case "String":
                    return String.valueOf(value);
                case "Integer":
                    if(value.isEmpty()) {
                        return 0;
                    } else {
                        return Integer.valueOf(value);
                    }
                case "Double":
                    if(value.isEmpty()) {
                        return 0.0d;
                    } else {
                        return Double.valueOf(value);
                    }
                case "Boolean":
                    if(value.isEmpty()) {
                        return false;
                    } else {
                        return Boolean.valueOf(value);
                    }
            }
        }
        return value;
    }
}
