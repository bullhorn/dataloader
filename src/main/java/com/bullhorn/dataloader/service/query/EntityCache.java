package com.bullhorn.dataloader.service.query;

import java.io.IOException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.api.BullhornApiUpdater;
import com.bullhorn.dataloader.util.StringConsts;
import com.google.common.cache.CacheLoader;

/**
 * EntityCache handles creating, updating and retrieving IDs for entity queries
 * with the help of BullhornApiUpdater.
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
 */
public class EntityCache extends CacheLoader<EntityQuery, Optional<Integer>> {

    private final Logger log = LogManager.getLogger(EntityCache.class);
    private final BullhornApiUpdater bhapiUpdater;

    public EntityCache(BullhornAPI bhapi) {
        this.bhapiUpdater = new BullhornApiUpdater(bhapi);
    }

    @Override
    public Optional<Integer> load(EntityQuery query) throws IOException {
        JSONObject qryJSON = bhapiUpdater.getCall(query);
        if (!qryJSON.has(StringConsts.COUNT)) {
            return Optional.empty();
        }
        int count = qryJSON.getInt(StringConsts.COUNT);
        JSONArray identifiers = qryJSON.getJSONArray(StringConsts.DATA);

        Optional<Integer> ret = Optional.empty();
        if (count == 0 || query.getFilterFieldCount() == 0) {
            ret = bhapiUpdater.merge(query);
        } else if (count == 1) {
            ret = Optional.of(identifiers.getJSONObject(0).getInt(StringConsts.ID));
        } else {
            log.error("Association returned more than 1 result" + query);
        }
        return ret;
    }
}
