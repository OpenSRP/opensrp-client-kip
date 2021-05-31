package org.smartregister.kip.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.NotImplementedException;
import org.smartregister.kip.dao.OpdMedicalCheckFormDao;
import org.smartregister.kip.pojo.OpdMedicalCheckForm;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.repository.BaseRepository;

import java.util.ArrayList;
import java.util.List;

public class OpdMedicalCheckFormRepository extends BaseRepository implements OpdMedicalCheckFormDao {

    private static final String INDEX_BASE_ENTITY_ID = "CREATE INDEX " + KipConstants.DbConstants.Tables.OPD_MEDICAL_CHECK__FORM
            + "_" + KipConstants.DbConstants.Columns.OpdMedicalCheck.BASE_ENTITY_ID + "_index ON " + KipConstants.DbConstants.Tables.OPD_MEDICAL_CHECK__FORM +
            "(" + KipConstants.DbConstants.Columns.OpdMedicalCheck.BASE_ENTITY_ID + " COLLATE NOCASE);";

    private String[] columns = new String[]{
            KipConstants.DbConstants.Columns.OpdMedicalCheck.ID,
            KipConstants.DbConstants.Columns.OpdMedicalCheck.BASE_ENTITY_ID,
            KipConstants.DbConstants.Columns.OpdMedicalCheck.VISIT_ID,
            KipConstants.DbConstants.Columns.OpdMedicalCheck.TEMPERATURE,
            KipConstants.DbConstants.Columns.OpdMedicalCheck.PRE_EXISTING_CONDITIONS,
            KipConstants.DbConstants.Columns.OpdMedicalCheck.OTHER_PRE_EXISTING_CONDITIONS,
            KipConstants.DbConstants.Columns.OpdMedicalCheck.ALLERGIES,
            KipConstants.DbConstants.Columns.OpdMedicalCheck.OTHER_ALLERGIES,
            KipConstants.DbConstants.Columns.OpdMedicalCheck.AGE,
            KipConstants.DbConstants.Columns.OpdMedicalCheck.DATE,
            KipConstants.DbConstants.Columns.OpdMedicalCheck.CREATED_AT};

    public static void updateIndex(@NonNull SQLiteDatabase database) {
        database.execSQL(INDEX_BASE_ENTITY_ID);
    }

