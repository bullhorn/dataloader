package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.meta.EntityInfo;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhornsdk.data.api.BullhornData;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

/**
 * Utility class for sending DataLoader complete call to REST
 */
public class CompleteUtil {

    final private HttpClient httpClient;
    final private PropertyFileUtil propertyFileUtil;
    final private PrintUtil printUtil;

    public CompleteUtil(HttpClient httpClient,
                        PropertyFileUtil propertyFileUtil,
                        PrintUtil printUtil) {
        this.httpClient = httpClient;
        this.propertyFileUtil = propertyFileUtil;
        this.printUtil = printUtil;
    }

    public void complete(Command command,
                         String fileName,
                         EntityInfo entityInfo,
                         ActionTotals actionTotals,
                         long durationMSec,
                         BullhornData bullhornData) {
        String restUrl = bullhornData.getRestUrl() + "services/dataLoader/complete";
        String bhRestToken = bullhornData.getBhRestToken();
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
            jsonObject.put("durationMsec", durationMSec);
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