package com.bullhorn.dataloader.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.bullhorn.dataloader.meta.MetaMap;
import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.csvreader.CsvWriter;
import com.google.common.collect.Sets;

public class TemplateUtil {

    private final BullhornAPI bullhornAPI;
    private final Set<String> compositeTypes = Sets.newHashSet("Address");
    private MetaMap metaMap;

    public TemplateUtil(BullhornAPI bullhornAPI) {
        this.bullhornAPI = bullhornAPI;
    }

    public void writeExampleEntityCsv(String entity) throws IOException {
        metaMap = bullhornAPI.getRootMetaDataTypes(entity);
        Map<String, String> fieldNameToDataType = metaMap.getFieldNameToDataType();
        Set<String> fieldNames = fieldNameToDataType.keySet();

        ArrayList<String> headers = new ArrayList<>();
        ArrayList<String> dataTypes = new ArrayList<>();
        for (String fieldName : fieldNames) {
            String dataType = fieldNameToDataType.get(fieldName);
            if (!isCompositeType(dataType) && !hasId(fieldName)) {
                headers.add(fieldName);
                dataTypes.add(dataType);
            }
        }

        CsvWriter csvWriter = new CsvWriter(entity + "Example.csv");
        csvWriter.writeRecord(headers.toArray(new String[0]));
        csvWriter.writeRecord(dataTypes.toArray(new String[0]));
        csvWriter.flush();
        csvWriter.close();
    }

    private boolean hasId(String column) {
        return metaMap.getDataTypeByFieldName(column + ".id").isPresent();
    }

    private boolean isCompositeType(String datetype) {
        return compositeTypes.contains(datetype);
    }
}
