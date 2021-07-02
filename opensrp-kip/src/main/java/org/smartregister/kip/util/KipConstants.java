package org.smartregister.kip.util;

public class KipConstants {

    public static final String MOTHER_TDV_DOSES = "mother_tdv_doses";
    public static final String PROTECTED_AT_BIRTH = "protected_at_birth";
    public static final String REACTION_VACCINE = "Reaction_Vaccine";
    public static final String FORM_CONFIG_LOCATION = "json.form/json.form.config.json";
    public static final String NATIONAL_ID = "national_id";
    public static final String BHT_MID = "bht_mid";
    public static final String MOTHER_HIV_STATUS = "mother_hiv_status";
    public static final String BIRTH_REGISTRATION_NUMBER = "birth_registration_number";
    public static final String AGE = "age";
    public static final String VALUE = "value";
    public static final String NONE = "none";
    public static final String COVID_19_RISK_FACTOR = "covid19_risk_factor";
    public static final String COVID_19_VACCINE_ELIGIBILITY = "covid19_vaccine_eligibility";
    public static final String COVID_19_WAITING_LIST = "covid19_waiting_list";
    public static final String COVID_19_VACCINE_GIVEN = "covid19_vaccine_given";
    public static final String INFLUENZA_VACCINE_GIVEN = "influenza_vaccine_given";
    public static final String COVID_19_VACCINE_NEXT_DATE = "covid19_vaccine_next_date";
    public static final String AZ_OXFORD_VIALS_AMOUNT = "az_oxford_vials_amount";
    public static final String SINOPHARM_VIALS_AMOUNT = "sinopharm_vials_amount";
    public static final String SINOVAC_VIALS_AMOUNT = "sinovac_vials_amount";
    public static final String PFIZER_VIALS_AMOUNT = "pfizer_vials_amount";
    public static final String MODERNA_VIALS_AMOUNT = "moderna_vials_amount";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String JOHNSON_AND_JOHNSON_VIALS_AMOUNT = "johnson_and_johnson_vials_amount";

    public interface DateFormat {
        String HH_MM_AMPM = "h:mm a";
    }

    public interface RegisterType {
        String ANC = "anc";
        String CHILD = "child";
        String OPD = "opd";
        String ALL_CLIENTS = "all_clients";
    }

