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
import java.util.Set;

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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class BullhornAPI {
    private final String AUTH_CODE_ACTION = "Login";
    private final String AUTH_CODE_RESPONSE_TYPE = "code";
    private final String ACCESS_TOKEN_GRANT_TYPE = "authorization_code";
    private final String username;
    private final String password;
    private final String authorizeUrl;
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final String loginUrl;
    private final Integer threadSize;
    private final Integer cacheSize;
    private final SimpleDateFormat dateParser;
    private final Map<String, String> entityExistsFields;
    private final Set<List<EntityInstance>> seenFlag;
    private String bhRestToken;
    private String restURL;
    private static final Log log = LogFactory.getLog(BullhornAPI.class);
    private MetaMap metaMap;

    public BullhornAPI(Properties properties, Set<List<EntityInstance>> seenFlag) {
        this.threadSize = Integer.valueOf(properties.getProperty("numThreads"));
        this.cacheSize = Integer.valueOf(properties.getProperty("cacheSize"));
        this.username = properties.getProperty("username");
        this.password = properties.getProperty("password");
        this.authorizeUrl = properties.getProperty("authorizeUrl");
        this.tokenUrl = properties.getProperty("tokenUrl");
        this.clientId = properties.getProperty("clientId");
        this.clientSecret = properties.getProperty("clientSecret");
        this.loginUrl = properties.getProperty("loginUrl");
        this.dateParser = new SimpleDateFormat(properties.getProperty("dateFormat"));
        this.entityExistsFields = ImmutableMap.copyOf(createEntityExistsFields(properties));
        this.seenFlag = seenFlag;

        // TODO: Don't do this in the constructor.
        // TODO: Maybe we split the session handling into it's own object?
        createSession();
    }

    protected Map<String, String> createEntityExistsFields(Properties properties) {
        Map<String, String> entityExistsFields = Maps.newHashMap();
        properties.stringPropertyNames()
                .stream()
                .filter(property -> property.endsWith(StringConsts.EXIST_FIELD))
                .forEach(property -> {
                    String upperCaseProperty = property.substring(0, 1).toUpperCase() + property.substring(1);
                    entityExistsFields.put(upperCaseProperty, properties.getProperty(property));
                });
        return entityExistsFields;
    }

    public Optional<String> getEntityExistsFieldsProperty(String entity) {
        return Optional.ofNullable(entityExistsFields.get(entity + "ExistField"));
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
        String authorizeCodeUrl = authorizeUrl + "?client_id=" + clientId +
                "&response_type=" + AUTH_CODE_RESPONSE_TYPE +
                "&action=" + AUTH_CODE_ACTION +
                "&username=" + username +
                "&password=" + password;

        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(authorizeCodeUrl);
        client.executeMethod(method);

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

    public void associate(EntityInstance parentEntity, EntityInstance associationEntity) throws IOException {
        List<EntityInstance> nestedEntities = getEntityInstances(parentEntity, associationEntity);
        if (!seenFlag.contains(nestedEntities)) {
            dissociate(parentEntity, associationEntity);
        }
        associateEntity(parentEntity, associationEntity);
    }

    private List<EntityInstance> getEntityInstances(EntityInstance parentEntity, EntityInstance entityInstance) {
        return Lists.newArrayList(parentEntity, new EntityInstance("", entityInstance.getEntityName()));
    }

    private void dissociate(EntityInstance parentEntity, EntityInstance associationEntity) throws IOException {
        synchronized (seenFlag) {
            List<EntityInstance> nestedEntities = getEntityInstances(parentEntity, associationEntity);
            if (!seenFlag.contains(nestedEntities)) {
                seenFlag.add(nestedEntities);
                log.debug("Dissociating " + parentEntity + " " + associationEntity);
                dissociateEverything(parentEntity, associationEntity);
            }
        }
    }

    public synchronized void dissociateEverything(EntityInstance parentEntity, EntityInstance childEntity) throws IOException {
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
        JSONObject data = response.getJSONObject(StringConsts.DATA);
        JSONArray elements = data.getJSONObject(data.keys().next()).getJSONArray(StringConsts.DATA);

        List<String> identifiers = new ArrayList<>(elements.length());
        for (int i = 0; i < elements.length(); i++) {
            identifiers.add(String.valueOf(elements.getJSONObject(i).getInt(StringConsts.ID)));
        }
        return identifiers;
    }

    public void associateEntity(EntityInstance parentEntity, EntityInstance childEntity) throws IOException {
        String associationUrl = getModificationAssociationUrl(parentEntity, childEntity);
        log.debug("Associating " + associationUrl);
        PutMethod putMethod = new PutMethod(associationUrl);
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

    private static JSONObject call(HttpMethodBase httpMethod) throws IOException {
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

    public Optional<String> getLabelByName(String entity) {
        return metaMap.getEntityNameByRootFieldName(entity);
    }

    public String getBhRestToken() {
        return bhRestToken;
    }

    public String getRestURL() {
        return restURL;
    }

    private SimpleDateFormat getDateParser() {
        return dateParser;
    }

    public Integer getThreadSize() {
        return threadSize;
    }

    public Integer getCacheSize() {
        return cacheSize;
    }
}
