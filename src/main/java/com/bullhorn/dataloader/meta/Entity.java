package com.bullhorn.dataloader.meta;

import java.util.Comparator;

import com.bullhornsdk.data.model.enums.BullhornEntityInfo;

/**
 * The list of all entities in SDK-REST supported by DataLoader.
 */
public enum Entity {

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
	CORPORATION_DEPARTMENT(false, BullhornEntityInfo.CORPORATION_DEPARTMENT, 0),
	CORPORATE_USER(false, BullhornEntityInfo.CORPORATE_USER, 0),
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
	TIME_UNIT(false, BullhornEntityInfo.TIME_UNIT, 0);

	private boolean modifiable;
	private BullhornEntityInfo entityInfo;
	private String upperCase;
	private Integer loadOrder;

	private Entity(boolean modifiable, BullhornEntityInfo entityInfo, Integer loadOrder) {
		this.modifiable = modifiable;
		this.entityInfo = entityInfo;
		this.upperCase = entityInfo.getName().toUpperCase();
		this.loadOrder = loadOrder;
	}

	/**
	 * @return true if this entity is modifiable by DataLoaser
	 */
	public boolean isModifiable() {
		return this.modifiable;
	}

	/**
	 * @return the entity name usable in the Bullhorn's SDK-REST
	 */
	public String getEntityName() {
		return entityInfo.getName();
	}

	/**
	 * @return the entity name usable in the Bullhorn's SDK-REST
	 */
	public Integer getLoadOrder() {
		return this.loadOrder;
	}

	/**
	 *
	 * @return the all upper case version of this entity's name
	 */
	public String getUpperCase() {
		return upperCase;
	}

    /**
     * Comparator for sorting Entity objects in a sorted collection.
     */
    final static public Comparator<Entity> loadOrderComparator = new Comparator<Entity>() {
        @Override public int compare(Entity firstEntity, Entity secondEntity) {
            return firstEntity.getLoadOrder() - secondEntity.getLoadOrder();
        }
    };
}
