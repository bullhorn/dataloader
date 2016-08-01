package com.bullhorn.dataloader.meta;

import com.bullhornsdk.data.model.enums.BullhornEntityInfo;

/**
 * The list of all entities in SDK-REST supported by DataLoader.
 */
public enum Entity {
	
	APPOINTMENT(true, BullhornEntityInfo.APPOINTMENT),
	APPOINTMENT_ATTENDEE(true, BullhornEntityInfo.APPOINTMENT_ATTENDEE),
	BUSINESS_SECTOR(false, BullhornEntityInfo.BUSINESS_SECTOR),
	CANDIDATE(true, BullhornEntityInfo.CANDIDATE),
	CANDIDATE_EDUCATION(true, BullhornEntityInfo.CANDIDATE_EDUCATION),
	CANDIDATE_REFERENCE(true, BullhornEntityInfo.CANDIDATE_REFERENCE),
	CANDIDATE_WORK_HISTORY(true, BullhornEntityInfo.CANDIDATE_WORK_HISTORY),
	CATEGORY(false, BullhornEntityInfo.CATEGORY),
	CERTIFICATION(false, BullhornEntityInfo.CERTIFICATION),
	CLIENT_CONTACT(true, BullhornEntityInfo.CLIENT_CONTACT),
	CLIENT_CORPORATION(true, BullhornEntityInfo.CLIENT_CORPORATION),
	CORPORATION_DEPARTMENT(false, BullhornEntityInfo.CORPORATION_DEPARTMENT),
	CORPORATE_USER(false, BullhornEntityInfo.CORPORATE_USER),
	COUNTRY(false, BullhornEntityInfo.COUNTRY),
	HOUSING_COMPLEX(true, BullhornEntityInfo.HOUSING_COMPLEX),
	JOB_ORDER(true, BullhornEntityInfo.JOB_ORDER),
	JOB_SUBMISSION(true, BullhornEntityInfo.JOB_SUBMISSION),
	JOB_SUBMISSION_HISTORY(false, BullhornEntityInfo.JOB_SUBMISSION_HISTORY),
	LEAD(true, BullhornEntityInfo.LEAD),
	NOTE(true, BullhornEntityInfo.NOTE),
	NOTE_ENTITY(true, BullhornEntityInfo.NOTE_ENTITY),
	OPPORTUNITY(true, BullhornEntityInfo.OPPORTUNITY),
	PLACEMENT(true, BullhornEntityInfo.PLACEMENT),
	PLACEMENT_CHANGEREQUEST(false, BullhornEntityInfo.PLACEMENT_CHANGE_REQUEST),
	PLACEMENT_COMMISION(true, BullhornEntityInfo.PLACEMENT_COMMISSION),
	SENDOUT(true, BullhornEntityInfo.SENDOUT),
	SKILL(false, BullhornEntityInfo.SKILL),
	SPECIALTY(false, BullhornEntityInfo.SPECIALTY),
	STATE(false, BullhornEntityInfo.STATE),
	TASK(true, BullhornEntityInfo.TASK),
	TEARSHEET(true, BullhornEntityInfo.TEARSHEET),
	TIME_UNIT(false, BullhornEntityInfo.TIME_UNIT);
	
	private boolean modifiable;
	private BullhornEntityInfo entityInfo;
	private String upperCase;
	
	private Entity(boolean modifiable, BullhornEntityInfo entityInfo) {
		this.modifiable = modifiable;
		this.entityInfo = entityInfo;
		this.upperCase = entityInfo.getName().toUpperCase();
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
	 * 
	 * @return the all upper case version of this entity's name
	 */
	public String getUpperCase() {
		return upperCase;
	}
}
