package org.smartregister.kip.repository;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.kip.util.KipConstants;
import org.smartregister.repository.BaseRepository;

import java.util.Date;

public class ChildAlertUpdatedRepository extends BaseRepository {

    private static final String CREATE_TABLE_SQL = "CREATE TABLE " + KipConstants.TABLE_NAME.CHILD_UPDATED_ALERTS + "("
            + KipConstants.Columns.RegisterType.BASE_ENTITY_ID + " VARCHAR NOT NULL,"
            + KipConstants.Columns.RegisterType.DATE_CREATED + " INTEGER NOT NULL, "
            + "UNIQUE(" + KipConstants.Columns.RegisterType.BASE_ENTITY_ID + ") ON CONFLICT REPLACE)";

    private static final String INDEX_BASE_ENTITY_ID = "CREATE INDEX " + KipConstants.TABLE_NAME.CHILD_UPDATED_ALERTS
            + "_" + KipConstants.Columns.RegisterType.BASE_ENTITY_ID + "_index ON " + KipConstants.TABLE_NAME.CHILD_UPDATED_ALERTS +
            "(" + KipConstants.Columns.RegisterType.BASE_ENTITY_ID + " COLLATE NOCASE);";

    public static void createTable(@NonNull SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_SQL);
        database.execSQL(INDEX_BASE_ENTITY_ID);
    }


    public boolean findOne(String baseEntityId) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(KipConstants.TABLE_NAME.CHILD_UPDATED_ALERTS, new String[]{KipConstants.Columns.RegisterType.BASE_ENTITY_ID},
                KipConstants.Columns.RegisterType.BASE_ENTITY_ID + "=?",
                new String[]{baseEntityId}, null, null, null, "1");
        return cursor != null && cursor.getCount() > 0;

    }

    public boolean saveOrUpdate(@NonNull String baseEntityId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KipConstants.Columns.RegisterType.BASE_ENTITY_ID, baseEntityId);
        contentValues.put(KipConstants.Columns.RegisterType.DATE_CREATED, new Date().getTime());
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        long rows = sqLiteDatabase.insert(KipConstants.TABLE_NAME.CHILD_UPDATED_ALERTS, null, contentValues);
        return rows != -1;
    }

    public boolean deleteAll() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        int rows = sqLiteDatabase.delete(KipConstants.TABLE_NAME.CHILD_UPDATED_ALERTS
                , null
                , null);
        return rows > 0;
    }
}
