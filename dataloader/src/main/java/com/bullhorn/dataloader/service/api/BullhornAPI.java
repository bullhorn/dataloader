package com.bullhorn.dataloader.service.api;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bullhorn.dataloader.meta.MetaMap;
import com.bullhorn.dataloader.util.CaseInsensitiveStringPredicate;
import com.bullhorn.dataloader.util.StringConsts;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class BullhornAPI {
    private final String AUTH_CODE_ACTION = "Login";
    private final String AUTH_CODE_RESPONSE_TYPE = "code";
    private final String ACCESS_TOKEN_GRANT_TYPE = "authorization_code";
    private String username;
    private String password;
    private String bhRestToken;
    private String restURL;
    private String authorizeUrl;
    private String tokenUrl;
    private String clientId;
    private String clientSecret;
    private String loginUrl;
    private SimpleDateFormat dateParser;
    private final Map<String, String> entityExistsFields;

    private static final Log log = LogFactory.getLog(BullhornAPI.class);
    private MetaMap metaMap;

    public BullhornAPI(Properties properties) {
        this.setUsername(properties.getProperty("username"));
        this.setPassword(properties.getProperty("password"));
        this.setAuthorizeUrl(properties.getProperty("authorizeUrl"));
        this.setTokenUrl(properties.getProperty("tokenUrl"));
        this.setClientId(properties.getProperty("clientId"));
        this.setClientSecret(properties.getProperty("clientSecret"));
        this.setLoginUrl(properties.getProperty("loginUrl"));
        this.setDateParser(properties.getProperty("dateFormat"));
        this.entityExistsFields = ImmutableMap.copyOf(createEntityExistsFields(properties));

        createSession();
    }

    private Map<String, String> createEntityExistsFields(Properties properties) {
        Map<String, String> entityExistsFields = Maps.newHashMap();
        entityExistsFields.put("CandidateExistField", properties.getProperty("candidateExistField"));
        entityExistsFields.put("ClientContactExistField", properties.getProperty("clientContactExistField"));
        entityExistsFields.put("ClientCorporationExistField", properties.getProperty("clientCorporationExistField"));
        entityExistsFields.put("OpportunityExistField", properties.getProperty("opportunityExistField"));
        entityExistsFields.put("LeadExistField", properties.getProperty("leadExistField"));
        return entityExistsFields;
    }

    public String getEntityExistsFieldsProperty(String entity) {
        return entityExistsFields.get(entity + "ExistField");
    }

    private void createSession() {
        try {
            String authCode = getAuthorizationCode();
            String accessToken = getAccessToken(authCode);
            loginREST(accessToken);
        } catch (Exception e) {
            log.error(e);
        }
    }

    private String getAuthorizationCode() throws IOException {

        // Construct authorize URL
        authorizeUrl = authorizeUrl + "?client_id=" + clientId +
                "&response_type=" + AUTH_CODE_RESPONSE_TYPE +
                "&action=" + AUTH_CODE_ACTION +
                "&username=" + username +
                "&password=" + password;

        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(authorizeUrl);
        client.executeMethod(method);

        // Return URL contains access code
        String returnURL = method.getResponseHeader("Location").getValue();
        returnURL = returnURL.substring(returnURL.indexOf("?") + 1);
        Map<String, String> map = Splitter.on("&").trimResults().withKeyValueSeparator('=').split(returnURL);

        return map.get("code");

    }

    private String getAccessToken(String authCode) throws IOException {

        // Get access token based on auth code returned from getAuthorizationCode()
        String url = tokenUrl + "?grant_type=" + ACCESS_TOKEN_GRANT_TYPE +
                "&code=" + authCode +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret;

        PostMethod pst = new PostMethod(url);
        JSONObject jsObj = this.post(pst);

        return jsObj.getString("access_token");
    }

    private void loginREST(String accessToken) {

        JSONObject responseJson;
        try {
            String accessTokenString = URLEncoder.encode(accessToken, StringConsts.UTF);
            String sessionMinutesToLive = "3000";
            String url = loginUrl + "?version=" + "*" + "&access_token=" + accessTokenString + "&ttl=" + sessionMinutesToLive;
            GetMethod get = new GetMethod(url);

            HttpClient client = new HttpClient();
            client.executeMethod(get);
            String responseStr = IOUtils.toString(get.getResponseBodyAsStream());
            responseJson = new JSONObject(responseStr);

            // Cache bhRestToken and REST URL
            this.bhRestToken = responseJson.getString(StringConsts.BH_REST_TOKEN);
            this.restURL = (String) responseJson.get("restUrl");

        } catch (Exception e) {
            log.error(e);
        }
    }

    public void dissociateEverything(EntityInstance parentEntity, EntityInstance childEntity) throws IOException {
        String associationUrl = getQueryAssociationUrl(parentEntity, childEntity);
        String associationIds = Joiner.on(',').join(getIds(associationUrl));
        EntityInstance toManyAssociations = new EntityInstance(associationIds, childEntity.getEntityName());
        String toManyUrl = getModificationAssociationUrl(parentEntity, toManyAssociations);
        DeleteMethod deleteMethod = new DeleteMethod(toManyUrl);
        this.delete(deleteMethod);
    }

    private List<String> getIds(String url) throws IOException {
        GetMethod getMethod = new GetMethod(url);
        JSONObject response = this.get(getMethod);
        JSONObject data = response.getJSONObject("data");
        JSONArray elements = data.getJSONObject(data.keys().next()).getJSONArray("data");

        List<String> identifiers = new ArrayList<>(elements.length());
        for (int i = 0; i < elements.length(); i++) {
            identifiers.add(String.valueOf(elements.getJSONObject(i).getInt("id")));
        }
        return identifiers;
    }

    public void associateEntity(EntityInstance parentEntity, EntityInstance childEntity) throws IOException {
        String associationUrl = getModificationAssociationUrl(parentEntity, childEntity);
        PutMethod putMethod= new PutMethod(associationUrl);
        this.put(putMethod);
    }

    private String getModificationAssociationUrl(EntityInstance parentEntity, EntityInstance childEntity) {
        return this.getRestURL() + StringConsts.ENTITY_SLASH
                + parentEntity.getEntityName() + "/"
                + parentEntity.getEntityId() + "/"
                + childEntity.getEntityName() + "/"
                + childEntity.getEntityId()
                + StringConsts.END_BH_REST_TOKEN + this.getBhRestToken();
    }

    private String getQueryAssociationUrl(EntityInstance parentEntity, EntityInstance childEntity) {
        return this.getRestURL() + StringConsts.ENTITY_SLASH
                + parentEntity.getEntityName() + "/"
                + parentEntity.getEntityId()
                + "?fields=" + childEntity.getEntityName()
                + StringConsts.AND_BH_REST_TOKEN + this.getBhRestToken();
    }

    public JSONObject get(GetMethod method) throws IOException {
        JSONObject responseJson = call(method);
        return responseJson;
    }

    public JSONObject put(PutMethod method) throws IOException {
        JSONObject responseJson = call(method);
        return responseJson;
    }

    public JSONObject post(PostMethod method) throws IOException {
        JSONObject responseJson = call(method);
        return responseJson;
    }

    private JSONObject delete(DeleteMethod method) throws IOException {
        JSONObject responseJson = call(method);
        return responseJson;
    }

    private JSONObject call(HttpMethodBase httpMethod) throws IOException {
        HttpClient client = new HttpClient();
        client.executeMethod(httpMethod);
        String responseStr = IOUtils.toString(httpMethod.getResponseBodyAsStream());
        JSONObject responseJson = new JSONObject(responseStr);

        return responseJson;
    }

    public MetaMap getMetaDataTypes(String entity) throws IOException {
        if (null == metaMap) {
            String url = this.getRestURL() + "meta/" + entity + "?fields=*&BhRestToken=" + this.getBhRestToken();
            GetMethod method = new GetMethod(url);
            JSONObject jsonObject = get(method);

            JSONArray fields = jsonObject.getJSONArray("fields");

            Deque<JsonObjectFields> deque = new ArrayDeque<>();
            JsonObjectFields jsonObjectFields = new JsonObjectFields("", fields);
            deque.add(jsonObjectFields);

            metaMap = new MetaMap(getDateParser());
            while (!deque.isEmpty()) {
                jsonObjectFields = deque.pop();
                fields = jsonObjectFields.getJsonArray();

                addEachFieldToMetaAndQueueNestedFields(fields, deque, jsonObjectFields, metaMap);
            }
        }
        return metaMap;
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

    private Optional<String> getAssociatedEntity(JSONObject field) {
        try {
            return Optional.of(field.getJSONObject("associatedEntity").getString("entity"));
        } catch (JSONException e) {
            return Optional.empty();
        }

    }

    private String getLabel(JSONObject field) {
        String label;
        try {
            label = field.getString("label");
        } catch (JSONException e) {
            label = "";
        }
        return label;
    }

    private String getDataType(JSONObject field) {
        String dataType;
        try {
            dataType = field.getString("dataType");
        } catch (JSONException e) {
            dataType = "String"; // default type
        }
        return dataType;
    }

    private JSONArray getSubField(JSONObject field) {
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

    // POJO to JSON via Jackson. Don't include null properties during serialization
    public String serialize(Object obj) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Inclusion.NON_NULL);
        mapper.setSerializationInclusion(Inclusion.NON_DEFAULT);

        String jsString = mapper.writeValueAsString(obj);

        return jsString;
    }

    // Serialize an object and save it
    public Optional<Integer> saveNonToMany(Object obj, String postURL, String type) throws IOException {

        String jsString = serialize(obj);
        log.info("Non-Associated entity saving to " + postURL);
        JSONObject jsResp = saveNonToMany(jsString, postURL, type);

        if (jsResp.has(StringConsts.CHANGED_ENTITY_ID)) {
            return Optional.of(jsResp.getInt(StringConsts.CHANGED_ENTITY_ID));
        } else {
            return Optional.empty();
        }
    }

    // Save a stringify'd object
    private JSONObject saveNonToMany(String jsString, String url, String type) throws IOException {

        // Post to BH
        StringRequestEntity requestEntity = new StringRequestEntity(jsString, StringConsts.APPLICATION_JSON, StringConsts.UTF);
        JSONObject jsResp;
        if (CaseInsensitiveStringPredicate.isPut(type)) {
            PutMethod method = new PutMethod(url);
            method.setRequestEntity(requestEntity);
            jsResp = this.put(method);

        } else {
            PostMethod method = new PostMethod(url);
            method.setRequestEntity(requestEntity);
            jsResp = this.post(method);
        }

        log.info(jsString);
        log.info(jsResp);

        return jsResp;
    }

    public Optional<String> getLabelByName(String entity) {
        return metaMap.getEntityNameByRootFieldName(entity);
    }

    public String getUsername() {
        return username;
    }

    private void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    public String getBhRestToken() {
        return bhRestToken;
    }

    public void setBhRestToken(String BhRestToken) {
        this.bhRestToken = BhRestToken;
    }

    public String getRestURL() {
        return restURL;
    }

    public void setRestURL(String restURL) {
        this.restURL = restURL;
    }

    public String getAuthorizeUrl() {
        return authorizeUrl;
    }

    private void setAuthorizeUrl(String authorizeUrl) {
        this.authorizeUrl = authorizeUrl;
    }

    public String getClientId() {
        return clientId;
    }

    private void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    private void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    private void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    private void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    private void setDateParser(String format) {
        this.dateParser = new SimpleDateFormat(format);
    }

    private SimpleDateFormat getDateParser() {
        return dateParser;
    }
}
