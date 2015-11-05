package com.bullhorn.dataloader;

import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

import com.bullhorn.dataloader.domain.MetaMap;
import com.bullhorn.dataloader.service.MasterDataService;
import com.bullhorn.dataloader.util.BullhornAPI;
import com.bullhorn.dataloader.util.FileUtil;

public class GenerateExampleData {

    private void writeExampleEntityCsv(String entity) throws Exception {
        FileUtil fileUtil = new FileUtil();
        Properties props = fileUtil.getProps("dataloader.properties");

        BullhornAPI bhapi = new BullhornAPI(props);

        String filePath = entity + "Example.csv";

        MasterDataService masterDataService = new MasterDataService();
        masterDataService.setBhapi(bhapi);

        MetaMap metaMap = bhapi.getMetaDataTypes(entity);

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

    @Test
    public void generateEntityCsvExamples() throws Exception {
        String[] entities = new String[] {
                "Candidate", "ClientCorporation", "ClientContact", "Lead", "Opportunity", "JobOrder"
        };
        for(String entity : entities) {
            writeExampleEntityCsv(entity);
        }
    }
}
