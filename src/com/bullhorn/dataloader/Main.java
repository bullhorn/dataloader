package com.bullhorn.dataloader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.domain.Candidate;
import com.bullhorn.dataloader.domain.ClientCorporation;
import com.bullhorn.dataloader.domain.CustomObject;
import com.bullhorn.dataloader.domain.MasterData;
import com.bullhorn.dataloader.domain.Opportunity;
import com.bullhorn.dataloader.domain.TranslatedType;
import com.bullhorn.dataloader.service.ConcurrentService;
import com.bullhorn.dataloader.service.MasterDataService;
import com.bullhorn.dataloader.util.FileUtil;
import com.csvreader.CsvReader;

public class Main {
	
	private static Log log = LogFactory.getLog(Main.class);
	
	public static void main(String[] args) {
			
		try {
			
			FileUtil fileUtil = new FileUtil();
			Properties props = fileUtil.getProps("dataloader.properties");
			
			String entity = args[0];
			String filePath = args[1];
			String BhRestToken = props.getProperty("BhRestToken");
			String numThreads = props.getProperty("numThreads");
			String dateFormat = props.getProperty("dateFormat");
			
			// Cache master data
			MasterDataService mds = new MasterDataService();
			mds.setBhRestToken(BhRestToken);
			
			// Start import
			doImport(entity, filePath, mds.getMasterData(), BhRestToken, numThreads, dateFormat);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
			
	private static <T> void doImport(String entity, 
									 String filePath, 
									 MasterData masterData, 
									 String BhRestToken, 
									 String numThreads, 
									 String dateFormat) throws Exception {
		
		// Read CSV
		CsvReader imp = new CsvReader(filePath);
		imp.readHeaders();
		String[] columns = imp.getHeaders();
		
		List<Object> records = new ArrayList<Object>();
		
		// Iterate through CSV. Map headers to domain object. Skip anything that doesn't map
		while(imp.readRecord()) {
			try {
				// Get generic object based on entity that is being imported
				T obj = getMappedObject(entity);
				@SuppressWarnings("rawtypes")
				Class cls = obj.getClass();
				// Iterate through columns in CSV
				for (String s : columns) {
					try {
						// Case insensitive matching
						Field fld = null;
						Field[] fields = cls.getDeclaredFields();
						for(Field f: fields){
						    if(f.getName().equalsIgnoreCase(s)){
						    	fld = f;
						    	break;
						    }
						}
						// Set the object properties
						if (fld != null) {
							// If there's a translation annotation
							if (fld.isAnnotationPresent(TranslatedType.class)) {
								Annotation annotation = fld.getAnnotation(TranslatedType.class);
								TranslatedType tt = (TranslatedType) annotation;
								if (tt.isDate()) {
									fld.set(obj, convertDate(imp.get(s), dateFormat));
								} else {
									fld.set(obj, imp.get(s));
								}
							} else {
								fld.set(obj, imp.get(s));
							}
						}
					} catch (Exception e) {
						log.info(e.getMessage() + " header not in domain object");
					}
				}
				records.add(obj);
			} catch (Exception e) {
				log.info(e.getMessage() + " cannot read from CSV");
			}
		}
		
		// Instantiate concurrency service
		ConcurrentService impSvc = new ConcurrentService();
		impSvc.setNumThreads(numThreads);
		impSvc.setEntity(WordUtils.capitalize(entity) + "Import");
		impSvc.setRecords(records);
		impSvc.setMasterData(masterData);
		impSvc.setBhRestToken(BhRestToken);
		
		// Start import
		impSvc.runProcess();
		
		// Close CSV Reader
		imp.close();
		
	}
	
		
	@SuppressWarnings("unchecked")
	private static <T> T getMappedObject(String entity) {
		
		if (entity.equalsIgnoreCase("candidate")) return (T) new Candidate();
		if (entity.equalsIgnoreCase("opportunity")) return (T) new Opportunity();
		if (entity.equalsIgnoreCase("clientcorporation")) return (T) new ClientCorporation();
		if (entity.equalsIgnoreCase("customobject")) return (T) new CustomObject();
		
		return null;
		
	}
	
	public static long convertDate(String date, String dateFormat) throws Exception{
		
		SimpleDateFormat sdf  = new SimpleDateFormat(dateFormat);
		Date dt = sdf.parse(date);
		long timeInMillisSinceEpoch = dt.getTime(); 
		
		return timeInMillisSinceEpoch;	
	}

}


