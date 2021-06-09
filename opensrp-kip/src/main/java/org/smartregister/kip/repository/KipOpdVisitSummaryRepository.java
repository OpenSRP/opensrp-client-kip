package org.smartregister.kip.repository;

import androidx.annotation.NonNull;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.kip.pojo.KipOpdVisitSummary;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.opd.pojo.OpdVisitSummary;
import org.smartregister.opd.repository.OpdVisitSummaryRepository;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.opd.utils.OpdUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class KipOpdVisitSummaryRepository extends OpdVisitSummaryRepository {

    @Override
    public String[] visitSummaryColumns() {
        return new String[]{
                OpdDbConstants.Table.OPD_VISIT + "." + OpdDbConstants.Column.OpdVisit.VISIT_DATE,
                OpdDbConstants.Table.OPD_VISIT + "." + OpdDbConstants.Column.OpdVisit.ID,

                KipConstants.DbConstants.Tables.CALCULATE_RISK_FACTOR_TABLE + "." + KipConstants.DbConstants.Columns.CalculateRiskFactor.PRE_EXISTING_CONDITIONS,
                KipConstants.DbConstants.Tables.CALCULATE_RISK_FACTOR_TABLE + "." + KipConstants.DbConstants.Columns.CalculateRiskFactor.OTHER_PRE_EXISTING_CONDITIONS,
                KipConstants.DbConstants.Tables.CALCULATE_RISK_FACTOR_TABLE + "." + KipConstants.DbConstants.Columns.CalculateRiskFactor.OCCUPATION,
                KipConstants.DbConstants.Tables.CALCULATE_RISK_FACTOR_TABLE + "." + KipConstants.DbConstants.Columns.CalculateRiskFactor.OTHER_OCCUPATION,

                KipConstants.DbConstants.Tables.OPD_VACCINATION_CONDITIONS_CHECK_TABLE + "." + KipConstants.DbConstants.Columns.VaccinationEligibility.TEMPERATURE,
                KipConstants.DbConstants.Tables.OPD_VACCINATION_CONDITIONS_CHECK_TABLE + "." + KipConstants.DbConstants.Columns.VaccinationEligibility.COVID_19_HISTORY,
                KipConstants.DbConstants.Tables.OPD_VACCINATION_CONDITIONS_CHECK_TABLE + "." + KipConstants.DbConstants.Columns.VaccinationEligibility.ORAL_CONFIRMATION,
                KipConstants.DbConstants.Tables.OPD_VACCINATION_CONDITIONS_CHECK_TABLE + "." + KipConstants.DbConstants.Columns.VaccinationEligibility.RESPIRATORY_SYMPTOMS,
                KipConstants.DbConstants.Tables.OPD_VACCINATION_CONDITIONS_CHECK_TABLE + "." + KipConstants.DbConstants.Columns.VaccinationEligibility.OTHER_RESPIRATORY_SYMPTOMS,
                KipConstants.DbConstants.Tables.OPD_VACCINATION_CONDITIONS_CHECK_TABLE + "." + KipConstants.DbConstants.Columns.VaccinationEligibility.ALLERGIES,
                KipConstants.DbConstants.Tables.OPD_VACCINATION_CONDITIONS_CHECK_TABLE + "." + KipConstants.DbConstants.Columns.VaccinationEligibility.OTHER_ALLERGIES,

                KipConstants.DbConstants.Tables.OPD_COVID_19_VACCINE_RECORD_TABLE + "." + KipConstants.DbConstants.Columns.VaccineRecord.COVID_19_ANTIGENS,
                KipConstants.DbConstants.Tables.OPD_COVID_19_VACCINE_RECORD_TABLE + "." + KipConstants.DbConstants.Columns.VaccineRecord.SITE_OF_ADMINISTRATION,
                KipConstants.DbConstants.Tables.OPD_COVID_19_VACCINE_RECORD_TABLE + "." + KipConstants.DbConstants.Columns.VaccineRecord.ADMINISTRATION_DATE,
                KipConstants.DbConstants.Tables.OPD_COVID_19_VACCINE_RECORD_TABLE + "." + KipConstants.DbConstants.Columns.VaccineRecord.ADMINISTRATION_ROUTE,
                KipConstants.DbConstants.Tables.OPD_COVID_19_VACCINE_RECORD_TABLE + "." + KipConstants.DbConstants.Columns.VaccineRecord.LOT_NUMBER,
                KipConstants.DbConstants.Tables.OPD_COVID_19_VACCINE_RECORD_TABLE + "." + KipConstants.DbConstants.Columns.VaccineRecord.VACCINE_EXPIRY,

                KipConstants.DbConstants.Tables.OPD_MEDICAL_CHECK__FORM + "." + KipConstants.DbConstants.Columns.OpdMedicalCheck.PRE_EXISTING_CONDITIONS,
                KipConstants.DbConstants.Tables.OPD_MEDICAL_CHECK__FORM + "." + KipConstants.DbConstants.Columns.OpdMedicalCheck.OTHER_PRE_EXISTING_CONDITIONS,
                KipConstants.DbConstants.Tables.OPD_MEDICAL_CHECK__FORM + "." + KipConstants.DbConstants.Columns.OpdMedicalCheck.ALLERGIES,
                KipConstants.DbConstants.Tables.OPD_MEDICAL_CHECK__FORM + "." + KipConstants.DbConstants.Columns.OpdMedicalCheck.OTHER_ALLERGIES,
                KipConstants.DbConstants.Tables.OPD_MEDICAL_CHECK__FORM + "." + KipConstants.DbConstants.Columns.OpdMedicalCheck.TEMPERATURE,

                KipConstants.DbConstants.Tables.OPD_INFLUENZA_VACCINE_RECORD_TABLE + "." + KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.INFLUENZA_VACCINE,
                KipConstants.DbConstants.Tables.OPD_INFLUENZA_VACCINE_RECORD_TABLE + "." + KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.SITE_OF_ADMINISTRATION,
                KipConstants.DbConstants.Tables.OPD_INFLUENZA_VACCINE_RECORD_TABLE + "." + KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.ADMINISTRATION_DATE,
                KipConstants.DbConstants.Tables.OPD_INFLUENZA_VACCINE_RECORD_TABLE + "." + KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.ADMINISTRATION_ROUTE,
                KipConstants.DbConstants.Tables.OPD_INFLUENZA_VACCINE_RECORD_TABLE + "." + KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.LOT_NUMBER,
                KipConstants.DbConstants.Tables.OPD_INFLUENZA_VACCINE_RECORD_TABLE + "." + KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.VACCINE_EXPIRY,

                KipConstants.TABLE_NAME.ALL_CLIENTS + "." + "covid19_vaccine_next_date",

        };
    }

    @NonNull
    public List<KipOpdVisitSummary> getKipOpdVisitSummaries(@NonNull String baseEntityId, int pageNo) {
        LinkedHashMap<String, KipOpdVisitSummary> opdVisitSummaries = new LinkedHashMap<>();

        Cursor mCursor = null;
        try {
            SQLiteDatabase db = getReadableDatabase();

            String[] visitIds = getVisitIds(baseEntityId, pageNo);
            String joinedIds = "'" + StringUtils.join(visitIds, "','") + "'";

            String query = "SELECT " + StringUtils.join(visitSummaryColumns(), ",") + " FROM " + OpdDbConstants.Table.OPD_VISIT +
                    " LEFT OUTER JOIN " + KipConstants.DbConstants.Tables.OPD_MEDICAL_CHECK__FORM + " ON "
                    + OpdDbConstants.Table.OPD_VISIT + "." + OpdDbConstants.Column.OpdVisit.ID + " = " + KipConstants.DbConstants.Tables.OPD_MEDICAL_CHECK__FORM + "." + KipConstants.DbConstants.Columns.OpdMedicalCheck.VISIT_ID +
                    " LEFT JOIN " + KipConstants.DbConstants.Tables.OPD_INFLUENZA_VACCINE_RECORD_TABLE + " ON "
                    + KipConstants.DbConstants.Tables.OPD_INFLUENZA_VACCINE_RECORD_TABLE + "." + KipConstants.DbConstants.Columns.CalculateRiskFactor.VISIT_ID + " = " + OpdDbConstants.Table.OPD_VISIT + "." + OpdDbConstants.Column.OpdVisit.ID +
                    " LEFT JOIN " + KipConstants.DbConstants.Tables.CALCULATE_RISK_FACTOR_TABLE + " ON "
                    + KipConstants.DbConstants.Tables.CALCULATE_RISK_FACTOR_TABLE + "." + KipConstants.DbConstants.Columns.CalculateRiskFactor.VISIT_ID + " = " + OpdDbConstants.Table.OPD_VISIT + "." + OpdDbConstants.Column.OpdVisit.ID +
                    " LEFT JOIN " + KipConstants.DbConstants.Tables.OPD_VACCINATION_CONDITIONS_CHECK_TABLE + " ON "
                    + OpdDbConstants.Table.OPD_VISIT + "." + OpdDbConstants.Column.OpdVisit.ID + " = " + KipConstants.DbConstants.Tables.OPD_VACCINATION_CONDITIONS_CHECK_TABLE + "." + KipConstants.DbConstants.Columns.VaccinationEligibility.VISIT_ID +
                    " LEFT JOIN " + KipConstants.DbConstants.Tables.OPD_COVID_19_VACCINE_RECORD_TABLE + " ON "
                    + OpdDbConstants.Table.OPD_VISIT + "." + OpdDbConstants.Column.OpdVisit.ID + " = " + KipConstants.DbConstants.Tables.OPD_COVID_19_VACCINE_RECORD_TABLE + "." + KipConstants.DbConstants.Columns.VaccineRecord.VISIT_ID +
                    " LEFT JOIN " + KipConstants.TABLE_NAME.ALL_CLIENTS + " ON "
                    + OpdDbConstants.Table.OPD_VISIT + "." + OpdDbConstants.Column.OpdVisit.BASE_ENTITY_ID + " = " + KipConstants.TABLE_NAME.ALL_CLIENTS + "." + OpdDbConstants.Column.OpdVisit.BASE_ENTITY_ID+
                    " WHERE " + OpdDbConstants.Table.OPD_VISIT + "." + OpdDbConstants.Column.OpdVisit.BASE_ENTITY_ID + " = '" + baseEntityId + "'"
                    + " AND " + OpdDbConstants.Table.OPD_VISIT + "." + OpdDbConstants.Column.OpdVisit.ID + " IN (" + joinedIds + ") " +
                    " ORDER BY " + OpdDbConstants.Table.OPD_VISIT + "." + OpdDbConstants.Column.OpdVisit.VISIT_DATE + " DESC";

            if (StringUtils.isNotBlank(baseEntityId)) {
                mCursor = db.rawQuery(query, null);

                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        KipOpdVisitSummary visitSummaryResult = getVisitSummaryResult(mCursor);
                        String dateString = (new SimpleDateFormat(OpdConstants.DateFormat.YYYY_MM_DD_HH_MM_SS, Locale.ENGLISH)).format(visitSummaryResult.getVisitDate());

                        OpdVisitSummary existingOpdVisitSummary = opdVisitSummaries.get(dateString);
                        if (existingOpdVisitSummary != null) {
                            // Add any extra disease codes
                            String disease = visitSummaryResult.getDisease();
                            if (disease != null && !existingOpdVisitSummary.getDisease().contains(disease)) {
                                existingOpdVisitSummary.addDisease(disease);
                            }

                            // Add any extra treatments/medicines
                            OpdVisitSummary.Treatment treatment = visitSummaryResult.getTreatment();
                            if (treatment != null && treatment.getMedicine() != null && !existingOpdVisitSummary.getTreatments().containsKey(treatment.getMedicine())) {
                                existingOpdVisitSummary.addTreatment(treatment);
                            }

                            // Add any extra Tests
                            OpdVisitSummary.Test test = visitSummaryResult.getTest();
                            if (test != null && StringUtils.isNotBlank(test.getType())) {
                                existingOpdVisitSummary.addTest(test);
                            }
                        } else {
                            opdVisitSummaries.put(dateString, visitSummaryResult);
                        }

                    }

                }
            }

        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (mCursor != null) {
                mCursor.close();
            }
        }
        return new ArrayList<>(opdVisitSummaries.values());
    }

    @NonNull
    public KipOpdVisitSummary getVisitSummaryResult(@NonNull Cursor cursor) {
        KipOpdVisitSummary opdVisitModel = new KipOpdVisitSummary();

        opdVisitModel.setPreExistingConditions(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.CalculateRiskFactor.PRE_EXISTING_CONDITIONS)));
        opdVisitModel.setOtherPreExistingCondition(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.CalculateRiskFactor.OTHER_PRE_EXISTING_CONDITIONS)));
        opdVisitModel.setOccupation(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.CalculateRiskFactor.OCCUPATION)));
        opdVisitModel.setOtherOccupation(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.CalculateRiskFactor.OTHER_OCCUPATION)));


        opdVisitModel.setInfluenzaPreExistingConditions(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.OpdMedicalCheck.PRE_EXISTING_CONDITIONS)));
        opdVisitModel.setOtherInfluenzaPreExistingCondition(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.OpdMedicalCheck.OTHER_PRE_EXISTING_CONDITIONS)));
        opdVisitModel.setInfluenzaAllergies(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.OpdMedicalCheck.ALLERGIES)));
        opdVisitModel.setOtherInfluenzaAllergies(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.OpdMedicalCheck.OTHER_ALLERGIES)));
        opdVisitModel.setInfTemperature(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.OpdMedicalCheck.TEMPERATURE)));

        opdVisitModel.setTemperature(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.VaccinationEligibility.TEMPERATURE)));
        opdVisitModel.setCovid19History(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.VaccinationEligibility.COVID_19_HISTORY)));
        opdVisitModel.setOralConfirmation(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.VaccinationEligibility.ORAL_CONFIRMATION)));
        opdVisitModel.setRespiratorySymptons(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.VaccinationEligibility.RESPIRATORY_SYMPTOMS)));
        opdVisitModel.setOtherRespiratorySymptons(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.VaccinationEligibility.OTHER_RESPIRATORY_SYMPTOMS)));
        opdVisitModel.setAllergies(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.VaccinationEligibility.ALLERGIES)));
        opdVisitModel.setOtherAllergies(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.VaccinationEligibility.OTHER_ALLERGIES)));

        opdVisitModel.setCovid19Atigens(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.VaccineRecord.COVID_19_ANTIGENS)));
        opdVisitModel.setSiteOfAdministration(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.VaccineRecord.SITE_OF_ADMINISTRATION)));
        opdVisitModel.setAdministrationDate(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.VaccineRecord.ADMINISTRATION_DATE)));
        opdVisitModel.setAdministratonRoute(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.VaccineRecord.ADMINISTRATION_ROUTE)));
        opdVisitModel.setLotNumber(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.VaccineRecord.LOT_NUMBER)));
        opdVisitModel.setVaccineExpiry(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.VaccineRecord.VACCINE_EXPIRY)));

        opdVisitModel.setInfluenzaVaccines(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.INFLUENZA_VACCINE)));
        opdVisitModel.setInfluenzaSiteOfAdministration(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.SITE_OF_ADMINISTRATION)));
        opdVisitModel.setInfluenzaAdministrationDate(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.ADMINISTRATION_DATE)));
        opdVisitModel.setInfluenzaAdministratonRoute(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.ADMINISTRATION_ROUTE)));
        opdVisitModel.setInfluenzaLotNumber(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.LOT_NUMBER)));
        opdVisitModel.setInfluenzaVaccineExpiry(cursor.getString(cursor.getColumnIndex(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.VACCINE_EXPIRY)));

        opdVisitModel.setCovid19VaccineNextDate(cursor.getString(cursor.getColumnIndex("covid19_vaccine_next_date")));



        opdVisitModel.setVisitDate(OpdUtils.convertStringToDate(OpdConstants.DateFormat.YYYY_MM_DD_HH_MM_SS, cursor.getString(cursor.getColumnIndex(OpdDbConstants.Column.OpdVisit.VISIT_DATE))));
        return opdVisitModel;
    }
}
