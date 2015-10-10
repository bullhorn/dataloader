package com.bullhorn.dataloader.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.bullhorn.dataloader.domain.Address;
import com.bullhorn.dataloader.domain.ClientCorporation;
import com.bullhorn.dataloader.domain.ID;
import com.bullhorn.dataloader.domain.MasterData;
import com.bullhorn.dataloader.util.BullhornAPI;

public class ClientCorporationImportService implements Runnable, ConcurrentServiceInterface {
	
	private static Log log = LogFactory.getLog(ClientCorporationImportService.class);
	
	Object obj;
	MasterData masterData;
	BullhornAPI bhapi;
	
	public Integer clientCorporation() {
		
		try {
			
			ClientCorporation corp = (ClientCorporation) obj;
			
			// Check if record exists in BH
			JSONObject qryJSON = bhapi.doesRecordExist(corp);
			
			// Assemble URL
			String type = "put";
			String postURL = bhapi.getRestURL() + "entity/ClientCorporation";
			if (corp.getClientCorporationID() != null && corp.getClientCorporationID().length() > 0) {
				if (qryJSON.getJSONObject("data").getInt("id") > 0) {
					postURL = postURL + "/" +  qryJSON.getJSONObject("data").getInt("id");
					type = "post";
				}
			}
			
			postURL = postURL + "?BhRestToken=" + bhapi.getBhRestToken();
			
			// Populate address fields
			if (corp.getAddress() == null) {
				Address address = new Address();
				address.setAddress1(corp.getAddress1());
				address.setAddress2(corp.getAddress2());
				address.setCity(corp.getCity());
				address.setCountryID(corp.getCountry());
				address.setState(corp.getState());
				address.setZip(corp.getZip());
				corp.setAddress(address);
			}
			
			// If there's a parent corporation, associate it
			if (corp.getParentClientCorporation() == null && 
					corp.getParentClientCorporationID() != null &&
					corp.getParentClientCorporationID().length() > 0) {
				ID parentClientCorporationID = new ID();
				parentClientCorporationID.setId(corp.getParentClientCorporationID());
				corp.setParentClientCorporation(parentClientCorporationID);
			}
			
			JSONObject jsResp = bhapi.save(corp, postURL, type);
			
			// Get ID of the created/updated record
			int clientCorporationID = jsResp.getInt("changedEntityId");
			
			return clientCorporationID;
			
		} catch (Exception e) {
			log.error(e);
		}
		
		return null;
	}
	
	public void run() {
		clientCorporation();
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
