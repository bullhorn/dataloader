package com.bullhorn.dataloader.util.validation;

import com.bullhorn.dataloader.util.StringConsts;

import java.util.Arrays;

public class EntityValidation {

    /**
     * Convenience method which returns true if the entity can be loaded using REST.
     *
     * @return True if can be loaded by DataLoader
     */
    public static boolean isLoadable(String entityName) {
        return (isHardDeletable(entityName) || isSoftDeletable(entityName) || isNotDeletable(entityName));
    }

    /**
     * Convenience method which returns true if the entity can be deleted using REST.
     *
     * @return True if can be deleted by DataLoader
     */
    public static boolean isDeletable(String entityName) {
        return (isHardDeletable(entityName) || isSoftDeletable(entityName));
    }

    /**
     * Convenience method which returns true if an entity is part of the set of hard deletable entities in REST.
     *
     * @return True if can be hard deleted
     */
    public static boolean isHardDeletable(String entityName) {
        return Arrays.asList(StringConsts.HARD_DELETABLE_ENTITIES).contains(entityName);
    }

    /**
     * Convenience method which returns true if an entity is part of the set of soft deletable entities in REST.
     *
     * @return True if must be soft deleted
     */
    public static boolean isSoftDeletable(String entityName) {
        return Arrays.asList(StringConsts.SOFT_DELETABLE_ENTITIES).contains(entityName);
    }

    /**
     * Convenience method which returns true if an entity is part of the set of mutable but not deletable entities
     * in REST.
     *
     * @return True if can be inserted and updated but not deleted
     */
    public static boolean isNotDeletable(String entityName) {
        return Arrays.asList(StringConsts.NOT_DELETABLE_ENTITIES).contains(entityName);
    }

    /**
     * Convenience method which returns true if an entity is part of the set of read only entities in REST.
     *
     * @return True if read only
     */
    public static boolean isReadOnly(String entityName) {
        return Arrays.asList(StringConsts.READ_ONLY_ENTITIES).contains(entityName);
    }
}
