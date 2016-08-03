package com.bullhorn.dataloader.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.meta.Field;
import com.bullhornsdk.data.model.entity.meta.MetaData;
import com.bullhornsdk.data.model.enums.BullhornEntityInfo;
import com.bullhornsdk.data.model.enums.MetaParameter;
import com.csvreader.CsvWriter;
import com.google.common.collect.Sets;

public class TemplateUtil<B extends BullhornEntity> {

    private final Set<String> compositeTypes = Sets.newHashSet("Address");
    private BullhornData bullhornData;

    public TemplateUtil(BullhornData bullhornData) {
        this.bullhornData = bullhornData;
    }

    public void writeExampleEntityCsv(String entity) throws IOException {
        Set<Field> metaFieldSet = getMetaFieldSet(entity);

        ArrayList<String> headers = new ArrayList<>();
        ArrayList<String> dataTypes = new ArrayList<>();
        populateDataTypes(metaFieldSet, headers, dataTypes);

        writeToCsv(entity, headers, dataTypes);
    }

    private Set<Field> getMetaFieldSet(String entity) {
        MetaData<B> metaData = bullhornData.getMetaData(BullhornEntityInfo.getTypeFromName(entity).getType(), MetaParameter.FULL, null);
        Set<Field> metaFieldSet = new HashSet<>(metaData.getFields());
        Set<Field> associationFields = metaFieldSet.stream().filter(n -> n.getAssociatedEntity() != null).collect(Collectors.toSet());
        addAssociatedFields(metaFieldSet, associationFields);
        return metaFieldSet;
    }

    private void writeToCsv(String entity, ArrayList<String> headers, ArrayList<String> dataTypes) throws IOException {
        CsvWriter csvWriter = new CsvWriter(entity + "Example.csv");
        csvWriter.writeRecord(headers.toArray(new String[0]));
        csvWriter.writeRecord(dataTypes.toArray(new String[0]));
        csvWriter.flush();
        csvWriter.close();
    }

    private void populateDataTypes(Set<Field> metaFieldSet, ArrayList<String> headers, ArrayList<String> dataTypes) {
        for (Field field : metaFieldSet) {
            if (!isCompositeType(field.getDataType()) && !hasId(metaFieldSet, field.getName())) {
                headers.add(field.getName());
                dataTypes.add(field.getDataType());
            }
        }
    }

    private void addAssociatedFields(Set<Field> metaFieldSet, Set<Field> associationFields) {
        for (Field field : associationFields){
            field.getAssociatedEntity().getFields().stream().forEach(n -> n.setName(field.getName() + "." + n.getName()));
            metaFieldSet.addAll(field.getAssociatedEntity().getFields());
        }
    }

    private boolean hasId(Set<Field> metaFieldSet, String column) {
        return metaFieldSet.stream().map(n -> n.getName()).anyMatch(n -> n.equalsIgnoreCase(column + ".id"));
    }

    private boolean isCompositeType(String datetype) {
        return compositeTypes.contains(datetype);
    }
}
