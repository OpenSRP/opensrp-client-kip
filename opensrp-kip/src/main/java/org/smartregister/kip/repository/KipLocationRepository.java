package org.smartregister.kip.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.support.annotation.Nullable;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.opensrp.api.domain.Location;
import org.smartregister.kip.domain.KipLocation;
import org.smartregister.kip.util.KipChildUtils;
import org.smartregister.repository.BaseRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public class KipLocationRepository extends BaseRepository {
    public static final String LOCATIONS_CSV_FILE = "OpenMrsLocations.csv";
    private static final String TAG = KipLocationRepository.class.getCanonicalName();
    private static final String LOCATIONS_SQL = "CREATE TABLE locations(_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, uuid VARCHAR NOT NULL UNIQUE, name VARCHAR NOT NULL UNIQUE, tag VARCHAR NOT NULL, parent_uuid VARCHAR NULL)";
    public static final String LOCATIONS_TABLE_NAME = "locations";
    public static final String ID_COLUMN = "_id";
    public static final String UUID_COLUMN = "uuid";
    public static final String NAME_COLUMN = "name";
    public static final String TAG_COLUMN = "tag";
    public static final String PARENT_UUID_COLUMN = "parent_uuid";
    public static final String[] LOCATIONS_TABLE_COLUMNS = {ID_COLUMN, UUID_COLUMN, NAME_COLUMN, TAG_COLUMN, PARENT_UUID_COLUMN};

    public KipLocationRepository() {
        super();
    }



    protected static void createLocationsTable(SQLiteDatabase database) {
        database.execSQL(LOCATIONS_SQL);
        Timber.d("--> createLocationsTable: %s", LOCATIONS_SQL);
    }

    public static final Map<Integer, String> CSV_COLUMN_MAPPING;

    static {
        CSV_COLUMN_MAPPING = new HashMap<>();
        CSV_COLUMN_MAPPING.put(0, KipLocationRepository.ID_COLUMN);
        CSV_COLUMN_MAPPING.put(1, KipLocationRepository.NAME_COLUMN);
        CSV_COLUMN_MAPPING.put(2, KipLocationRepository.PARENT_UUID_COLUMN);
        CSV_COLUMN_MAPPING.put(3, KipLocationRepository.TAG_COLUMN);
        CSV_COLUMN_MAPPING.put(4, KipLocationRepository.UUID_COLUMN);
    }

    public void save(@Nullable SQLiteDatabase database, @Nullable List<Map<String, String>> hia2Indicators) {
        if (database != null && hia2Indicators != null && !hia2Indicators.isEmpty()) {
            try {
                database.beginTransaction();
                for (Map<String, String> hia2Indicator : hia2Indicators) {
                    ContentValues cv = new ContentValues();

                    for (String column : hia2Indicator.keySet()) {

                        String value = hia2Indicator.get(column);
                        cv.put(column, value);

                    }
                    Long id = checkIfExists(database, cv.getAsString(UUID_COLUMN));

                    if (id != null) {
                        database.update(LOCATIONS_TABLE_NAME, cv, ID_COLUMN + " = ?", new String[]{id.toString()});

                    } else {
                        database.insert(LOCATIONS_TABLE_NAME, null, cv);
                    }
                }
                database.setTransactionSuccessful();
            } catch (SQLException e) {
                Timber.e(e);
            } finally {
                database.endTransaction();
            }
        }
    }

    private Long checkIfExists(SQLiteDatabase db, String indicatorCode) {
        Cursor mCursor = null;
        Long exists = null;

        try {
            String query = "SELECT " + ID_COLUMN + " FROM " + LOCATIONS_TABLE_NAME + " WHERE " + UUID_COLUMN + " = '" + indicatorCode + "' COLLATE NOCASE ";
            mCursor = db.rawQuery(query, null);
            if (mCursor != null && mCursor.moveToFirst()) {

                exists = mCursor.getLong(0);
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (mCursor != null) mCursor.close();
        }
        return exists;
    }

    public void bulkSave(){
        SQLiteDatabase database = getWritableDatabase();
        KipChildUtils.saveOpenMrsLocation(database);
    }

    /**
     * inserts ids in bulk to the db in a transaction since normally, each time db.insert() is used, SQLite creates a transaction (and resulting journal file in the filesystem), which slows things down.
     *
     * @param locations
     */

    public void bulkInsertLocations(List<KipLocation> locations) throws JSONException {
        SQLiteDatabase database = getWritableDatabase();

        try {

            database.beginTransaction();
            for (Location l : locations) {
                ContentValues values = new ContentValues();

                values.put(UUID_COLUMN, l.getLocationId());
                values.put(NAME_COLUMN, l.getName());

                Set<String> tags = l.getTags();
                if(tags != null && tags.size() > 0){
                    String tagz = "";
                    for(String s : tags){
                        tagz += s + ":";
                    }
                    values.put(TAG_COLUMN, StringUtils.removeEnd(tagz, ":"));
                }

                Location parent = l.getParentLocation();
                if(parent != null){
                    values.put(PARENT_UUID_COLUMN, parent.getLocationId());
                }

                database.insertWithOnConflict(LOCATIONS_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                Timber.d("--> bulkInsertLocations String: %s", values);
            }
            database.setTransactionSuccessful();
        } catch (SQLException e) {
            Timber.e(e, "--> bulkInsertLocations");
        } finally {
            database.endTransaction();
        }
    }

    public KipLocation getLocationByName(String name) {
        KipLocation location = null;
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(LOCATIONS_TABLE_NAME, LOCATIONS_TABLE_COLUMNS, NAME_COLUMN + " = ?", new String[]{name}, null, null, " 1 ASC", "1");
            List<KipLocation> locations = readAll(cursor);
            location = locations.isEmpty() ? null : locations.get(0);
        } catch (Exception e) {
            Timber.e(e, "--> getLocationByName");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return location;
    }

    public Location getLocationWithNoParent(){
        return null;
    }

    public List<KipLocation> getChildLocations(String parentUuid) {
        List<KipLocation> locations = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(LOCATIONS_TABLE_NAME, LOCATIONS_TABLE_COLUMNS, PARENT_UUID_COLUMN + " = ?", new String[]{parentUuid}, null, null, " 1 ASC");
            locations = readAll(cursor);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Timber.d("-->ChildLocations list %s: ", locations.toString());
        return locations;
    }

    public List<KipLocation> getLocationsByTag(String tag) {
        List<KipLocation> locations = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(LOCATIONS_TABLE_NAME, LOCATIONS_TABLE_COLUMNS, TAG_COLUMN + " = ?", new String[]{tag}, null, null, " 1 ASC")) {
            locations = readAll(cursor);
        } catch (Exception e) {
            Timber.e(e, " --> getLocationsByTag");
        }
        return locations;
    }

    private List<KipLocation> readAll(Cursor cursor) {
        List<KipLocation> locations = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            cursor.moveToFirst();
            while (cursor.getCount() > 0 && !cursor.isAfterLast()) {
                locations.add(getLocation(cursor));
                cursor.moveToNext();
            }
        }
        return locations;
    }

    @NotNull
    private KipLocation getLocation(Cursor cursor) {
        KipLocation location = new KipLocation();
        location.setLocationId(cursor.getString(cursor.getColumnIndex(ID_COLUMN)));
        location.setName(cursor.getString(cursor.getColumnIndex(NAME_COLUMN)));
        location.setTags(getTags(cursor));
        location.setUuid(cursor.getString(cursor.getColumnIndex(UUID_COLUMN)));
        location.setParentUuid(cursor.getString(cursor.getColumnIndex(PARENT_UUID_COLUMN)));
        return location;
    }

    @NotNull
    private Set<String> getTags(Cursor cursor) {
        String tagString = cursor.getString(cursor.getColumnIndex(TAG_COLUMN));
        String[] tagsArray = tagString.split(":");
        return new HashSet<>(Arrays.asList(tagsArray));
    }
}
