package com.bullhorn.dataloader.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;

public class MetaMap {
    private final Log log = LogFactory.getLog(MetaMap.class);

    private final SimpleDateFormat simpleDateFormat;

    private Map<String, String> nameToDataType = Maps.newHashMap();
    private Map<String, String> labelToDataType = Maps.newHashMap();
    private Map<String, String> associationType = Maps.newHashMap();
    private Map<String, String> nameToLabel = Maps.newHashMap();

    public MetaMap(SimpleDateFormat simpleDateFormat) {
        this.simpleDateFormat = simpleDateFormat;
    }

    public void setNameToAssociatedEntityName(String name, String label) {
        nameToLabel.put(name, label);
    }

    public Optional<String> getLabelByName(String name) {
        return Optional.ofNullable(nameToLabel.get(name));
    }

    public void setNameToDataType(String name, String dataType) {
        nameToDataType.put(name, dataType);
    }

    public void setLabelToDataType(String label, String dataType) {
        labelToDataType.put(label, dataType);
    }

    public void setAssociationToType(String association, String type) {
        associationType.put(association, type);
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
                case "Timestamp":
                    if(value.isEmpty()) {
                        return null;
                    } else {
                        try {
                            return simpleDateFormat.parse(value);
                        } catch (ParseException e) {
                            log.error(e);
                            return value;
                        }
                    }
            }
        }
        return value;
    }
}
