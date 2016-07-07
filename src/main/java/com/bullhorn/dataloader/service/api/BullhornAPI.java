package com.bullhorn.dataloader.service.api;

import static com.bullhorn.dataloader.util.AssociationFilter.isCustomObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bullhorn.dataloader.meta.MetaMap;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.AssociationFilter;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class BullhornAPI {

    private static final Logger log = LogManager.getLogger(BullhornAPI.class);

    private static final String AUTH_CODE_ACTION = "Login";
    private static final String AUTH_CODE_RESPONSE_TYPE = "code";
    private static final String ACCESS_TOKEN_GRANT_TYPE = "authorization_code";

    private String bhRestToken;
    private String restURL;
    private int privateLabel;

    private MetaMap rootMetaMap;
    private Map<String, MetaMap> metaMaps = new ConcurrentHashMap<>();
    private ConcurrentMap<String, BiMap<String, Integer>> frontLoadedValues = Maps.newConcurrentMap();

    private PropertyFileUtil propertyFileUtil;

    public BullhornAPI(PropertyFileUtil propertyFileUtil) {
        this.propertyFileUtil = propertyFileUtil;
    }

    public void createSession() {
        try {
            String authCode = getAuthorizationCode();
            String accessToken = getAccessToken(authCode);
            loginREST(accessToken);
            privateLabel = getPrivateLabelFromRest();
        } catch (Exception e) {
            log.error("Failed to create session. Please check your clientId and clientSecret properties.", e);
        }
    }

    public void frontLoad() throws IOException {
        for (String entity : propertyFileUtil.getFrontLoadedEntities()) {
            BiMap<String, Integer> frontLoadedCache = frontLoadEntity(entity, getFirstToManyExistField(entity));
            frontLoadedValues.put(entity, frontLoadedCache);
        }
    }

    private BiMap<String, Integer> frontLoadEntity(String entity, String existFieldKey) throws IOException {
        log.info("Front loading " + entity);
        int currentIndex = 0;
        boolean stillGoing = true;
        BiMap<String, Integer> frontLoadedCache = HashBiMap.create();
        while (stillGoing) {
            JSONObject jsonObject = getEntityPage(entity, propertyFileUtil.getPageSize(), currentIndex);
            if (!jsonObject.has(StringConsts.DATA)
                    || jsonObject.getJSONArray(StringConsts.DATA).length() == 0) {
                stillGoing = false;
            } else {
                currentIndex = frontLoadRecord(existFieldKey, currentIndex, frontLoadedCache, jsonObject);
            }
        }
        return frontLoadedCache;
    }

    private int frontLoadRecord(String existFieldKey, int currentIndex, BiMap<String, Integer> frontLoadedCache, JSONObject dataPage) {
        JSONArray entityPage = dataPage.getJSONArray(StringConsts.DATA);
        for (Object jsnObject : entityPage) {
            JSONObject dataElement = (JSONObject) jsnObject;
            frontLoadedCache.put(dataElement.getString(existFieldKey), dataElement.getInt("id"));
        }
        return currentIndex + propertyFileUtil.getPageSize();
    }

    public String getFirstToManyExistField(String entity) {
        String existFieldKey = StringConsts.NAME;

        Optional<List<String>> existFields = propertyFileUtil.getEntityExistFields(entity);
        if (existFields.isPresent()) {
            existFieldKey = existFields.get().get(0);
        }

        return existFieldKey;
    }

    private JSONObject getEntityPage(String entity, int pageSize, int currentIndex) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(getRestURL());
        sb.append(StringConsts.QUERY);
        sb.append(entity);
        sb.append("?fields=*&where=");
        if (entity.equals(StringConsts.CATEGORY)) {
            sb.append(URLEncoder.encode("id > -1 AND " + getPrivateLabel() + " member of privateLabels", StringConsts.UTF));
        } else {
            sb.append(URLEncoder.encode("id > -1", StringConsts.UTF));
        }
        sb.append("&count=");
        sb.append(pageSize);
        sb.append("&start=");
        sb.append(currentIndex);
        sb.append(StringConsts.AND_BH_REST_TOKEN);
        sb.append(getBhRestToken());
        GetMethod getMethod = new GetMethod(sb.toString());
        return call(getMethod);
    }

    private String getAuthorizationCode() throws IOException {
        String authorizeCodeUrl = propertyFileUtil.getAuthorizeUrl() + "?client_id=" + propertyFileUtil.getClientId() +
                "&response_type=" + AUTH_CODE_RESPONSE_TYPE +
                "&action=" + AUTH_CODE_ACTION +
                "&username=" + propertyFileUtil.getUsername() +
                "&password=" + propertyFileUtil.getPassword();

        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(authorizeCodeUrl);
        client.executeMethod(method);

        String returnURL = method.getResponseHeader("Location").getValue();
        returnURL = returnURL.substring(returnURL.indexOf("?") + 1);
        Map<String, String> map = Splitter.on("&").trimResults().withKeyValueSeparator('=').split(returnURL);

        return map.get("code");
    }

    /**
     * Get access token based on auth code returned from getAuthorizationCode()
     */
    private String getAccessToken(String authCode) throws IOException {
        String url = propertyFileUtil.getTokenUrl() + "?grant_type=" + ACCESS_TOKEN_GRANT_TYPE +
                "&code=" + authCode +
                "&client_id=" + propertyFileUtil.getClientId() +
                "&client_secret=" + propertyFileUtil.getClientSecret();

        PostMethod postMethod = new PostMethod(url);
        JSONObject jsonResponse = call(postMethod);

        return jsonResponse.getString("access_token");
    }

    private void loginREST(String accessToken) {
        JSONObject responseJson;
        try {
            String accessTokenString = URLEncoder.encode(accessToken, StringConsts.UTF);
            String sessionMinutesToLive = "3000"; // 50hr window
            String url = propertyFileUtil.getLoginUrl() + "?version=" + "*" + "&access_token=" + accessTokenString + "&ttl=" + sessionMinutesToLive;
            GetMethod get = new GetMethod(url);

            HttpClient client = new HttpClient();
            client.executeMethod(get);
            String responseStr = IOUtils.toString(get.getResponseBodyAsStream());
            responseJson = new JSONObject(responseStr);

            // Cache bhRestToken and REST URL
            bhRestToken = responseJson.getString(StringConsts.BH_REST_TOKEN);
            restURL = (String) responseJson.get("restUrl");

        } catch (Exception e) {
            log.error(e);
        }
    }

    private int getPrivateLabelFromRest() throws IOException {
        String url = getRestURL() + "settings/privateLabelId?BhRestToken=" + getBhRestToken();
        GetMethod get = new GetMethod(url);
        HttpClient client = new HttpClient();
        client.executeMethod(get);
        String response = IOUtils.toString(get.getResponseBodyAsStream());
        JSONObject json = new JSONObject(response);

        return json.getJSONObject("privateLabelId").getInt("id");
    }

    public String getModificationAssociationUrl(EntityInstance parentEntity, EntityInstance childEntity) {
        return this.getRestURL() + StringConsts.ENTITY_SLASH
                + parentEntity.getEntityName() + "/"
                + parentEntity.getEntityId() + "/"
                + childEntity.getEntityName() + "/"
                + childEntity.getEntityId()
                + StringConsts.END_BH_REST_TOKEN + this.getBhRestToken();
    }

    public JSONObject call(HttpMethodBase httpMethod) throws IOException {
        HttpClient client = new HttpClient();
        client.executeMethod(httpMethod);
        String responseStr = IOUtils.toString(httpMethod.getResponseBodyAsStream());
        return new JSONObject(responseStr);
    }

    private static final List<String> CUSTOM_OBJECT_META_FIELDS = ImmutableList.of(
            "customObject1s",
            "customObject2s",
            "customObject3s",
            "customObject4s",
            "customObject5s",
            "customObject6s",
            "customObject7s",
            "customObject8s",
            "customObject9s"
    );

    private static final String CUSTOM_OBJECT_ADDITIONAL_FIELDS = Joiner.on("(*),").join(CUSTOM_OBJECT_META_FIELDS).concat("(*)");

    public MetaMap getMetaDataTypes(String entity) throws IOException {
        if (!metaMaps.containsKey(entity)) {
            JSONArray fields = getFieldsFromMetaResponse(entity, "*");
            addCustomObjectsFieldsWhenApplicable(entity, fields);

            Deque<JsonObjectFields> deque = new ArrayDeque<>();
            JsonObjectFields jsonObjectFields = new JsonObjectFields("", fields);
            deque.add(jsonObjectFields);

            MetaMap metaMap = new MetaMap(propertyFileUtil.getDateParser(), propertyFileUtil.getListDelimiter());
            while (!deque.isEmpty()) {
                jsonObjectFields = deque.pop();
                fields = jsonObjectFields.getJsonArray();
                addEachFieldToMetaAndQueueNestedFields(fields, deque, jsonObjectFields, metaMap);
            }
            metaMaps.put(entity, metaMap);
            return metaMap;
        }
        return metaMaps.get(entity);
    }

    /**
     * CustomObjects are TO_MANY's, but we can and must treat them as if
     * they were TO_ONE.
     * Here we inject customObject meta fields into the general meta for the given Entity
     * as TO_MANY's in a 'fields=*' meta query only returns id.
     */
    private void addCustomObjectsFieldsWhenApplicable(String entity, JSONArray fields) throws IOException {
        if (fieldsContainsCustomObjects(fields)) {
            JSONArray customObjectFields = getFieldsFromMetaResponse(entity, CUSTOM_OBJECT_ADDITIONAL_FIELDS);
            for (Object customObjectField : customObjectFields) {
                fields.put(customObjectField);
            }
        }
    }

    private boolean fieldsContainsCustomObjects(JSONArray fields) {
        for (int i = 0; i < fields.length(); i++) {
            JSONObject field = fields.getJSONObject(i);
            Optional<String> optionalEntity = getAssociatedEntity(field);
            if (optionalEntity.isPresent() && isCustomObject(optionalEntity.get())) {
                return true;
            }
        }
        return false;
    }

    private JSONArray getFieldsFromMetaResponse(String entity, String fields) throws IOException {
        String url = getMetaUrl(entity, fields);
        GetMethod getMethod = new GetMethod(url);
        JSONObject jsonObject = call(getMethod);
        return jsonObject.getJSONArray("fields");
    }

    private String getMetaUrl(String entity, String fields) {
        return this.getRestURL() + "meta/" + entity + "?fields=" + fields + "&BhRestToken=" + this.getBhRestToken();
    }

    private void addEachFieldToMetaAndQueueNestedFields(JSONArray fields,
                                                        Deque<JsonObjectFields> deque,
                                                        JsonObjectFields jsonObjectFields,
                                                        MetaMap meta) {
        for (int i = 0; i < fields.length(); i++) {
            JSONObject field = fields.getJSONObject(i);

            JSONArray subFields = getSubField(field);

            String name = field.getString("name");
            String type = field.getString("type");

            if (subFields != null) {
                String path = jsonObjectFields.getPath();
                path = path.isEmpty() ? name + "." : path + name + ".";
                JsonObjectFields nestedFields = new JsonObjectFields(path, subFields);
                String dataType = getDataType(field);
                String label = getLabel(field);
                Optional<String> associatedEntity = getAssociatedEntity(field);
                String entityName = path.substring(0, path.length() - 1);
                if (associatedEntity.isPresent()) {
                    meta.setRootFieldNameToEntityName(entityName, associatedEntity.get());
                }
                meta.setFieldMapLabelToDataType(label, dataType);
                meta.setFieldNameToDataType(entityName, dataType);
                meta.setFieldNameToAssociationType(entityName, type);
                deque.add(nestedFields);
            } else {
                String dataType = getDataType(field);
                String label = getLabel(field);
                String entityUniversalResourceName = jsonObjectFields.getPath() + name;
                meta.setFieldMapLabelToDataType(label, dataType);
                meta.setFieldNameToDataType(entityUniversalResourceName, dataType);
                meta.setFieldNameToAssociationType(entityUniversalResourceName, type);
            }
        }
    }

    private static Optional<String> getAssociatedEntity(JSONObject field) {
        try {
            return Optional.of(field.getJSONObject("associatedEntity").getString("entity"));
        } catch (JSONException e) {
            return Optional.empty();
        }
    }

    // Save a stringify'd object
    private JSONObject saveNonToMany(String jsString, String url, String type) throws IOException {

        // Post to BH
        StringRequestEntity requestEntity = new StringRequestEntity(jsString, StringConsts.APPLICATION_JSON, StringConsts.UTF);
        JSONObject jsonResponse;
        if (AssociationFilter.isPut(type)) {
            PutMethod putMethod = new PutMethod(url);
            putMethod.setRequestEntity(requestEntity);
            jsonResponse = call(putMethod);
        } else {
            PostMethod postMethod = new PostMethod(url);
            postMethod.setRequestEntity(requestEntity);
            jsonResponse = call(postMethod);
        }

        log.info(jsString);
        log.info(jsonResponse);

        return jsonResponse;
    }

    private static String getLabel(JSONObject field) {
        String label;
        try {
            label = field.getString("label");
        } catch (JSONException e) {
            label = "";
        }
        return label;
    }

    private static String getDataType(JSONObject field) {
        String dataType;
        try {
            dataType = field.getString("dataType");
        } catch (JSONException e) {
            dataType = "String"; // default type
        }
        return dataType;
    }

    private static JSONArray getSubField(JSONObject field) {
        JSONArray subFields;
        try {
            subFields = field.getJSONArray("fields");
        } catch (JSONException e) {
            subFields = null;
        }
        if (subFields == null) {
            try {
                JSONObject associatedEntity = field.getJSONObject("associatedEntity");
                subFields = associatedEntity.getJSONArray("fields");
            } catch (JSONException e) {
                subFields = null;
            }
        }
        return subFields;
    }

    public MetaMap getRootMetaDataTypes(String entity) throws IOException {
        rootMetaMap = getMetaDataTypes(entity);
        return rootMetaMap;
    }

    // POJO to JSON via Jackson. Don't include null properties during serialization
    public String serialize(Object obj) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Inclusion.NON_NULL);
        mapper.setSerializationInclusion(Inclusion.NON_DEFAULT);
        return mapper.writeValueAsString(obj);
    }

    // Serialize an object and save it
    public Result saveNonToMany(Object obj, String postURL, String type) throws IOException {
        String jsString = serialize(obj);
        log.info("Non-Associated entity saving to " + postURL + " - " + jsString);
        JSONObject jsResp = saveNonToMany(jsString, postURL, type);

        if (jsResp.has(StringConsts.CHANGED_ENTITY_ID)) {
            return Result.Update(jsResp.getInt(StringConsts.CHANGED_ENTITY_ID));
        } else {
            return Result.Failure("Error saving Non-To-Many Field: " + jsResp.toString());
        }
    }

    public boolean containsFields(String field) {
        if (rootMetaMap == null) {
            log.error("Root meta map not initialized");
            return false;
        }
        return rootMetaMap.hasField(field);
    }

    public Optional<String> getLabelByName(String entity) {
        return rootMetaMap.getEntityNameByRootFieldName(entity);
    }

    public String getBhRestToken() {
        return bhRestToken;
    }

    public String getRestURL() {
        return restURL;
    }

    public int getPrivateLabel() {
        return privateLabel;
    }

    public Result getFrontLoadedFromKey(String entity, String key) {
        Optional<Integer> bullhornId = Optional.ofNullable(frontLoadedValues.get(entity).get(key));
        if (bullhornId.isPresent()) {
            return Result.Update(bullhornId.get());
        } else {
            return Result.Failure("");
        }
    }

    public Result getFrontLoadedIdExists(String entity, String id) {
        if (frontLoadedValues.get(entity).containsValue(Integer.parseInt(id))) {
            return Result.Update(Integer.parseInt(id));
        } else {
            return Result.Failure("");
        }
    }

    public boolean entityContainsFields(String entity, String field) throws IOException {
        return getMetaDataTypes(entity).hasField(field);
    }
}
