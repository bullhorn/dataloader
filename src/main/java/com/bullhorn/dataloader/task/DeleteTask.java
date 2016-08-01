package com.bullhorn.dataloader.task;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONObject;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.JsonRow;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhorn.dataloader.util.validation.EntityValidation;

/**
 * Responsible for deleting a single row from a CSV input file.
 */
public class DeleteTask implements Runnable {
    private String entityName;
    private final BullhornAPI bhApi;
    private JsonRow data;
    private CsvFileWriter csvFileWriter;
    private PropertyFileUtil propertyFileUtil;
    private final ActionTotals actionTotals = new ActionTotals();
    private final PrintUtil printUtil = new PrintUtil();
    private static AtomicInteger rowProcessedCount = new AtomicInteger(0);

    public DeleteTask(String entityName,
                      BullhornAPI bhApi,
                      JsonRow data,
                      CsvFileWriter csvFileWriter,
                      PropertyFileUtil propertyFileUtil) {
        this.bhApi = bhApi;
        this.entityName = entityName;
        this.data = data;
        this.csvFileWriter = csvFileWriter;
        this.propertyFileUtil = propertyFileUtil;
    }

    /**
     * Run method on this runnable object called by the thread manager.
     * <p>
     * At this point, we should have an entity type that we know we can delete (soft or hard).
     */
    @Override
    public void run() {
        try {
            Result result = Result.Failure("Internal Error: row: " + data.getRowNumber() + ". This should never happen.");
            String entityBase = bhApi.getRestURL() + StringConsts.ENTITY_SLASH + entityName;
            String restToken = StringConsts.END_BH_REST_TOKEN + bhApi.getBhRestToken();
            printUtil.log("Deleting row: " + data.getRowNumber());

            if (!EntityValidation.isDeletable(entityName)) {
                String failureText = "ERROR: Cannot delete " + entityName + " because it is not deletable in REST.";
                result = Result.Failure(failureText);
                printUtil.printAndLog(failureText);
            } else if (!data.getImmediateActions().containsKey(StringConsts.ID)) {
                String failureText = "ERROR: Cannot delete row: " + data.getRowNumber() + ". " +
                        " CSV row is missing the \"" + StringConsts.ID + "\" column.";
                result = Result.Failure(failureText);
                printUtil.printAndLog(failureText);
            } else {
                Integer entityId = new Integer(data.getImmediateActions().get(StringConsts.ID).toString());
                String url = entityBase + "/" + entityId + restToken;

                if (EntityValidation.isHardDeletable(entityName)) {
                    printUtil.log("Calling hard delete: " + url);
                    DeleteMethod deleteMethod = new DeleteMethod(url);
                    JSONObject jsonResponse = bhApi.call(deleteMethod);
                    result = parseJsonResponse(jsonResponse, entityId);
                } else if (EntityValidation.isSoftDeletable(entityName)) {
                    PostMethod postMethod = new PostMethod(url);

                    // Send a body of: {"isDeleted" : true}
                    Map<String, Object> map = new HashMap<>();
                    map.put("isDeleted", true);
                    String jsonString = bhApi.serialize(map);
                    StringRequestEntity stringRequestEntity = new StringRequestEntity(jsonString, StringConsts.APPLICATION_JSON, StringConsts.UTF);
                    postMethod.setRequestEntity(stringRequestEntity);

                    printUtil.log("Calling soft delete: " + url + " with request body: " + jsonString);
                    JSONObject jsonResponse = bhApi.call(postMethod);
                    result = parseJsonResponse(jsonResponse, entityId);
                }
            }

            csvFileWriter.writeRow(data.getValues(), result);

            if(result.getAction().equals(Result.Action.DELETE)) {
                actionTotals.incrementTotalDelete();
            } else {
                actionTotals.incrementTotalError();
            }

            rowProcessedCount.incrementAndGet();
            if(rowProcessedCount.intValue() % 111 == 0) {
                printUtil.printAndLog("Processed: " + NumberFormat.getNumberInstance(Locale.US).format(rowProcessedCount) + " records.");
            }

        } catch (IOException e) {
            printUtil.printAndLog(e.toString());
        }
    }

    /**
     * Handles the REST JSON response to our delete (hard or soft)
     *
     * @param jsonResponse The response from the Bullhorn API
     * @param entityId The entity ID we are requesting
     * @return A filled out Result object
     */
    private Result parseJsonResponse(JSONObject jsonResponse, Integer entityId) {
        printUtil.log("REST Response: " + jsonResponse.toString());
        if (jsonResponse.has("errorMessage")) {
            String restFailureMessage = jsonResponse.getString("errorMessage");
            if (jsonResponse.has("errors")) {
                restFailureMessage += ". REST Errors: " + jsonResponse.getJSONArray("errors");
            }

            printUtil.printAndLog("ERROR: Row " + data.getRowNumber() + " - Cannot delete " + entityName + " with ID: " + entityId);
            printUtil.printAndLog("       Failure Message From Rest: " + restFailureMessage);
            return Result.Failure(restFailureMessage);
        } else {
            return Result.Delete(jsonResponse.getInt(StringConsts.CHANGED_ENTITY_ID));
        }
    }
}
