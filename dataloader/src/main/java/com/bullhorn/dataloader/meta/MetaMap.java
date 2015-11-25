package com.bullhorn.dataloader.meta;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.bullhorn.dataloader.util.CaseInsensitiveStringPredicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MetaMap {

    private final String ESCAPE_DELIMITER_REGEX = "[\\\\\\.\\[\\]\\{\\}\\(\\)\\*\\+\\-\\?\\^\\$\\|]";
    private final SimpleDateFormat simpleDateFormat;
    private final String listDelimiter;
    private final String escapedListDelimiter;

    private Map<String, String> fieldNameToDataType = Maps.newHashMap();
    private Map<String, String> fieldMapLabelToDataType = Maps.newHashMap();
    private Map<String, String> fieldNameToAssociationType = Maps.newHashMap();
    private Map<String, String> rootFieldNameToEntityName = Maps.newHashMap(); //rootFieldName is the base entity for the "dot" notation (e.g. clientCompany for clientCompany.name)

    public MetaMap(SimpleDateFormat simpleDateFormat, String listDelimiter) {
        this.simpleDateFormat = simpleDateFormat;
        this.listDelimiter = listDelimiter;
        this.escapedListDelimiter = escapeDelimiter(listDelimiter);
    }


    public Map<String, String> getFieldNameToDataType() {
        return ImmutableMap.copyOf(fieldNameToDataType);
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

    public boolean hasField(String fieldName) {
        return getDataTypeByFieldName(fieldName).isPresent();
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
                if(isDelimitedToManyField(fieldName, value)) {
                    return getMultiValueResult(value, metaDataType);
                } else {
                    return metaDataType.convertFieldValue(value, simpleDateFormat);
                }
            }
        }
        return value;
    }

    private boolean isDelimitedToManyField(String fieldName, String value) {
        String[] fieldNames = fieldName.split("\\.");
        if(fieldNames.length > 0) {
            return value.contains(listDelimiter) && CaseInsensitiveStringPredicate.isToMany(getAssociationTypeByFieldName(fieldNames[0]));
        }
        return false;
    }

    private Object getMultiValueResult(String value, MetaDataType metaDataType) {
        String[] values = value.split(escapedListDelimiter);
        List<Object> result = Lists.newArrayList();
        for(String v : values) {
            result.add(metaDataType.convertFieldValue(v, simpleDateFormat));
        }
        return result;
    }

    /**
     * CSV supports both field name or field map labels
     * @param fieldName
     * @return
     */
    Optional<String> determineDataType(String fieldName) {
        Optional<String> dataType = getDataTypeByFieldName(fieldName);
        if (!dataType.isPresent()) {
            dataType = getDataTypeByFieldMapLabel(fieldName);
        }
        return dataType;
    }

    private String escapeDelimiter(String listDelimiter) {
        String result = listDelimiter;
        Pattern pattern = Pattern.compile(ESCAPE_DELIMITER_REGEX);
        if(pattern.matcher(result).matches()) {
            result = "\\" + result;
        }
        return result;
    }
}
