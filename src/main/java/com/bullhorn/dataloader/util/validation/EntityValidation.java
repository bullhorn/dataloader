package com.bullhorn.dataloader.util.validation;

import com.bullhornsdk.data.model.entity.core.customobject.CustomObjectInstance;
import com.bullhornsdk.data.model.entity.core.type.CreateEntity;
import com.bullhornsdk.data.model.entity.core.type.HardDeleteEntity;
import com.bullhornsdk.data.model.entity.core.type.SoftDeleteEntity;
import com.bullhornsdk.data.model.entity.core.type.UpdateEntity;
import com.bullhornsdk.data.model.enums.BullhornEntityInfo;

public class EntityValidation {

	/**
	 * Convenience method which returns true if the entity can be loaded using REST.
	 *
	 * @return True if can be loaded by DataLoader
	 */
	public static boolean isLoadable(String entityName) {
        return isInsertable(entityName) || isUpdatable(entityName);
	}

    /**
     * Convenience method which returns true if the entity can be inserted using REST.
     *
     * @return True if can be loaded by DataLoader
     */
    public static boolean isInsertable(String entityName) {
        return CreateEntity.class.isAssignableFrom(BullhornEntityInfo.getTypeFromName(entityName).getType());
    }

    /**
     * Convenience method which returns true if the entity can be updated using REST.
     *
     * @return True if can be loaded by DataLoader
     */
    public static boolean isUpdatable(String entityName) {
        return UpdateEntity.class.isAssignableFrom(BullhornEntityInfo.getTypeFromName(entityName).getType());
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
		return HardDeleteEntity.class.isAssignableFrom(BullhornEntityInfo.getTypeFromName(entityName).getType());
	}

	/**
	 * Convenience method which returns true if an entity is part of the set of soft deletable entities in REST.
	 *
	 * @return True if must be soft deleted
	 */
	public static boolean isSoftDeletable(String entityName) {
        return SoftDeleteEntity.class.isAssignableFrom(BullhornEntityInfo.getTypeFromName(entityName).getType());
	}

	/**
	 * Convenience method which returns true if an entity is part of the set of mutable but not deletable entities
	 * in REST.
	 *
	 * @return True if can be inserted and updated but not deleted
	 */
	public static boolean isNotDeletable(String entityName) {
		return !isDeletable(entityName);
	}

	/**
	 * Convenience method which returns true if an entity is part of the set of read only entities in REST.
	 *
	 * @return True if read only
	 */
	public static boolean isReadOnly(String entityName) {
		return !isLoadable(entityName);
	}

    /**
     * Convenience method which returns true if an entity is a custom object class.
     *
     * @return True if read only
     */
    public static boolean isCustomObject(String entityName) {
        return CustomObjectInstance.class.isAssignableFrom(BullhornEntityInfo.getTypeFromName(entityName).getType());
    }
}
