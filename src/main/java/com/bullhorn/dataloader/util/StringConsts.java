package com.bullhorn.dataloader.util;

/**
 * Global String Constants
 * <p>
 * Only strings that are used in more than one class should go here. If a string constant is only used internal to a
 * class, that class should contain the string constant.
 */
public class StringConsts {
    public static final String UTF = "UTF-8";
    public static final String APPLICATION_JSON = "application/json";
    public static final String BH_REST_TOKEN = "BhRestToken";
    public static final String END_BH_REST_TOKEN = "?BhRestToken=";
    public static final String AND_BH_REST_TOKEN = "&BhRestToken=";
    public static final String ENTITY_SLASH = "entity/";
    public static final String CHANGED_ENTITY_ID = "changedEntityId";
    public static final String ID = "id";
    public static final String PROPERTYFILE_ARG = "propertyfile";
    public static final String COUNT = "count";
    public static final String NAME = "name";
    public static final String DATA = "data";
    public static final String IS_DELETED = "isDeleted";
    public static final String SEARCH = "search/";
    public static final String QUERY = "query/";
    public static final String CANDIDATE = "candidate";
    public static final String PRIVATE_LABELS = "privateLabels";
    public static final String CATEGORY = "Category";
    public static final String STRING = "String";
    public static final String TIMESTAMP = DateUtil.getTimestamp();

    public static final String[] HARD_DELETABLE_ENTITIES = {
            "AppointmentAttendee",
            "CandidateCertification",
            "CandidateSource",
            "NoteEntity",
            "Placement",
            "PlacementCommission",
            "Sendout",
            "TearsheetRecipient"
    };

    public static final String[] SOFT_DELETABLE_ENTITIES = {
            "Appointment",
            "Candidate",
            "CandidateEducation",
            "CandidateReference",
            "CandidateWorkHistory",
            "ClientContact",
            "HousingComplex",
            "JobOrder",
            "JobSubmission",
            "Lead",
            "Note",
            "Opportunity",
            "Task",
            "Tearsheet"
    };

    public static final String[] NOT_DELETABLE_ENTITIES = {
            "ClientCorporation"
    };

    public static final String[] READ_ONLY_ENTITIES = {
            "BusinessSector",
            "Category",
            "Certification",
            "CorporationDepartment",
            "CorporateUser",
            "Country",
            "CustomAction",
            "JobBoardPost",
            "JobSubmissionHistory",
            "LeadHistory",
            "OpportunityHistory",
            "PlacementChangeRequest",
            "Skill",
            "Specialty",
            "State",
            "TimeUnit"
    };

    private StringConsts() {}
}
