package com.bullhorn.dataloader.service.query;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bullhorn.dataloader.service.api.BullhornApiUpdater;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.StringConsts;
import com.google.common.cache.CacheLoader;

/**
 * EntityCache handles creating, updating and retrieving IDs for entity queries with the help of BullhornApiUpdater.
 * <p>
 * It is implemented as a LoadingCache so we can easily swap out expiration or eviction and we won't run into
 * ConcurrentModificationException when this is passed around by the top-level executor service.
 * <p>
 * For more information on caches, see: https://github.com/google/guava/wiki/CachesExplained
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
public class EntityCache extends CacheLoader<EntityQuery, Result> {

    private final BullhornApiUpdater bhApiUpdater;

    public EntityCache(BullhornApiUpdater bhApiUpdater) {
        this.bhApiUpdater = bhApiUpdater;
    }

    /**
     * Called by the LoadingCache.get() method whenever there is a cache miss.
     * Our load mechanism will grab the Bullhorn Internal ID for associating it with the given EntityQuery object.
     *
     * @param query The entity query for searching REST
     * @return The result of the REST query
     * @throws IOException For REST errors.
     */
    @Override
    public Result load(EntityQuery query) throws IOException {
        // Make get call to determine if entity already exists
        JSONObject jsonResponse = bhApiUpdater.getCall(query);
        if (!jsonResponse.has(StringConsts.COUNT)) {
            return Result.Failure("ERROR: JSON response is missing \"" + StringConsts.COUNT + "\" field. Received: " +
                    jsonResponse.toString());
        }

        int count = jsonResponse.getInt(StringConsts.COUNT);
        JSONArray identifiers = jsonResponse.getJSONArray(StringConsts.DATA);

        if (count == 0 || query.getFilterFieldCount() == 0) {
            return bhApiUpdater.merge(query);
        } else if (count == 1) {
            return Result.Update(identifiers.getJSONObject(0).getInt(StringConsts.ID));
        } else {
            return Result.Failure("ERROR: Association returned more than 1 result: " + query);
        }
    }
}
