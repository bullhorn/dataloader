package com.bullhorn.dataloader.meta;

import com.bullhornsdk.data.model.enums.BullhornEntityInfo;

import java.util.Comparator;

/**
 * The list of all entities in SDK-REST supported by DataLoader.
 */
public enum EntityInfo {

    APPOINTMENT(true, BullhornEntityInfo.APPOINTMENT, 8),
    APPOINTMENT_ATTENDEE(true, BullhornEntityInfo.APPOINTMENT_ATTENDEE, 9),
    BUSINESS_SECTOR(false, BullhornEntityInfo.BUSINESS_SECTOR, 0),
    CANDIDATE(true, BullhornEntityInfo.CANDIDATE, 4),
    CANDIDATE_EDUCATION(true, BullhornEntityInfo.CANDIDATE_EDUCATION, 10),
    CANDIDATE_REFERENCE(true, BullhornEntityInfo.CANDIDATE_REFERENCE, 11),
    CANDIDATE_WORK_HISTORY(true, BullhornEntityInfo.CANDIDATE_WORK_HISTORY, 12),
    CATEGORY(false, BullhornEntityInfo.CATEGORY, 0),
    CERTIFICATION(false, BullhornEntityInfo.CERTIFICATION, 0),
    CLIENT_CONTACT(true, BullhornEntityInfo.CLIENT_CONTACT, 2),
    CLIENT_CORPORATION(true, BullhornEntityInfo.CLIENT_CORPORATION, 1),
    CORPORATE_USER(false, BullhornEntityInfo.CORPORATE_USER, 0),
    CORPORATION_DEPARTMENT(false, BullhornEntityInfo.CORPORATION_DEPARTMENT, 0),
    COUNTRY(false, BullhornEntityInfo.COUNTRY, 0),
    HOUSING_COMPLEX(true, BullhornEntityInfo.HOUSING_COMPLEX, 13),
    JOB_ORDER(true, BullhornEntityInfo.JOB_ORDER, 6),
    JOB_SUBMISSION(true, BullhornEntityInfo.JOB_SUBMISSION, 16),
    JOB_SUBMISSION_HISTORY(false, BullhornEntityInfo.JOB_SUBMISSION_HISTORY, 0),
    LEAD(true, BullhornEntityInfo.LEAD, 3),
    NOTE(true, BullhornEntityInfo.NOTE, 14),
    NOTE_ENTITY(true, BullhornEntityInfo.NOTE_ENTITY, 15),
    OPPORTUNITY(true, BullhornEntityInfo.OPPORTUNITY, 5),
    PLACEMENT(true, BullhornEntityInfo.PLACEMENT, 7),
    PLACEMENT_CHANGE_REQUEST(false, BullhornEntityInfo.PLACEMENT_CHANGE_REQUEST, 0),
    PLACEMENT_COMMISSION(true, BullhornEntityInfo.PLACEMENT_COMMISSION, 17),
    SENDOUT(true, BullhornEntityInfo.SENDOUT, 18),
    SKILL(false, BullhornEntityInfo.SKILL, 0),
    SPECIALTY(false, BullhornEntityInfo.SPECIALTY, 0),
    STATE(false, BullhornEntityInfo.STATE, 0),
    TASK(true, BullhornEntityInfo.TASK, 19),
    TEARSHEET(true, BullhornEntityInfo.TEARSHEET, 20),
    TIME_UNIT(false, BullhornEntityInfo.TIME_UNIT, 0),

    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1(true, BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1,51),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2(true, BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2,52),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_3(true, BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_3,53),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_4(true, BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_4,54),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_5(true, BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_5,55),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_6(true, BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_6,56),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_7(true, BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_7,57),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_8(true, BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_8,58),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_9(true, BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_9,59),
    CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_10(true, BullhornEntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_10,60),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_1(true, BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_1,61),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_2(true, BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_2,62),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_3(true, BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_3,63),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_4(true, BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_4,64),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_5(true, BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_5,65),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_6(true, BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_6,66),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_7(true, BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_7,67),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_8(true, BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_8,68),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_9(true, BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_9,69),
    JOB_ORDER_CUSTOM_OBJECT_INSTANCE_10(true, BullhornEntityInfo.JOB_ORDER_CUSTOM_OBJECT_INSTANCE_10,70),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_1(true, BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_1,71),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_2(true, BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_2,72),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_3(true, BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_3,73),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_4(true, BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_4,74),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_5(true, BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_5,75),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_6(true, BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_6,76),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_7(true, BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_7,77),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_8(true, BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_8,78),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_9(true, BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_9,79),
    OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_10(true, BullhornEntityInfo.OPPORTUNITY_CUSTOM_OBJECT_INSTANCE_10,80),
    PERSON_CUSTOM_OBJECT_INSTANCE_1(true, BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_1,81),
    PERSON_CUSTOM_OBJECT_INSTANCE_2(true, BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2,82),
    PERSON_CUSTOM_OBJECT_INSTANCE_3(true, BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_3,83),
    PERSON_CUSTOM_OBJECT_INSTANCE_4(true, BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_4,84),
    PERSON_CUSTOM_OBJECT_INSTANCE_5(true, BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_5,85),
    PERSON_CUSTOM_OBJECT_INSTANCE_6(true, BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_6,86),
    PERSON_CUSTOM_OBJECT_INSTANCE_7(true, BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_7,87),
    PERSON_CUSTOM_OBJECT_INSTANCE_8(true, BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_8,88),
    PERSON_CUSTOM_OBJECT_INSTANCE_9(true, BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_9,89),
    PERSON_CUSTOM_OBJECT_INSTANCE_10(true, BullhornEntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_10,90),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_1(true, BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_1,91),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_2(true, BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_2,92),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_3(true, BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_3,93),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_4(true, BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_4,94),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_5(true, BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_5,95),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_6(true, BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_6,96),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_7(true, BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_7,97),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_8(true, BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_8,98),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_9(true, BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_9,99),
    PLACEMENT_CUSTOM_OBJECT_INSTANCE_10(true, BullhornEntityInfo.PLACEMENT_CUSTOM_OBJECT_INSTANCE_10,100);

