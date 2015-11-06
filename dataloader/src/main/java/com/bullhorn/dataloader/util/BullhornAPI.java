package com.bullhorn.dataloader.util;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bullhorn.dataloader.domain.MetaMap;
import com.google.common.base.Splitter;

public class BullhornAPI {

    private final Properties properties;
    private final String AUTH_CODE_ACTION = "Login";
    private final String AUTH_CODE_RESPONSE_TYPE = "code";
    private final String ACCESS_TOKEN_GRANT_TYPE = "authorization_code";
    private String username;
    private String password;
    private String BhRestToken;
    private String restURL;
    private String authorizeUrl;
    private String tokenUrl;
    private String clientId;
    private String clientSecret;
    private String loginUrl;

    private static Log log = LogFactory.getLog(BullhornAPI.class);

    public BullhornAPI(Properties properties) {
        this.properties = properties;
        this.setUsername(properties.getProperty("username"));
        this.setPassword(properties.getProperty("password"));
        this.setAuthorizeUrl(properties.getProperty("authorizeUrl"));
        this.setTokenUrl(properties.getProperty("tokenUrl"));
        this.setClientId(properties.getProperty("clientId"));
        this.setClientSecret(properties.getProperty("clientSecret"));
        this.setLoginUrl(properties.getProperty("loginUrl"));
        createSession();
    }

    public void createSession() {
        try {
            String authCode = getAuthorizationCode();
            String accessToken = getAccessToken(authCode);
            loginREST(accessToken);
        } catch (Exception e) {
            log.error(e);
        }
    }

    private String getAuthorizationCode() throws Exception {

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

    private String getAccessToken(String authCode) throws Exception {

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
            String accessTokenString = URLEncoder.encode(accessToken, "UTF-8");
            String sessionMinutesToLive = "3000";
            String url = loginUrl + "?version=" + "*" + "&access_token=" + accessTokenString + "&ttl=" + sessionMinutesToLive;
            GetMethod get = new GetMethod(url);

            HttpClient client = new HttpClient();
            client.executeMethod(get);
            String responseStr = IOUtils.toString(get.getResponseBodyAsStream());
            responseJson = new JSONObject(responseStr);

            // Cache BhRestToken and REST URL
            this.BhRestToken = responseJson.getString("BhRestToken");
            this.restURL = (String) responseJson.get("restUrl");

        } catch (Exception e) {
            log.error(e);
        }
    }

    public JSONObject get(GetMethod method) throws Exception {
        JSONObject responseJson = call("get", method);
        return responseJson;
    }

    public JSONObject put(PutMethod method) throws Exception {
        JSONObject responseJson = call("put", method);
        return responseJson;
    }

    public JSONObject post(PostMethod method) throws Exception {
        JSONObject responseJson = call("post", method);
        return responseJson;
    }

    public JSONObject delete(String postURL) throws Exception {
        DeleteMethod method = new DeleteMethod(postURL);
        JSONObject responseJson = this.delete(method);
        return responseJson;
    }

    public JSONObject delete(DeleteMethod method) throws Exception {
        JSONObject responseJson = call("delete", method);
        return responseJson;
    }

    public JSONObject call(String type, Object meth) throws Exception {

        JSONObject responseJson = null;
        HttpClient client = new HttpClient();
        String responseStr = "";

        if (type.equalsIgnoreCase("get")) {
            GetMethod method = (GetMethod) meth;
            client.executeMethod(method);
            responseStr = IOUtils.toString(method.getResponseBodyAsStream());
        } else if (type.equalsIgnoreCase("put")) {
            PutMethod method = (PutMethod) meth;
            client.executeMethod(method);
            responseStr = IOUtils.toString(method.getResponseBodyAsStream());
        } else if (type.equalsIgnoreCase("delete")) {
            DeleteMethod method = (DeleteMethod) meth;
            client.executeMethod(method);
            responseStr = IOUtils.toString(method.getResponseBodyAsStream());
        } else {
            PostMethod method = (PostMethod) meth;
            client.executeMethod(method);
            responseStr = IOUtils.toString(method.getResponseBodyAsStream());
        }

        responseJson = new JSONObject(responseStr);

        return responseJson;
    }

    public String[] getPostURL(Object obj) throws Exception {
        // Get class
        Class<?> cls = obj.getClass();
        // Domain object name = entity
        String entity = cls.getSimpleName();
        // Get field used for determining if record exists
        String field = properties.getProperty(WordUtils.uncapitalize(entity + "ExistField"));
        Field fld = cls.getField(field);
        // If there's an isID annotation, change the field to "id" (from opportunityID for example)
//        if (fld.isAnnotationPresent(TranslatedType.class)) {
//            Annotation annotation = fld.getAnnotation(TranslatedType.class);
//            TranslatedType tt = (TranslatedType) annotation;
//            if (tt.isID()) field = "id";
//        }

        String value = "-1";
        Object fldObj = fld.get(obj);
        if (fldObj != null) {
            value = fldObj.toString();
        }

        JSONObject qryJSON = doesRecordExist(entity, field, value);

        // Assemble URL
        String type = "put";
        String postURL = this.getRestURL() + "entity/" + entity;

        // If it's not an array, check if it's an object
        JSONArray optJsa = qryJSON.optJSONArray("data");
        if (optJsa == null) {
            JSONObject optJso = qryJSON.optJSONObject("data");
            // If it is an object, use it
            if (optJso != null) {
                postURL = postURL + "/" + optJso.getInt("id");
                type = "post";
            }
            // If an array is returned, use the ID of the first record
        } else {
            JSONObject optJso = optJsa.optJSONObject(0);
            if (optJso != null) {
                postURL = postURL + "/" + optJsa.getJSONObject(0).getInt("id");
                type = "post";
            }
        }

        // Append REST token
        postURL = postURL + "?BhRestToken=" + this.getBhRestToken();

        // return type and URL
        String[] retArr = new String[2];
        retArr[0] = type;
        retArr[1] = postURL;

        return retArr;

    }

