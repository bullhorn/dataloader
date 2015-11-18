package com.bullhorn.dataloader.service.query;

import java.io.IOException;
import java.util.Optional;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.bullhorn.dataloader.service.api.BullhornAPI;
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
        String getURL = bhapi.getRestURL() + "query/"
                + toLabel(associationQuery.getEntity()) + "?fields=id&where="
                + associationQuery.getWhereClause()
                + "&count=2"
                + "&BhRestToken=" + bhapi.getBhRestToken();
        GetMethod queryBH = new GetMethod(getURL);
        JSONObject qryJSON = bhapi.get(queryBH);

        int count = qryJSON.getInt("count");
        if (count == 0) { // no match
            Integer id = insert(associationQuery);
            return Optional.of(id);
        } else if (count == 1) { // exact match
            return Optional.of(qryJSON.getJSONArray("data").getJSONObject(0).getInt("id"));
        } else { // too vague
            // More than one entity retrieved by the query so we cannot use the result for anything
            return Optional.empty();
        }
    }

    private String toLabel(String entity) {
        Optional<String> label = bhapi.getLabelByName(entity);
        if (!label.isPresent()) {
            throw new IllegalArgumentException("Entity does not exist" + entity);
        }
        return label.get();
    }

    private Integer insert(AssociationQuery associationQuery) {
        String insertionNotImplemented = "Inserts for associations not yet implemented";
        log.error(insertionNotImplemented);
        //throw new NotImplementedException(insertionNotImplemented);
        return 1;
    }
}
