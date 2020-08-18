package org.smartregister.kip.util;

public interface Constants {

    String DBNAME = "drishti.db";
    String COPYDBNAME = "reveal";

    interface SyncInfo {
        String SYNCED_EVENTS = "syncedEvents";
        String SYNCED_CLIENTS = "syncedClients";
        String UNSYNCED_EVENTS = "unsyncedEvents";
        String UNSYNCED_CLIENTS = "unsyncedClients";
        String VALID_EVENTS = "validEvents";
        String INVALID_EVENTS = "invalidEvents";
        String VALID_CLIENTS = "validClients";
        String INVALID_CLIENTS = "INValidClients";
        String TASK_UNPROCESSED_EVENTS = "taskUnprocessedEvents";
        String NULL_EVENT_SYNC_STATUS = "nullEventSyncStatus";
    }

    interface DatabaseKeys {

        String TASK_TABLE = "task";

        String SPRAYED_STRUCTURES = "sprayed_structures";

        String STRUCTURES_TABLE = "structure";

        String STRUCTURE_NAME = "structure_name";

        String STRUCTURE_ID = "structure_id";

        String ID = "_id";

        String CODE = "code";

        String FOR = "for";

        String BUSINESS_STATUS = "business_status";

        String STATUS = "status";

        String REFERENCE_REASON = "reason_reference";

        String FAMILY_NAME = "family_head_name";

        String SPRAY_STATUS = "spray_status";

        String LATITUDE = "latitude";

        String LONGITUDE = "longitude";

        String NAME = "name";

        String GROUPID = "group_id";

        String PLAN_ID = "plan_id";

        String NOT_SRAYED_REASON = "not_sprayed_reason";

        String NOT_SRAYED_OTHER_REASON = "not_sprayed_other_reason";

        String OTHER = "other";

        String COMPLETED_TASK_COUNT = "completed_task_count";

        String TASK_COUNT = "task_count";

        String BASE_ENTITY_ID = "base_entity_id";

        String FIRST_NAME = "first_name";

        String LAST_NAME = "last_name";

        String GROUPED_STRUCTURE_TASK_CODE_AND_STATUS = "grouped_structure_task_code_and_status";

        String GROUPED_TASKS = "grouped_tasks";

        String LAST_UPDATED_DATE = "last_updated_date";

        String PAOT_STATUS = "paot_status";

        String PAOT_COMMENTS = "paot_comments";

        String EVENT_TASK_TABLE = "event_task";

        String EVENT_ID = "event_id";

        String TASK_ID = "task_id";

        String EVENT_DATE = "event_date";

        String EVENTS_PER_TASK = "events_per_task";

        String TRUE_STRUCTURE = "true_structure";

        String ELIGIBLE_STRUCTURE = "eligible_structure";

        String REPORT_SPRAY = "report_spray";

        String CHALK_SPRAY = "chalk_spray";

        String STICKER_SPRAY = "sticker_spray";

        String CARD_SPRAY = "card_spray";

        String SYNC_STATUS = "syncStatus";

        String VALIDATION_STATUS = "validationStatus";

        String AUTHORED_ON = "authored_on";

        String OWNER = "owner";

        String PROPERTY_TYPE = "property_type";

        String PARENT_ID = "parent_id";

        String FORM_SUBMISSION_ID = "formSubmissionId";

        String EVENT_TYPE_FIELD = "eventType";

        String CASE_CONFIRMATION_FIELD = "case_confirmation";

        String EVENT_TABLE = "event";

        String PERSON_TESTED = "person_tested";

    }
}
