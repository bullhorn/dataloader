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
 * EntityCache handles creating, updating and retrieving IDs for entity queries.
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
public class EntityCache extends CacheLoader<EntityQuery, Optional<Integer>> {

    private final Log log = LogFactory.getLog(EntityCache.class);
    private final BullhornAPI bhapi;

    public EntityCache(BullhornAPI bullhornAPI) {
        this.bhapi = bullhornAPI;
    }

    @Override
    public Optional<Integer> load(EntityQuery query) throws IOException {
        JSONObject qryJSON = getCall(query);
        if (!qryJSON.has(StringConsts.COUNT)) {
            return Optional.empty();
        }
        int count = qryJSON.getInt(StringConsts.COUNT);
        JSONArray identifiers = qryJSON.getJSONArray(StringConsts.DATA);

        Optional<Integer> ret = Optional.empty();
        if (count == 0 || query.getFilterFieldCount() == 0) {
            ret = merge(query);
        } else if (count == 1) {
            ret = Optional.of(identifiers.getJSONObject(0).getInt(StringConsts.ID));
        } else {
            log.error("Association returned more than 1 result" + query);
        }
        return ret;
    }


    private Optional<Integer> merge(EntityQuery entityQuery) throws IOException {
        if (idExistsButNotInRest(entityQuery)) {
            log.error("Association cannot be merged " + entityQuery.toString());
            return Optional.empty();
        }

        JSONObject ret;
        if (entityQuery.getId().isPresent()) {
            ret = update(entityQuery);
        } else {
            ret = insert(entityQuery);
        }
        if (ret.has("errorMessage")) {
            log.error("Association query " + entityQuery.toString()
                    + " failed for reason " + ret.getString("errorMessage"));
            if (ret.has("errors")) {
                log.error(" errors: " + ret.getJSONArray("errors"));
            }
            return Optional.empty();
        }
        return Optional.of(ret.getInt(StringConsts.CHANGED_ENTITY_ID));
    }

    private JSONObject insert(EntityQuery entityQuery) throws IOException {
        log.info("Inserting association " + entityQuery.toString());
        String putUrl = getEntityUrl(entityQuery);
        PutMethod method = getPutMethod(entityQuery, putUrl);
        return bhapi.put(method);
    }

    private PutMethod getPutMethod(EntityQuery entityQuery, String putUrl) throws IOException {
        StringRequestEntity requestEntity = getStringRequestEntity(entityQuery);
        PutMethod method = new PutMethod(putUrl);
        method.setRequestEntity(requestEntity);
        return method;
    }

    private StringRequestEntity getStringRequestEntity(EntityQuery entityQuery) throws IOException {
        return new StringRequestEntity(
                bhapi.serialize(entityQuery.getNestedJson()),
                StringConsts.APPLICATION_JSON, StringConsts.UTF);
    }

    private JSONObject update(EntityQuery entityQuery) throws IOException {
        log.info("Updating association " + entityQuery.toString());
        String postUrl = getEntityUrl(entityQuery,
                entityQuery.getId().get().toString());
        StringRequestEntity requestEntity = getStringRequestEntity(entityQuery);
        PostMethod method = new PostMethod(postUrl);
        method.setRequestEntity(requestEntity);
        return bhapi.post(method);
    }

    private String getEntityUrl(EntityQuery entityQuery) {
        String url = bhapi.getRestURL() + StringConsts.ENTITY_SLASH
                + toLabel(entityQuery.getEntity())
                + "?" + restToken();
        log.info("insert: " + url);
        return url;
    }

    private String getEntityUrl(EntityQuery entityQuery, String identifier) {
        String url = bhapi.getRestURL() + StringConsts.ENTITY_SLASH
                + toLabel(entityQuery.getEntity()) + "/"
                + identifier + "?" + restToken();
        log.info("update: " + url);
        return url;
    }


    private boolean idExistsButNotInRest(EntityQuery entityQuery) throws IOException {
        return entityQuery.getId().isPresent() && idExistsInRest(entityQuery);
    }

    private boolean idExistsInRest(EntityQuery entityQuery) throws IOException {
        JSONObject queryResult = getCallById(entityQuery);
        int count = queryResult.getInt(StringConsts.COUNT);
        return count == 1;
    }

    private JSONObject getCallById(EntityQuery entityQuery) throws IOException {
        String queryURL = bhapi.getRestURL() + "query/"
                + toLabel(entityQuery.getEntity()) + "?fields=id&where="
                + entityQuery.getWhereByIdClause()
                + "&count=2"
                + restToken();
        return getCall(queryURL);
    }

    private String restToken() {
        return StringConsts.AND_BH_REST_TOKEN + bhapi.getBhRestToken();
    }

    private JSONObject getCall(EntityQuery entityQuery) throws IOException {
        if (entityQuery.getWhereClause().isEmpty()) {
            return getEmptyCountResponse();
        }
        String validationURL;
        if (StringConsts.CANDIDATE.equals(entityQuery.getEntity())) {
            validationURL = getSearchURL(entityQuery);
        } else {
            validationURL = getQueryURL(entityQuery);
        }
        return getCall(validationURL);
    }

    private String getSearchURL(EntityQuery entityQuery) {
        return bhapi.getRestURL() + StringConsts.SEARCH
                + toLabel(entityQuery.getEntity()) + "?fields=id&query="
                + entityQuery.getSearchClause()
                + "&count=2"
                + "&useV2=true"
                + restToken();
    }

    private String getQueryURL(EntityQuery entityQuery) {
        return bhapi.getRestURL() + StringConsts.QUERY
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
