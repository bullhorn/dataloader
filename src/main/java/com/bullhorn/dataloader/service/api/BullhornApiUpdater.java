package com.bullhorn.dataloader.service.api;

import java.io.IOException;
import java.util.Optional;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.query.EntityQuery;
import com.bullhorn.dataloader.util.StringConsts;

public class BullhornApiUpdater {
    private final Logger log = LogManager.getLogger(BullhornApiUpdater.class);

    private final BullhornAPI bhApi;

    public BullhornApiUpdater(BullhornAPI bhApi) {
        this.bhApi = bhApi;
    }

    /**
     * Performs an update if the entity record exists, or an insert if it does not.
     *
     * @param entityQuery The query to determine if the entity exists.
     * @return The results from REST
     * @throws IOException
     */
    public Result merge(EntityQuery entityQuery) throws IOException {
        Result result = Result.Failure("");

        if (idExistsButNotInRest(entityQuery)) {
            String failureText = "Association cannot be merged " + entityQuery.toString();
            result.setFailureText(failureText);
            log.error(failureText);
        } else {
            JSONObject jsonResponse;
            if (entityQuery.getId().isPresent()) {
                jsonResponse = update(entityQuery);
                result.setAction(Result.Action.UPDATE);
            } else {
                jsonResponse = insert(entityQuery);
                result.setAction(Result.Action.INSERT);
            }

            if (jsonResponse.has("errorMessage")) {
                String failureText = "Association query " + entityQuery.toString() + " failed for reason " +
                        jsonResponse.getString("errorMessage");
                if (jsonResponse.has("errors")) {
                    failureText += "\nerrors: " + jsonResponse.getJSONArray("errors");
                }
                result.setFailureText(failureText);
                log.error(failureText);
            } else {
                result.setStatus(Result.Status.SUCCESS);
                result.setBullhornId(jsonResponse.getInt(StringConsts.CHANGED_ENTITY_ID));
            }
        }

        return result;
    }

    private JSONObject insert(EntityQuery entityQuery) throws IOException {
        String putUrl = getEntityUrl(entityQuery);
        log.info("Insert Association: " + putUrl + " - " + entityQuery.getNestedJson());
        PutMethod putMethod = new PutMethod(putUrl);
        putMethod.setRequestEntity(getStringRequestEntity(entityQuery));
        return bhApi.call(putMethod);
    }

    private JSONObject update(EntityQuery entityQuery) throws IOException {
        String postUrl = getEntityUrl(entityQuery, entityQuery.getId().get().toString());
        log.info("Update Association: " + postUrl + " - " + entityQuery.toString());
        PostMethod postMethod = new PostMethod(postUrl);
        postMethod.setRequestEntity(getStringRequestEntity(entityQuery));
        return bhApi.call(postMethod);
    }

    private StringRequestEntity getStringRequestEntity(EntityQuery entityQuery) throws IOException {
        return new StringRequestEntity(
                bhApi.serialize(entityQuery.getNestedJson()),
                StringConsts.APPLICATION_JSON, StringConsts.UTF);
    }

    private String getEntityUrl(EntityQuery entityQuery) {
        return bhApi.getRestURL() + StringConsts.ENTITY_SLASH +
                toLabel(entityQuery.getEntity()) + "?" + restToken();
    }

    private String getEntityUrl(EntityQuery entityQuery, String identifier) {
        return bhApi.getRestURL() + StringConsts.ENTITY_SLASH +
                toLabel(entityQuery.getEntity()) + "/" + identifier + "?" + restToken();
    }

    private boolean idExistsButNotInRest(EntityQuery entityQuery) throws IOException {
        return entityQuery.getId().isPresent() && !idExistsInRest(entityQuery);
    }

    private boolean idExistsInRest(EntityQuery entityQuery) throws IOException {
        JSONObject queryResult = getCallById(entityQuery);
        int count = queryResult.getInt(StringConsts.COUNT);
        return count == 1;
    }

    // TODO: "query/" is being hardcoded, which fails for Candidate/Note
    private JSONObject getCallById(EntityQuery entityQuery) throws IOException {
        String queryURL = bhApi.getRestURL() + "query/"
                + toLabel(entityQuery.getEntity()) + "?fields=id&where="
                + entityQuery.getWhereByIdClause()
                + "&count=2"
                + restToken();
        return getCall(queryURL);
    }

    private String restToken() {
        return StringConsts.AND_BH_REST_TOKEN + bhApi.getBhRestToken();
    }

    public JSONObject getCall(EntityQuery entityQuery) throws IOException {
        if (entityQuery.getWhereClause().isEmpty()) {
            return getEmptyCountResponse();
        }
        String validationURL;
        if (StringConsts.CANDIDATE.equalsIgnoreCase(entityQuery.getEntity())) {
            validationURL = getSearchURL(entityQuery);
        } else {
            validationURL = getQueryURL(entityQuery);
        }
        return getCall(validationURL);
    }

    private String getSearchURL(EntityQuery entityQuery) {
        return bhApi.getRestURL() + StringConsts.SEARCH
                + toLabel(entityQuery.getEntity()) + "?fields=id&query="
                + entityQuery.getSearchClause()
                + "&count=2"
                + "&useV2=true"
                + restToken();
    }

    private String getQueryURL(EntityQuery entityQuery) {
        return bhApi.getRestURL() + StringConsts.QUERY
                + toLabel(entityQuery.getEntity()) + "?fields=id&where="
                + entityQuery.getWhereClause()
                + "&count=2"
                + restToken();
    }

    private static JSONObject getEmptyCountResponse() {
        JSONObject response = new JSONObject();
        response.put(StringConsts.COUNT, 0);
        response.put(StringConsts.DATA, new JSONArray());
        return response;
    }

    private JSONObject getCall(String queryURL) throws IOException {
        log.info("Querying for " + queryURL);
        GetMethod getMethod = new GetMethod(queryURL);
        return bhApi.call(getMethod);
    }

    private String toLabel(String entity) {
        Optional<String> label = bhApi.getLabelByName(entity);
        if (!label.isPresent()) {
            return entity;
        }
        return label.get();
    }
}
