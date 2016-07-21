package com.bullhorn.dataloader.service.executor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.bullhorn.dataloader.util.validation.EntityValidation;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.JsonRow;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;

/**
 * Responsible for deleting a single row from a CSV input file.
 */
public class DeleteTask implements Runnable {
    private String entityName;
    private final BullhornAPI bhApi;
    private JsonRow data;
    private CsvFileWriter csvFileWriter;
    private PropertyFileUtil propertyFileUtil;

    private static final Logger log = LogManager.getLogger(DeleteTask.class);

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
            log.info("Deleting row: " + data.getRowNumber());

            if (!EntityValidation.isDeletable(entityName)) {
                String failureText = "ERROR: Cannot delete " + entityName + " because it is not deletable in REST.";
                result = Result.Failure(failureText);
                System.out.println(failureText);
                log.error(failureText);
            } else if (!data.getImmediateActions().containsKey(StringConsts.ID)) {
                String failureText = "ERROR: Cannot delete row: " + data.getRowNumber() + ". " +
                        " CSV row is missing the \"" + StringConsts.ID + "\" column.";
                result = Result.Failure(failureText);
                System.out.println(failureText);
                log.error(failureText);
            } else {
                Integer entityId = new Integer(data.getImmediateActions().get(StringConsts.ID).toString());
                String url = entityBase + "/" + entityId + restToken;

                if (EntityValidation.isHardDeletable(entityName)) {
                    log.info("Calling hard delete: " + url);
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

                    log.info("Calling soft delete: " + url + " with request body: " + jsonString);
                    JSONObject jsonResponse = bhApi.call(postMethod);
                    result = parseJsonResponse(jsonResponse, entityId);
                }
            }

            csvFileWriter.writeRow(data, result);
        } catch (IOException e) {
            System.out.println(e);
            log.error(e);
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
        log.info("REST Response: " + jsonResponse.toString());
        if (jsonResponse.has("errorMessage")) {
            String restFailureMessage = jsonResponse.getString("errorMessage");
            if (jsonResponse.has("errors")) {
                restFailureMessage += ". REST Errors: " + jsonResponse.getJSONArray("errors");
            }

            System.out.println("ERROR: Row " + data.getRowNumber() + " - Cannot delete " + entityName + " with ID: " + entityId);
            System.out.println("       Failure Message From Rest: " + restFailureMessage);
            log.error(restFailureMessage);
            return Result.Failure(restFailureMessage);
        } else {
            return Result.Delete(jsonResponse.getInt(StringConsts.CHANGED_ENTITY_ID));
        }
    }
}
