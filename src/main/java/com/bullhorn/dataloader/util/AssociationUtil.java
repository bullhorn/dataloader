package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Field;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.association.AssociationFactory;
import com.bullhornsdk.data.model.entity.association.AssociationField;
import com.bullhornsdk.data.model.entity.association.EntityAssociations;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.Category;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.ClientCorporation;
import com.bullhornsdk.data.model.entity.core.standard.CorporateUser;
import com.bullhornsdk.data.model.entity.core.standard.JobOrder;
import com.bullhornsdk.data.model.entity.core.standard.Lead;
import com.bullhornsdk.data.model.entity.core.standard.Note;
import com.bullhornsdk.data.model.entity.core.standard.Opportunity;
import com.bullhornsdk.data.model.entity.core.standard.Placement;
import com.bullhornsdk.data.model.entity.core.standard.Tearsheet;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for determining the associated fields that are on a given entity.
 *
 * These are used for checking which associations are possible in order to throw meaningful errors up front.
 */
public class AssociationUtil {
    // This is a cached list of the associations per entity
    // The key is always this entity, and each value is the entity that it is associated to
    private static Map<EntityInfo, List<AssociationField<AssociationEntity, BullhornEntity>>>
        entityToAssociationsMap = new HashMap<>();

    /**
     * Returns the list of associated fields for the given SDK-REST entity class.
     *
     * Synchronized to avoid race condition when multiple tasks are initializing at the same time on their
     * different threads, and all calling this method the first time through.
     *
     * @param entityInfo The entity type
     * @return The list of this entity (key is always this entity class) to the entity's associated classes
     */
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public static synchronized List<AssociationField<AssociationEntity, BullhornEntity>> getToManyFields(
        EntityInfo entityInfo) {
        try {
            if (entityToAssociationsMap.containsKey(entityInfo)) {
                return entityToAssociationsMap.get(entityInfo);
            } else {
                EntityAssociations entityAssociations = getEntityAssociations(entityInfo);
                List<AssociationField<AssociationEntity, BullhornEntity>> associationFields =
                    entityAssociations.allAssociations();
                entityToAssociationsMap.put(entityInfo, associationFields);
                return associationFields;
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Returns the AssociationField object for a given entity and association on that entity.
     *
     * @param entityInfo          the entity type
     * @param associationBaseName the association on the entity
     * @return the association object, if it exists
     */
    public static AssociationField getToManyField(EntityInfo entityInfo, String associationBaseName) {
        List<AssociationField<AssociationEntity, BullhornEntity>> associationFields = getToManyFields(entityInfo);
        for (AssociationField associationField : associationFields) {
            if (associationField.getAssociationFieldName().equalsIgnoreCase(associationBaseName)) {
                return associationField;
            }
        }
        throw new RestApiException("'" + associationBaseName + "' does not exist on " + entityInfo.getEntityName());
    }

    public static AssociationField getToManyField(Field field) {
        return getToManyField(field.getEntityInfo(), field.getCell().getAssociationBaseName());
    }

    /**
     * Returns the Custom Object AssociationField for a given parent entity type and custom object type.
     *
     * @param customObject The type of the custom object
     * @param parent       The type of the parent
     * @return The associationField if it exists
     */
    public static AssociationField getCustomObjectField(EntityInfo customObject, EntityInfo parent) {
        String associationName = getCustomObjectAssociationName(customObject);
        return getToManyField(parent, associationName);
    }

    /**
     * Returns the associated entity for To-One or To-Many fields, or the current entity for direct fields.
     *
     * For To-Many associations, the AssociationFields are used. For To-One associations, the name of the field is used
     * to get the name of the associated entity.
     *
     * @param entityInfo the current entity
     * @param cell       the cell of data for the current entity
     * @return either the current entity or the associated entity
     */
    public static EntityInfo getFieldEntity(EntityInfo entityInfo, Cell cell) {
        if (cell.isAssociation()) {
            if (isToMany(entityInfo, cell.getAssociationBaseName())) {
                AssociationField associationField = getToManyField(entityInfo, cell.getAssociationBaseName());
                return EntityInfo.fromString(associationField.getAssociationType().getSimpleName());
            } else {
                Method setMethod = MethodUtil.getSetterMethod(entityInfo, cell.getAssociationBaseName());
                return EntityInfo.fromString(setMethod.getParameterTypes()[0].getSimpleName());
            }
        }
        return entityInfo;
    }

    /**
     * Returns the 'get' method for getting the associations from an entity.
     *
     * @param associationField The association field on the entity
     * @param associationName  The association field name
     * @return The get method that returns the association list
     */
    @SuppressWarnings("unchecked")
    public static Method getAssociationGetMethod(AssociationField associationField, String associationName) {
        String methodName = "get" + associationName.substring(0, 1).toUpperCase() + associationName.substring(1);
        try {
            return associationField.getAssociationType().getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new RestApiException("'" + associationField.getAssociationFieldName()
                + "." + associationName + "': '" + associationName + "' does not exist on "
                + associationField.getAssociationType().getSimpleName());
        }
    }

    /**
     * Returns true if the association is a To-Many association, false otherwise (To-One).
     */
    public static Boolean isToMany(EntityInfo entityInfo, String associationBaseName) {
        List<AssociationField<AssociationEntity, BullhornEntity>> associationFields =
            AssociationUtil.getToManyFields(entityInfo);
        for (AssociationField associationField : associationFields) {
            if (associationField.getAssociationFieldName().equalsIgnoreCase(associationBaseName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the associations object from SDK-REST for the given entity
     *
     * Synchronized to avoid race condition when multiple tasks are initializing at the same time on their
     * different threads, and all calling this method the first time through.
     *
     * @param entityInfo An SDK-REST class with associations
     * @return The associations list
     */
    private static synchronized EntityAssociations getEntityAssociations(EntityInfo entityInfo) {
        return (entityInfo.getEntityClass() == Candidate.class ? AssociationFactory.candidateAssociations() :
            (entityInfo.getEntityClass() == Category.class ? AssociationFactory.categoryAssociations() :
                (entityInfo.getEntityClass() == ClientContact.class ? AssociationFactory.clientContactAssociations() :
                    (entityInfo.getEntityClass() == ClientCorporation.class ? AssociationFactory.clientCorporationAssociations() :
                        (entityInfo.getEntityClass() == CorporateUser.class ? AssociationFactory.corporateUserAssociations() :
                            (entityInfo.getEntityClass() == JobOrder.class ? AssociationFactory.jobOrderAssociations() :
                                (entityInfo.getEntityClass() == Note.class ? AssociationFactory.noteAssociations() :
                                    (entityInfo.getEntityClass() == Placement.class ? AssociationFactory.placementAssociations() :
                                        (entityInfo.getEntityClass() == Opportunity.class ? AssociationFactory.opportunityAssociations() :
                                            (entityInfo.getEntityClass() == Lead.class ? AssociationFactory.leadAssociations() :
                                                entityInfo.getEntityClass() == Tearsheet.class ? AssociationFactory.tearsheetAssociations() : null))))))))));
    }

    /**
     * Returns the name of the custom object association.
     *
     * @param customObjectEntityInfo The entityInfo object for a custom object
     * @return the association name from the parent entity
     */
    private static String getCustomObjectAssociationName(EntityInfo customObjectEntityInfo) {
        String entityName = customObjectEntityInfo.getEntityName();
        String instanceNumber = entityName.substring(entityName.length() - 1, entityName.length());
        return "customObject" + instanceNumber + "s";
    }
}
