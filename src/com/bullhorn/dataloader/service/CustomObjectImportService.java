package com.bullhorn.dataloader.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bullhorn.dataloader.domain.CustomObject;
import com.bullhorn.dataloader.domain.MasterData;
import com.bullhorn.dataloader.util.BullhornAPI;

public class CustomObjectImportService implements Runnable, ConcurrentServiceInterface {
	
	private static Log log = LogFactory.getLog(CustomObjectImportService.class);
	
	Object obj;
	MasterData masterData;
	BullhornAPI bhapi;
	
	public void run() {
		
		try {
			
			CustomObject co = (CustomObject) obj;
			co.setCustomObjectName(co.getCustomObjectName() + "s");
			
			// If there's an ID, disassociate then re-create
			if (co.getId() != null && co.getId().length() > 0) {
				String postURL = bhapi.getRestURL() + "entity/" + co.getEntity() + "/" + co.getEntityID() + "/" + co.getCustomObjectName() + "/" + co.getId();
				postURL = postURL + "?BhRestToken=" + bhapi.getBhRestToken();
				bhapi.delete(postURL);
			}
						
			JSONObject coObj = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			jsonArray.put(new JSONObject(bhapi.serialize(co)));
			
			coObj.put(co.getCustomObjectName(), jsonArray);
			String jsString = coObj.toString();
			
			// Post to BH
			String postURL = bhapi.getRestURL() + "entity/" + co.getEntity() + "/" + co.getEntityID();
			postURL = postURL + "?BhRestToken=" + bhapi.getBhRestToken();
			
			bhapi.save(jsString, postURL, "post");
			
			
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	
	public Object getObj() {
		return obj;
	}
	
	public void setObj(Object obj) {
		this.obj = obj;
	}

	public MasterData getMasterData() {
		return masterData;
	}

	public void setMasterData(MasterData masterData) {
		this.masterData = masterData;
	}


	public BullhornAPI getBhapi() {
		return bhapi;
	}


	public void setBhapi(BullhornAPI bhapi) {
		this.bhapi = bhapi;
	}

}
