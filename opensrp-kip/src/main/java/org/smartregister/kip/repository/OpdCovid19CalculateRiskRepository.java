package org.smartregister.kip.repository;


import android.content.ContentValues;
import android.support.annotation.NonNull;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.NotImplementedException;
import org.smartregister.kip.dao.Covid19OpdCalculateRiskFactorFormDao;
import org.smartregister.kip.pojo.OpdCovid19CalculateRiskFactorForm;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.repository.BaseRepository;

import java.util.ArrayList;
import java.util.List;

public class OpdCovid19CalculateRiskRepository extends BaseRepository implements Covid19OpdCalculateRiskFactorFormDao {
    private static final String INDEX_BASE_ENTITY_ID = "CREATE INDEX " + KipConstants.DbConstants.Tables.CALCULATE_RISK_FACTOR_TABLE
            + "_" + KipConstants.DbConstants.Columns.CalculateRiskFactor.BASE_ENTITY_ID + "_index ON " + KipConstants.DbConstants.Tables.CALCULATE_RISK_FACTOR_TABLE +
            "(" + KipConstants.DbConstants.Columns.CalculateRiskFactor.BASE_ENTITY_ID + " COLLATE NOCASE);";

    private String[] columns = new String[]{
            KipConstants.DbConstants.Columns.CalculateRiskFactor.ID,
            KipConstants.DbConstants.Columns.CalculateRiskFactor.BASE_ENTITY_ID,
            KipConstants.DbConstants.Columns.CalculateRiskFactor.VISIT_ID,
            KipConstants.DbConstants.Columns.CalculateRiskFactor.PRE_EXISTING_CONDITIONS,
            KipConstants.DbConstants.Columns.CalculateRiskFactor.OTHER_PRE_EXISTING_CONDITIONS,
            KipConstants.DbConstants.Columns.CalculateRiskFactor.OCCUPATION,
            KipConstants.DbConstants.Columns.CalculateRiskFactor.OTHER_OCCUPATION,
            KipConstants.DbConstants.Columns.CalculateRiskFactor.AGE,
            KipConstants.DbConstants.Columns.CalculateRiskFactor.DATE,
            KipConstants.DbConstants.Columns.CalculateRiskFactor.CREATED_AT};

    public static void updateIndex(@NonNull SQLiteDatabase database) {
        database.execSQL(INDEX_BASE_ENTITY_ID);
    }

