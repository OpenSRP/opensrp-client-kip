package org.smartregister.kip.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.kip.domain.MohIndicator;
import org.smartregister.repository.BaseRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Moh710IndicatorsRepository extends BaseRepository {
    private static final String TAG = Moh710IndicatorsRepository.class.getCanonicalName();
    public static final String INDICATORS_CSV_FILE = "KIP_MOH_710_Report.csv";
    private static final String MOH_INDICATORS_SQL = "CREATE TABLE moh_indicators (_id INTEGER NOT NULL,provider_id VARCHAR,indicator_code VARCHAR NOT NULL,antigen VARCHAR,age VARCHAR,created_at DATETIME NULL,updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP)";
    private static final String MOH_INDICATORS_TABLE_NAME = "moh_indicators";
    private static final String ID_COLUMN = "_id";
    private static final String PROVIDER_ID = "provider_id";
    private static final String INDICATOR_CODE = "indicator_code";
    private static final String ANTIGEN = "antigen";
    private static final String CATEGORY = "category";
    private static final String AGE = "age";
    private static final String CREATED_AT_COLUMN = "created_at";
    private static final String UPDATED_AT_COLUMN = "updated_at";

    private static final String[] MOH_TABLE_COLUMNS = {ID_COLUMN, PROVIDER_ID, INDICATOR_CODE, ANTIGEN, AGE, CREATED_AT_COLUMN, UPDATED_AT_COLUMN};
    public static final Map<Integer, String> CSV_COLUMN_MAPPING;

    private static final String PROVIDER_ID_INDEX = "CREATE INDEX " + MOH_INDICATORS_TABLE_NAME + "_" + PROVIDER_ID + "_index ON " + MOH_INDICATORS_TABLE_NAME + "(" + PROVIDER_ID + " COLLATE NOCASE);";
    private static final String KEY_INDEX = "CREATE INDEX " + MOH_INDICATORS_TABLE_NAME + "_" + INDICATOR_CODE + "_index ON " + MOH_INDICATORS_TABLE_NAME + "(" + INDICATOR_CODE + " COLLATE NOCASE);";
    private static final String ANTIGEN_INDEX = "CREATE INDEX " + MOH_INDICATORS_TABLE_NAME + "_" + ANTIGEN + "_index ON " + MOH_INDICATORS_TABLE_NAME + "(" + ANTIGEN + " COLLATE NOCASE);";
    private static final String AGE_INDEX = "CREATE INDEX " + MOH_INDICATORS_TABLE_NAME + "_" + AGE + "_index ON " + MOH_INDICATORS_TABLE_NAME + "(" + AGE + " COLLATE NOCASE);";
    private static final String UPDATED_AT_INDEX = "CREATE INDEX " + MOH_INDICATORS_TABLE_NAME + "_" + UPDATED_AT_COLUMN + "_index ON " + MOH_INDICATORS_TABLE_NAME + "(" + UPDATED_AT_COLUMN + ");";

    static {
        CSV_COLUMN_MAPPING = new HashMap<>();
        CSV_COLUMN_MAPPING.put(0, Moh710IndicatorsRepository.ID_COLUMN);
        CSV_COLUMN_MAPPING.put(1, Moh710IndicatorsRepository.INDICATOR_CODE);
        CSV_COLUMN_MAPPING.put(2, Moh710IndicatorsRepository.ANTIGEN);
        CSV_COLUMN_MAPPING.put(3, Moh710IndicatorsRepository.AGE);
    }

    public Moh710IndicatorsRepository(KipRepository kipRepository) {
        super(kipRepository);

    }

    protected static void createTable(SQLiteDatabase database) {
        database.execSQL(MOH_INDICATORS_SQL);
        database.execSQL(PROVIDER_ID_INDEX);
        database.execSQL(KEY_INDEX);
        database.execSQL(ANTIGEN_INDEX);
        database.execSQL(AGE_INDEX);
        database.execSQL(UPDATED_AT_INDEX);
    }


    public void save(SQLiteDatabase database, List<Map<String, String>> hia2Indicators) {
        try {

            String previousAntigen = null;
            database.beginTransaction();
            for (Map<String, String> hia2Indicator : hia2Indicators) {
                ContentValues cv = new ContentValues();
                for (String column : hia2Indicator.keySet()) {

                    if (column.equals(CATEGORY)) {
                        continue;
                    }

                    String value = hia2Indicator.get(column);
                    if (column.equals(ANTIGEN)) {
                        if (StringUtils.isNotBlank(value)) {
                            previousAntigen = value;
                        } else if (StringUtils.isNotBlank(previousAntigen)) {
                            value = previousAntigen;
                        }
                    }

                    cv.put(column, value);
                }

                Long id = checkIfExists(database, cv.getAsString(INDICATOR_CODE));

                if (id != null) {
                    database.update(MOH_INDICATORS_TABLE_NAME, cv, ID_COLUMN + " = ?", new String[]{id.toString()});

                } else {
                    database.insert(MOH_INDICATORS_TABLE_NAME, null, cv);
                }
            }
            database.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            database.endTransaction();
        }
    }

    private Long checkIfExists(SQLiteDatabase db, String indicatorCode) {
        Cursor mCursor = null;
        try {
            String query = "SELECT " + ID_COLUMN + " FROM " + MOH_INDICATORS_TABLE_NAME + " WHERE " + INDICATOR_CODE + " = '" + indicatorCode + "' COLLATE NOCASE ";
            mCursor = db.rawQuery(query, null);
            if (mCursor != null && mCursor.moveToFirst()) {

                return mCursor.getLong(0);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        } finally {
            if (mCursor != null) mCursor.close();
        }
        return null;
    }


    public MohIndicator findByIndicatorCode(String indicatorCode) {
        Cursor cursor = null;

        try {
            cursor = getReadableDatabase().query(MOH_INDICATORS_TABLE_NAME, MOH_TABLE_COLUMNS, INDICATOR_CODE + " = ? COLLATE NOCASE ", new String[]{indicatorCode}, null, null, null, null);
            List<MohIndicator> mohIndicators = readAllDataElements(cursor);
            if (!mohIndicators.isEmpty()) {
                return mohIndicators.get(0);
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    public List<String> fetchDistinctAntigens() {
        String sql = " SELECT DISTINCT " + ANTIGEN + " FROM " + MOH_INDICATORS_TABLE_NAME + " ORDER BY " + UPDATED_AT_COLUMN;
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(sql, null);

        List<String> antigens = new ArrayList<String>();

        try {

            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    String antigen = cursor.getString(cursor.getColumnIndex(ANTIGEN));
                    antigens.add(antigen);

                    cursor.moveToNext();

                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return antigens;
    }

    public List<MohIndicator> findByAntigen(String antigen) {
        Cursor cursor = null;

        try {
            cursor = getReadableDatabase().query(MOH_INDICATORS_TABLE_NAME, MOH_TABLE_COLUMNS, ANTIGEN + " = ? COLLATE NOCASE ", new String[]{antigen}, null, null, null, null);
            return readAllDataElements(cursor);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }


    private List<MohIndicator> readAllDataElements(Cursor cursor) {
        List<MohIndicator> mohIndicators = new ArrayList<>();
        try {
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    MohIndicator mohIndicator = new MohIndicator();
                    mohIndicator.setId(cursor.getLong(cursor.getColumnIndex(ID_COLUMN)));
                    mohIndicator.setAntigen(cursor.getString(cursor.getColumnIndex(ANTIGEN)));
                    mohIndicator.setAge(cursor.getString(cursor.getColumnIndex(AGE)));
                    mohIndicator.setIndicatorCode(cursor.getString(cursor.getColumnIndex(INDICATOR_CODE)));
                    mohIndicator.setCreatedAt(new Date(cursor.getLong(cursor.getColumnIndex(CREATED_AT_COLUMN))));
                    mohIndicator.setUpdatedAt(new Date(Timestamp.valueOf(cursor.getString(cursor.getColumnIndex(UPDATED_AT_COLUMN))).getTime()));
                    mohIndicators.add(mohIndicator);

                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            cursor.close();
        }

        return mohIndicators;

    }
}
