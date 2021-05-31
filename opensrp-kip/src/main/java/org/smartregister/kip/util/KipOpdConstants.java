package org.smartregister.kip.util;

import org.smartregister.opd.utils.OpdConstants;

public class KipOpdConstants extends OpdConstants {
    public interface KipForms {
        String OPD_CALCULATE_RISK_FACTOR_FORM = "opd_calculate_risk_factor";
        String OPD_ADVERSE_EVENT = "opd_adverse_event";
        String OPD_SMS_REMINDER = "opd_sms_reminder";
        String OPD_INFLUENZA_ADVERSE_EVENT = "opd_influenza_adverse_event";
    }

    public interface RegisterType {
        String ANC = "anc";
        String CHILD = "child";
        String OPD = "opd";
        String ALL_CLIENTS = "all_clients";
        String MATERNITY = "maternity";
        String PNC = "pnc";
    }

    public interface FactKey {

        String VISIT_TO_APPOINTMENT_DATE = "visit_to_appointment_date";

        interface ProfileOverview {
            String PREGNANCY_STATUS = "pregnancy_status";
            String IS_PREVIOUSLY_TESTED_HIV = "is_previously_tested_hiv";
            String PREVIOUSLY_HIV_STATUS_RESULTS = "previous_hiv_status";
            String PATIENT_ON_ART = "patient_on_art";
            String HIV_STATUS = "hiv_status";
            String CURRENT_HIV_STATUS = "current_hiv_status";
            String VISIT_TYPE = "visit_type";
            String APPOINTMENT_SCHEDULED_PREVIOUSLY = "previous_appointment";
            String DATE_OF_APPOINTMENT = "date_of_appointment";
            String PENDING_DIAGNOSE_AND_TREAT = "pending_diagnose_and_treat";
        }

        interface OpdVisit {
            String VISIT_DATE = "visit_date";

            String TEST_NAME = "test_name";
            String TEST_RESULT = "test_result";

            String TEST_TYPE = "test_type";
            String TEST_TYPE_LABEL = "test_type_label";

            String TESTS = "tests";
            String TESTS_LABEL = "tests_label";

            String DIAGNOSIS = "diagnosis";
            String DIAGNOSIS_LABEL = "diagnosis_label";

            String DIAGNOSIS_SAME = "diagnosis_same";
            String DIAGNOSIS_SAME_LABEL = "diagnosis_same_label";

            String DIAGNOSIS_TYPE = "diagnosis_type";
            String DIAGNOSIS_TYPE_LABEL = "diagnosis_type_label";

            String DISEASE_CODE = "disease_code";
            String DISEASE_CODE_LABEL = "disease_code_label";

            String TREATMENT = "treatment";
            String TREATMENT_LABEL = "treatment_label";

            String TREATMENT_TYPE = "treatment_type";
            String TREATMENT_TYPE_LABEL = "treatment_type_label";

            String TREATMENT_TYPE_SPECIFY = "treatment_type_specify";
            String TREATMENT_TYPE_SPECIFY_LABEL = "treatment_type_specify_label";

            String DISCHARGED_ALIVE = "discharged_alive";
            String DISCHARGED_ALIVE_LABEL = "discharged_alive_label";

            String DISCHARGED_HOME = "discharged_home";
            String DISCHARGED_HOME_LABEL = "discharged_home_label";

            String REFERRAL = "referral";
            String REFERRAL_LABEL = "referral_label";

            String REFERRAL_LOCATION = "referral_location";
            String REFERRAL_LOCATION_LABEL = "referral_location_label";

            String REFERRAL_LOCATION_SPECIFY = "referral_location_specify";
            String REFERRAL_LOCATION_SPECIFY_LABEL = "referral_location_specify_label";

            String PRE_EXISTING_CONDITIONS = "pre_existing_conditions";
            String PRE_EXISTING_CONDITIONS_LABEL = "pre_existing_conditions_label";

            String OTHER_PRE_EXISTING_CONDITIONS = "other_pre_existing_conditions";
            String OTHER_PRE_EXISTING_CONDITIONS_LABEL = "other_pre_existing_conditions_label";

            String INFLUENZA_PRE_EXISTING_CONDITIONS = "influenza_pre_existing_conditions";
            String INFLUENZA_PRE_EXISTING_CONDITIONS_LABEL = "influenza_pre_existing_conditions_label";

            String OTHER_INFLUENZA_PRE_EXISTING_CONDITIONS = "other_influenza_pre_existing_conditions";
            String OTHER_INFLUENZA_PRE_EXISTING_CONDITIONS_LABEL = "other_influenza_pre_existing_conditions_label";

            String INFLUENZA_ALLERGIES = "influenza_allergies";
            String INFLUENZA_ALLERGIES_LABEL = "influenza_allergies_label";

