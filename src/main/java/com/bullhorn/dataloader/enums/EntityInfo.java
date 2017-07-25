package com.bullhorn.dataloader.enums;

import com.bullhorn.dataloader.util.MethodUtil;
import com.bullhornsdk.data.model.entity.core.customobject.CustomObjectInstance;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.ClientCorporation;
import com.bullhornsdk.data.model.entity.core.standard.JobOrder;
import com.bullhornsdk.data.model.entity.core.standard.Opportunity;
import com.bullhornsdk.data.model.entity.core.standard.Placement;
import com.bullhornsdk.data.model.entity.core.type.CreateEntity;
import com.bullhornsdk.data.model.entity.core.type.HardDeleteEntity;
import com.bullhornsdk.data.model.entity.core.type.SoftDeleteEntity;
import com.bullhornsdk.data.model.entity.core.type.UpdateEntity;
import com.bullhornsdk.data.model.entity.embedded.Address;
import com.bullhornsdk.data.model.enums.BullhornEntityInfo;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;

/**
 * The list of all entities in SDK-REST supported by DataLoader.
 */
public enum EntityInfo {

    APPOINTMENT(BullhornEntityInfo.APPOINTMENT, 8),
    APPOINTMENT_ATTENDEE(BullhornEntityInfo.APPOINTMENT_ATTENDEE, 9),
    BUSINESS_SECTOR(BullhornEntityInfo.BUSINESS_SECTOR, 0),
    CANDIDATE(BullhornEntityInfo.CANDIDATE, 4),
    CANDIDATE_EDUCATION(BullhornEntityInfo.CANDIDATE_EDUCATION, 10),
    CANDIDATE_REFERENCE(BullhornEntityInfo.CANDIDATE_REFERENCE, 11),
    CANDIDATE_WORK_HISTORY(BullhornEntityInfo.CANDIDATE_WORK_HISTORY, 12),
    CATEGORY(BullhornEntityInfo.CATEGORY, 0),
    CERTIFICATION(BullhornEntityInfo.CERTIFICATION, 0),
    CLIENT_CONTACT(BullhornEntityInfo.CLIENT_CONTACT, 2),
    CLIENT_CORPORATION(BullhornEntityInfo.CLIENT_CORPORATION, 1),
    CORPORATE_USER(BullhornEntityInfo.CORPORATE_USER, 0),
    CORPORATION_DEPARTMENT(BullhornEntityInfo.CORPORATION_DEPARTMENT, 0),
    COUNTRY(BullhornEntityInfo.COUNTRY, 0),
    HOUSING_COMPLEX(BullhornEntityInfo.HOUSING_COMPLEX, 13),
    JOB_ORDER(BullhornEntityInfo.JOB_ORDER, 6),
    JOB_SUBMISSION(BullhornEntityInfo.JOB_SUBMISSION, 16),
    JOB_SUBMISSION_HISTORY(BullhornEntityInfo.JOB_SUBMISSION_HISTORY, 0),
    LEAD(BullhornEntityInfo.LEAD, 3),
    NOTE(BullhornEntityInfo.NOTE, 14),
    NOTE_ENTITY(BullhornEntityInfo.NOTE_ENTITY, 15),
    OPPORTUNITY(BullhornEntityInfo.OPPORTUNITY, 5),
    PLACEMENT(BullhornEntityInfo.PLACEMENT, 7),
    PLACEMENT_CHANGE_REQUEST(BullhornEntityInfo.PLACEMENT_CHANGE_REQUEST, 0),
    PLACEMENT_COMMISSION(BullhornEntityInfo.PLACEMENT_COMMISSION, 17),
    SENDOUT(BullhornEntityInfo.SENDOUT, 18),
    SKILL(BullhornEntityInfo.SKILL, 0),
    SPECIALTY(BullhornEntityInfo.SPECIALTY, 0),
    STATE(BullhornEntityInfo.STATE, 0),
    TASK(BullhornEntityInfo.TASK, 19),
    TEARSHEET(BullhornEntityInfo.TEARSHEET, 20),
    TIME_UNIT(BullhornEntityInfo.TIME_UNIT, 0),

    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, 51),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, 52),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_3(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_3, 53),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_4(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_4, 54),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_5(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_5, 55),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_6(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_6, 56),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_7(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_7, 57),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_8(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_8, 58),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_9(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_9, 59),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_10(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_10, 60),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_1(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_1, 61),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_2(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_2, 62),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_3(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_3, 63),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_4(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_4, 64),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_5(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_5, 65),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_6(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_6, 66),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_7(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_7, 67),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_8(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_8, 68),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_9(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_9, 69),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_10(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_10, 70),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_1(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_1, 71),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_2(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_2, 72),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_3(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_3, 73),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_4(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_4, 74),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_5(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_5, 75),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_6(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_6, 76),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_7(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_7, 77),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_8(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_8, 78),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_9(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_9, 79),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_10(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_10, 80),
    PERSON_CUSTOM_OBJECT_INSTANCE_1(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_1, 81),
    PERSON_CUSTOM_OBJECT_INSTANCE_2(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, 82),
    PERSON_CUSTOM_OBJECT_INSTANCE_3(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_3, 83),
    PERSON_CUSTOM_OBJECT_INSTANCE_4(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_4, 84),
    PERSON_CUSTOM_OBJECT_INSTANCE_5(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_5, 85),
    PERSON_CUSTOM_OBJECT_INSTANCE_6(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_6, 86),
    PERSON_CUSTOM_OBJECT_INSTANCE_7(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_7, 87),
    PERSON_CUSTOM_OBJECT_INSTANCE_8(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_8, 88),
    PERSON_CUSTOM_OBJECT_INSTANCE_9(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_9, 89),
    PERSON_CUSTOM_OBJECT_INSTANCE_10(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_10, 90),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_1(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_1, 91),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_2(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_2, 92),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_3(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_3, 93),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_4(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_4, 94),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_5(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_5, 95),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_6(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_6, 96),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_7(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_7, 97),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_8(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_8, 98),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_9(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_9, 99),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_10(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_10, 100);

    /**
     * Comparator for sorting EntityInfo objects in a sorted collection.
     */
    final static public Comparator<EntityInfo> loadOrderComparator =
        Comparator.comparingInt(EntityInfo::getLoadOrder);

    /**
     * Comparator for sorting EntityInfo objects in a sorted collection in reverse of the load.
     */
    final static public Comparator<EntityInfo> deleteOrderComparator =
        (firstEntityInfo, secondEntityInfo) -> secondEntityInfo.getLoadOrder() - firstEntityInfo.getLoadOrder();

    private BullhornEntityInfo bullhornEntityInfo;
    private Integer loadOrder;

    /**
     * Constructor for enum containing information about each entity type
     *
     * @param bullhornEntityInfo The contained SDK-REST Entity Info
     * @param loadOrder          The order in which this entity is loaded, when loading from a directory
     */
    EntityInfo(BullhornEntityInfo bullhornEntityInfo, Integer loadOrder) {
        this.bullhornEntityInfo = bullhornEntityInfo;
        this.loadOrder = loadOrder;
    }

    /**
     * Returns the entity that matches the given string, or null otherwise
     *
     * @param entityName Any entity name
     * @return the entity if it exists, null otherwise
     */
    public static EntityInfo fromString(String entityName) {
        for (EntityInfo entityInfo : EntityInfo.values()) {
            if (entityInfo.getEntityName().equalsIgnoreCase(entityName)) {
                return entityInfo;
            }
        }
        return null;
    }

    /**
     * The bullhornEntityInfo enum
     */
    public BullhornEntityInfo getBullhornEntityInfo() {
        return this.bullhornEntityInfo;
    }

    /**
     * The entity name usable in the Bullhorn's SDK-REST
     */
    public String getEntityName() {
        return bullhornEntityInfo.getName();
    }

    /**
     * The entity class used in the Bullhorn's SDK-REST
     */
    public Class getEntityClass() {
        return bullhornEntityInfo.getType();
    }

    /**
     * The order in which this entity should be loaded relative to other entities, to avoid
     * referencing issues.
     */
    public Integer getLoadOrder() {
        return this.loadOrder;
    }

    /**
     * True if this entity can be loaded using REST.
     */
    public boolean isLoadable() {
        return isInsertable() || isUpdatable();
    }

    /**
     * True if this entity can be inserted using REST.
     */
    public boolean isInsertable() {
        return CreateEntity.class.isAssignableFrom(getEntityClass());
    }

    /**
     * True if this entity can be updated using REST.
     */
    public boolean isUpdatable() {
        return UpdateEntity.class.isAssignableFrom(getEntityClass());
    }

    /**
     * True if this entity can be deleted using REST.
     */
    public boolean isDeletable() {
        return (isHardDeletable() || isSoftDeletable());
    }

    /**
     * True if this entity can be hard deleted using REST.
     */
    public boolean isHardDeletable() {
        return HardDeleteEntity.class.isAssignableFrom(getEntityClass());
    }

    /**
     * True if this entity can be soft deleted using REST.
     */
    public boolean isSoftDeletable() {
        return SoftDeleteEntity.class.isAssignableFrom(getEntityClass());
    }

    /**
     * True if this entity can not be loaded using REST.
     */
    public boolean isReadOnly() {
        return !isLoadable();
    }

    /**
     * True if this entity is a custom object class.
     */
    public boolean isCustomObject() {
        return CustomObjectInstance.class.isAssignableFrom(getEntityClass());
    }

    /**
     * True if this entity can have attachments.
     */
    public boolean isAttachmentEntity() {
        return (Candidate.class.isAssignableFrom(getEntityClass())
            || ClientContact.class.isAssignableFrom(getEntityClass())
            || ClientCorporation.class.isAssignableFrom(getEntityClass())
            || JobOrder.class.isAssignableFrom(getEntityClass())
            || Opportunity.class.isAssignableFrom(getEntityClass())
            || Placement.class.isAssignableFrom(getEntityClass()));
    }

    /**
     * Returns the setter methods that exist on the SDK-REST entity class as a map of field name to method
     *
     * @return A map of field name to setter methods that can invoked generically using `method.invoke`
     */
    public Map<String, Method> getSetterMethodMap() {
        Map<String, Method> methodMap = MethodUtil.getSetterMethodMap(getEntityClass());

        // Add individual address setters for any entity that has an address field, since address is a composite
        // field, not a direct field or an associated record.
        if (methodMap.containsKey("address")) {
            methodMap.putAll(MethodUtil.getSetterMethodMap(Address.class));
        }

        return methodMap;
    }
}
