package com.bullhorn.dataloader.service;

import java.io.IOException;
import java.util.Optional;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.bullhorn.dataloader.service.query.AssociationQuery;
import com.bullhorn.dataloader.util.BullhornAPI;
import com.google.common.cache.CacheLoader;

/**
 * AssociationCache handles creating, updating and retrieving IDs for entity queries.
 * It is implemented as a LoadingCache so we can easily swap out expiration or eviction
 * and we won't run into ConcurrentModificationException when this is passed around by
 * the top-level executor service.
 * <p>
 *     If the filter fields for the associated entities does not narrow it down to
 *     one result then the call returns Optional.empty().
 * </p>
 * <p>
 *     If the insertion fails then it returns Optional.empty().
 * </p>
 * <p>
 *     If the associated entity does not exist, then it is inserted and the id for the
 *     entity is returned.
 * </p>
 * <p>
 *
 * </p>
 */
public class AssociationCache extends CacheLoader<AssociationQuery, Optional<Integer>> {

    private final Log log = LogFactory.getLog(AssociationCache.class);
    private final BullhornAPI bhapi;

    public AssociationCache(BullhornAPI bullhornAPI) {
        this.bhapi = bullhornAPI;
    }

    @Override
    public Optional<Integer> load(AssociationQuery key) throws IOException {
        return query(key);
    }

    private Optional<Integer> query(AssociationQuery associationQuery) throws IOException {
        String getURL = bhapi.getRestURL() + "query/" +
                associationQuery.getEntity() + "?fields=id,name&where="
                + associationQuery.getWhereClause()
                + "&count=2"
                + "&BhRestToken=" + bhapi.getBhRestToken();
        GetMethod queryBH = new GetMethod(getURL);
        JSONObject qryJSON = bhapi.get(queryBH);
        if (qryJSON.getInt("count") == 0) {
            Integer id = insert(associationQuery);
            return Optional.of(id);
        } else if (qryJSON.getInt("count") == 1) {
            return Optional.of(qryJSON.getJSONArray("data").getJSONObject(0).getInt("id"));
        } else {
            // More than one entity retrieved by the query so we cannot use the result for anything
            return Optional.empty();
        }

    }

    private Integer insert(AssociationQuery associationQuery) {
        String insertionNotImplemented = "Inserts for associations not yet implemented";
        log.error(insertionNotImplemented);
        throw new NotImplementedException(insertionNotImplemented);
    }
}
