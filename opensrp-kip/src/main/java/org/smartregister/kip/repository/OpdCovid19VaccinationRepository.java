package org.smartregister.kip.repository;


import android.content.ContentValues;
import android.support.annotation.NonNull;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.NotImplementedException;
import org.smartregister.kip.dao.Covid19OpdVaccinationFormDao;
import org.smartregister.kip.pojo.OpdCovid19VaccinationForm;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.repository.BaseRepository;

import java.util.List;


public class OpdCovid19VaccinationRepository extends BaseRepository implements Covid19OpdVaccinationFormDao {
    private static final String INDEX_BASE_ENTITY_ID = "CREATE INDEX " + KipConstants.DbConstants.Tables.OPD_COVID_19_VACCINE_RECORD_TABLE
            + "_" + KipConstants.DbConstants.Columns.VaccineRecord.BASE_ENTITY_ID + "_index ON " + KipConstants.DbConstants.Tables.OPD_COVID_19_VACCINE_RECORD_TABLE +
            "(" + KipConstants.DbConstants.Columns.VaccineRecord.BASE_ENTITY_ID + " COLLATE NOCASE);";

    private String[] columns = new String[]{
            KipConstants.DbConstants.Columns.VaccineRecord.ID,
            KipConstants.DbConstants.Columns.VaccineRecord.BASE_ENTITY_ID,
            KipConstants.DbConstants.Columns.VaccineRecord.VISIT_ID,
            KipConstants.DbConstants.Columns.VaccineRecord.COVID_19_ANTIGENS,
            KipConstants.DbConstants.Columns.VaccineRecord.SITE_OF_ADMINISTRATION,
            KipConstants.DbConstants.Columns.VaccineRecord.ADMINISTRATION_DATE,
            KipConstants.DbConstants.Columns.VaccineRecord.ADMINISTRATION_ROUTE,
            KipConstants.DbConstants.Columns.VaccineRecord.LOT_NUMBER,
            KipConstants.DbConstants.Columns.VaccineRecord.VACCINE_EXPIRY,
            KipConstants.DbConstants.Columns.VaccineRecord.AGE,
            KipConstants.DbConstants.Columns.VaccineRecord.DATE,
            KipConstants.DbConstants.Columns.VaccineRecord.CREATED_AT};

    public static void updateIndex(@NonNull SQLiteDatabase database) {
        database.execSQL(INDEX_BASE_ENTITY_ID);
    }

    @Override
    public boolean saveOrUpdate(OpdCovid19VaccinationForm OpdCovid19VaccinationForm) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KipConstants.DbConstants.Columns.VaccineRecord.VISIT_ID, OpdCovid19VaccinationForm.getVisitId());
        contentValues.put(KipConstants.DbConstants.Columns.WaitingList.ID, OpdCovid19VaccinationForm.getId());
        contentValues.put(KipConstants.DbConstants.Columns.VaccineRecord.BASE_ENTITY_ID, OpdCovid19VaccinationForm.getBaseEntityId());
        contentValues.put(KipConstants.DbConstants.Columns.VaccineRecord.COVID_19_ANTIGENS, OpdCovid19VaccinationForm.getCovid19Antigens());
        contentValues.put(KipConstants.DbConstants.Columns.VaccineRecord.SITE_OF_ADMINISTRATION, OpdCovid19VaccinationForm.getSiteOfAdministration());
        contentValues.put(KipConstants.DbConstants.Columns.VaccineRecord.ADMINISTRATION_DATE, OpdCovid19VaccinationForm.getAdministrationDate());
        contentValues.put(KipConstants.DbConstants.Columns.VaccineRecord.ADMINISTRATION_ROUTE, OpdCovid19VaccinationForm.getAdministrationRoute());
        contentValues.put(KipConstants.DbConstants.Columns.VaccineRecord.LOT_NUMBER, OpdCovid19VaccinationForm.getLotNumber());
        contentValues.put(KipConstants.DbConstants.Columns.VaccineRecord.VACCINE_EXPIRY, OpdCovid19VaccinationForm.getVaccineExpiry());
        contentValues.put(KipConstants.DbConstants.Columns.VaccineRecord.AGE, OpdCovid19VaccinationForm.getAge());
        contentValues.put(KipConstants.DbConstants.Columns.VaccineRecord.DATE, OpdCovid19VaccinationForm.getDate());
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        contentValues.put(KipConstants.DbConstants.Columns.VaccineRecord.CREATED_AT, OpdCovid19VaccinationForm.getCreatedAt());
        long rows = sqLiteDatabase.insertWithOnConflict(KipConstants.DbConstants.Tables.OPD_COVID_19_VACCINE_RECORD_TABLE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return rows != -1;
    }

    @Override
    public boolean save(OpdCovid19VaccinationForm OpdCovid19VaccinationForm) {
        throw new NotImplementedException("not implemented");
    }

    @Override
    public OpdCovid19VaccinationForm findOne(OpdCovid19VaccinationForm OpdCovid19VaccinationForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(KipConstants.DbConstants.Tables.OPD_COVID_19_VACCINE_RECORD_TABLE
                , columns
                , KipConstants.DbConstants.Columns.CalculateRiskFactor.BASE_ENTITY_ID + " = ? "
                , new String[]{OpdCovid19VaccinationForm.getBaseEntityId()}
                , null
                , null
                , OpdDbConstants.Column.OpdCheckIn.CREATED_AT + " DESC "
                , "1");

        if (cursor.getCount() == 0) {
            return null;
        }

        org.smartregister.kip.pojo.OpdCovid19VaccinationForm VaccineRecordForm = null;
        if (cursor.moveToNext()) {
            VaccineRecordForm = new OpdCovid19VaccinationForm(
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
                    cursor.getString(11));
        }
        cursor.close();
        return VaccineRecordForm;
    }

    @Override
    public boolean delete(OpdCovid19VaccinationForm OpdCovid19VaccinationForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        int rows = sqLiteDatabase.delete(KipConstants.DbConstants.Tables.OPD_COVID_19_VACCINE_RECORD_TABLE
                , KipConstants.DbConstants.Columns.VaccineRecord.BASE_ENTITY_ID + " = ? "
                , new String[]{OpdCovid19VaccinationForm.getBaseEntityId()});

        return rows > 0;
    }

    @Override
    public List<OpdCovid19VaccinationForm> findAll() {
        throw new NotImplementedException("Not Implemented");
    }

    public OpdCovid19VaccinationForm findOneByVisit(OpdCovid19VaccinationForm OpdCovid19VaccinationForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(KipConstants.DbConstants.Tables.OPD_COVID_19_VACCINE_RECORD_TABLE
                , columns
                , KipConstants.DbConstants.Columns.CalculateRiskFactor.BASE_ENTITY_ID + " = ? AND " + KipConstants.DbConstants.Columns.CalculateRiskFactor.VISIT_ID + " = ?"
                , new String[]{OpdCovid19VaccinationForm.getBaseEntityId(), OpdCovid19VaccinationForm.getVisitId()}
                , null
                , null
                , OpdDbConstants.Column.OpdCheckIn.CREATED_AT + " DESC "
                , "1");

        if (cursor.getCount() == 0) {
            return null;
        }

        org.smartregister.kip.pojo.OpdCovid19VaccinationForm VaccineRecordForm = null;
        if (cursor.moveToNext()) {
            VaccineRecordForm = new OpdCovid19VaccinationForm(
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
                    cursor.getString(11));
        }
        cursor.close();

        return VaccineRecordForm;
    }
}
