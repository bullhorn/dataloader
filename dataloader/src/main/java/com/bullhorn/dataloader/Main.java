package com.bullhorn.dataloader;

import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.domain.MasterData;
import com.bullhorn.dataloader.service.ConcurrentService;
import com.bullhorn.dataloader.service.MasterDataService;
import com.bullhorn.dataloader.util.BullhornAPI;
import com.bullhorn.dataloader.util.CSVtoObject;
import com.bullhorn.dataloader.util.FileUtil;

public class Main {

    private static Log log = LogFactory.getLog(Main.class);

    public static void main(String[] args) {

        try {

            // Create API object. Pass this object to each import service via ConcurrentService
            // BullhornAPI contains REST session and helper methods to communicate with Bullhorn
            BullhornAPI bhapi = new BullhornAPI();

            // Entity to be imported and path to the CSV are passed in at runtime
            FileUtil fileUtil = new FileUtil();
            Properties props = fileUtil.getProps("dataloader.properties");

            String entity = args[0];
            String filePath = args[1];
            String numThreads = props.getProperty("numThreads");
            String dateFormat = props.getProperty("dateFormat");

            // Cache master data - category, skill, business sector, internal users
            MasterDataService mds = new MasterDataService();
            mds.setBhapi(bhapi);
            MasterData masterData = mds.getMasterData();

            // Read CSV and map to domain model
            CSVtoObject csv = new CSVtoObject();
            csv.setDateFormat(dateFormat);
            csv.setEntity(entity);
            csv.setFilePath(filePath);

            List<Object> records = csv.map();

            // Import to Bullhorn
            ConcurrentService impSvc = new ConcurrentService();
            impSvc.setNumThreads(numThreads);
            impSvc.setEntity(WordUtils.capitalize(entity) + "Import");
            impSvc.setRecords(records);
            impSvc.setMasterData(masterData);
            impSvc.setBhapi(bhapi);

            // Start import
            impSvc.runProcess();

        } catch (Exception e) {
            log.error(e);
        }
    }
}