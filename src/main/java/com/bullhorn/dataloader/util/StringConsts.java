package com.bullhorn.dataloader.util;

/**
 * Global String Constants
 * <p>
 * Only strings that are used in more than one class should go here. If a string constant is only used internal to a
 * class, that class should contain the string constant.
 */
public class StringConsts {
    public static final String PROPERTYFILE_ARG = "propertyfile";
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

    private StringConsts() {
    }
}
