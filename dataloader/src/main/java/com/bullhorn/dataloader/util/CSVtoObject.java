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
import com.bullhorn.dataloader.domain.ClientContact;
import com.bullhorn.dataloader.domain.ClientCorporation;
import com.bullhorn.dataloader.domain.CustomObject;
import com.bullhorn.dataloader.domain.Lead;
import com.bullhorn.dataloader.domain.Opportunity;
import com.bullhorn.dataloader.domain.TranslatedType;
import com.csvreader.CsvReader;


public class CSVtoObject {

    private static Log log = LogFactory.getLog(CSVtoObject.class);

    String entity;
    String filePath;
    String dateFormat;

    public List<Object> map() throws Exception {

        // Read CSV
        CsvReader imp = new CsvReader(filePath);
        imp.readHeaders();
        String[] columns = imp.getHeaders();

        List<Object> records = new ArrayList<Object>();

        // Iterate through CSV. Map headers to domain object. Skip anything that doesn't map
        while (imp.readRecord()) {
            try {
                // Get generic object based on entity that is being imported
                Object obj = getMappedObject(entity);
                Class<?> cls = obj.getClass();
                // Iterate through columns in CSV
                for (String s : columns) {
                    try {
                        // Case insensitive matching
                        Field fld = null;
                        Field[] fields = cls.getDeclaredFields();
                        for (Field f : fields) {
                            if (f.getName().equalsIgnoreCase(s)) {
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


    public Object getMappedObject(String entity) {

        if (entity.equalsIgnoreCase("candidate")) return new Candidate();
        if (entity.equalsIgnoreCase("opportunity")) return new Opportunity();
        if (entity.equalsIgnoreCase("clientcorporation")) return new ClientCorporation();
        if (entity.equalsIgnoreCase("clientcontact")) return new ClientContact();
        if (entity.equalsIgnoreCase("customobject")) return new CustomObject();
        if (entity.equalsIgnoreCase("lead")) return new Lead();

        return null;

    }

    public long convertDate(String date, String dateFormat) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
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

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
}