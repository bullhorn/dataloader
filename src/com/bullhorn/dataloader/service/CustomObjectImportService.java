package com.bullhorn.dataloader.service;

import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bullhorn.dataloader.domain.CustomObject;
import com.bullhorn.dataloader.domain.MasterData;
import com.bullhorn.dataloader.util.BullhornAPI;

public class CustomObjectImportService implements Runnable, ConcurrentServiceInterface {
	
	private static Log log = LogFactory.getLog(CustomObjectImportService.class);
	
	Object obj;
	MasterData masterData;
	String BhRestToken;
	
	public void run() {
		
		try {
			
			BullhornAPI bhapi = new BullhornAPI();
			bhapi.setBhRestToken(BhRestToken);
			CustomObject co = (CustomObject) obj;
			co.setCustomObjectName(co.getCustomObjectName() + "s");
			
			// If there's an ID, disassociate then re-create
			// DELETE https://bhnext.bullhornstaffing.com/core/entity/Candidate/32056/customObject1s/3680
			if (co.getId() != null && co.getId().length() > 0) {
				String postURL = "https://bhnext.bullhornstaffing.com/core/entity/" + co.getEntity() + "/" + co.getEntityID() + "/" + co.getCustomObjectName() + "/" + co.getId();
				postURL = postURL + "?BhRestToken=" + BhRestToken;
				JSONObject jsResp = new JSONObject();
				DeleteMethod method = new DeleteMethod(postURL);
				jsResp = bhapi.delete(method);
				
				System.out.println(jsResp);
			}
			
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Inclusion.NON_NULL);
			mapper.setSerializationInclusion(Inclusion.NON_DEFAULT);
			
			JSONObject coObj = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			jsonArray.put(new JSONObject(mapper.writeValueAsString(co)));
			
			coObj.put(co.getCustomObjectName(), jsonArray);
			String jsString = coObj.toString();
			
			// Post to BH
			//https://bhnext.bullhornstaffing.com/core/entity/Candidate/32039
			//{"customObject2s":[{"id":3649,"int1":7},{"int1":4}]}
			String postURL = "https://bhnext.bullhornstaffing.com/core/entity/" + co.getEntity() + "/" + co.getEntityID();
			postURL = postURL + "?BhRestToken=" + BhRestToken;
			StringRequestEntity requestEntity = new StringRequestEntity(jsString,"application/json","UTF-8");
			JSONObject jsResp = new JSONObject();
			PostMethod method = new PostMethod(postURL);
			method.setRequestEntity(requestEntity);
			jsResp = bhapi.post(method);
			
			System.out.println(jsString);
			System.out.println(jsResp);
						
			
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

	public String getBhRestToken() {
		return BhRestToken;
	}

	public void setBhRestToken(String bhRestToken) {
		BhRestToken = bhRestToken;
	}

}
