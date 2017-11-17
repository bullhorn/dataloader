package com.bullhorn.dataloader.rest;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhorn.dataloader.util.Timer;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class for sending DataLoader complete call to REST and optionally outputting results file
 */
public class CompleteUtil {

    private final RestSession restSession;
    private final HttpClient httpClient;
    private final PropertyFileUtil propertyFileUtil;
    private final PrintUtil printUtil;
    private final Timer timer;
    private AtomicReference<JSONObject> resultsWrapper = new AtomicReference<>(new JSONObject());
    private ScheduledExecutorService scheduler;

    public CompleteUtil(RestSession restSession,
                        HttpClient httpClient,
                        PropertyFileUtil propertyFileUtil,
                        PrintUtil printUtil,
                        Timer timer) {
        this.restSession = restSession;
        this.httpClient = httpClient;
        this.propertyFileUtil = propertyFileUtil;
        this.printUtil = printUtil;
        this.timer = timer;

        if (propertyFileUtil.getResultsFileEnabled()) {
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(this::writeResultsFile, propertyFileUtil.getResultsFileWriteIntervalMsec(),
                propertyFileUtil.getResultsFileWriteIntervalMsec(), TimeUnit.MILLISECONDS);
        }
    }

    public void complete(Command command,
                         String fileName,
                         EntityInfo entityInfo,
                         ActionTotals actionTotals) {
        if (propertyFileUtil.getResultsFileEnabled()) {
            // Cleanup output file timer and write final results
            scheduler.shutdown();
            writeResultsFile();
        }

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

    /**
     * Called by each individual task once a row has been processed, if we are outputting a results file. Must be
     * synchronized, as this call can be made from multiple threads concurrently.
     *
     * @param row          the row that just finished processing
     * @param result       the results of the row processing
     * @param actionTotals the totals so far
     */
    public synchronized void rowComplete(Row row, Result result, ActionTotals actionTotals) {
        JSONObject updatedResults = new JSONObject(resultsWrapper.get(), new String[]{"errors"});
        updatedResults.put("processed", actionTotals.getAllActionsTotal());
        updatedResults.put("inserted", actionTotals.getActionTotal(Result.Action.INSERT));
        updatedResults.put("updated", actionTotals.getActionTotal(Result.Action.UPDATE));
        updatedResults.put("deleted", actionTotals.getActionTotal(Result.Action.DELETE));
        updatedResults.put("failed", actionTotals.getActionTotal(Result.Action.FAILURE));
        updatedResults.put("successFile", CsvFileWriter.successFilePath);
        updatedResults.put("failureFile", CsvFileWriter.failureFilePath);
        updatedResults.put("logFile", "log/dataloader_" + StringConsts.TIMESTAMP + ".log");
        updatedResults.put("startTime", timer.getStartTime());
        updatedResults.put("durationMsec", timer.getDurationMillis());
        if (!result.isSuccess()) {
            JSONObject error = new JSONObject();
            error.put("row", row.getNumber());
            error.put("id", result.getBullhornId());
            error.put("message", result.getFailureText());
            JSONArray errors = updatedResults.has("errors") ? updatedResults.getJSONArray("errors") : new JSONArray();
            errors.put(error);
            updatedResults.put("errors", errors);
        }
        resultsWrapper.set(updatedResults);
    }

    private synchronized void writeResultsFile() {
        try {
            String resultsString = resultsWrapper.get().toString(2);
            File file = new File(propertyFileUtil.getResultsFilePath());
            FileUtils.writeStringToFile(file, resultsString, "UTF-8");
        } catch (Exception e) {
            printUtil.printAndLog("Error writing results file: " + e);
        }
    }
}