    public JSONObject doesRecordExist(String entity, String field, String value) throws Exception {
        String getURL;
        if (field.equalsIgnoreCase("id")) {
            getURL = this.getRestURL() + "entity/" + entity + "/" + value;
            getURL = getURL + "?fields=*&BhRestToken=" + this.BhRestToken;
        } else {
            // Determine if record already exists in BH by using email address
            String where = field + ":(+" + "\"" + value + "\"" + ")";
            getURL = this.getRestURL() + "search/" + entity + "?fields=id&query=" + URLEncoder.encode(where, "UTF-8");
            getURL = getURL + "&count=1&BhRestToken=" + this.BhRestToken;
        }

        GetMethod queryBH = new GetMethod(getURL);
        JSONObject qryJSON = this.get(queryBH);

        return qryJSON;
    }

    public MetaMap getMetaDataTypes(String entity) throws Exception {
        String url = this.getRestURL() + "meta/" + entity + "?fields=*&BhRestToken=" + this.getBhRestToken();
        GetMethod method = new GetMethod(url);
        JSONObject jsonObject = get(method);

        JSONArray fields = jsonObject.getJSONArray("fields");

        Deque<JsonObjectFields> deque = new ArrayDeque<>();
        JsonObjectFields jsonObjectFields = new JsonObjectFields("", fields);
        deque.add(jsonObjectFields);

        MetaMap meta = new MetaMap();
        while (!deque.isEmpty()) {
            jsonObjectFields = deque.pop();
            fields = jsonObjectFields.getJsonArray();

            addEachFieldToMetaAndQueueNestedFields(fields, deque, jsonObjectFields, meta);
        }
        return meta;
    }

    private void addEachFieldToMetaAndQueueNestedFields(JSONArray fields,
                                                        Deque<JsonObjectFields> deque,
                                                        JsonObjectFields jsonObjectFields,
                                                        MetaMap meta) {
        for (int i = 0; i < fields.length(); i++) {
            JSONObject field = fields.getJSONObject(i);

            JSONArray subFields = getSubField(field);

            String label, name, dataType, type;
            name = field.getString("name");
            type = field.getString("type");

            if(null != subFields) {
                String path = jsonObjectFields.getPath();
                path = path.isEmpty() ? name + "." : path + name + ".";
                JsonObjectFields nestedFields = new JsonObjectFields(path, subFields);
                try {
                    dataType = field.getString("dataType");
                } catch (JSONException e) {
                    dataType = "String"; // default type
                }
                try {
                    label = field.getString("label");
                } catch (JSONException e) {
                    label = "";
                }
                meta.addMeta(path.substring(0, path.length() - 1), label, dataType, type);
                deque.add(nestedFields);
            } else {
                try {
                    dataType = field.getString("dataType");
                } catch (JSONException e) {
                    dataType = "String"; // default type
                }
                try {
                    label = field.getString("label");
                } catch (JSONException e) {
                    label = "";
                }
                meta.addMeta(jsonObjectFields.getPath() + name, label, dataType, type);
            }
        }
    }

    private JSONArray getSubField(JSONObject field) {
        JSONArray subFields;
        try {
            subFields = field.getJSONArray("fields");
        } catch (JSONException e) {
            subFields = null;
        }
        if(subFields == null) {
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
    public String serialize(Object obj) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Inclusion.NON_NULL);
        mapper.setSerializationInclusion(Inclusion.NON_DEFAULT);

        String jsString = mapper.writeValueAsString(obj);

        return jsString;
    }

    // Serialize an object and save it
    public JSONObject save(Object obj, String postURL, String type) throws Exception {

        String jsString = serialize(obj);
        JSONObject jsResp = save(jsString, postURL, type);

        return jsResp;
    }

    // Save a stringify'd object
    public JSONObject save(String jsString, String postURL, String type) throws Exception {

        // Post to BH
        StringRequestEntity requestEntity = new StringRequestEntity(jsString, "application/json", "UTF-8");
        JSONObject jsResp;
        if (type.equalsIgnoreCase("put")) {
            PutMethod method = new PutMethod(postURL);
            method.setRequestEntity(requestEntity);
            jsResp = this.put(method);

        } else {
            PostMethod method = new PostMethod(postURL);
            method.setRequestEntity(requestEntity);
            jsResp = this.post(method);
        }

        log.info(jsString);
        log.info(jsResp);

        return jsResp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBhRestToken() {
        return BhRestToken;
    }

    public void setBhRestToken(String BhRestToken) {
        this.BhRestToken = BhRestToken;
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

    public void setAuthorizeUrl(String authorizeUrl) {
        this.authorizeUrl = authorizeUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

}
