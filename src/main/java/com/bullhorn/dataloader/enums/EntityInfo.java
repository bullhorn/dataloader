package com.bullhorn.dataloader.enums;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;

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

/**
 * The list of all entities in SDK-REST supported by DataLoader.
 *
 * Load order must be unique. If entity B references entity A, then entity A should have a lower load order number.
 */
public enum EntityInfo {

    // Entities that are referenced by other entities, but do not reference
    GENERAL_LEDGER_ACCOUNT(BullhornEntityInfo.GENERAL_LEDGER_ACCOUNT, 10),
    GENERAL_LEDGER_SEGMENT_1(BullhornEntityInfo.GENERAL_LEDGER_SEGMENT_1, 20),
    GENERAL_LEDGER_SEGMENT_2(BullhornEntityInfo.GENERAL_LEDGER_SEGMENT_2, 30),
    GENERAL_LEDGER_SEGMENT_3(BullhornEntityInfo.GENERAL_LEDGER_SEGMENT_3, 40),
    GENERAL_LEDGER_SEGMENT_4(BullhornEntityInfo.GENERAL_LEDGER_SEGMENT_4, 50),
    GENERAL_LEDGER_SEGMENT_5(BullhornEntityInfo.GENERAL_LEDGER_SEGMENT_5, 60),
    GENERAL_LEDGER_SERVICE_CODE(BullhornEntityInfo.GENERAL_LEDGER_SERVICE_CODE, 70),
    INVOICE_STATEMENT_MESSAGE_TEMPLATE(BullhornEntityInfo.INVOICE_STATEMENT_MESSAGE_TEMPLATE, 80),

    // Entities that reference other entities, in load order
    CLIENT_CORPORATION(BullhornEntityInfo.CLIENT_CORPORATION, 100),
    CLIENT_CONTACT(BullhornEntityInfo.CLIENT_CONTACT, 110),
    LEAD(BullhornEntityInfo.LEAD, 120),
    CANDIDATE(BullhornEntityInfo.CANDIDATE, 130),
    LOCATION(BullhornEntityInfo.LOCATION, 140),
    OPPORTUNITY(BullhornEntityInfo.OPPORTUNITY, 150),
    JOB_ORDER(BullhornEntityInfo.JOB_ORDER, 160),
    PLACEMENT(BullhornEntityInfo.PLACEMENT, 170),
    APPOINTMENT(BullhornEntityInfo.APPOINTMENT, 180),
    APPOINTMENT_ATTENDEE(BullhornEntityInfo.APPOINTMENT_ATTENDEE, 190),
    CANDIDATE_CERTIFICATION(BullhornEntityInfo.CANDIDATE_CERTIFICATION, 200),
    CANDIDATE_EDUCATION(BullhornEntityInfo.CANDIDATE_EDUCATION, 210),
    CANDIDATE_REFERENCE(BullhornEntityInfo.CANDIDATE_REFERENCE, 220),
    CANDIDATE_WORK_HISTORY(BullhornEntityInfo.CANDIDATE_WORK_HISTORY, 230),
    HOUSING_COMPLEX(BullhornEntityInfo.HOUSING_COMPLEX, 240),
    NOTE(BullhornEntityInfo.NOTE, 250),
    NOTE_ENTITY(BullhornEntityInfo.NOTE_ENTITY, 260),
    JOB_SUBMISSION(BullhornEntityInfo.JOB_SUBMISSION, 270),
    PLACEMENT_COMMISSION(BullhornEntityInfo.PLACEMENT_COMMISSION, 280),
    SENDOUT(BullhornEntityInfo.SENDOUT, 290),
    TASK(BullhornEntityInfo.TASK, 300),
    TEARSHEET(BullhornEntityInfo.TEARSHEET, 310),
    PLACEMENT_CHANGE_REQUEST(BullhornEntityInfo.PLACEMENT_CHANGE_REQUEST, 320),
    INVOICE_TERM(BullhornEntityInfo.INVOICE_TERM, 330),
    BILLING_PROFILE(BullhornEntityInfo.BILLING_PROFILE, 340),

