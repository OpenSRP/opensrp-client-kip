package org.smartregister.kip.repository;


import android.content.ContentValues;
import android.support.annotation.NonNull;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.NotImplementedException;
import org.smartregister.kip.dao.Covid19OpdVaccinationEligibilityCheckFormDao;
import org.smartregister.kip.pojo.OpdCovid19VaccinationEligibilityCheckForm;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.repository.BaseRepository;

import java.util.List;


public class OpdCovid19VaccinationEligibilityRepository extends BaseRepository implements Covid19OpdVaccinationEligibilityCheckFormDao {
    private static final String INDEX_BASE_ENTITY_ID = "CREATE INDEX " + KipConstants.DbConstants.Tables.OPD_VACCINATION_CONDITIONS_CHECK_TABLE
            + "_" + KipConstants.DbConstants.Columns.VaccinationEligibility.BASE_ENTITY_ID + "_index ON " + KipConstants.DbConstants.Tables.OPD_VACCINATION_CONDITIONS_CHECK_TABLE +
            "(" + KipConstants.DbConstants.Columns.VaccinationEligibility.BASE_ENTITY_ID + " COLLATE NOCASE);";

    private String[] columns = new String[]{
            KipConstants.DbConstants.Columns.VaccinationEligibility.ID,
            KipConstants.DbConstants.Columns.VaccinationEligibility.BASE_ENTITY_ID,
            KipConstants.DbConstants.Columns.VaccinationEligibility.VISIT_ID,
            KipConstants.DbConstants.Columns.VaccinationEligibility.TEMPERATURE,
            KipConstants.DbConstants.Columns.VaccinationEligibility.COVID_19_HISTORY,
            KipConstants.DbConstants.Columns.VaccinationEligibility.ORAL_CONFIRMATION,
            KipConstants.DbConstants.Columns.VaccinationEligibility.RESPIRATORY_SYMPTOMS,
            KipConstants.DbConstants.Columns.VaccinationEligibility.OTHER_RESPIRATORY_SYMPTOMS,
            KipConstants.DbConstants.Columns.VaccinationEligibility.ALLERGIES,
            KipConstants.DbConstants.Columns.VaccinationEligibility.OTHER_ALLERGIES,
            KipConstants.DbConstants.Columns.VaccinationEligibility.AGE,
            KipConstants.DbConstants.Columns.VaccinationEligibility.DATE,
            KipConstants.DbConstants.Columns.VaccinationEligibility.CREATED_AT};

    public static void updateIndex(@NonNull SQLiteDatabase database) {
        database.execSQL(INDEX_BASE_ENTITY_ID);
    }

