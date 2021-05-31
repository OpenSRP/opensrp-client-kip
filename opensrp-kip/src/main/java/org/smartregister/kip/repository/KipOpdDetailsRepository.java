package org.smartregister.kip.repository;

import android.content.ContentValues;

import org.smartregister.kip.util.KipConstants;
import org.smartregister.opd.repository.OpdDetailsRepository;
import org.smartregister.repository.Repository;
import org.smartregister.view.activity.DrishtiApplication;

import java.util.Calendar;

import timber.log.Timber;

public class KipOpdDetailsRepository extends OpdDetailsRepository {

    public static final String COVID_19_RISK_FACTOR = "covid19_risk_factor";
    public static final String COVID_19_VACCINE_ELIGIBILITY = "covid19_vaccine_eligibility";
    public static final String COVID_19_WAITING_LIST = "covid19_waiting_list";
    public static final String COVID_19_VACCINE_GIVEN = "covid19_vaccine_given";
    public static final String INFLUENZA_VACCINE_GIVEN = "influenza_vaccine_given";
    public static final String COVID_19_VACCINE_NEXT_DATE = "covid19_vaccine_next_date";
    private static final String SMS_REMINDER = "opd_sms_reminder";
    public static final String COVID_19_VACCINE_NEXT_NUMBER = "covid19_vaccine_next_number";
    public static final String INFLUENZA_MEDICAL_CHECK = "opd_medical_check";

    public static void updateRiskStatus(String baseEntityId, int riskStatus) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COVID_19_RISK_FACTOR, riskStatus);
        contentValues.put(COVID_19_VACCINE_GIVEN, "");
        contentValues.put(COVID_19_VACCINE_NEXT_DATE, "");
        updatePatient(baseEntityId, contentValues, getRegisterQueryProvider().getDemographicTable());
        updateLastInteractedWith(baseEntityId);
    }

    public static void updateCovid19Eligibility(String baseEntityId, String eligibility) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COVID_19_VACCINE_ELIGIBILITY, eligibility);

        updatePatient(baseEntityId, contentValues, getRegisterQueryProvider().getDemographicTable());
        updateLastInteractedWith(baseEntityId);
    }

    public static void updateCovid19WaitingList(String baseEntityId, String riskFactor) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COVID_19_WAITING_LIST, riskFactor);

        updatePatient(baseEntityId, contentValues, getRegisterQueryProvider().getDemographicTable());
        updateLastInteractedWith(baseEntityId);
    }

    public static void updateOpdMedicalCheck(String baseEntityId){
        ContentValues contentValues = new ContentValues();
        contentValues.put(INFLUENZA_MEDICAL_CHECK, 1);
        contentValues.put(INFLUENZA_VACCINE_GIVEN, "");

        updatePatient(baseEntityId, contentValues, getRegisterQueryProvider().getDemographicTable());
        updateLastInteractedWith(baseEntityId);
    }

    public static void updateSmsReminder(String baseEntityId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SMS_REMINDER, 1);

        updatePatient(baseEntityId, contentValues, getRegisterQueryProvider().getDemographicTable());
        updateLastInteractedWith(baseEntityId);
    }

    public static void updateInfluenzaVaccineStatus(String baseEntityId){
        ContentValues contentValues = new ContentValues();
        contentValues.put(INFLUENZA_VACCINE_GIVEN, 1);

        updatePatient(baseEntityId, contentValues, getRegisterQueryProvider().getDemographicTable());
        updateLastInteractedWith(baseEntityId);
    }

    public static void updateCovid19VaccineDispense(String baseEntityId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COVID_19_VACCINE_GIVEN, 1);

        updatePatient(baseEntityId, contentValues, getRegisterQueryProvider().getDemographicTable());
        updateLastInteractedWith(baseEntityId);
    }

    public static void updateCovid19NextVaccineDetails(String baseEntityId, String date, String number) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COVID_19_VACCINE_NEXT_DATE, date);
        contentValues.put(COVID_19_VACCINE_NEXT_NUMBER, number);

        updatePatient(baseEntityId, contentValues, getRegisterQueryProvider().getDemographicTable());
        updateLastInteractedWith(baseEntityId);
    }

    public static void resetCovid19VaccineSchedule(String baseEntityId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COVID_19_RISK_FACTOR, "");
        contentValues.put(COVID_19_VACCINE_ELIGIBILITY, "");
        contentValues.put(COVID_19_WAITING_LIST, "");

        updatePatient(baseEntityId, contentValues, getRegisterQueryProvider().getDemographicTable());
        updateLastInteractedWith(baseEntityId);
    }

    public static void restInfluenzaSchedule(String baseEntityId){
        ContentValues contentValues = new ContentValues();
        contentValues.put(INFLUENZA_MEDICAL_CHECK, "");
        updatePatient(baseEntityId, contentValues, getRegisterQueryProvider().getDemographicTable());
        updateLastInteractedWith(baseEntityId);
    }


    public static void updatePatient(String baseEntityId, ContentValues contentValues, String table) {
        getMasterRepository().getWritableDatabase()
                .update(table, contentValues, KipConstants.KeyUtils.BASE_ENTITY_ID + " = ?",
                        new String[]{baseEntityId});
        Timber.i("-->Patient updated %s",baseEntityId);
    }

    private static void updateLastInteractedWith(String baseEntityId) {
        ContentValues lastInteractedWithContentValue = new ContentValues();
        lastInteractedWithContentValue.put(KipConstants.KeyUtils.LAST_INTERACTED_WITH, Calendar.getInstance().getTimeInMillis());
        updatePatient(baseEntityId, lastInteractedWithContentValue, getRegisterQueryProvider().getDemographicTable());
    }

    protected static Repository getMasterRepository() {
        return DrishtiApplication.getInstance().getRepository();
    }

    private static KipChildRegisterQueryProvider getRegisterQueryProvider() {
        return new KipChildRegisterQueryProvider();
    }

    public static void updateKepiSmsReminder(String baseEntityId, String date) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("kepi_sms_reminder_date", date);

        updatePatient(baseEntityId, contentValues, getRegisterQueryProvider().getDemographicTable());
        updateLastInteractedWith(baseEntityId);
        Timber.i("-->updateKepiSmsReminder %s, %s",contentValues,baseEntityId);
    }
}