    // Custom Objects
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, 1000),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, 1001),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_3(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_3, 1002),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_4(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_4, 1003),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_5(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_5, 1004),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_6(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_6, 1005),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_7(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_7, 1006),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_8(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_8, 1007),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_9(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_9, 1008),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_10(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_10, 1009),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_11(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_11, 1010),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_12(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_12, 1011),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_13(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_13, 1012),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_14(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_14, 1013),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_15(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_15, 1014),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_16(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_16, 1015),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_17(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_17, 1016),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_18(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_18, 1017),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_19(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_19, 1018),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_20(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_20, 1019),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_21(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_21, 1020),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_22(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_22, 1021),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_23(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_23, 1022),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_24(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_24, 1023),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_25(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_25, 1024),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_26(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_26, 1025),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_27(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_27, 1026),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_28(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_28, 1027),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_29(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_29, 1028),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_30(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_30, 1029),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_31(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_31, 1030),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_32(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_32, 1031),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_33(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_33, 1032),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_34(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_34, 1033),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_35(BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_35, 1034),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_1(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_1, 1035),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_2(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_2, 1036),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_3(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_3, 1037),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_4(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_4, 1038),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_5(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_5, 1039),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_6(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_6, 1040),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_7(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_7, 1041),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_8(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_8, 1042),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_9(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_9, 1043),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_10(BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_10, 1044),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_1(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_1, 1045),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_2(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_2, 1046),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_3(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_3, 1047),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_4(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_4, 1048),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_5(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_5, 1049),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_6(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_6, 1050),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_7(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_7, 1051),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_8(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_8, 1052),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_9(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_9, 1053),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_10(BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_10, 1054),
    PERSON_CUSTOM_OBJECT_INSTANCE_1(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_1, 1055),
    PERSON_CUSTOM_OBJECT_INSTANCE_2(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, 1056),
    PERSON_CUSTOM_OBJECT_INSTANCE_3(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_3, 1057),
    PERSON_CUSTOM_OBJECT_INSTANCE_4(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_4, 1058),
    PERSON_CUSTOM_OBJECT_INSTANCE_5(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_5, 1059),
    PERSON_CUSTOM_OBJECT_INSTANCE_6(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_6, 1060),
    PERSON_CUSTOM_OBJECT_INSTANCE_7(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_7, 1061),
    PERSON_CUSTOM_OBJECT_INSTANCE_8(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_8, 1062),
    PERSON_CUSTOM_OBJECT_INSTANCE_9(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_9, 1063),
    PERSON_CUSTOM_OBJECT_INSTANCE_10(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_10, 1064),
    PERSON_CUSTOM_OBJECT_INSTANCE_11(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_11, 1065),
    PERSON_CUSTOM_OBJECT_INSTANCE_12(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_12, 1066),
    PERSON_CUSTOM_OBJECT_INSTANCE_13(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_13, 1067),
    PERSON_CUSTOM_OBJECT_INSTANCE_14(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_14, 1068),
    PERSON_CUSTOM_OBJECT_INSTANCE_15(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_15, 1069),
    PERSON_CUSTOM_OBJECT_INSTANCE_16(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_16, 1070),
    PERSON_CUSTOM_OBJECT_INSTANCE_17(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_17, 1071),
    PERSON_CUSTOM_OBJECT_INSTANCE_18(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_18, 1072),
    PERSON_CUSTOM_OBJECT_INSTANCE_19(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_19, 1073),
    PERSON_CUSTOM_OBJECT_INSTANCE_20(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_20, 1074),
    PERSON_CUSTOM_OBJECT_INSTANCE_21(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_21, 1075),
    PERSON_CUSTOM_OBJECT_INSTANCE_22(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_22, 1076),
    PERSON_CUSTOM_OBJECT_INSTANCE_23(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_23, 1077),
    PERSON_CUSTOM_OBJECT_INSTANCE_24(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_24, 1078),
    PERSON_CUSTOM_OBJECT_INSTANCE_25(BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_25, 1079),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_1(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_1, 1080),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_2(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_2, 1081),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_3(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_3, 1082),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_4(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_4, 1083),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_5(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_5, 1084),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_6(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_6, 1085),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_7(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_7, 1086),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_8(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_8, 1087),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_9(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_9, 1088),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_10(BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_10, 1089),

    // Entities that can be referenced, but not loaded
    ADDRESS(BullhornEntityInfo.ADDRESS, 2000),
    BUSINESS_SECTOR(BullhornEntityInfo.BUSINESS_SECTOR, 2001),
    CATEGORY(BullhornEntityInfo.CATEGORY, 2002),
    CERTIFICATION(BullhornEntityInfo.CERTIFICATION, 2003),
    CORPORATE_USER(BullhornEntityInfo.CORPORATE_USER, 2004),
    CORPORATION_DEPARTMENT(BullhornEntityInfo.CORPORATION_DEPARTMENT, 2005),
    COUNTRY(BullhornEntityInfo.COUNTRY, 2006),
    CURRENCY_UNIT(BullhornEntityInfo.CURRENCY_UNIT, 2007),
    FILE(BullhornEntityInfo.FILE, 2008),
    GENERAL_LEDGER_SEGMENT_TYPE(BullhornEntityInfo.GENERAL_LEDGER_SEGMENT_TYPE, 2009),
    INVOICE_STATEMENT_TEMPLATE(BullhornEntityInfo.INVOICE_STATEMENT_TEMPLATE, 2010),
    JOB_SUBMISSION_HISTORY(BullhornEntityInfo.JOB_SUBMISSION_HISTORY, 2011),
    PERSON(BullhornEntityInfo.PERSON, 2012),
    SKILL(BullhornEntityInfo.SKILL, 2013),
    SPECIALTY(BullhornEntityInfo.SPECIALTY, 2014),
    STATE(BullhornEntityInfo.STATE, 2015),
    TIME_UNIT(BullhornEntityInfo.TIME_UNIT, 2016),
    WORKERS_COMPENSATION(BullhornEntityInfo.WORKERS_COMPENSATION, 2017),
    WORKERS_COMPENSATION_RATE(BullhornEntityInfo.WORKERS_COMPENSATION_RATE, 2018),
    ;

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
    @SuppressWarnings("rawtypes")
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
    private Integer getLoadOrder() {
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