    @Override
    public boolean saveOrUpdate(OpdCovid19VaccinationEligibilityCheckForm OpdCovid19VaccinationEligibilityCheckForm) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KipConstants.DbConstants.Columns.VaccinationEligibility.VISIT_ID, OpdCovid19VaccinationEligibilityCheckForm.getVisitId());
        contentValues.put(KipConstants.DbConstants.Columns.WaitingList.ID, OpdCovid19VaccinationEligibilityCheckForm.getId());
        contentValues.put(KipConstants.DbConstants.Columns.VaccinationEligibility.BASE_ENTITY_ID, OpdCovid19VaccinationEligibilityCheckForm.getBaseEntityId());
        contentValues.put(KipConstants.DbConstants.Columns.VaccinationEligibility.TEMPERATURE, OpdCovid19VaccinationEligibilityCheckForm.getTemperature());
        contentValues.put(KipConstants.DbConstants.Columns.VaccinationEligibility.COVID_19_HISTORY, OpdCovid19VaccinationEligibilityCheckForm.getCovid19History());
        contentValues.put(KipConstants.DbConstants.Columns.VaccinationEligibility.ORAL_CONFIRMATION, OpdCovid19VaccinationEligibilityCheckForm.getOralConfirmation());
        contentValues.put(KipConstants.DbConstants.Columns.VaccinationEligibility.RESPIRATORY_SYMPTOMS, OpdCovid19VaccinationEligibilityCheckForm.getRespiratorySymptoms());
        contentValues.put(KipConstants.DbConstants.Columns.VaccinationEligibility.OTHER_RESPIRATORY_SYMPTOMS, OpdCovid19VaccinationEligibilityCheckForm.getOtherRespiratorySymptoms());
        contentValues.put(KipConstants.DbConstants.Columns.VaccinationEligibility.ALLERGIES, OpdCovid19VaccinationEligibilityCheckForm.getAllergies());
        contentValues.put(KipConstants.DbConstants.Columns.VaccinationEligibility.OTHER_ALLERGIES, OpdCovid19VaccinationEligibilityCheckForm.getOtherAllergies());
        contentValues.put(KipConstants.DbConstants.Columns.VaccinationEligibility.AGE, OpdCovid19VaccinationEligibilityCheckForm.getAge());
        contentValues.put(KipConstants.DbConstants.Columns.VaccinationEligibility.DATE, OpdCovid19VaccinationEligibilityCheckForm.getDate());
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        contentValues.put(KipConstants.DbConstants.Columns.VaccinationEligibility.CREATED_AT, OpdCovid19VaccinationEligibilityCheckForm.getCreatedAt());
        long rows = sqLiteDatabase.insertWithOnConflict(KipConstants.DbConstants.Tables.OPD_VACCINATION_CONDITIONS_CHECK_TABLE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return rows != -1;
    }

    @Override
    public boolean save(OpdCovid19VaccinationEligibilityCheckForm OpdCovid19VaccinationEligibilityCheckForm) {
        throw new NotImplementedException("not implemented");
    }

    @Override
    public OpdCovid19VaccinationEligibilityCheckForm findOne(OpdCovid19VaccinationEligibilityCheckForm OpdCovid19VaccinationEligibilityCheckForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(KipConstants.DbConstants.Tables.OPD_VACCINATION_CONDITIONS_CHECK_TABLE
                , columns
                , KipConstants.DbConstants.Columns.CalculateRiskFactor.BASE_ENTITY_ID + " = ? "
                , new String[]{OpdCovid19VaccinationEligibilityCheckForm.getBaseEntityId()}
                , null
                , null
                , OpdDbConstants.Column.OpdCheckIn.CREATED_AT + " DESC "
                , "1");

        if (cursor.getCount() == 0) {
            return null;
        }

        org.smartregister.kip.pojo.OpdCovid19VaccinationEligibilityCheckForm VaccinationEligibilityForm = null;
        if (cursor.moveToNext()) {
            VaccinationEligibilityForm = new OpdCovid19VaccinationEligibilityCheckForm(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8),
                    cursor.getString(9),
                    cursor.getString(10),
                    cursor.getString(11),
                    cursor.getString(12));
        }
        cursor.close();

        return VaccinationEligibilityForm;
    }

    @Override
    public boolean delete(OpdCovid19VaccinationEligibilityCheckForm OpdCovid19VaccinationEligibilityCheckForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        int rows = sqLiteDatabase.delete(KipConstants.DbConstants.Tables.OPD_VACCINATION_CONDITIONS_CHECK_TABLE
                , KipConstants.DbConstants.Columns.VaccinationEligibility.BASE_ENTITY_ID + " = ? "
                , new String[]{OpdCovid19VaccinationEligibilityCheckForm.getBaseEntityId()});

        return rows > 0;
    }

    @Override
    public List<OpdCovid19VaccinationEligibilityCheckForm> findAll() {
        return null;
    }

    public OpdCovid19VaccinationEligibilityCheckForm findOneByVisit(OpdCovid19VaccinationEligibilityCheckForm OpdCovid19VaccinationEligibilityCheckForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(KipConstants.DbConstants.Tables.OPD_VACCINATION_CONDITIONS_CHECK_TABLE
                , columns
                , KipConstants.DbConstants.Columns.CalculateRiskFactor.BASE_ENTITY_ID + " = ? AND " + KipConstants.DbConstants.Columns.CalculateRiskFactor.VISIT_ID + " = ?"
                , new String[]{OpdCovid19VaccinationEligibilityCheckForm.getBaseEntityId(), OpdCovid19VaccinationEligibilityCheckForm.getVisitId()}
                , null
                , null
                , OpdDbConstants.Column.OpdCheckIn.CREATED_AT + " DESC "
                , "1");

        if (cursor.getCount() == 0) {
            return null;
        }

        org.smartregister.kip.pojo.OpdCovid19VaccinationEligibilityCheckForm VaccinationEligibilityForm = null;
        if (cursor.moveToNext()) {
            VaccinationEligibilityForm = new OpdCovid19VaccinationEligibilityCheckForm(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8),
                    cursor.getString(9),
                    cursor.getString(10),
                    cursor.getString(11),
                    cursor.getString(12));
        }

        cursor.close();
        return VaccinationEligibilityForm;
    }

}