            String OTHER_INFLUENZA_ALLERGIES = "other_influenza_allergies";
            String OTHER_INFLUENZA_ALLERGIES_LABEL = "other_influenza_allergies_label";

            String OCCUPATION = "occupation";
            String OCCUPATION_LABEL = "occupation_label";

            String OTHER_OCCUPATION = "other_occupation";
            String OTHER_OCCUPATION_LABEL = "other_occupation_label";

            String TEMPERATURE = "temperature";
            String TEMPERATURE_LABEL = "temperature_label";

            String INF_TEMPERATURE = "influenza_temperature";
            String INF_TEMPERATURE_LABEL = "influenza_temperature_label";

            String COVID_19_HISTORY = "covid_19_history";
            String COVID_19_HISTORY_LABEL = "covid_19_history_label";

            String ORAL_CONFIRMATION = "oral_confirmation";
            String ORAL_CONFIRMATION_LABEL = "oral_confirmation_label";

            String RESPIRATORY_SYMPTONS = "respiratory_symptoms";
            String RESPIRATORY_SYMPTONS_LABEL = "respiratory_symptoms_label";

            String OTHER_RESPIRATORY_SYMPTONS = "other_respiratory_symptoms";
            String OTHER_RESPIRATORY_SYMPTONS_LABEL = "other_respiratory_symptoms_label";

            String ALLERGIES = "allergies";
            String ALLERGIES_LABEL = "allergies_label";

            String OTHER_ALLERGIES = "other_allergies";
            String OTHER_ALLERGIES_LABEL = "other_allergies_label";

            String COVID19_ANTIGENS = "covid19_antigens";
            String COVID19_ANTIGENS_LABEL = "covid19_antigens_label";

            String ADMINISTRATION_DATE = "administration_date";
            String ADMINISTRATION_DATE_LABEL = "administration_date_label";

            String SITE_OF_ADMINISTRATION = "site_of_administration";
            String SITE_OF_ADMINISTRATION_LABEL = "site_of_administration_label";

            String OTHER_SITE_OF_ADMINISTRATION = "other_site_of_administration";
            String OTHER_SITE_OF_ADMINISTRATION_LABEL = "_other_site_of_administration_label";

            String ADMINISTRATION_ROUTE = "administration_route";
            String ADMINISTRATION_ROUTE_LABEL = "administration_route_label";

            String OTHER_ADMINISTRATION_ROUTE = "other_administration_route";
            String OTHER_ADMINISTRATION_ROUTE_LABEL = "other_administration_route_label";

            String LOT_NUMBER = "lot_number";
            String LOT_NUMBER_LABEL = "lot_number_label";

            String VACCINE_EXPIRY = "vaccine_expiry";
            String VACCINE_EXPIRY_LABEL = "vaccine_expiry_label";

            String COVID_19_VACCINE_NEXT_DATE = "covid19_vaccine_next_date";
            String COVID_19_VACCINE_NEXT_DATE_LABEL = "covid19_vaccine_next_date_label";

            String INFLUENZA_VACCINES = "influenza_vaccines";
            String INFLUENZA_VACCINES_LABEL = "influenza_vaccines_label";

            String INFLUENZA_ADMINISTRATION_DATE = "influenza_administration_date";
            String INFLUENZA_ADMINISTRATION_DATE_LABEL = "influenza_administration_date_label";

            String INFLUENZA_SITE_OF_ADMINISTRATION = "influenza_site_of_vaccine_administration";
            String INFLUENZA_SITE_OF_ADMINISTRATION_LABEL = "influenza_site_of_vaccine_administration_label";

            String INFLUENZA_OTHER_SITE_OF_ADMINISTRATION = "other_site_of_administration";
            String INFLUENZA_OTHER_SITE_OF_ADMINISTRATION_LABEL = "_other_site_of_administration_label";

            String INFLUENZA_ADMINISTRATION_ROUTE = "influenza_vaccine_administration_route";
            String INFLUENZA_ADMINISTRATION_ROUTE_LABEL = "influenza_vaccine_administration_route_label";

            String INFLUENZA_OTHER_ADMINISTRATION_ROUTE = "other_administration_route";
            String INFLUENZA_OTHER_ADMINISTRATION_ROUTE_LABEL = "other_administration_route_label";

            String INFLUENZA_LOT_NUMBER = "influenza_vaccine_lot_number";
            String INFLUENZA_LOT_NUMBER_LABEL = "influenza_vaccine_lot_number_label";

            String INFLUENZA_VACCINE_EXPIRY = "influenza_vaccine_expiry";
            String INFLUENZA_VACCINE_EXPIRY_LABEL = "influenza_vaccine_expiry_label";

        }

    }
}