    public static final class KEY {
        public static final String MOTHER_BASE_ENTITY_ID = "mother_base_entity_id";
        public static final String DB_MOTHER_DOB = "mother_dob";
        public static final String MALAWI_ID = "mer_id";
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
        public static final String OPENMRS_ID = "OPENMRS_ID";
        public static final String LOST_TO_FOLLOW_UP = "lost_to_follow_up";
        public static final String GENDER = "gender";
        public static final String INACTIVE = "inactive";
        public static final String DATE = "date";
        public static final String VACCINE = "vaccine";
        public static final String ALERT = "alert";
        public static final String WEEK = "week";
        public static final String MONTH = "month";
        public static final String DAY = "day";
        public static final String PMTCT_STATUS = "pmtct_status";
        public static final String LOCATION_NAME = "location_name";
        public static final String LAST_INTERACTED_WITH = "last_interacted_with";
        public static final String BIRTH_WEIGHT = "Birth_Weight";
        public static final String RELATIONAL_ID = "relational_id";
        public static final String MOTHER = "mother";
        public static final String ENTITY_ID = "entity_id";
        public static final String VALUE = "value";
        public static final String STEPNAME = "stepName";
        public static final String TITLE = "title";
        public static final String ERR = "err";
        public static final String HIA_2_INDICATOR = "hia2_indicator";
        public static final String LOOK_UP = "look_up";
        public static final String NUMBER_PICKER = "number_picker";
        public static final String VISIT_NOT_DONE = "visit_not_done";
        public static final String LAST_HOME_VISIT = "last_home_visit";
        public static final String DATE_CREATED = "date_created";
        public static final String RELATIONALID = "relationalid";
        public static final String FAMILY_FIRST_NAME = "family_first_name";
        public static final String FAMILY_MIDDLE_NAME = "family_middle_name";
        public static final String FAMILY_LAST_NAME = "family_last_name";
        public static final String FAMILY_HOME_ADDRESS = "family_home_address";
        public static final String ENTITY_TYPE = "entity_type";
        public static final String CHILD_BF_HR = "early_bf_1hr";
        public static final String CHILD_PHYSICAL_CHANGE = "physically_challenged";
        public static final String BIRTH_CERT = "birth_cert";
        public static final String BIRTH_CERT_ISSUE_DATE = "birth_cert_issue_date";
        public static final String BIRTH_CERT_NUMBER = "birth_cert_num";
        public static final String BIRTH_CERT_NOTIFIICATION = "birth_notification";
        public static final String ILLNESS_DATE = "date_of_illness";
        public static final String ILLNESS_DESCRIPTION = "illness_description";
        public static final String ILLNESS_ACTION = "action_taken";
        public static final String ILLNESS_ACTION_BA = "action_taken_1m5yr";
        public static final String OTHER_ACTION = "other_treat_1m5yr";
        public static final String EVENT_DATE = "event_date";
        public static final String EVENT_TYPE = "event_type";
        public static final String INSURANCE_PROVIDER = "insurance_provider";
        public static final String INSURANCE_PROVIDER_NUMBER = "insurance_provider_number";
        public static final String INSURANCE_PROVIDER_OTHER = "insurance_provider_other";
        public static final String TYPE_OF_DISABILITY = "type_of_disability";
        public static final String RHC_CARD = "rhc_card";
        public static final String NUTRITION_STATUS = "nutrition_status";
        public static final String VACCINE_CARD = "vaccine_card";
        public static final String ID_LOWER_CASE = "_id";
        public static final String BASE_ENTITY_ID = "base_entity_id";
        public static final String DOB = "dob";//Date Of Birth
        public static final String DOD = "dod";//Date Of Birth
        public static final String UNIQUE_ID = "unique_id";
        public static final String HOME_ADDRESS = "home_address";
        public static final String VILLAGE = "village";
        public static final String TRADITIONAL_AUTHORITY = "traditional_authority";
        public static final String DATE_REMOVED = "date_removed";
        public static final String MOTHER_HIV_STATUS = "mother_hiv_status";
        public static final String DETAILS = "details";
        public static final String CHILD_HIV_STATUS = "child_hiv_status";
        public static final String CONTACT_PHONE_NUMBER = "contact_phone_number";
        public static final String CLIENT_REG_DATE = "client_reg_date";
        public static final String CHILD_TREATMENT = "child_treatment";
        public static final String OBJECT_ID = "object_id";
        public static final String OBJECT_RELATIONAL_ID = "object_relational_id";
        public static final String MOTHER_DOB = "dob";
        public static final String MOTHER_NRC_NUMBER = "nrc_number";
        public static final String MOTHER_NID = "mother_nid";
        public static final String MOTHER_GUARDIAN_NUMBER = "mother_guardian_number";
        public static final String MOTHER_SECOND_PHONE_NUMBER = "second_phone_number";
        public static final String PROTECTED_AT_BIRTH = "protected_at_birth";
        public static final String DEVICEID = "deviceid";
        public static final String VIEW_CONFIGURATION_PREFIX = "ViewConfiguration_";
        public static final String ARABIC_LOCALE = "ar";
        public static final String HOME_FACILITY = "Home_Facility";
        public static final String COUNTY = "County";
        public static final String SUB_COUNTY = "Sub_County";
        public static final String WARD = "Ward";
        public static final String KIP_ID = "zeir_id";
        public static final String CHILD_REGISTER_CARD_NUMBER = "Child_Register_Card_Number";
        public static final String MIDDLE_NAME = "middle_name";
        public static final String BIRTH_HEIGHT = "Birth_Height";
        public static final String BIRTH_TETANUS_PROTECTION = "Birth_Tetanus_Protection";
        public static final String MOTHER_MIDDLE_NAME = "middle_name";
        public static final String MOTHER_GUARDIAN_PHONE_NUMBER = "Mother_Guardian_Phone_Number";
        public static final String IS_PLACE_BIRTH = "isPlace_Birth";
        public static final String OTHER = "other";
        public static final String BIRTH_FACILITY_NAME_OTHER = "Birth_Facility_Name_Other";
        public static final String ADDRESS_3 = "address3";
        public static final String ADDRESS_5 = "address5";
        public static final String ADDRESS_2 = "address2";
        public static final String ADDRESS_1 = "address1";
        public static final String PREFERRED_LANGUAGE = "Preferred_Language";
        public static final String FIRST_HEALTH_FACILITY_CONTACT = "First_Health_Facility_Contact";
        public static final String PLACE_BIRTH = "Place_Birth";
        public static final int ON_KEY_BACK_PRESSED = 610;
        public static final String ITEM_LIST = "ITEM_LIST";
        public static final String TAPCARD_INFO = "TAPCARD_INFO";
        public static final String VOUCHER_DETAILS = "VOUCHER_DETAILS";
        public static final String FRAGMENT = "FRAGMENT";
        public static final String NEWBORN = "NEWBORN";
        public static final String PREV_PAPER = "PREV_PAPER";
        public static final int REQUEST_CODE_LAUNCH_XIP = 100;
        public static final String ERROR_DETECTED = "No NFC tag detected!";
        public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
        public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";
        public static final String TAPCARD_OPTION = "TAPCARD_OPTION";
        public static final String PREV_CARD = "PREV_CARD";
        public static final String NEW_CARD = "NEW_CARD";
        public static final String writeMessage = "to write information";
        public static final String readMessage = "to read information";
        public static final String BIRTH_FACILITY_NAME = "Birth_Facility_Name";
        public static final String RESIDENTIAL_AREA = "Residential_Area";
        public static final String MOTHER_ = "mother_";
        public static final String ENCOUNTER_TYPE = "encounter_type";
        public static final String BIRTH_REGISTRATION = "Birth Registration";
        public static final String REGISTRATION_HOME_ADDRESS = "home_address";
        public static final String IDENTIFIERS = "identifiers";
        public static final String FIRSTNAME = "firstName";
        public static final String MIDDLENAME = "middleName";
        public static final String LASTNAME = "lastName";
        public static final String ATTRIBUTES = "attributes";
        public static final int FIVE_YEAR = 5;
        public static final String OPD_REGISTRATION = "Opd Registration";
        public static final String PHOTO = "photo";
        public static String FINGERPRINT_MESSAGE = "FINGERPRINT_MESSAGE";
        public static String SITE_CHARACTERISTICS = "site_characteristics";

