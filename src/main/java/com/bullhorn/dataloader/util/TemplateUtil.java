package com.bullhorn.dataloader.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
    private MetaData<B> metaData;
    private BullhornData bullhornData;

    public TemplateUtil(BullhornData bullhornData) {
        this.bullhornData = bullhornData;
    }

    public void writeExampleEntityCsv(String entity) throws IOException {
        metaData = bullhornData.getMetaData(BullhornEntityInfo.getTypeFromName(entity).getType(), MetaParameter.FULL, null);
        Set<Field> fieldSet = new HashSet<>(metaData.getFields());

        ArrayList<String> headers = new ArrayList<>();
        ArrayList<String> dataTypes = new ArrayList<>();
        for (Field field : fieldSet) {
            if (!isCompositeType(field.getDataType())) {
                headers.add(field.getName());
                dataTypes.add(field.getDataType());
            }
        }

        CsvWriter csvWriter = new CsvWriter(entity + "Example.csv");
        csvWriter.writeRecord(headers.toArray(new String[0]));
        csvWriter.writeRecord(dataTypes.toArray(new String[0]));
        csvWriter.flush();
        csvWriter.close();
    }

    private boolean isCompositeType(String datetype) {
        return compositeTypes.contains(datetype);
    }
}
