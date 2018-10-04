package org.smartregister.kip.repository;

import android.content.ContentValues;
import android.util.Log;

import com.google.gson.Gson;

import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.domain.db.Column;
import org.smartregister.domain.db.ColumnAttribute;
import org.smartregister.kip.domain.Client;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Repository;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import util.JsonFormUtils;

/**
 * Created by amosl on 8/31/17.
 */

public class KipEventClientRepository extends EventClientRepository {

    private static final String TAG = "KipEventClientRepositry";

    public KipEventClientRepository(Repository repository) {
        super(repository);
    }

    @Override
    public long batchInsertClients(JSONArray array) {
        if (array == null || array.length() == 0) {
            return 0l;
        }

        try {
            long lastServerVersion = 0l;

            getWritableDatabase().beginTransaction();

            for (int i = 0; i < array.length(); i++) {
                Object o = array.get(i);
                if (o instanceof JSONObject) {
                    JSONObject jo = (JSONObject) o;
                    Client c = convert(jo, Client.class);
                    if (c != null) {
                        insert(getWritableDatabase(), c, jo);
                        /*
                        if (c.getServerVersion() > 0l) {
                            lastServerVersion = c.getServerVersion();
                        }
                        */
                    }
                }
            }

            getWritableDatabase().setTransactionSuccessful();
            getWritableDatabase().endTransaction();
            return lastServerVersion;
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return 0l;
        }
    }

    public void insert(SQLiteDatabase db, Client client, JSONObject serverJsonObject) {
        try {
            insert(db,
                    Client.class,
                    Table.client,
                    client_column.values(),
                    client,
                    serverJsonObject);
            for (Address a : client.getAddresses()) {
                insert(db,
                        Address.class,
                        Table.address,
                        address_column.values(),
                        address_column.baseEntityId.name(),
                        client.getBaseEntityId(),
                        a,
                        serverJsonObject);
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "", e);
        }
    }


    @Override
    public <T> T convert(JSONObject jo, Class<T> t) {
        if (jo == null) {
            return null;
        }
        try {
            return JsonFormUtils.gson.fromJson(jo.toString(), t);
        } catch (Exception e) {
            Log.e(getClass().getName(), "", e);
            Log.e(getClass().getName(), "Unable to convert: " + jo.toString());
            return null;
        }
    }

    @Override
    public void insert(SQLiteDatabase db,
                       Class<?> cls,
                       Table table,
                       Column[] cols,
                       Object o,
                       JSONObject serverJsonObject) throws
            IllegalAccessException,
            IllegalArgumentException,
            NoSuchFieldException {
        insert(db, cls, table, cols, null, null, o, serverJsonObject);
    }

    @Override
    public void insert(SQLiteDatabase db,
                       Class<?> cls,
                       Table table,
                       Column[] cols,
                       String referenceColumn,
                       String referenceValue,
                       Object o,
                       JSONObject serverJsonObject) throws
            IllegalAccessException,
            IllegalArgumentException,
            NoSuchFieldException {
        try {
            Map<Column, Object> fm = new HashMap<Column, Object>();
            if (!table.name().equalsIgnoreCase("obs") && !table.name()
                    .equalsIgnoreCase("address")) {
                fm.put(client_column.json, serverJsonObject);
                fm.put(client_column.baseEntityId,
                        serverJsonObject.getString(client_column.baseEntityId.name()));
                fm.put(client_column.syncStatus, BaseRepository.TYPE_Synced);
                fm.put(client_column.updatedAt, new DateTime(new Date().getTime()));
                if (table.name().equalsIgnoreCase("event")) {
                    fm.put(event_column.eventId, serverJsonObject.getString("id"));
                }
            } else {
                return;
            }

            for (Column c : cols) {
                if (c.name().equalsIgnoreCase(referenceColumn)) {
                    continue; // skip reference column as it is already appended
                }
                Field f = null;
                try {
                    f = cls.getDeclaredField(c.name()); // 1st level
                } catch (NoSuchFieldException e) {
                    try {
                        f = cls.getSuperclass().getDeclaredField(c.name()); // 2nd level
                    } catch (NoSuchFieldException e2) {
                        continue;
                    }
                }

                f.setAccessible(true);
                Object v = f.get(o);
                if (c.name().equalsIgnoreCase(event_column.eventId.name())) {
                    fm.put(c, serverJsonObject.getString("id"));
                } else {
                    fm.put(c, v);
                }
            }

            String columns = referenceColumn == null ? "" : ("`" + referenceColumn + "`,");
            String values = referenceColumn == null ? "" : ("'" + referenceValue + "',");
            ContentValues cv = new ContentValues();

            for (Column c : fm.keySet()) {
                columns += "`" + c.name() + "`,";
                values += formatValue(fm.get(c), c.column()) + ",";

                // These Fields should be your String values of actual column names
                cv.put(c.name(), formatValueRemoveSingleQuote(fm.get(c), c.column()));

            }
            String beid = fm.get(client_column.baseEntityId).toString();
            String formSubmissionId = null;
            if (table.name().equalsIgnoreCase("event")) {
                formSubmissionId = fm.get(event_column.formSubmissionId).toString();

            }

            if (table.name().equalsIgnoreCase("client") && checkIfExists(table, beid)) {
                // check if a client exists
                if (cv.containsKey(client_column.baseEntityId.name())) {
                    // this tends to avoid unique constraint exception
                    cv.remove(client_column.baseEntityId.name());
                }
                db.update(table.name(),
                        cv,
                        client_column.baseEntityId.name() + "=?",
                        new String[]{beid});

            } else if (table.name().equalsIgnoreCase("event") && checkIfExistsByFormSubmissionId(
                    table,
                    formSubmissionId)) {
                // check if a event exists
                if (cv.containsKey(event_column.formSubmissionId.name())) {
                    // this tends to avoid unique constraint exception
                    cv.remove(event_column.formSubmissionId.name());
                }
                db.update(table.name(),
                        cv,
                        event_column.formSubmissionId.name() + "=?",
                        new String[]{formSubmissionId});

            } else {
                //for events just insert
                columns = removeEndingComma(columns);
                values = removeEndingComma(values);

                String sql = "INSERT INTO "
                        + table.name()
                        + " ("
                        + columns
                        + ") VALUES ("
                        + values
                        + ")";
                db.execSQL(sql);
            }

        } catch (Exception e) {

            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public String formatValue(Object v, ColumnAttribute c) {
        if (v == null || v.toString().trim().equalsIgnoreCase("")) {
            return null;
        }

        ColumnAttribute.Type type = c.type();
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.text.name())) {
            return "'" + v.toString() + "'";
        }
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.bool.name())) {
            return (Boolean.valueOf(v.toString()) ? 1 : 0) + "";
        }
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.date.name())) {
            return "'" + getSQLDate(v == null ? null : new DateTime(v)) + "'";
        }
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.list.name())) {
            return "'" + new Gson().toJson(v) + "'";
        }
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.map.name())) {
            return "'" + new Gson().toJson(v) + "'";
        }

        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.longnum.name())) {
            return v.toString();
        }
        return null;
    }
}