        public static final String FIELDS = "fields";
        public static final String KEY = "key";
        public static final String IS_VACCINE_GROUP = "is_vaccine_group";
        public static final String OPTIONS = "options";
        public static final String AGE = "age";
        public static final String FATHER_DOB = "father_dob";
        public static final String FATHER_FIRST_NAME = "father_first_name";
        public static final String FATHER_LAST_NAME = "father_last_name";
        public static final String FATHER_PHONE = "father_phone";
        public static final String FATHER_RELATIONAL_ID = "father_relational_id";
    }

    public static final class DrawerMenu {
        public static final String ALL_FAMILIES = "All Families";
        public static final String ALL_CLIENTS = "All Clients";
        public static final String ANC_CLIENTS = "ANC Clients";
        public static final String CHILD_CLIENTS = "KEPI Clients";

        public static final String HIV_CLIENTS = "Hiv Clients";
        public static final String ANC = "ANC";
        public static final String LD = "L&D";
        public static final String PNC = "PNC";
        public static final String FAMILY_PLANNING = "Family Planning";
        public static final String MALARIA = "Malaria";

        public static final String OPD_CLIENTS = "OPD Clients";
    }

    public static final class FormTitleUtil {
        public static final String UPDATE_CHILD_FORM = "Update Child Registration";
    }

