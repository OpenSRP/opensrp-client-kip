package util;

import org.smartregister.AllConstants;
import org.smartregister.kip.BuildConfig;
import org.smartregister.kip.application.KipApplication;

/**
 * Created by coder on 2/14/17.
 */
public class KipConstants extends AllConstants {
    private static final String OPENMRS_URL = BuildConfig.OPENMRS_URL;
    public static final int DATABASE_VERSION = BuildConfig.DATABASE_VERSION;

    public static final String OPENMRS_IDGEN_URL = BuildConfig.OPENMRS_IDGEN_URL;
    public static final int OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE = BuildConfig.OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE;
    public static final int OPENMRS_UNIQUE_ID_BATCH_SIZE = BuildConfig.OPENMRS_UNIQUE_ID_BATCH_SIZE;
    public static final int OPENMRS_UNIQUE_ID_SOURCE = BuildConfig.OPENMRS_UNIQUE_ID_SOURCE;
    public static final long MAX_SERVER_TIME_DIFFERENCE = BuildConfig.MAX_SERVER_TIME_DIFFERENCE;
    public static final boolean TIME_CHECK = BuildConfig.TIME_CHECK;

    public static final String CHILD_TABLE_NAME = "ec_child";
    public static final String MOTHER_TABLE_NAME = "ec_mother";
    public static final String MOTHER_TABLE_ALIAS = "m";
    public static final String GUARDIAN_TABLE_ALIAS = "g";
    public static final String CURRENT_LOCATION_ID = "CURRENT_LOCATION_ID";

    public static final String DEFAULT_DATE_STRING = "1970-1-1";

    public static final String CHILD_ENROLLMENT = "Child Enrollment";

    public static String openmrsUrl() {
        String baseUrl = KipApplication.getInstance().context().allSharedPreferences().fetchBaseURL("");
        int lastIndex = baseUrl.lastIndexOf("/");
        baseUrl = baseUrl.substring(0, lastIndex) + "/openmrs";
        return OPENMRS_URL.isEmpty() || OPENMRS_URL == null ? baseUrl : OPENMRS_URL;
    }

    public static final class ServiceType {
        public static final int DATA_SYNCHRONIZATION = 1;
        public static final int DAILY_TALLIES_GENERATION = 2;
        public static final int MONTHLY_TALLIES_GENERATION = 3;
        public static final int PULL_UNIQUE_IDS = 4;
        public static final int VACCINE_SYNC_PROCESSING = 5;
        public static final int WEIGHT_SYNC_PROCESSING = 6;
        public static final int RECURRING_SERVICES_SYNC_PROCESSING = 7;
    }


    public static final class EventType {
        public static final String DEATH = "Death";
    }

    public static final class EntityType {
        public static final String CHILD = "child";
    }

    public static final class EC_CHILD_TABLE {
        public static final String DOD = "dod";
        public static final String GENDER = "gender";
        public static final String DUE_DATE = "due_date";
        public static final String CHW_NAME = "chw_name";
        public static final String CHW_PHONE_NUMBER = "chw_phone_number";
    }

    public static final class GENDER {

        public static final String MALE = "male";
        public static final String FEMALE = "female";
        public static final String TRANSGENDER = "transgender";
    }

    public static final class KEY {
        public static final String CHILD = "child";
        public static final String MOTHER_FIRST_NAME = "mother_first_name";
        public static final String FIRST_NAME = "first_name";
        public static final String LAST_NAME = "last_name";
        public static final String BIRTHDATE = "birthdate";
        public static final String DEATHDATE = "deathdate";
        public static final String DEATHDATE_ESTIMATED = "deathdate_estimated";
        public static final String BIRTHDATE_ESTIMATED = "birthdate_estimated";
        public static final String EPI_CARD_NUMBER = "epi_card_number";
        public static final String MOTHER_LAST_NAME = "mother_last_name";
        public static final String ZEIR_ID = "zeir_id";
        public static final String LOST_TO_FOLLOW_UP = "lost_to_follow_up";
        public static final String DOB = "dob";
        public static final String GENDER = "gender";
        public static final String MOTHER_BASE_ENTITY_ID = "mother_base_entity_id";
        public static final String INACTIVE = "inactive";
        public static final String DATE = "date";
        public static final String VACCINE = "vaccine";
        public static final String ALERT = "alert";
        public static final String WEEK = "week";
        public static final String MONTH = "month";
        public static final String DAY = "day";
        public static final String NORMAL = "normal";
        public static final String UPCOMING = "upcoming";
        public static final String URGENT = "urgent";
        public static final String EXPIRED = "expired";
        public static final String PMTCT_STATUS = "pmtct_status";
        public static final String LOCATION_NAME = "location_name";
        public static final String LAST_INTERACTED_WITH = "last_interacted_with";
        public static final String BIRTH_WEIGHT = "Birth_Weight";
        public static final String RELATIONAL_ID = "relational_id";
    }


}
