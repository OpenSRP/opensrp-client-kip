package org.smartregister.kip.repository;


import android.content.ContentValues;
import android.support.annotation.NonNull;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.NotImplementedException;
import org.smartregister.kip.dao.Covid19OpdWaitingListFormDao;
import org.smartregister.kip.pojo.OpdCovid19WaitingListForm;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.repository.BaseRepository;

import java.util.List;


public class OpdCovid19WaitingListRepository extends BaseRepository implements Covid19OpdWaitingListFormDao {
    private static final String INDEX_BASE_ENTITY_ID = "CREATE INDEX " + KipConstants.DbConstants.Tables.OPD_COVID_19_WAITING_LIST_TABLE
            + "_" + KipConstants.DbConstants.Columns.WaitingList.BASE_ENTITY_ID + "_index ON " + KipConstants.DbConstants.Tables.OPD_COVID_19_WAITING_LIST_TABLE +
            "(" + KipConstants.DbConstants.Columns.WaitingList.BASE_ENTITY_ID + " COLLATE NOCASE);";

    private String[] columns = new String[]{
            KipConstants.DbConstants.Columns.WaitingList.ID,
            KipConstants.DbConstants.Columns.WaitingList.BASE_ENTITY_ID,
            KipConstants.DbConstants.Columns.WaitingList.VISIT_ID,
            KipConstants.DbConstants.Columns.WaitingList.WAITING_LIST,
            KipConstants.DbConstants.Columns.WaitingList.AGE,
            KipConstants.DbConstants.Columns.WaitingList.DATE,
            KipConstants.DbConstants.Columns.WaitingList.CREATED_AT};

    public static void updateIndex(@NonNull SQLiteDatabase database) {
        database.execSQL(INDEX_BASE_ENTITY_ID);
    }

    @Override
    public boolean saveOrUpdate(OpdCovid19WaitingListForm OpdCovid19WaitingListForm) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KipConstants.DbConstants.Columns.WaitingList.VISIT_ID, OpdCovid19WaitingListForm.getVisitId());
        contentValues.put(KipConstants.DbConstants.Columns.WaitingList.BASE_ENTITY_ID, OpdCovid19WaitingListForm.getBaseEntityId());
        contentValues.put(KipConstants.DbConstants.Columns.WaitingList.ID, OpdCovid19WaitingListForm.getId());
        contentValues.put(KipConstants.DbConstants.Columns.WaitingList.WAITING_LIST, OpdCovid19WaitingListForm.getWaitingList());
        contentValues.put(KipConstants.DbConstants.Columns.WaitingList.AGE, OpdCovid19WaitingListForm.getAge());
        contentValues.put(KipConstants.DbConstants.Columns.WaitingList.DATE, OpdCovid19WaitingListForm.getDate());
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        contentValues.put(KipConstants.DbConstants.Columns.WaitingList.CREATED_AT, OpdCovid19WaitingListForm.getCreatedAt());
        long rows = sqLiteDatabase.insertWithOnConflict(KipConstants.DbConstants.Tables.OPD_COVID_19_WAITING_LIST_TABLE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return rows != -1;
    }

    @Override
    public boolean save(OpdCovid19WaitingListForm OpdCovid19WaitingListForm) {
        throw new NotImplementedException("not implemented");
    }

    @Override
    public OpdCovid19WaitingListForm findOne(OpdCovid19WaitingListForm OpdCovid19WaitingListForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(KipConstants.DbConstants.Tables.OPD_COVID_19_WAITING_LIST_TABLE
                , columns
                , KipConstants.DbConstants.Columns.WaitingList.BASE_ENTITY_ID + " = ? "
                , new String[]{OpdCovid19WaitingListForm.getBaseEntityId()}
                , null
                , null
                , null);

        if (cursor.getCount() == 0) {
            return null;
        }

        org.smartregister.kip.pojo.OpdCovid19WaitingListForm WaitingListForm = null;
        if (cursor.moveToNext()) {
            WaitingListForm = new OpdCovid19WaitingListForm(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6));
        }
        cursor.close();
        return WaitingListForm;
    }

    @Override
    public boolean delete(OpdCovid19WaitingListForm OpdCovid19WaitingListForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        int rows = sqLiteDatabase.delete(KipConstants.DbConstants.Tables.OPD_COVID_19_WAITING_LIST_TABLE
                , KipConstants.DbConstants.Columns.WaitingList.BASE_ENTITY_ID + " = ? "
                , new String[]{OpdCovid19WaitingListForm.getBaseEntityId()});

        return rows > 0;
    }

    @Override
    public List<OpdCovid19WaitingListForm> findAll() {
        throw new NotImplementedException("Not Implemented");
    }
}