    public static final class RQ_CODE {
        public static final int STORAGE_PERMISIONS = 1;
    }

    public static class CONFIGURATION {
        public static final String LOGIN = "login";
        public static final String CHILD_REGISTER = "child_register";

    }

    public static final class EventType {
        public static final String CHILD_REGISTRATION = "Birth Registration";
        public static final String UPDATE_CHILD_REGISTRATION = "Update Birth Registration";
        public static final String OUT_OF_CATCHMENT = "Out of Area Service";
        public static final String OUT_OF_CATCHMENT_SERVICE = "Out of Catchment Service";
        public static final String REPORT_CREATION = "report_creation";
        public static final String NEW_GUARDIAN_REGISTRATION = "New Guardian Registration";
        public static final String OPD_CALCULATE_RISK_FACTOR = "OPD Calculate Risk Factor";
        public static final String OPD_VACCINATION_ELIGIBILITY_CHECK = "OPD Vaccination Eligibility Check";
        public static final String OPD_COVID19_WAITING_LIST = "OPD Covid19 Vaccine Waiting List";
        public static final String OPD_COVID_19_VACCINE_ADMINISTRATION = "OPD Covid19 Vaccine Administration";
        public static final String OPD_COVID_19_VACCINE_STOCK_TAKE = "OPD Covid19 Vaccine Stock Take";
        public static final String OPD_SMS_REMINDER = "OPD SMS Reminder";
        public static final String COVID_AEFI = "Covid AEFI";
        public static final String OPD_COVID19_VACCINE_STOCK = "OPD Covid19 Vaccine Stock";
        public static final String OPD_INFLUENZA_MEDIAL_CONDITION = "OPD Influenza Medical Condition Check";
        public static final String OPD_INFLUENZA_VACCINE_ADMINISTRATION = "OPD Influenza Vaccine Administration";
        public static final String INFLUENZA_VACCINE_AEFI = "INFLUENZA VACCINE AEFI";

    }

    public static class JSON_FORM {
        public static final String CHILD_ENROLLMENT = "child_enrollment";
        public static final String OUT_OF_CATCHMENT_SERVICE = "out_of_catchment_service";
        public static final String OPD_CALCULATE_RISK_FACTOR_FORM = "opd_calculate_risk_factor";
        public static final String OPD_VACCINATION_ELIGIBILITY_CHECK_FORM = "opd_vaccination_eligibility_check";
        public static final String OPD_COVID19_WAITING_LIST_FORM = "opd_covid19_waiting_list";
        public static final String OPD_COVID19_VACCINE_ADMINISTRATION_FORM = "opd_covid19_vaccine_administration";
        public static final String OPD_COVID_19_VACCINE_STOCK_TAKE_FORM = "opd_covid19_vaccine_stock_take";
        public static final String OPD_SMS_REMINDER = "opd_sms_reminder";
        public static final String OPD_INFLUENZA_MEDICAL_CONDITION = "opd_influenza_medical_condition";
        public static final String OPD_INFLUENZA_VACCINE_ADMINISTRATION = "opd_influenza_vaccine_administration";

    }

    public static class RELATIONSHIP {
        public static final String MOTHER = "mother";
        public static final String FATHER = "father";

    }

