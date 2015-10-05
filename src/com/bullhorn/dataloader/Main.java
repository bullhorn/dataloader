package com.bullhorn.dataloader;

import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.domain.MasterData;
import com.bullhorn.dataloader.service.ConcurrentService;
import com.bullhorn.dataloader.service.MasterDataService;
import com.bullhorn.dataloader.util.CSVtoObject;
import com.bullhorn.dataloader.util.FileUtil;

public class Main {
	
	private static Log log = LogFactory.getLog(Main.class);
	
	public static void main(String[] args) {
			
		try {
			
			FileUtil fileUtil = new FileUtil();
			Properties props = fileUtil.getProps("dataloader.properties");
			
			String entity = args[0];
			String filePath = args[1];
			String BhRestToken = args[2];
			String numThreads = props.getProperty("numThreads");
			String dateFormat = props.getProperty("dateFormat");
			
			// Cache master data
			MasterDataService mds = new MasterDataService();
			mds.setBhRestToken(BhRestToken);
			MasterData masterData = mds.getMasterData();
			
			// Read CSV and map to domain model
			CSVtoObject csv = new CSVtoObject();
			csv.setDateFormat(dateFormat);
			csv.setEntity(entity);
			csv.setFilePath(filePath);
			csv.setMasterData(masterData);
			csv.setNumThreads(numThreads);
			
			List<Object> records = csv.map();
			
			// Import to Bullhorn
			ConcurrentService impSvc = new ConcurrentService();
			impSvc.setNumThreads(numThreads);
			impSvc.setEntity(WordUtils.capitalize(entity) + "Import");
			impSvc.setRecords(records);
			impSvc.setMasterData(masterData);
			impSvc.setBhRestToken(BhRestToken);
			
			// Start import
			impSvc.runProcess();
			
		} catch (Exception e) {
			log.error(e);
		}
	}
}