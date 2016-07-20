package com.bullhorn.dataloader.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

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

    private static String timestamp = null;

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

    /**
     * Returns a timestamp that is set to the time when DataLoader was started. This allows for the same
     * timestamp to be used throughout the same session.
     *
     * @return The timestamp string
     */
    public static String getTimestamp() {
        if (timestamp == null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
            timestamp = dateFormat.format(new Date());
        }

        return timestamp;
    }

    /**
     * Convenience method which returns true if the entity can be loaded using REST.
     *
     * @return True if can be loaded by DataLoader
     */
    public static boolean isLoadable(String entityName) {
        return (isHardDeletable(entityName) || isSoftDeletable(entityName) || isNotDeletable(entityName));
    }

    /**
     * Convenience method which returns true if the entity can be deleted using REST.
     *
     * @return True if can be deleted by DataLoader
     */
    public static boolean isDeletable(String entityName) {
        return (isHardDeletable(entityName) || isSoftDeletable(entityName));
    }

    /**
     * Convenience method which returns true if an entity is part of the set of hard deletable entities in REST.
     *
     * @return True if can be hard deleted
     */
    public static boolean isHardDeletable(String entityName) {
        return Arrays.asList(HARD_DELETABLE_ENTITIES).contains(entityName);
    }

    /**
     * Convenience method which returns true if an entity is part of the set of soft deletable entities in REST.
     *
     * @return True if must be soft deleted
     */
    public static boolean isSoftDeletable(String entityName) {
        return Arrays.asList(SOFT_DELETABLE_ENTITIES).contains(entityName);
    }

    /**
     * Convenience method which returns true if an entity is part of the set of mutable but not deletable entities
     * in REST.
     *
     * @return True if can be inserted and updated but not deleted
     */
    public static boolean isNotDeletable(String entityName) {
        return Arrays.asList(NOT_DELETABLE_ENTITIES).contains(entityName);
    }

    /**
     * Convenience method which returns true if an entity is part of the set of read only entities in REST.
     *
     * @return True if read only
     */
    public static boolean isReadOnly(String entityName) {
        return Arrays.asList(READ_ONLY_ENTITIES).contains(entityName);
    }

    private StringConsts() {
    }
}
