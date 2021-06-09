package org.smartregister.kip.repository;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.NonNull;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.NotImplementedException;
import org.smartregister.kip.dao.OpdInfluenzaVaccineAdministrationFormDao;
import org.smartregister.kip.pojo.OpdInfluenzaVaccineAdministrationForm;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.repository.BaseRepository;

import java.util.List;

public class OpdInfluenzaVaccineAdministrationFormRepository extends BaseRepository implements OpdInfluenzaVaccineAdministrationFormDao {

    private static final String INDEX_BASE_ENTITY_ID = "CREATE INDEX " + KipConstants.DbConstants.Tables.OPD_INFLUENZA_VACCINE_RECORD_TABLE
            + "_" + KipConstants.DbConstants.Columns.VaccineRecord.BASE_ENTITY_ID + "_index ON " + KipConstants.DbConstants.Tables.OPD_INFLUENZA_VACCINE_RECORD_TABLE +
            "(" + KipConstants.DbConstants.Columns.VaccineRecord.BASE_ENTITY_ID + " COLLATE NOCASE);";
    private String[] columns = new String[]{
            KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.ID,
            KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.BASE_ENTITY_ID,
            KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.VISIT_ID,
            KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.INFLUENZA_VACCINE,
            KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.SITE_OF_ADMINISTRATION,
            KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.ADMINISTRATION_DATE,
            KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.ADMINISTRATION_ROUTE,
            KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.LOT_NUMBER,
            KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.VACCINE_EXPIRY,
            KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.AGE,
            KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.DATE,
            KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.CREATED_AT};

    public static void updateIndex(@NonNull SQLiteDatabase database) {
        database.execSQL(INDEX_BASE_ENTITY_ID);
    }

    @Override
    public boolean saveOrUpdate(OpdInfluenzaVaccineAdministrationForm opdInfluenzaVaccineAdministrationForm) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.VISIT_ID, opdInfluenzaVaccineAdministrationForm.getVisitId());
        contentValues.put(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.ID, opdInfluenzaVaccineAdministrationForm.getId());
        contentValues.put(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.BASE_ENTITY_ID, opdInfluenzaVaccineAdministrationForm.getBaseEntityId());
        contentValues.put(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.INFLUENZA_VACCINE, opdInfluenzaVaccineAdministrationForm.getInfluenzaVaccines());
        contentValues.put(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.SITE_OF_ADMINISTRATION, opdInfluenzaVaccineAdministrationForm.getInfluenzaSiteOfAdministration());
        contentValues.put(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.ADMINISTRATION_DATE, opdInfluenzaVaccineAdministrationForm.getInfluenzaAdministrationDate());
        contentValues.put(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.ADMINISTRATION_ROUTE, opdInfluenzaVaccineAdministrationForm.getInfluenzaAdministrationRoute());
        contentValues.put(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.LOT_NUMBER, opdInfluenzaVaccineAdministrationForm.getInfluenzaLotNumber());
        contentValues.put(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.VACCINE_EXPIRY, opdInfluenzaVaccineAdministrationForm.getInfluenzaVaccineExpiry());
        contentValues.put(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.AGE, opdInfluenzaVaccineAdministrationForm.getAge());
        contentValues.put(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.DATE, opdInfluenzaVaccineAdministrationForm.getDate());
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        contentValues.put(KipConstants.DbConstants.Columns.VaccineRecord.CREATED_AT, opdInfluenzaVaccineAdministrationForm.getCreatedAt());
        long rows = sqLiteDatabase.insertWithOnConflict(KipConstants.DbConstants.Tables.OPD_INFLUENZA_VACCINE_RECORD_TABLE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return rows != -1;
    }

    @Override
    public boolean save(OpdInfluenzaVaccineAdministrationForm opdInfluenzaVaccineAdministrationForm) {
        throw new NotImplementedException("not implemented");
    }

    @Override
    public OpdInfluenzaVaccineAdministrationForm findOne(OpdInfluenzaVaccineAdministrationForm opdInfluenzaVaccineAdministrationForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(KipConstants.DbConstants.Tables.OPD_INFLUENZA_VACCINE_RECORD_TABLE
                , columns
                , KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.BASE_ENTITY_ID + " = ? "
                , new String[]{opdInfluenzaVaccineAdministrationForm.getBaseEntityId()}
                , null
                , null
                , OpdDbConstants.Column.OpdCheckIn.CREATED_AT + " DESC "
                , "1");

        if (cursor.getCount() == 0) {
            return null;
        }

        OpdInfluenzaVaccineAdministrationForm influenzaVaccineAdministrationForm = null;
        if (cursor.moveToNext()) {
            influenzaVaccineAdministrationForm = new OpdInfluenzaVaccineAdministrationForm(
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
        return influenzaVaccineAdministrationForm;
    }

    @Override
    public boolean delete(OpdInfluenzaVaccineAdministrationForm opdInfluenzaVaccineAdministrationForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        int rows = sqLiteDatabase.delete(KipConstants.DbConstants.Tables.OPD_INFLUENZA_VACCINE_RECORD_TABLE
                , KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.BASE_ENTITY_ID + " = ? "
                , new String[]{opdInfluenzaVaccineAdministrationForm.getBaseEntityId()});

        return rows > 0;
    }

    @Override
    public List<OpdInfluenzaVaccineAdministrationForm> findAll() {
        throw new NotImplementedException("Not Implemented");
    }

    public OpdInfluenzaVaccineAdministrationForm findOneByVisit(OpdInfluenzaVaccineAdministrationForm opdInfluenzaVaccineAdministrationForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(KipConstants.DbConstants.Tables.OPD_INFLUENZA_VACCINE_RECORD_TABLE
                , columns
                , KipConstants.DbConstants.Columns.CalculateRiskFactor.BASE_ENTITY_ID + " = ? AND " + KipConstants.DbConstants.Columns.CalculateRiskFactor.VISIT_ID + " = ?"
                , new String[]{opdInfluenzaVaccineAdministrationForm.getBaseEntityId(), opdInfluenzaVaccineAdministrationForm.getVisitId()}
                , null
                , null
                , OpdDbConstants.Column.OpdCheckIn.CREATED_AT + " DESC "
                , "1");

        if (cursor.getCount() == 0) {
            return null;
        }

        OpdInfluenzaVaccineAdministrationForm influenzaVaccineAdministrationForm = null;
        if (cursor.moveToNext()) {
            influenzaVaccineAdministrationForm = new OpdInfluenzaVaccineAdministrationForm(
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

        return influenzaVaccineAdministrationForm;
    }
}
