package org.smartregister.kip.repository;

import android.content.ContentValues;
import androidx.annotation.NonNull;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.NotImplementedException;
import org.smartregister.kip.dao.OpdSMSReminderFormDao;
import org.smartregister.kip.pojo.OpdSMSReminderForm;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.repository.BaseRepository;

import java.util.List;

public class OpdSMSReminderFormRepository extends BaseRepository implements OpdSMSReminderFormDao {

    private static final String INDEX_BASE_ENTITY_ID = "CREATE INDEX " + KipConstants.DbConstants.Tables.OPD_SMS_REMINDER
            + "_" + KipConstants.DbConstants.Columns.SmsReminder.BASE_ENTITY_ID + "_index ON " + KipConstants.DbConstants.Tables.OPD_SMS_REMINDER +
            "(" + KipConstants.DbConstants.Columns.SmsReminder.BASE_ENTITY_ID + " COLLATE NOCASE);";

    private String[] columns = new String[]{
            KipConstants.DbConstants.Columns.SmsReminder.ID,
            KipConstants.DbConstants.Columns.SmsReminder.BASE_ENTITY_ID,
            KipConstants.DbConstants.Columns.SmsReminder.VISIT_ID,
            KipConstants.DbConstants.Columns.SmsReminder.SMS_REMINDER,
            KipConstants.DbConstants.Columns.SmsReminder.DATE,
            KipConstants.DbConstants.Columns.SmsReminder.CREATED_AT
    };

    public static void updateIndex(@NonNull SQLiteDatabase database) {
        database.execSQL(INDEX_BASE_ENTITY_ID);
    }

    @Override
    public boolean saveOrUpdate(OpdSMSReminderForm opdSmsReminderForm) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KipConstants.DbConstants.Columns.SmsReminder.VISIT_ID, opdSmsReminderForm.getVisitId());
        contentValues.put(KipConstants.DbConstants.Columns.SmsReminder.ID, opdSmsReminderForm.getId());
        contentValues.put(KipConstants.DbConstants.Columns.SmsReminder.BASE_ENTITY_ID, opdSmsReminderForm.getBaseEntityId());
        contentValues.put(KipConstants.DbConstants.Columns.SmsReminder.SMS_REMINDER, opdSmsReminderForm.getSmsReminder());
        contentValues.put(KipConstants.DbConstants.Columns.SmsReminder.DATE, opdSmsReminderForm.getDate());
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        contentValues.put(KipConstants.DbConstants.Columns.SmsReminder.CREATED_AT, opdSmsReminderForm.getCreatedAt());
        long rows = sqLiteDatabase.insertWithOnConflict(KipConstants.DbConstants.Tables.OPD_SMS_REMINDER, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return rows != -1;
    }

    @Override
    public boolean save(OpdSMSReminderForm opdSmsReminderForm) {
        throw new NotImplementedException("not implemented");
    }

    @Override
    public OpdSMSReminderForm findOne(OpdSMSReminderForm opdSmsReminderForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(KipConstants.DbConstants.Tables.OPD_SMS_REMINDER
                , columns
                , KipConstants.DbConstants.Columns.SmsReminder.BASE_ENTITY_ID + " = ? "
                , new String[]{opdSmsReminderForm.getBaseEntityId(), opdSmsReminderForm.getVisitId()}
                , null
                , null
                , OpdDbConstants.Column.OpdCheckIn.CREATED_AT + " DESC "
                , "1");

        if (cursor.getCount() == 0) {
            return null;
        }

        OpdSMSReminderForm opdSMSReminderForm = null;
        if (cursor.moveToNext()) {
            opdSMSReminderForm = new OpdSMSReminderForm(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5));
            cursor.close();
        }

        return opdSMSReminderForm;
    }

    @Override
    public boolean delete(OpdSMSReminderForm opdSmsReminderForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        int rows = sqLiteDatabase.delete(KipConstants.DbConstants.Tables.OPD_SMS_REMINDER
                , KipConstants.DbConstants.Columns.SmsReminder.BASE_ENTITY_ID + " = ? "
                , new String[]{opdSmsReminderForm.getBaseEntityId()});

        return rows > 0;
    }

    @Override
    public List<OpdSMSReminderForm> findAll() {
        throw new NotImplementedException("not implemented");
    }

    public OpdSMSReminderForm findOneByVisit(OpdSMSReminderForm opdSmsReminderForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(KipConstants.DbConstants.Tables.OPD_SMS_REMINDER
                , columns
                , KipConstants.DbConstants.Columns.CalculateRiskFactor.BASE_ENTITY_ID + " = ? AND " + KipConstants.DbConstants.Columns.CalculateRiskFactor.VISIT_ID + " = ?"
                , new String[]{opdSmsReminderForm.getBaseEntityId(), opdSmsReminderForm.getVisitId()}
                , null
                , null
                , OpdDbConstants.Column.OpdCheckIn.CREATED_AT + " DESC "
                , "1");

        if (cursor.getCount() == 0) {
            return null;
        }

        OpdSMSReminderForm opdSMSReminderForm = null;
        if (cursor.moveToNext()) {
            opdSMSReminderForm = new OpdSMSReminderForm(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5));
            cursor.close();
        }

        return opdSMSReminderForm;
    }
}
