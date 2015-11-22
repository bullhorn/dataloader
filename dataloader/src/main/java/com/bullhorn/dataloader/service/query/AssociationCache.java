package com.bullhorn.dataloader.service.query;

import java.io.IOException;
import java.util.Optional;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.util.StringConsts;
import com.google.common.cache.CacheLoader;

/**
 * AssociationCache handles creating, updating and retrieving IDs for entity queries.
 * It is implemented as a LoadingCache so we can easily swap out expiration or eviction
 * and we won't run into ConcurrentModificationException when this is passed around by
 * the top-level executor service.
 * <p>
 * If the filter fields for the associated entities does not narrow it down to
 * one result then the call returns Optional.empty().
 * </p>
 * <p>
 * If the insertion fails then it returns Optional.empty().
 * </p>
 * <p>
 * If the associated entity does not exist, then it is inserted and the id for the
 * entity is returned.
 * </p>
 * <p>
 * <p>
 * </p>
 */
public class AssociationCache extends CacheLoader<AssociationQuery, Optional<Integer>> {

    private final Log log = LogFactory.getLog(AssociationCache.class);
    private final BullhornAPI bhapi;

    public AssociationCache(BullhornAPI bullhornAPI) {
        this.bhapi = bullhornAPI;
    }

    @Override
    public Optional<Integer> load(AssociationQuery query) throws IOException {
        JSONObject qryJSON = getCall(query);
        if (!qryJSON.has(StringConsts.COUNT)) {
            return Optional.empty();
        }
        int count = qryJSON.getInt("count");
        JSONArray identifiers = qryJSON.getJSONArray("data");

        Optional<Integer> ret = Optional.empty();
        if (count == 0) {
            ret = merge(query);
        } else if (count == 1) {
            ret = Optional.of(identifiers.getJSONObject(0).getInt("id"));
        } else {
            log.error("Association returned more than 1 result" + query);
        }
        return ret;
    }


    private Optional<Integer> merge(AssociationQuery associationQuery) throws IOException {
        if (idExistsButNotInRest(associationQuery)) {
            log.error("Association cannot be merged " + associationQuery.toString());
            return Optional.empty();
        }

        JSONObject ret;
        if (associationQuery.getId().isPresent()) {
            ret = update(associationQuery);
        } else {
            ret = insert(associationQuery);
        }
        if (ret.has("errorMessage")) {
            log.error("Association query " + associationQuery.toString()
                    + " failed for reason " + ret.getString("errorMessage"));
            return Optional.empty();
        }
        return Optional.of(ret.getInt(StringConsts.CHANGED_ENTITY_ID));
    }

    private JSONObject insert(AssociationQuery associationQuery) throws IOException {
        log.info("Inserting association " + associationQuery.toString());
        String putUrl = getEntityUrl(associationQuery);
        PutMethod method = getPutMethod(associationQuery, putUrl);
        return bhapi.put(method);
    }

    private PutMethod getPutMethod(AssociationQuery associationQuery, String putUrl) throws IOException {
        StringRequestEntity requestEntity = getStringRequestEntity(associationQuery);
        PutMethod method = new PutMethod(putUrl);
        method.setRequestEntity(requestEntity);
        return method;
    }

    private StringRequestEntity getStringRequestEntity(AssociationQuery associationQuery) throws IOException {
        return new StringRequestEntity(
                    bhapi.serialize(associationQuery.getNestedJson()),
                    StringConsts.APPLICATION_JSON, StringConsts.UTF);
    }

    private JSONObject update(AssociationQuery associationQuery) throws IOException {
        log.info("Updating association " + associationQuery.toString());
        String postUrl = getEntityUrl(associationQuery,
                associationQuery.getId().get().toString());
        StringRequestEntity requestEntity = getStringRequestEntity(associationQuery);
        PostMethod method = new PostMethod(postUrl);
        method.setRequestEntity(requestEntity);
        return bhapi.post(method);
    }

    private String getEntityUrl(AssociationQuery associationQuery) {
        String url = bhapi.getRestURL() + StringConsts.ENTITY_SLASH
                + toLabel(associationQuery.getEntity())
                + "?" + restToken();
        log.info("insert: " + url);
        return url;
    }

    private String getEntityUrl(AssociationQuery associationQuery, String identifier) {
        String url = bhapi.getRestURL() + StringConsts.ENTITY_SLASH
                + toLabel(associationQuery.getEntity()) + "/"
                + identifier + "?" + restToken();
        log.info("update: " + url);
        return url;
    }


    private boolean idExistsButNotInRest(AssociationQuery associationQuery) throws IOException {
        return associationQuery.getId().isPresent() && idExistsInRest(associationQuery);
    }

    private boolean idExistsInRest(AssociationQuery associationQuery) throws IOException {
        JSONObject queryResult = getCallById(associationQuery);
        int count = queryResult.getInt("count");
        return count == 1;
    }

    private JSONObject getCallById(AssociationQuery associationQuery) throws IOException {
        String queryURL = bhapi.getRestURL() + "query/"
                + toLabel(associationQuery.getEntity()) + "?fields=id&where="
                + associationQuery.getWhereByIdClause()
                + "&count=2"
                + restToken();
        return getCall(queryURL);
    }

    private String restToken() {
        return StringConsts.AND_BH_REST_TOKEN + bhapi.getBhRestToken();
    }

    private JSONObject getCall(AssociationQuery associationQuery) throws IOException {
        if(associationQuery.getWhereClause().isEmpty()) {
            return getEmptyCountResponse();
        }

        String queryURL = bhapi.getRestURL() + "query/"
                + toLabel(associationQuery.getEntity()) + "?fields=id&where="
                + associationQuery.getWhereClause()
                + "&count=2"
                + restToken();
        return getCall(queryURL);
    }

    private static JSONObject getEmptyCountResponse() {
        JSONObject response = new JSONObject();
        response.put("count", 0);
        response.put("data", new JSONArray());
        return response;
    }

    private JSONObject getCall(String queryURL) throws IOException {
        log.info("Querying for " + queryURL);
        GetMethod queryBH = new GetMethod(queryURL);
        return bhapi.get(queryBH);
    }

    private String toLabel(String entity) {
        Optional<String> label = bhapi.getLabelByName(entity);
        if (!label.isPresent()) {
            return entity;
        }
        return label.get();
    }
}
