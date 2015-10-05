package com.bullhorn.dataloader.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.domain.Candidate;
import com.bullhorn.dataloader.domain.ClientCorporation;
import com.bullhorn.dataloader.domain.CustomObject;
import com.bullhorn.dataloader.domain.MasterData;
import com.bullhorn.dataloader.domain.Opportunity;
import com.bullhorn.dataloader.domain.TranslatedType;
import com.csvreader.CsvReader;


public class CSVtoObject {

	private static Log log = LogFactory.getLog(CSVtoObject.class);
	
	String entity;
	String filePath;
	MasterData masterData;
	String numThreads;
	String dateFormat;
	
	public <T> List<Object> map() throws Exception {
		
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
		
		// Close CSV Reader
		imp.close();
		
		return records;
	}
	
		
	@SuppressWarnings("unchecked")
	public <T> T getMappedObject(String entity) {
		
		if (entity.equalsIgnoreCase("candidate")) return (T) new Candidate();
		if (entity.equalsIgnoreCase("opportunity")) return (T) new Opportunity();
		if (entity.equalsIgnoreCase("clientcorporation")) return (T) new ClientCorporation();
		if (entity.equalsIgnoreCase("customobject")) return (T) new CustomObject();
		
		return null;
		
	}
	
	public long convertDate(String date, String dateFormat) throws Exception{
		
		SimpleDateFormat sdf  = new SimpleDateFormat(dateFormat);
		Date dt = sdf.parse(date);
		long timeInMillisSinceEpoch = dt.getTime(); 
		
		return timeInMillisSinceEpoch;	
	}

	public String getEntity() {
		return entity;
	}
	public void setEntity(String entity) {
		this.entity = entity;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public MasterData getMasterData() {
		return masterData;
	}
	public void setMasterData(MasterData masterData) {
		this.masterData = masterData;
	}
	public String getNumThreads() {
		return numThreads;
	}
	public void setNumThreads(String numThreads) {
		this.numThreads = numThreads;
	}
	public String getDateFormat() {
		return dateFormat;
	}
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
}