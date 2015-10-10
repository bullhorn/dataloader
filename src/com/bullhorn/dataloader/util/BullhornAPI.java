package com.bullhorn.dataloader.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.json.JSONObject;

import com.bullhorn.dataloader.domain.TranslatedType;
import com.google.common.base.Splitter;

public class BullhornAPI {
	
	String AUTH_CODE_ACTION = "Login";
	String AUTH_CODE_RESPONSE_TYPE = "code";
	String ACCESS_TOKEN_GRANT_TYPE = "authorization_code";
	String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";
	String username;
	String password;
	String BhRestToken;
	String restURL;
	String authorizeUrl;
	String tokenUrl;
	String clientId;
	String clientSecret;
	
	FileUtil fileUtil = new FileUtil();
	Properties props = fileUtil.getProps("dataloader.properties");
	
	public BullhornAPI() throws Exception {
		this.setUsername(props.getProperty("username"));
		this.setPassword(props.getProperty("password"));
		this.setAuthorizeUrl(props.getProperty("authorizeUrl"));
		this.setTokenUrl(props.getProperty("tokenUrl"));
		this.setClientId(props.getProperty("clientId"));
		this.setClientSecret(props.getProperty("clientSecret"));
	}
	
	public void createSession() {
			try {
				String authCode = getAuthorizationCode();
				String accessToken = getAccessToken(authCode);
				loginREST(accessToken);
			} catch (Exception e) {
		}
	}

	private String getAuthorizationCode() throws Exception {
		
		authorizeUrl = authorizeUrl + "?client_id=" + clientId + "&response_type=" + AUTH_CODE_RESPONSE_TYPE + "&action=" + AUTH_CODE_ACTION + "&username=" + username + "&password=" + password;

		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(authorizeUrl);
		
		client.executeMethod(method);
		String returnURL = method.getResponseHeader("Location").getValue();
		returnURL = returnURL.substring(returnURL.indexOf("?") + 1);
		Map<String, String> map = Splitter.on("&").trimResults().withKeyValueSeparator('=').split(returnURL);

		return map.get("code");

	}
	
	private String getAccessToken(String authCode) throws Exception {

		String url = tokenUrl + "?grant_type=" + ACCESS_TOKEN_GRANT_TYPE + "&code=" + authCode + "&client_id=" + clientId + "&client_secret=" + clientSecret;
		PostMethod pst = new PostMethod(url);
		JSONObject jsObj = this.post(pst);
		
		return jsObj.getString("access_token");
	}
	
	private void loginREST(String accessToken) {

		JSONObject responseJson = null;
		try {
			String accessTokenString = URLEncoder.encode(accessToken, "UTF-8");
			String loginUrl = "https://rest9.bullhornstaffing.com/rest-services/login";
			String sessionMinutesToLive = "3000";
			String url = loginUrl + "?version=" + "*" + "&access_token=" + accessTokenString + "&ttl=" + sessionMinutesToLive;
			GetMethod get = new GetMethod(url);

			HttpClient client = new HttpClient();
			client.executeMethod(get);
			String responseStr = get.getResponseBodyAsString();
			responseJson = new JSONObject(responseStr);

			this.BhRestToken = responseJson.getString("BhRestToken");
			this.restURL = (String) responseJson.get("restUrl");			
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
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
			responseStr = method.getResponseBodyAsString();
		} else if (type.equalsIgnoreCase("put")) {
			PutMethod method = (PutMethod) meth;
			client.executeMethod(method);
			responseStr = method.getResponseBodyAsString();
		} else if (type.equalsIgnoreCase("delete")) {
			DeleteMethod method = (DeleteMethod) meth;
			client.executeMethod(method);
			responseStr = method.getResponseBodyAsString();
		} else {
			PostMethod method = (PostMethod) meth;
			client.executeMethod(method);
			responseStr = method.getResponseBodyAsString();
		}
		
		responseJson = new JSONObject(responseStr);
		
		return responseJson;
	}
		
	public JSONObject doesRecordExist(Object obj) throws Exception {
		// Get class
		Class<?> cls = obj.getClass();
		// Domain object name = entity
		String entity = cls.getSimpleName();
		// Get field used for determining if record exists
		String field = props.getProperty(WordUtils.uncapitalize(entity + "ExistField"));
		Field fld = cls.getField(field);
		// If there's an isID annotation, change the field to "id" (from opportunityID for example)
		if (fld.isAnnotationPresent(TranslatedType.class)) {
			Annotation annotation = fld.getAnnotation(TranslatedType.class);
			TranslatedType tt = (TranslatedType) annotation;
			if (tt.isID()) field = "id";
		}

		String value = fld.get(obj).toString();
		
		return doesRecordExist(entity, field, value);
	}
	
	public JSONObject doesRecordExist(String entity, String field, String value) throws Exception {
		String getURL = "";
		if (field.equalsIgnoreCase("id")) {
			getURL = this.getRestURL() + "entity/" + entity + "/" + value;
			getURL = getURL + "?fields=*&BhRestToken=" + this.BhRestToken;
		} else {
			// Determine if candidate already exists in BH by using email address
			String where = field + ":(+" + value + ")";
			getURL = this.getRestURL() + "search/" + entity + "/?fields=id&query=" + URLEncoder.encode(where, "UTF-8");
			getURL = getURL + "&BhRestToken=" + this.BhRestToken;
		}
		
		GetMethod queryBH = new GetMethod(getURL);
		JSONObject qryJSON = this.get(queryBH);
		
		return qryJSON;
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
		StringRequestEntity requestEntity = new StringRequestEntity(jsString,"application/json","UTF-8");
		JSONObject jsResp = new JSONObject();
		if (type.equalsIgnoreCase("put")) {
			PutMethod method = new PutMethod(postURL);
			method.setRequestEntity(requestEntity);
			jsResp = this.put(method);
			
		} else {
			PostMethod method = new PostMethod(postURL);
			method.setRequestEntity(requestEntity);
			jsResp = this.post(method);
		}
		
		System.out.println(jsString);
		System.out.println(jsResp);
		
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

}
