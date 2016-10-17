package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.meta.EntityInfo;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.Result;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Utility class for sending DataLoader complete call to REST
 */
public class CompleteUtil {

    final private PropertyFileUtil propertyFileUtil;

    // TODO: Get access to REST endpoint.
    public CompleteUtil(PropertyFileUtil propertyFileUtil) {
        this.propertyFileUtil = propertyFileUtil;
    }

    public void complete(Command command, String fileName, EntityInfo entityInfo, ActionTotals actionTotals, long durationMSec) {
        Integer totalRecords = actionTotals.getAllActionsTotal();
        Integer failureRecords = actionTotals.getActionTotal(Result.Action.FAILURE);
        Integer successRecords = totalRecords - failureRecords;

        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder("http://<rest>/dataloader/complete");
        } catch (URISyntaxException e) {
            // TODO: Use PrintUtil for Exceptions
            e.printStackTrace();
        }

        uriBuilder.addParameter("command", command.toString());
        uriBuilder.addParameter("entity", entityInfo.getEntityName());
        uriBuilder.addParameter("file", fileName);
        uriBuilder.addParameter("totalRecords", totalRecords.toString());
        uriBuilder.addParameter("successRecords", successRecords.toString());
        uriBuilder.addParameter("failureRecords", failureRecords.toString());
        uriBuilder.addParameter("durationMSec", String.valueOf(durationMSec));
        uriBuilder.addParameter("numThreads", propertyFileUtil.getNumThreads().toString());

        sendCompleteCall(uriBuilder);
    }

    private void sendCompleteCall(URIBuilder uriBuilder) {
        try {
            HttpClient client = new HttpClient();
            GetMethod getMethod = new GetMethod(uriBuilder.toString());
            client.executeMethod(getMethod);
        } catch (URIException e) {
            // TODO: Use PrintUtil for Exceptions
            e.printStackTrace();
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