    public static class TABLE_NAME {
        public static final String ALL_CLIENTS = "ec_client";
        public static final String REGISTER_TYPE = "client_register_type";
        public static final String CHILD = "ec_client";
        public static final String MOTHER_TABLE_NAME = "ec_mother_details";
        public static final String FAMILY = "ec_family";
        public static final String FAMILY_MEMBER = "ec_family_member";
        public static final String CHILD_ACTIVITY = "ec_child_activity";
        public static final String ANC_MEMBER = "ec_anc_register";
        public static final String ANC_MEMBER_LOG = "ec_anc_log";
        public static final String MALARIA_CONFIRMATION = "ec_malaria_confirmation";
        public static final String ANC_PREGNANCY_OUTCOME = "ec_pregnancy_outcome";
        public static final String CHILD_UPDATED_ALERTS = "child_updated_alerts";
    }

    public static final class VACCINE {
        public static final String CHILD = "child";
    }

    public static final class EntityType {
        public static final String CHILD = "child";
    }

    public static final class EC_CHILD_TABLE {
        public static final String BASE_ENTITY_ID = "baseEntityId";
        public static final String ZEIR_ID = "zeir_id";
        public static final String DOB = "dob";
        public static final String DOD = "dod";
        public static final String REG_DATE = "client_reg_date";
        public static final String INACTIVE = "inactive";
        public static final String LOST_TO_FOLLOW_UP = "lost_to_follow_up";


    }

    public static final class GENDER {
        public static final String MALE = "Male";
        public static final String FEMALE = "Female";
        public static final String TRANSGENDER = "Transgender";
    }

    public static final class GENDER_KEY {
        public static final String MALE = "1";
        public static final String FEMALE = "2";
        public static final String TRANSGENDER = "3";
    }

    public static final class ANSWER {
        public static final String YES = "Yes";
        public static final String NO = "No";
    }

    public static final class ANSWER_KEY {
        public static final String YES = "1";
        public static final String NO = "2";
    }

    public interface Columns {
        interface RegisterType {
            String BASE_ENTITY_ID = "base_entity_id";
            String REGISTER_TYPE = "register_type";
            String DATE_REMOVED = "date_removed";
            String DATE_CREATED = "date_created";
        }
    }

    public static final class CONCEPT {
        public final static String VACCINE_DATE = "1410AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    }

    public static final class JSONFORM {
        public final static String CHILD_ENROLLMENT = "child_enrollment";
        public final static String OUT_OF_CATCHMENT = "out_of_catchment_service";
    }

    public class IntentKeyUtils {
        public static final String IS_REMOTE_LOGIN = "is_remote_login";
    }

    public interface MultiResultProcessor {
        String GROUPING_SEPARATOR = "_";
    }

    public interface IntentKey {
        String REPORT_GROUPING = "report-grouping";
    }

    public interface Pref {
        String APP_VERSION_CODE = "APP_VERSION_CODE";
        String INDICATOR_DATA_INITIALISED = "INDICATOR_DATA_INITIALISED";
    }

    public interface File {
        String INDICATOR_CONFIG_FILE = "config/indicator-definitions.yml";
    }

    public static final class ServiceType {
        public static final int DAILY_TALLIES_GENERATION = 2;
        public static final int MONTHLY_TALLIES_GENERATION = 3;

    }

    public static final class KeyUtils {
        public static final String ID = "_ID";
        public static final String KEY = "key";
        public static final String VALUE = "value";
        public static final String DESCRIPTION = "description";
        public static final String TYPE = "type";
        public static final String ID_LOWER_CASE = "_id";
        public static final String STEPNAME = "stepName";
        public static final String NUMBER_PICKER = "number_picker";
        public static final String FIRST_NAME = "first_name";
        public static final String LAST_NAME = "last_name";
        public static final String BASE_ENTITY_ID = "base_entity_id";
        public static final String JSON = "json";
        public static final String DOB = "dob";//Date Of Birth
        public static final String DOB_UNKNOWN = "dob_unknown";
        public static final String EDD = "edd";
        public static final String GENDER = "gender";
        public static final String ANC_ID = "register_id";
        public static final String LAST_INTERACTED_WITH = "last_interacted_with";
        public static final String DATE_REMOVED = "date_removed";
        public static final String PHONE_NUMBER = "phone_number";
        public static final String ALT_NAME = "alt_name";
        public static final String ALT_PHONE_NUMBER = "alt_phone_number";
        public static final String HOME_ADDRESS = "home_address";
        public static final String AGE = "age";
        public static final String REMINDERS = "reminders";
        public static final String RED_FLAG_COUNT = "red_flag_count";
        public static final String YELLOW_FLAG_COUNT = "yellow_flag_count";
        public static final String CONTACT_STATUS = "contact_status";
        public static final String PREVIOUS_CONTACT_STATUS = "previous_contact_status";
        public static final String NEXT_CONTACT = "next_contact";
        public static final String NEXT_CONTACT_DATE = "next_contact_date";
        public static final String LAST_CONTACT_RECORD_DATE = "last_contact_record_date";
        public static final String RELATIONAL_ID = "relationalid";
        public static final String VISIT_START_DATE = "visit_start_date";
        public static final String IS_FIRST_VISIT = "is_first_visit";
    }