    @Override
    public boolean saveOrUpdate(OpdCovid19CalculateRiskFactorForm opdCovid19CalculateRiskFactorForm) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KipConstants.DbConstants.Columns.CalculateRiskFactor.VISIT_ID, opdCovid19CalculateRiskFactorForm.getVisitId());
        contentValues.put(KipConstants.DbConstants.Columns.WaitingList.ID, opdCovid19CalculateRiskFactorForm.getId());
        contentValues.put(KipConstants.DbConstants.Columns.CalculateRiskFactor.BASE_ENTITY_ID, opdCovid19CalculateRiskFactorForm.getBaseEntityId());
        contentValues.put(KipConstants.DbConstants.Columns.CalculateRiskFactor.PRE_EXISTING_CONDITIONS, opdCovid19CalculateRiskFactorForm.getPreExistingConditions());
        contentValues.put(KipConstants.DbConstants.Columns.CalculateRiskFactor.OTHER_PRE_EXISTING_CONDITIONS, opdCovid19CalculateRiskFactorForm.getOtherPreExistingConditions());
        contentValues.put(KipConstants.DbConstants.Columns.CalculateRiskFactor.OCCUPATION, opdCovid19CalculateRiskFactorForm.getOccupation());
        contentValues.put(KipConstants.DbConstants.Columns.CalculateRiskFactor.OTHER_OCCUPATION, opdCovid19CalculateRiskFactorForm.getOtherOccupation());
        contentValues.put(KipConstants.DbConstants.Columns.CalculateRiskFactor.DATE, opdCovid19CalculateRiskFactorForm.getDate());
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        contentValues.put(KipConstants.DbConstants.Columns.CalculateRiskFactor.CREATED_AT, opdCovid19CalculateRiskFactorForm.getCreatedAt());
        long rows = sqLiteDatabase.insertWithOnConflict(KipConstants.DbConstants.Tables.CALCULATE_RISK_FACTOR_TABLE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return rows != -1;
    }

    @Override
    public boolean save(OpdCovid19CalculateRiskFactorForm opdCovid19CalculateRiskFactorForm) {
        throw new NotImplementedException("not implemented");
    }

    @Override
    public OpdCovid19CalculateRiskFactorForm findOne(OpdCovid19CalculateRiskFactorForm opdCovid19CalculateRiskFactorForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(KipConstants.DbConstants.Tables.CALCULATE_RISK_FACTOR_TABLE
                , columns
                , KipConstants.DbConstants.Columns.CalculateRiskFactor.BASE_ENTITY_ID + " = ? "
                , new String[]{opdCovid19CalculateRiskFactorForm.getBaseEntityId(), opdCovid19CalculateRiskFactorForm.getVisitId()}
                , null
                , null
                , OpdDbConstants.Column.OpdCheckIn.CREATED_AT + " DESC "
                , "1");

        if (cursor.getCount() == 0) {
            return null;
        }

        OpdCovid19CalculateRiskFactorForm calculateRiskFactorForm = null;
        if (cursor.moveToNext()) {
            calculateRiskFactorForm = new OpdCovid19CalculateRiskFactorForm(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8),
                    cursor.getString(9));
        }
        cursor.close();

        return calculateRiskFactorForm;
    }

    @Override
    public boolean delete(OpdCovid19CalculateRiskFactorForm opdCovid19CalculateRiskFactorForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        int rows = sqLiteDatabase.delete(KipConstants.DbConstants.Tables.CALCULATE_RISK_FACTOR_TABLE
                , KipConstants.DbConstants.Columns.CalculateRiskFactor.BASE_ENTITY_ID + " = ? "
                , new String[]{opdCovid19CalculateRiskFactorForm.getBaseEntityId()});

        return rows > 0;
    }

    @Override
    public List<OpdCovid19CalculateRiskFactorForm> findAll() {
        throw new NotImplementedException("not implemented");
    }

    public OpdCovid19CalculateRiskFactorForm findOneByVisit(OpdCovid19CalculateRiskFactorForm opdCovid19CalculateRiskFactorForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(KipConstants.DbConstants.Tables.CALCULATE_RISK_FACTOR_TABLE
                , columns
                , KipConstants.DbConstants.Columns.CalculateRiskFactor.BASE_ENTITY_ID + " = ? AND " + KipConstants.DbConstants.Columns.CalculateRiskFactor.VISIT_ID + " = ?"
                , new String[]{opdCovid19CalculateRiskFactorForm.getBaseEntityId(), opdCovid19CalculateRiskFactorForm.getVisitId()}
                , null
                , null
                , OpdDbConstants.Column.OpdCheckIn.CREATED_AT + " DESC "
                , "1");

        if (cursor.getCount() == 0) {
            return null;
        }

        OpdCovid19CalculateRiskFactorForm calculateRiskFactorForm = null;
        if (cursor.moveToNext()) {
            calculateRiskFactorForm = new OpdCovid19CalculateRiskFactorForm(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8),
                    cursor.getString(9));
        }
        cursor.close();
        return calculateRiskFactorForm;
    }

    @Override
    public List<OpdCovid19CalculateRiskFactorForm> findAll(OpdCovid19CalculateRiskFactorForm opdCovid19CalculateRiskFactorForm) {
        List<OpdCovid19CalculateRiskFactorForm> opdCovid19CalculateRiskFactorFormList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(KipConstants.DbConstants.Tables.CALCULATE_RISK_FACTOR_TABLE
                , columns
                , KipConstants.DbConstants.Columns.CalculateRiskFactor.BASE_ENTITY_ID + " = ? "
                , new String[]{opdCovid19CalculateRiskFactorForm.getBaseEntityId()}
                , null
                , null
                , OpdDbConstants.Column.OpdCheckIn.CREATED_AT + " DESC ");

        if (cursor.getCount() == 0) {
            return null;
        }

        OpdCovid19CalculateRiskFactorForm calculateRiskFactorForm = null;
        while (cursor.moveToNext()) {
            calculateRiskFactorForm = new OpdCovid19CalculateRiskFactorForm(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8),
                    cursor.getString(9));
            opdCovid19CalculateRiskFactorFormList.add(calculateRiskFactorForm);
        }
        cursor.close();
        return opdCovid19CalculateRiskFactorFormList;
    }
}
