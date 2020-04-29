package org.smartregister.kip.repository;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.kip.util.KipConstants;
import org.smartregister.repository.BaseRepository;

import java.util.Date;
import java.util.List;


public class ClientRegisterTypeRepository extends BaseRepository implements ClientRegisterTypeDao {

    private static final String CREATE_TABLE_SQL = "CREATE TABLE " + KipConstants.TABLE_NAME.REGISTER_TYPE + "("
            + KipConstants.Columns.RegisterType.BASE_ENTITY_ID + " VARCHAR NOT NULL,"
            + KipConstants.Columns.RegisterType.REGISTER_TYPE + " VARCHAR NOT NULL, "
            + KipConstants.Columns.RegisterType.DATE_CREATED + " INTEGER NOT NULL, "
            + KipConstants.Columns.RegisterType.DATE_REMOVED + " INTEGER NULL, "
            + "UNIQUE(" + KipConstants.Columns.RegisterType.BASE_ENTITY_ID + ", " + KipConstants.Columns.RegisterType.REGISTER_TYPE + ") ON CONFLICT REPLACE)";

    private static final String INDEX_BASE_ENTITY_ID = "CREATE INDEX " + KipConstants.TABLE_NAME.REGISTER_TYPE
            + "_" + KipConstants.Columns.RegisterType.BASE_ENTITY_ID + "_index ON " + KipConstants.TABLE_NAME.REGISTER_TYPE +
            "(" + KipConstants.Columns.RegisterType.BASE_ENTITY_ID + " COLLATE NOCASE);";

    public static void createTable(@NonNull SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_SQL);
        database.execSQL(INDEX_BASE_ENTITY_ID);
    }

    @Override
    public List<ClientRegisterType> findAll(String baseEntityId) {
        return null;
    }

    @Override
    public boolean remove(String registerType, String baseEntityId) {
        return false;
    }

    @Override
    public boolean removeAll(String baseEntityId) {
        return false;
    }

    @Override
    public boolean add(String registerType, String baseEntityId) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KipConstants.Columns.RegisterType.BASE_ENTITY_ID, baseEntityId);
        contentValues.put(KipConstants.Columns.RegisterType.REGISTER_TYPE, registerType);
        contentValues.put(KipConstants.Columns.RegisterType.DATE_CREATED, new Date().getTime());
        long result = database.insert(KipConstants.TABLE_NAME.REGISTER_TYPE, null, contentValues);
        return result != -1;
    }
}