    public interface Settings {
        String VACCINE_STOCK_IDENTIFIER = "covid19_vaccine_stock";
    }

    public interface Properties {
        String CAN_SAVE_SITE_INITIAL_SETTING = "CAN_SAVE_INITIAL_SITE_SETTING";
    }

    public interface TemplateUtils {
        interface Settings {
            String TEAM_ID = "teamId";
            String TEAM = "team";
            String LOCATION_ID = "locationId";
            String PROVIDER_ID = "providerId";
        }
    }

    public static class JsonFormKeyUtils {
        public static final String ENTITY_ID = "entity_id";
    }

    public interface DbConstants {
        interface Tables {
            String CALCULATE_RISK_FACTOR_TABLE = "opd_calculate_risk_factor";
            String OPD_VACCINATION_CONDITIONS_CHECK_TABLE = "opd_vaccination_eligibility_check";
            String OPD_COVID_19_WAITING_LIST_TABLE = "opd_covid19_waiting_list";
            String OPD_COVID_19_VACCINE_RECORD_TABLE = "opd_covid19_vaccine_administration";
            String OPD_INFLUENZA_VACCINE_RECORD_TABLE = "opd_influenza_vaccine_administration";
            String OPD_SMS_REMINDER = "opd_sms_reminder";
            String OPD_COVD19_ADVERSE_REACTION = "covid_aefi";
            String OPD_MEDICAL_CHECK__FORM = "opd_medical_check";
        }

        interface Columns {
            interface CalculateRiskFactor {
                String ID = "id";
                String VISIT_ID = "visit_id";
                String VISIT_DATE = "visit_date";
                String BASE_ENTITY_ID = "base_entity_id";
                String VISIT_TYPE = "visit_type";
                String PRE_EXISTING_CONDITIONS = "pre_existing_conditions";
                String OTHER_PRE_EXISTING_CONDITIONS = "other_pre_existing_conditions";
                String OCCUPATION = "occupation";
                String OTHER_OCCUPATION = "other_occupation";
                String AGE = "age";
                String DATE = "date";
                String CREATED_AT = "created_at";
            }

            interface VaccinationEligibility {
                String ID = "id";
                String VISIT_ID = "visit_id";
                String VISIT_DATE = "visit_date";
                String BASE_ENTITY_ID = "base_entity_id";
                String VISIT_TYPE = "visit_type";
                String TEMPERATURE = "temperature";
                String COVID_19_HISTORY = "covid_19_history";
                String ORAL_CONFIRMATION = "oral_confirmation";
                String RESPIRATORY_SYMPTOMS = "respiratory_symptoms";
                String OTHER_RESPIRATORY_SYMPTOMS = "other_respiratory_symptoms";
                String ALLERGIES = "allergies";
                String OTHER_ALLERGIES = "other_allergies";
                String AGE = "age";
                String DATE = "date";
                String CREATED_AT = "created_at";
            }