    @Override
    public boolean saveOrUpdate(OpdMedicalCheckForm opdMedicalCheckForm) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KipConstants.DbConstants.Columns.OpdMedicalCheck.VISIT_ID, opdMedicalCheckForm.getVisitId());
        contentValues.put(KipConstants.DbConstants.Columns.OpdMedicalCheck.ID, opdMedicalCheckForm.getId());
        contentValues.put(KipConstants.DbConstants.Columns.OpdMedicalCheck.BASE_ENTITY_ID, opdMedicalCheckForm.getBaseEntityId());
        contentValues.put(KipConstants.DbConstants.Columns.OpdMedicalCheck.TEMPERATURE, opdMedicalCheckForm.getTemperature());
        contentValues.put(KipConstants.DbConstants.Columns.OpdMedicalCheck.PRE_EXISTING_CONDITIONS, opdMedicalCheckForm.getPreExistingConditions());
        contentValues.put(KipConstants.DbConstants.Columns.OpdMedicalCheck.OTHER_PRE_EXISTING_CONDITIONS, opdMedicalCheckForm.getOtherPreExistingConditions());
        contentValues.put(KipConstants.DbConstants.Columns.OpdMedicalCheck.ALLERGIES, opdMedicalCheckForm.getAllergies());
        contentValues.put(KipConstants.DbConstants.Columns.OpdMedicalCheck.OTHER_ALLERGIES, opdMedicalCheckForm.getOtherAllergies());
        contentValues.put(KipConstants.DbConstants.Columns.OpdMedicalCheck.DATE, opdMedicalCheckForm.getDate());
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        contentValues.put(KipConstants.DbConstants.Columns.OpdMedicalCheck.CREATED_AT, opdMedicalCheckForm.getCreatedAt());
        long rows = sqLiteDatabase.insertWithOnConflict(KipConstants.DbConstants.Tables.OPD_MEDICAL_CHECK__FORM, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return rows != -1;
    }

    @Override
    public boolean save(OpdMedicalCheckForm opdMedicalCheckForm) {
        throw new NotImplementedException("not implemented");
    }

    @Override
    public OpdMedicalCheckForm findOne(OpdMedicalCheckForm opdMedicalCheckForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(KipConstants.DbConstants.Tables.OPD_MEDICAL_CHECK__FORM
                , columns
                , KipConstants.DbConstants.Columns.OpdMedicalCheck.BASE_ENTITY_ID + " = ? "
                , new String[]{opdMedicalCheckForm.getBaseEntityId(), opdMedicalCheckForm.getVisitId()}
                , null
                , null
                , OpdDbConstants.Column.OpdCheckIn.CREATED_AT + " DESC "
                , "1");

        if (cursor.getCount() == 0) {
            return null;
        }

        OpdMedicalCheckForm medicalCheckForm = null;
        if (cursor.moveToNext()) {
            medicalCheckForm = new OpdMedicalCheckForm(
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
                    cursor.getString(10));


            cursor.close();
        }

        return medicalCheckForm;
    }

    @Override
    public boolean delete(OpdMedicalCheckForm opdMedicalCheckForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        int rows = sqLiteDatabase.delete(KipConstants.DbConstants.Tables.OPD_MEDICAL_CHECK__FORM
                , KipConstants.DbConstants.Columns.OpdMedicalCheck.BASE_ENTITY_ID + " = ? "
                , new String[]{opdMedicalCheckForm.getBaseEntityId()});

        return rows > 0;
    }

    @Override
    public List<OpdMedicalCheckForm> findAll() {
        throw new NotImplementedException("Not Implemented");
    }

    public OpdMedicalCheckForm findOneByVisit(OpdMedicalCheckForm opdMedicalCheckForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(KipConstants.DbConstants.Tables.OPD_MEDICAL_CHECK__FORM
                , columns
                , KipConstants.DbConstants.Columns.CalculateRiskFactor.BASE_ENTITY_ID + " = ? AND " + KipConstants.DbConstants.Columns.CalculateRiskFactor.VISIT_ID + " = ?"
                , new String[]{opdMedicalCheckForm.getBaseEntityId(), opdMedicalCheckForm.getVisitId()}
                , null
                , null
                , OpdDbConstants.Column.OpdCheckIn.CREATED_AT + " DESC "
                , "1");

        if (cursor.getCount() == 0) {
            return null;
        }

        OpdMedicalCheckForm medicalCheckForm = null;
        if (cursor.moveToNext()) {
            medicalCheckForm = new OpdMedicalCheckForm(
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
                    cursor.getString(10));
        }
        cursor.close();
        return medicalCheckForm;
    }

    @Override
    public List<OpdMedicalCheckForm> findAll(OpdMedicalCheckForm opdMedicalCheckForm) {
        List<OpdMedicalCheckForm> opdMedicalCheckFormArrayList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(KipConstants.DbConstants.Tables.OPD_MEDICAL_CHECK__FORM
                , columns
                , KipConstants.DbConstants.Columns.CalculateRiskFactor.BASE_ENTITY_ID + " = ? "
                , new String[]{opdMedicalCheckForm.getBaseEntityId()}
                , null
                , null
                , OpdDbConstants.Column.OpdCheckIn.CREATED_AT + " DESC ");

        if (cursor.getCount() == 0) {
            return null;
        }

        OpdMedicalCheckForm medicalCheckForm = null;
        while (cursor.moveToNext()) {
            medicalCheckForm = new OpdMedicalCheckForm(
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
                    cursor.getString(10));
            opdMedicalCheckFormArrayList.add(medicalCheckForm);
        }
        cursor.close();
        return opdMedicalCheckFormArrayList;
    }
}
