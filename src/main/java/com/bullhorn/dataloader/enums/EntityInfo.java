package com.bullhorn.dataloader.enums;

import com.bullhorn.dataloader.util.MethodUtil;
import com.bullhornsdk.data.model.entity.core.customobjectinstances.CustomObjectInstance;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.ClientCorporation;
import com.bullhornsdk.data.model.entity.core.standard.JobOrder;
import com.bullhornsdk.data.model.entity.core.standard.Opportunity;
import com.bullhornsdk.data.model.entity.core.standard.Placement;
import com.bullhornsdk.data.model.entity.core.type.CreateEntity;
import com.bullhornsdk.data.model.entity.core.type.HardDeleteEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.entity.core.type.SoftDeleteEntity;
import com.bullhornsdk.data.model.entity.core.type.UpdateEntity;
import com.bullhornsdk.data.model.entity.embedded.Address;
import com.bullhornsdk.data.model.enums.BullhornEntityInfo;
import com.bullhornsdk.data.model.file.FileMeta;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;

/**
 * The list of all entities in SDK-REST supported by DataLoader.
 *
 * A loadOrder of 0 is used for Read-Only Lookup Entities that must be configured via BullhornAdmin.
 */
public enum EntityInfo {

    APPOINTMENT(BullhornEntityInfo.APPOINTMENT, 8),
    APPOINTMENT_ATTENDEE(BullhornEntityInfo.APPOINTMENT_ATTENDEE, 9),
    BUSINESS_SECTOR(BullhornEntityInfo.BUSINESS_SECTOR, 0),
    CANDIDATE(BullhornEntityInfo.CANDIDATE, 4),
    CANDIDATE_CERTIFICATION(BullhornEntityInfo.CANDIDATE_CERTIFICATION, 10),
    CANDIDATE_EDUCATION(BullhornEntityInfo.CANDIDATE_EDUCATION, 11),
    CANDIDATE_REFERENCE(BullhornEntityInfo.CANDIDATE_REFERENCE, 12),
    CANDIDATE_WORK_HISTORY(BullhornEntityInfo.CANDIDATE_WORK_HISTORY, 13),
    CATEGORY(BullhornEntityInfo.CATEGORY, 0),
    CERTIFICATION(BullhornEntityInfo.CERTIFICATION, 0),
    CLIENT_CONTACT(BullhornEntityInfo.CLIENT_CONTACT, 2),
    CLIENT_CORPORATION(BullhornEntityInfo.CLIENT_CORPORATION, 1),
    CORPORATE_USER(BullhornEntityInfo.CORPORATE_USER, 0),
    CORPORATION_DEPARTMENT(BullhornEntityInfo.CORPORATION_DEPARTMENT, 0),
    COUNTRY(BullhornEntityInfo.COUNTRY, 0),
    HOUSING_COMPLEX(BullhornEntityInfo.HOUSING_COMPLEX, 14),
    JOB_ORDER(BullhornEntityInfo.JOB_ORDER, 6),
    JOB_SUBMISSION(BullhornEntityInfo.JOB_SUBMISSION, 17),
    JOB_SUBMISSION_HISTORY(BullhornEntityInfo.JOB_SUBMISSION_HISTORY, 0),
    LEAD(BullhornEntityInfo.LEAD, 3),
    NOTE(BullhornEntityInfo.NOTE, 15),
    NOTE_ENTITY(BullhornEntityInfo.NOTE_ENTITY, 16),
    OPPORTUNITY(BullhornEntityInfo.OPPORTUNITY, 5),
    PLACEMENT(BullhornEntityInfo.PLACEMENT, 7),
    PLACEMENT_CHANGE_REQUEST(BullhornEntityInfo.PLACEMENT_CHANGE_REQUEST, 22),
    PLACEMENT_COMMISSION(BullhornEntityInfo.PLACEMENT_COMMISSION, 18),
    SENDOUT(BullhornEntityInfo.SENDOUT, 19),
    SKILL(BullhornEntityInfo.SKILL, 0),
    SPECIALTY(BullhornEntityInfo.SPECIALTY, 0),
    STATE(BullhornEntityInfo.STATE, 0),
    TASK(BullhornEntityInfo.TASK, 20),
    TEARSHEET(BullhornEntityInfo.TEARSHEET, 21),
    TIME_UNIT(BullhornEntityInfo.TIME_UNIT, 0),
    WORKERS_COMPENSATION(BullhornEntityInfo.WORKERS_COMPENSATION, 0),
    WORKERS_COMPENSATION_RATE(BullhornEntityInfo.WORKERS_COMPENSATION_RATE, 0),

    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, 100),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, 101),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_3(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_3, 102),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_4(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_4, 103),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_5(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_5, 104),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_6(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_6, 105),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_7(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_7, 106),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_8(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_8, 107),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_9(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_9, 108),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_10(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_10, 109),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_11(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_11, 110),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_12(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_12, 111),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_13(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_13, 112),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_14(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_14, 113),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_15(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_15, 114),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_16(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_16, 115),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_17(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_17, 116),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_18(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_18, 117),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_19(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_19, 118),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_20(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_20, 119),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_21(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_21, 120),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_22(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_22, 121),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_23(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_23, 122),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_24(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_24, 123),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_25(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_25, 124),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_26(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_26, 125),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_27(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_27, 126),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_28(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_28, 127),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_29(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_29, 128),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_30(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_30, 129),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_31(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_31, 130),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_32(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_32, 131),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_33(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_33, 132),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_34(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_34, 133),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_35(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_35, 134),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_1(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_1, 135),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_2(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_2, 136),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_3(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_3, 137),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_4(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_4, 138),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_5(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_5, 139),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_6(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_6, 140),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_7(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_7, 141),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_8(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_8, 142),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_9(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_9, 143),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_10(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_10, 144),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_1(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_1, 145),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_2(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_2, 146),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_3(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_3, 147),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_4(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_4, 148),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_5(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_5, 149),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_6(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_6, 150),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_7(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_7, 151),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_8(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_8, 152),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_9(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_9, 153),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_10(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_10, 154),
    PERSON_CUSTOM_OBJECT_INSTANCE_1(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_1, 155),
    PERSON_CUSTOM_OBJECT_INSTANCE_2(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, 156),
    PERSON_CUSTOM_OBJECT_INSTANCE_3(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_3, 157),
    PERSON_CUSTOM_OBJECT_INSTANCE_4(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_4, 158),
    PERSON_CUSTOM_OBJECT_INSTANCE_5(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_5, 159),
    PERSON_CUSTOM_OBJECT_INSTANCE_6(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_6, 160),
    PERSON_CUSTOM_OBJECT_INSTANCE_7(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_7, 161),
    PERSON_CUSTOM_OBJECT_INSTANCE_8(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_8, 162),
    PERSON_CUSTOM_OBJECT_INSTANCE_9(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_9, 163),
    PERSON_CUSTOM_OBJECT_INSTANCE_10(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_10, 164),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_1(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_1, 165),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_2(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_2, 166),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_3(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_3, 167),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_4(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_4, 168),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_5(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_5, 169),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_6(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_6, 170),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_7(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_7, 171),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_8(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_8, 172),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_9(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_9, 173),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_10(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_10, 174),

    // Abstract base class
    PERSON(BullhornEntityInfo.PERSON, 1000),

    // Compound class
    ADDRESS(BullhornEntityInfo.ADDRESS, 1001),

    // File Class - Handles Setting/Getting File Meta
    FILE(BullhornEntityInfo.FILE, 1002);

    /**
     * Comparator for sorting EntityInfo objects in a sorted collection.
     */
    public static final Comparator<EntityInfo> loadOrderComparator =
        Comparator.comparingInt(EntityInfo::getLoadOrder);

    /**
     * Comparator for sorting EntityInfo objects in a sorted collection in reverse of the load.
     */
    public static final Comparator<EntityInfo> deleteOrderComparator =
        (firstEntityInfo, secondEntityInfo) -> secondEntityInfo.getLoadOrder() - firstEntityInfo.getLoadOrder();

    private final BullhornEntityInfo bullhornEntityInfo;
    private final Integer loadOrder;
    private Map<String, Method> setterMethodMap = null;

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
     * The entity class used in the Bullhorn's SDK-REST. For address, we account for the null value in REST-SDK.
     * For files, we swap out the file itself for the FileMeta object that DataLoader sets/gets.
     *
     * @return the address class for use in DataLoader, since we are not loading directly, but need it to be available
     */
    public Class getEntityClass() {
        if (bullhornEntityInfo == BullhornEntityInfo.ADDRESS) {
            return Address.class;
        } else if (bullhornEntityInfo == BullhornEntityInfo.FILE) {
            return FileMeta.class;
        }
        return bullhornEntityInfo.getType();
    }

    /**
     * The order in which this entity should be loaded relative to other entities, to avoid referencing issues.
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
     * True if this entity can be used in /search calls.
     */
    public boolean isSearchEntity() {
        return SearchEntity.class.isAssignableFrom(getEntityClass());
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
     * Returns the setter methods that exist on the SDK-REST entity class as a map of field name to method.
     *
     * Since the methods do not change, we will only compute the map once.
     *
     * @return A map of field name to setter methods that can invoked generically using `method.invoke`
     */
    public Map<String, Method> getSetterMethodMap() {
        if (setterMethodMap == null) {
            setterMethodMap = MethodUtil.getSetterMethodMap(getEntityClass());

            // Add individual address setters for any entity that has an address field, since address is a composite
            // field, not a direct field or an associated record.
            if (setterMethodMap.containsKey("address")) {
                setterMethodMap.putAll(MethodUtil.getSetterMethodMap(Address.class));
            }
        }

        return setterMethodMap;
    }
}