            interface WaitingList {
                String ID = "id";
                String VISIT_ID = "visit_id";
                String VISIT_DATE = "visit_date";
                String BASE_ENTITY_ID = "base_entity_id";
                String VISIT_TYPE = "visit_type";
                String WAITING_LIST = "waiting_list";
                String AGE = "age";
                String DATE = "date";
                String CREATED_AT = "created_at";
            }

            interface VaccineRecord {
                String ID = "id";
                String VISIT_ID = "visit_id";
                String VISIT_DATE = "visit_date";
                String BASE_ENTITY_ID = "base_entity_id";
                String VISIT_TYPE = "visit_type";
                String COVID_19_ANTIGENS = "covid19_antigens";
                String SITE_OF_ADMINISTRATION = "site_of_administration";
                String ADMINISTRATION_DATE = "administration_date";
                String ADMINISTRATION_ROUTE = "administration_route";
                String LOT_NUMBER = "lot_number";
                String VACCINE_EXPIRY = "vaccine_expiry";
                String AGE = "age";
                String DATE = "date";
                String CREATED_AT = "created_at";
            }

            interface InfluenzaVaccineRecord {
                String ID = "id";
                String VISIT_ID = "visit_id";
                String VISIT_DATE = "visit_date";
                String BASE_ENTITY_ID = "base_entity_id";
                String VISIT_TYPE = "visit_type";
                String INFLUENZA_VACCINE = "influenza_vaccines";
                String SITE_OF_ADMINISTRATION = "influenza_site_of_vaccine_administration";
                String ADMINISTRATION_DATE = "influenza_administration_date";
                String ADMINISTRATION_ROUTE = "influenza_vaccine_administration_route";
                String LOT_NUMBER = "influenza_vaccine_lot_number";
                String VACCINE_EXPIRY = "influenza_vaccine_expiry";
                String AGE = "age";
                String DATE = "date";
                String CREATED_AT = "created_at";
            }

            interface SmsReminder {
                String ID = "id";
                String VISIT_ID = "visit_id";
                String VISIT_DATE = "visit_date";
                String BASE_ENTITY_ID = "base_entity_id";
                String VISIT_TYPE = "visit_type";
                String SMS_REMINDER = "sms_reminder";
                String DATE = "date";
                String CREATED_AT = "created_at";

            }

            interface Covid19Eafi {
                String ID = "id";
                String BASE_ENTITY_ID = "base_entity_id";
                String VISIT_TYPE = "visit_type";
                String SMS_REMINDER = "sms_reminder";
                String DATE = "date";
                String CREATED_AT = "created_at";

            }

            interface OpdMedicalCheck {
                String ID = "id";
                String VISIT_ID = "visit_id";
                String VISIT_DATE = "visit_date";
                String BASE_ENTITY_ID = "base_entity_id";
                String VISIT_TYPE = "visit_type";
                String TEMPERATURE = "influenza_temperature";
                String PRE_EXISTING_CONDITIONS = "influenza_pre_existing_conditions";
                String OTHER_PRE_EXISTING_CONDITIONS = "other_influenza_pre_existing_conditions";
                String ALLERGIES = "influenza_allergies";
                String OTHER_ALLERGIES = "other_influenza_allergies";
                String AGE = "age";
                String DATE = "date";
                String CREATED_AT = "created_at";
            }
        }
    }

    public interface ReportKeys {
        String REPORT_JSON = "reportJson";
        String REPORT_DATE = "reportDate";
        String GROUPING = "grouping";
        String PROVIDER_ID = "providerId";
        String DATE_CREATED = "dateCreated";
        String HIA2_INDICATORS = "hia2Indicators";
        String VALUE = "value";
        String INDICATOR_CODE = "indicatorCode";
    }

    public static String TXT_SMS_REMINDER = "You have a scheduled appointment for covid 19 vaccine on ";

    public static String KEPI_SMS_REMINDER = " is due for immunization on 25/05/2021. You are therefore, asked to visit ";
}
