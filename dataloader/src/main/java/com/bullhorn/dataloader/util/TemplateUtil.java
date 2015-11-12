package com.bullhorn.dataloader.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import com.bullhorn.dataloader.domain.MetaMap;
import com.bullhorn.dataloader.service.MasterDataService;
import com.google.common.collect.Sets;

public class TemplateUtil {

    private final BullhornAPI bullhornAPI;

    private final Set<String> compositeTypes = Sets.newHashSet("Address");

    private MetaMap metaMap;

    public TemplateUtil(BullhornAPI bullhornAPI) {
        this.bullhornAPI = bullhornAPI;
    }

    public void writeExampleEntityCsv(String entity) throws IOException, NoSuchFieldException, IllegalAccessException {
        String filePath = entity + "Example.csv";

        MasterDataService masterDataService = new MasterDataService();
        masterDataService.setBhapi(bullhornAPI);

        metaMap = bullhornAPI.getMetaDataTypes(entity);

        Field nameToDataTypeMap = MetaMap.class.getDeclaredField("nameToDataType");
        nameToDataTypeMap.setAccessible(true);
        Map<String, String> nameMap = (Map<String, String>) nameToDataTypeMap.get(metaMap);

        Set<String> nameColumns = nameMap.keySet();
        StringBuilder header = new StringBuilder();
        StringBuilder exampleRow = new StringBuilder();
        for (String column : nameColumns) {
            String datatype = metaMap.getDataTypeByFieldName(column).get();
            if (!isCompositeType(datatype)
                    && !hasId(column)) {
                header.append(column).append(",");
                exampleRow.append(datatype).append(",");
            }
        }
        String csvStr = header.toString();
        csvStr = csvStr.substring(0, csvStr.length() - 1);
        csvStr += "\n" + exampleRow.toString();
        csvStr = csvStr.substring(0, csvStr.length() - 1);

        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        fileOutputStream.write(csvStr.getBytes());
        fileOutputStream.close();
    }

    private boolean hasId(String column) {
        return metaMap.getDataTypeByFieldName(column + ".id").isPresent();
    }

    private boolean isCompositeType(String datetype) {
        return compositeTypes.contains(datetype);
    }

}
