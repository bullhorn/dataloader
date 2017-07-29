package com.bullhorn.dataloader.rest;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

// TODO: Add a CompleteUtil with a --jsonOutput capability

/**
 * Utility class for sending DataLoader complete call to REST
 */
public class CompleteCall {

    private final RestSession restSession;
    private final HttpClient httpClient;
    private final PropertyFileUtil propertyFileUtil;
    private final PrintUtil printUtil;

    public CompleteCall(RestSession restSession,
                        HttpClient httpClient,
                        PropertyFileUtil propertyFileUtil,
                        PrintUtil printUtil) {
        this.restSession = restSession;
        this.httpClient = httpClient;
        this.propertyFileUtil = propertyFileUtil;
        this.printUtil = printUtil;
    }

    public void complete(Command command,
                         String fileName,
                         EntityInfo entityInfo,
                         ActionTotals actionTotals,
                         Timer timer) {
        RestApi restApi = restSession.getRestApi();
        String restUrl = restApi.getRestUrl() + "services/dataLoader/complete";
        String bhRestToken = restApi.getBhRestToken();
        Integer totalRecords = actionTotals.getAllActionsTotal();
        Integer failureRecords = actionTotals.getActionTotal(Result.Action.FAILURE);
        Integer successRecords = totalRecords - failureRecords;

        try {
            URIBuilder uriBuilder = new URIBuilder(restUrl);
            uriBuilder.addParameter("BhRestToken", bhRestToken);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", command.toString());
            jsonObject.put("entity", entityInfo.getEntityName());
            jsonObject.put("file", fileName);
            jsonObject.put("totalRecords", totalRecords);
            jsonObject.put("successRecords", successRecords);
            jsonObject.put("failureRecords", failureRecords);
            jsonObject.put("durationMsec", timer.getDurationMillis());
            jsonObject.put("numThreads", propertyFileUtil.getNumThreads());
            String jsonString = jsonObject.toString();
            StringRequestEntity stringRequestEntity = new StringRequestEntity(jsonString, "application/json", "UTF-8");

            PostMethod postMethod = new PostMethod(uriBuilder.toString());
            postMethod.setRequestEntity(stringRequestEntity);
            httpClient.executeMethod(postMethod);
        } catch (Exception e) {
            printUtil.printAndLog(e);
        }
    }
}
