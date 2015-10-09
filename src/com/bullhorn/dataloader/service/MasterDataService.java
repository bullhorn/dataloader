package com.bullhorn.dataloader.service;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bullhorn.dataloader.domain.MasterData;
import com.bullhorn.dataloader.util.BullhornAPI;

public class MasterDataService {

	private static Log log = LogFactory.getLog(MasterDataService.class);	
	
	String BhRestToken;
	MasterData masterData = new MasterData();
	
	// Generate map
	private HashMap<Integer, String> generateMap(String entity) {
		
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		
		try {
			
			BullhornAPI bhapi = new BullhornAPI();
			
			String where = "id>0";
			String getURL = bhapi.getRestURL() + "query/" + entity + "/?fields=id,name&where=" + URLEncoder.encode(where, "UTF-8") + "&count=500";
			getURL = getURL + "&BhRestToken=" + BhRestToken;
			GetMethod queryBH = new GetMethod(getURL);
			JSONObject qryJSON = bhapi.get(queryBH);
			if (qryJSON.getInt("count") > 0) {
				JSONArray jsAr = qryJSON.getJSONArray("data");
				 for (int i = 0 ; i < jsAr.length(); i++) {
				        JSONObject obj = jsAr.getJSONObject(i);
				        map.put(obj.getInt("id"), obj.getString("name"));
				 }
			}

		} catch (Exception e) {
			log.error(e);
		}
		
		return map;
		
	}
	
	public void associateCategories(Integer id, String categories, String entity) throws Exception {
		
		BullhornAPI bhapi = new BullhornAPI();
		
		List<String> categoryList = Arrays.asList(categories.split(","));
		
		// Master list of categories
		HashMap<Integer, String> masterCategories = masterData.getCategories();
		
		// Loop through categories in spreadsheet. They will be a comma separated list of names
		// Get the associated key based on the value and construct a comma separate list of values
		String categoryIdList = "";
		for (String s : categoryList) {
			if (categoryIdList.length() <= 0) {
				categoryIdList = String.valueOf(getKeyByValue(masterCategories, s));
			} else {
				categoryIdList = categoryIdList + "," + String.valueOf(getKeyByValue(masterCategories, s));
			}
		}
		
		
		String postURL = bhapi.getRestURL() + "entity/" + entity + "/" + id + "/categories/" + categoryIdList + "?BhRestToken=" + BhRestToken;
		
		PutMethod method = new PutMethod(postURL);
		JSONObject jsResp = new JSONObject();
		jsResp = bhapi.put(method);
		
		System.out.println(jsResp);
		
	}
	
	// Lookup key by value
	public Integer getKeyByValue(Map<Integer, String> map, String value) {
	    for (Entry<Integer, String> entry : map.entrySet()) {
	        if (Objects.equals(value, entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}

	// When you request master data, rehydrate the MasterData object from BH
	public MasterData getMasterData() {
		
		masterData.setCategories(generateMap("Category"));
		masterData.setSkills(generateMap("Skill"));
		masterData.setBusinessSectors(generateMap("BusinessSector"));
		masterData.setInternalUsers(generateMap("CorporateUser"));
		
		return masterData;
	}

	// When you instantiate the MDS, you should pass it a cached object if you have it
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