    /**
     * Comparator for sorting EntityInfo objects in a sorted collection.
     */
    final static public Comparator<EntityInfo> loadOrderComparator = new Comparator<EntityInfo>() {
        @Override
        public int compare(EntityInfo firstEntityInfo, EntityInfo secondEntityInfo) {
            return firstEntityInfo.getLoadOrder() - secondEntityInfo.getLoadOrder();
        }
    };
    /**
     * Comparator for sorting EntityInfo objects in a sorted collection in reverse of the load.
     */
    final static public Comparator<EntityInfo> deleteOrderComparator = new Comparator<EntityInfo>() {
        @Override
        public int compare(EntityInfo firstEntityInfo, EntityInfo secondEntityInfo) {
            return secondEntityInfo.getLoadOrder() - firstEntityInfo.getLoadOrder();
        }
    };
    private boolean modifiable;
    private BullhornEntityInfo bullhornEntityInfo;
    private String upperCase;
    private Integer loadOrder;

    private EntityInfo(boolean modifiable, BullhornEntityInfo bullhornEntityInfo, Integer loadOrder) {
        this.modifiable = modifiable;
        this.bullhornEntityInfo = bullhornEntityInfo;
        this.upperCase = bullhornEntityInfo.getName().toUpperCase();
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
     * @return true if this entity is modifiable by DataLoader
     */
    public boolean isModifiable() {
        return this.modifiable;
    }

    /**
     * @return the entity name usable in the Bullhorn's SDK-REST
     */
    public String getEntityName() {
        return bullhornEntityInfo.getName();
    }

    /**
     * @return the order in which this entity should be loaded relative to other entities, to avoid
     * referencing issues.
     */
    public Integer getLoadOrder() {
        return this.loadOrder;
    }

    /**
     * @return the all upper case version of this entity's name
     */
    public String getUpperCase() {
        return upperCase;
    }

    /**
     * @return the bullhornEntityInfo enum
     */
    public BullhornEntityInfo getBullhornEntityInfo() {
        return this.bullhornEntityInfo;
    }
}
