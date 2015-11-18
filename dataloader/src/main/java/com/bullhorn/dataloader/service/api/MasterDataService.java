package com.bullhorn.dataloader.service.api;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bullhorn.dataloader.meta.MasterData;
import com.bullhorn.dataloader.util.CaseInsensitiveStringPredicate;

public class MasterDataService {

    private final Log log = LogFactory.getLog(MasterDataService.class);

    private BullhornAPI bhapi;
    private MasterData masterData;

    // Generate map
    private HashMap<Integer, String> generateMap(String entity) {

        HashMap<Integer, String> map = new HashMap<>();

        try {

            String where = "id>0";
            String getURL = bhapi.getRestURL() + "query/" + entity + "?fields=id,name&where=" + URLEncoder.encode(where, "UTF-8") + "&count=500";
            getURL = getURL + "&BhRestToken=" + bhapi.getBhRestToken();
            GetMethod queryBH = new GetMethod(getURL);
            JSONObject qryJSON = bhapi.get(queryBH);
            if (qryJSON.getInt("count") > 0) {
                JSONArray jsAr = qryJSON.getJSONArray("data");
                for (int i = 0; i < jsAr.length(); i++) {
                    JSONObject obj = jsAr.getJSONObject(i);
                    map.put(obj.getInt("id"), obj.getString("name"));
                }
            }

        } catch (Exception e) {
            log.error(e);
        }

        return map;

    }

    private void associate(Integer id, String associationName, String assocationList, String entity) throws Exception {

        List<String> asscList = Arrays.asList(assocationList.split(","));
        HashMap<Integer, String> associatons = null;

        // Master list of associations
        if (CaseInsensitiveStringPredicate.isCategories(associationName)) {
            associatons = masterData.getCategories();
        } else if (CaseInsensitiveStringPredicate.isSkills(associationName)) {
            associatons = masterData.getSkills();
        } else if (CaseInsensitiveStringPredicate.isBusinessSectors(associationName)) {
            associatons = masterData.getBusinessSectors();
        }

        // Loop through assocations in spreadsheet. They will be a comma separated list of names
        // Get the associated key based on the value and construct a comma separate list of values
        if (associatons != null) {

            String asscIdList = "";
            for (String s : asscList) {
                if (asscIdList.length() <= 0) {
                    asscIdList = String.valueOf(getKeyByValue(associatons, s));
                } else {
                    asscIdList = asscIdList + "," + String.valueOf(getKeyByValue(associatons, s));
                }
            }

            //Disassociate then re-associate in order to do a "replace"
            String getURL = bhapi.getRestURL() + "entity/" + entity + "/" + id + "/" + associationName + "?fields=id" + "&BhRestToken=" + bhapi.getBhRestToken();

            GetMethod getMethod = new GetMethod(getURL);
            JSONObject ascIDJSON = bhapi.get(getMethod);
            log.info(ascIDJSON);

            if (ascIDJSON.getInt("count") > 0) {
                JSONArray jsAr = ascIDJSON.getJSONArray("data");
                String existingAssc = "";
                for (int i = 0; i < jsAr.length(); i++) {
                    JSONObject obj = jsAr.getJSONObject(i);
                    existingAssc = existingAssc + obj.getInt("id") + ",";
                }

                // There's no delete all, so have to get existing ID's and disassociate
                String deleteURL = bhapi.getRestURL() + "entity/" + entity + "/" + id + "/" + associationName + "/" + existingAssc + "?BhRestToken=" + bhapi.getBhRestToken();
                DeleteMethod deleteMethod = new DeleteMethod(deleteURL);
                JSONObject deleteJsResp = bhapi.delete(deleteMethod);
                log.info(deleteJsResp);
            }

            // Associate the id's in the spreadsheet
            String putURL = bhapi.getRestURL() + "entity/" + entity + "/" + id + "/" + associationName + "/" + asscIdList + "?BhRestToken=" + bhapi.getBhRestToken();

            PutMethod method = new PutMethod(putURL);
            JSONObject jsResp = bhapi.put(method);
            log.info(jsResp);
        }
    }

    public void associateCategories(Integer id, String categories, String entity) throws Exception {
        associate(id, "categories", categories, entity);
    }

    public void associateSkills(Integer id, String skills, String entity) throws Exception {
        associate(id, "skills", skills, entity);
    }

    public void associateBusinessSectors(Integer id, String businessSectors, String entity) throws Exception {
        associate(id, "businesssectors", businessSectors, entity);
    }

    // Lookup key by value
    private Integer getKeyByValue(Map<Integer, String> map, String value) {
        for (Entry<Integer, String> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return 0;
    }

    // When you request master data, rehydrate the MasterData object from BH
    private MasterData getMasterData() {

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

    public BullhornAPI getBhapi() {
        return bhapi;
    }

    public void setBhapi(BullhornAPI bhapi) {
        this.bhapi = bhapi;
    }

    public String getOwnerID(String ownerName) {
        return String.valueOf(getKeyByValue(getMasterData().getInternalUsers(), ownerName));
    }

    public String getCategoryID(String primaryCategory) {
        return String.valueOf(getKeyByValue(getMasterData().getCategories(), primaryCategory));
    }

    public String getBusinessSectorID(String primaryBusinessSector) {
        return String.valueOf(getKeyByValue(getMasterData().getCategories(), primaryBusinessSector));
    }
}