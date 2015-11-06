package com.bullhorn.dataloader.domain;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;

public class MetaMap {
    private Map<String, String> nameToDataType = Maps.newHashMap();
    private Map<String, String> labelToDataType = Maps.newHashMap();
    private Map<String, String> associationType = Maps.newHashMap();

    public void addMeta(String name, String label, String dataType, String type) {
        nameToDataType.put(name, dataType);
        labelToDataType.put(label, dataType);
        associationType.put(name, type);
    }

    public Optional<String> getDataTypeByName(String name) {
        return Optional.ofNullable(nameToDataType.get(name));
    }

    public Optional<String> getDataTypeByLabel(String label) {
        return Optional.ofNullable(labelToDataType.get(label));
    }

    public String getTypeByName(String name) {
        return associationType.get(name);
    }

    public Object convertType(String columnName, String value) {
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
