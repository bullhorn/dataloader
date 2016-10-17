package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.meta.EntityInfo;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhornsdk.data.api.BullhornData;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.client.utils.URIBuilder;

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
        String restUrl = bullhornData.getRestUrl() + "/dataloader/complete";
        Integer totalRecords = actionTotals.getAllActionsTotal();
        Integer failureRecords = actionTotals.getActionTotal(Result.Action.FAILURE);
        Integer successRecords = totalRecords - failureRecords;

        try {
            URIBuilder uriBuilder = new URIBuilder(restUrl);
            uriBuilder.addParameter("command", command.toString());
            uriBuilder.addParameter("entity", entityInfo.getEntityName());
            uriBuilder.addParameter("file", fileName);
            uriBuilder.addParameter("totalRecords", totalRecords.toString());
            uriBuilder.addParameter("successRecords", successRecords.toString());
            uriBuilder.addParameter("failureRecords", failureRecords.toString());
            uriBuilder.addParameter("durationMSec", String.valueOf(durationMSec));
            uriBuilder.addParameter("numThreads", propertyFileUtil.getNumThreads().toString());

            PostMethod postMethod = new PostMethod(uriBuilder.toString());
            httpClient.executeMethod(postMethod);
        } catch (Exception e) {
            printUtil.printAndLog(e);
        }
    }
}
