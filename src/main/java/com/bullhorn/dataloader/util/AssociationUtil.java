package com.bullhorn.dataloader.util;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for determining the associated fields that are on a given entity.
 * <p>
 * These are used for checking which associations are possible in order to throw meaningful errors up front.
 */
public class AssociationUtil {
    // This is a cached list of the associations per entity
    // The key is always this entity, and each value is the entity that it is associated to
    static private Map<Class<AssociationEntity>, List<AssociationField<AssociationEntity, BullhornEntity>>> entityClassToAssociationsMap = new HashMap<>();

    /**
     * Returns the list of associated fields for the given SDK-REST entity class.
     * <p>
     * Synchronized to avoid race condition when multiple tasks are initializing at the same time on their
     * different threads, and all calling this method the first time through.
     *
     * @param entityClass The SDK-REST entity class
     * @return The list of this entity (key is always this entity class) to the entity's associated classes
     */
    public static synchronized List<AssociationField<AssociationEntity, BullhornEntity>> getAssociationFields(Class entityClass) {
        try {
            if (entityClassToAssociationsMap.containsKey(entityClass)) {
                return entityClassToAssociationsMap.get(entityClass);
            } else {
                EntityAssociations entityAssociations = getEntityAssociations(entityClass);
                List<AssociationField<AssociationEntity, BullhornEntity>> associationFields = entityAssociations.allAssociations();
                entityClassToAssociationsMap.put(entityClass, associationFields);
                return associationFields;
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Returns the associations object from SDK-REST for the given entity
     * <p>
     * Synchronized to avoid race condition when multiple tasks are initializing at the same time on their
     * different threads, and all calling this method the first time through.
     *
     * @param entityClass An SDK-REST class with associations
     * @return The associations list
     */
    private static synchronized EntityAssociations getEntityAssociations(Class entityClass) {
        return (entityClass == Candidate.class ? AssociationFactory.candidateAssociations() :
            (entityClass == Category.class ? AssociationFactory.categoryAssociations() :
                (entityClass == ClientContact.class ? AssociationFactory.clientContactAssociations() :
                    (entityClass == ClientCorporation.class ? AssociationFactory.clientCorporationAssociations() :
                        (entityClass == CorporateUser.class ? AssociationFactory.corporateUserAssociations() :
                            (entityClass == JobOrder.class ? AssociationFactory.jobOrderAssociations() :
                                (entityClass == Note.class ? AssociationFactory.noteAssociations() :
                                    (entityClass == Placement.class ? AssociationFactory.placementAssociations() :
                                        (entityClass == Opportunity.class ? AssociationFactory.opportunityAssociations() :
                                            (entityClass == Lead.class ? AssociationFactory.leadAssociations() :
                                                entityClass == Tearsheet.class ? AssociationFactory.tearsheetAssociations() : null))))))))));
    }
}
