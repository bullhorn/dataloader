package com.bullhorn.dataloader.meta;

/**
 * The list of all entities supported by DataLoader
 * 
 * @author jlrutledge
 *
 */
public enum Entity {
	
	APPOINTMENT(true, "Appointment"),
	APPOINTMENT_ATTENDEE(true, "AppointmentAttendee"),
	BUSINESS_SECTOR(false, "BusinessSector"),
	CANDIDATE(true, "Candidate"),
	CANDIDATE_CERTIFICATION(true, "CandidateCertification"),
	CANDIDATE_EDUCATION(true, "CandidateEducation"),
	CANDIDATE_REFERENCE(true, "CandidateReference"),
	CANDIDATE_SOURCE(true, "CandidateSource"),
	CANDIDATE_WORK_HISTORY(true, "CandidateWorkHistory"),
	CATEGORY(false, "Category"),
	CERTIFICATION(true, "Certification"),
	CLIENT_CONTACT(true, "ClientContact"),
	CLIENT_CORPORATION(true, "ClientCorporation"),
	CORPORATION_DEPARTMENT(false, "CorporationDepartment"),
	CORPORATE_USER(false, "CorporateUser"),
	COUNTRY(false, "Country"),
	CUSTOM_ACTION(false, "CustomAction"),
	HOUSING_COMPLEX(true, "HousingComplex"),
	JOB_BOARD_POST(false, "JobBoardPost"),
	JOB_ORDER(true, "JobOrder"),
	JOB_SUBMISSION(true, "JobSubmission"),
	JOB_SUBMISSION_HISTORY(false, "JobSubmissionHistory"),
	LEAD(true, "Lead"),
	LEAD_HISTORY(false, "LeadHistory"),
	NOTE(true, "Note"),
	NOTE_ENTITY(true, "NoteEntity"),
	OPPORTUNITY(true, "Opportunity"),
	OPPORTUNITY_HISTORY(false, "OpportunityHistory"),
	PLACEMENT(true, "Placement"),
	PLACEMENT_CHANGEREQUEST(false, "PlacementChangeRequest"),
	PLACEMENT_COMMISION(true, "PlacementCommission"),
	SENDOUT(true, "Sendout"),
	SKILL(false, "Skill"),
	SPECIALTY(false, "Specialty"),
	STATE(false, "State"),
	TASK(true, "Task"),
	TEARSHEET(true, "Tearsheet"),
	TEARSHEET_RECIPIENT(true, "TearsheetRecipient"),
	TIME_UNIT(false, "TimeUnit");
	
	private boolean modifiable;
	private String entityName;
	private String upperCase;
	
	private Entity(boolean modifiable, String entityName) {
		this.modifiable = modifiable;
		this.entityName = entityName;
		this.upperCase = entityName.toUpperCase();
	}
	
	/**
	 * @return true if this entity is modifiable by DataLoaser
	 */
	public boolean isModifiable() {
		return this.modifiable;
	}
	
	/**
	 * @return the entity name usable in the Bullhorn's sdk-rest
	 */
	public String getEntityName() {
		return entityName;
	}
	
	/**
	 * 
	 * @return the all uppercase version of this entity's name
	 */
	public String getUpperCase() {
		return upperCase;
	}
}
