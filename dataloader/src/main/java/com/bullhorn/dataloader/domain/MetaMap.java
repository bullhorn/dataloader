package com.bullhorn.dataloader.domain;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;

public class MetaMap {

    private final SimpleDateFormat simpleDateFormat;

    private Map<String, String> fieldNameToDataType = Maps.newHashMap();
    private Map<String, String> fieldMapLabelToDataType = Maps.newHashMap();
    private Map<String, String> fieldNameToAssociationType = Maps.newHashMap();
    private Map<String, String> rootFieldNameToEntityName = Maps.newHashMap(); //rootFieldName is the base entity for the "dot" notation (e.g. clientCompany for clientCompany.name)

    public MetaMap(SimpleDateFormat simpleDateFormat) {
        this.simpleDateFormat = simpleDateFormat;
    }

    public void setRootFieldNameToEntityName(String rootFieldName, String entityName) {
        rootFieldNameToEntityName.put(rootFieldName, entityName);
    }

    public Optional<String> getEntityNameByRootFieldName(String rootFieldName) {
        return Optional.ofNullable(rootFieldNameToEntityName.get(rootFieldName));
    }

    public void setFieldNameToDataType(String fieldName, String dataType) {
        fieldNameToDataType.put(fieldName, dataType);
    }

    public void setFieldMapLabelToDataType(String fieldMapLabel, String dataType) {
        fieldMapLabelToDataType.put(fieldMapLabel, dataType);
    }

    public void setFieldNameToAssociationType(String fieldName, String associationType) {
        fieldNameToAssociationType.put(fieldName, associationType);
    }

    public Optional<String> getDataTypeByFieldName(String fieldName) {
        return Optional.ofNullable(fieldNameToDataType.get(fieldName));
    }

    public Optional<String> getDataTypeByFieldMapLabel(String fieldMapLabel) {
        return Optional.ofNullable(fieldMapLabelToDataType.get(fieldMapLabel));
    }

    public String getAssociationTypeByFieldName(String fieldName) {
        return fieldNameToAssociationType.get(fieldName);
    }

    public Object convertFieldValue(String fieldName, String value) {
        Optional<String> dataType = determineDataType(fieldName);
        if(dataType.isPresent()) {
            MetaDataType metaDataType = MetaDataType.fromName(dataType.get());
            if(metaDataType != null) {
                return metaDataType.convertFieldValue(value, simpleDateFormat);
            }
        }
        return value;
    }

    /**
     * CSV supports both field name or field map labels
     * @param fieldName
     * @return dataType
     */
    protected Optional<String> determineDataType(String fieldName) {
        Optional<String> dataType = getDataTypeByFieldName(fieldName);
        if (!dataType.isPresent()) {
            dataType = getDataTypeByFieldMapLabel(fieldName);
        }
        return dataType;
    }

}
