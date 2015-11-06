package com.bullhorn.dataloader.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import com.bullhorn.dataloader.domain.MetaMap;
import com.bullhorn.dataloader.service.MasterDataService;

public class TemplateUtil {

    private final BullhornAPI bullhornAPI;

    public TemplateUtil(BullhornAPI bullhornAPI) {
        this.bullhornAPI = bullhornAPI;
    }

    public void writeExampleEntityCsv(String entity) throws IOException, NoSuchFieldException, IllegalAccessException {
        String filePath = entity + "Example.csv";

        MasterDataService masterDataService = new MasterDataService();
        masterDataService.setBhapi(bullhornAPI);

        MetaMap metaMap = bullhornAPI.getMetaDataTypes(entity);

        Field nameToDataTypeMap = MetaMap.class.getDeclaredField("nameToDataType");
        nameToDataTypeMap.setAccessible(true);
        Map<String, String> nameMap = (Map<String, String>) nameToDataTypeMap.get(metaMap);

        Set<String> nameColumns = nameMap.keySet();
        StringBuffer header = new StringBuffer();
        StringBuffer exampleRow = new StringBuffer();
        for (String column : nameColumns) {
            header.append(column + ",");
            exampleRow.append(metaMap.getDataTypeByName(column).get() + ",");
        }
        String csvStr = header.toString();
        csvStr = csvStr.substring(0, csvStr.length() - 1);
        csvStr += "\n" + exampleRow.toString();
        csvStr = csvStr.substring(0, csvStr.length() - 1);

        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        fileOutputStream.write(csvStr.getBytes());
        fileOutputStream.close();
    }

}
