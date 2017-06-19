package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.executor.BullhornRestApi;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

/**
 * Utility class for sending DataLoader complete call to REST
 */
public class CompleteUtil {

    final private ConnectionUtil connectionUtil;
    final private HttpClient httpClient;
    final private PropertyFileUtil propertyFileUtil;
    final private PrintUtil printUtil;

    public CompleteUtil(ConnectionUtil connectionUtil,
                        HttpClient httpClient,
                        PropertyFileUtil propertyFileUtil,
                        PrintUtil printUtil) {
        this.connectionUtil = connectionUtil;
        this.httpClient = httpClient;
        this.propertyFileUtil = propertyFileUtil;
        this.printUtil = printUtil;
    }

    public void complete(Command command,
                         String fileName,
                         EntityInfo entityInfo,
                         ActionTotals actionTotals,
                         Timer timer) {
        BullhornRestApi bullhornRestApi = connectionUtil.getSession();
        String restUrl = bullhornRestApi.getRestUrl() + "services/dataLoader/complete";
        String bhRestToken = bullhornRestApi.getBhRestToken();
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
