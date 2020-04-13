package org.smartregister.kip.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.text.TextUtils;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.domain.Moh710DailyTally;
import org.smartregister.kip.domain.MohIndicator;
import org.smartregister.repository.BaseRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Moh710DailyTalliesRepository extends BaseRepository {
    private static final String TAG = DailyTalliesRepository.class.getCanonicalName();
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final String TABLE_NAME = "daily_tallies";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_PROVIDER_ID = "provider_id";
    private static final String COLUMN_INDICATOR_ID = "indicator_id";
    private static final String COLUMN_VALUE = "value";
    private static final String COLUMN_DAY = "day";
    private static final String COLUMN_UPDATED_AT = "updated_at";
    private static final String[] TABLE_COLUMNS = {
            COLUMN_ID, COLUMN_INDICATOR_ID, COLUMN_PROVIDER_ID,
            COLUMN_VALUE, COLUMN_DAY, COLUMN_UPDATED_AT
    };
    private static final String CREATE_TABLE_QUERY = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            COLUMN_INDICATOR_ID + " INTEGER NOT NULL," +
            COLUMN_PROVIDER_ID + " VARCHAR NOT NULL," +
            COLUMN_VALUE + " VARCHAR NOT NULL," +
            COLUMN_DAY + " DATETIME NOT NULL," +
            COLUMN_UPDATED_AT + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)";
    private static final String INDEX_PROVIDER_ID = "CREATE INDEX " + TABLE_NAME + "_" + COLUMN_PROVIDER_ID + "_index" +
            " ON " + TABLE_NAME + "(" + COLUMN_PROVIDER_ID + " COLLATE NOCASE);";
    private static final String INDEX_INDICATOR_ID = "CREATE INDEX " + TABLE_NAME + "_" + COLUMN_INDICATOR_ID + "_index" +
            " ON " + TABLE_NAME + "(" + COLUMN_INDICATOR_ID + " COLLATE NOCASE);";
    private static final String INDEX_UPDATED_AT = "CREATE INDEX " + TABLE_NAME + "_" + COLUMN_UPDATED_AT + "_index" +
            " ON " + TABLE_NAME + "(" + COLUMN_UPDATED_AT + ");";
    private static final String INDEX_DAY = "CREATE INDEX " + TABLE_NAME + "_" + COLUMN_DAY + "_index" +
            " ON " + TABLE_NAME + "(" + COLUMN_DAY + ");";
    private static final String INDEX_UNIQUE = "CREATE UNIQUE INDEX " + TABLE_NAME + "_" + COLUMN_INDICATOR_ID + "_" + COLUMN_DAY + "_index" +
            " ON " + TABLE_NAME + "(" + COLUMN_INDICATOR_ID + "," + COLUMN_DAY + ");";
    public static final ArrayList<String> IGNORED_INDICATOR_CODES;

    static {
        IGNORED_INDICATOR_CODES = new ArrayList<>();
    }

    public Moh710DailyTalliesRepository() {
        super();
    }

    protected static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_QUERY);
        database.execSQL(INDEX_PROVIDER_ID);
        database.execSQL(INDEX_INDICATOR_ID);
        database.execSQL(INDEX_UPDATED_AT);
        database.execSQL(INDEX_DAY);
        database.execSQL(INDEX_UNIQUE);
    }

    /**
     * Saves a set of tallies
     *
     * @param day       The day the tallies correspond to
     * @param mohReport Object holding the tallies, the first key in the map holds the indicator
     *                  code, and the second the DHIS id for the indicator. It's expected that
     *                  the inner most map will always hold one value
     */
    public void save(String day, Map<String, Object> mohReport) {
        SQLiteDatabase database = getWritableDatabase();
        try {
            database.beginTransaction();
            String userName = KipApplication.getInstance().context().allSharedPreferences().fetchRegisteredANM();
            for (String indicatorCode : mohReport.keySet()) {
                Integer indicatorValue = (Integer) mohReport.get(indicatorCode);

                // Get the Moh710 Indicator corresponding to the current tally
                MohIndicator indicator = KipApplication.getInstance().moh710IndicatorsRepository().findByIndicatorCode(indicatorCode);

                if (indicator != null) {
                    ContentValues cv = new ContentValues();
                    cv.put(Moh710DailyTalliesRepository.COLUMN_INDICATOR_ID, indicator.getId());
                    cv.put(Moh710DailyTalliesRepository.COLUMN_VALUE, indicatorValue);
                    cv.put(Moh710DailyTalliesRepository.COLUMN_PROVIDER_ID, userName);
                    cv.put(Moh710DailyTalliesRepository.COLUMN_DAY, DAY_FORMAT.parse(day).getTime());
                    cv.put(Moh710DailyTalliesRepository.COLUMN_UPDATED_AT, Calendar.getInstance().getTimeInMillis());

                    database.insertWithOnConflict(TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
                }
            }
            database.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            database.endTransaction();
        }
    }

    /**
     * Returns a list of dates for distinct months with daily tallies
     *
     * @param dateFormat The format to use to format the months' dates
     * @param startDate  The first date to consider. Set argument to null if you
     *                   don't want this enforced
     * @param endDate    The last date to consider. Set argument to null if you
     *                   don't want this enforced
     * @return A list of months that have daily tallies
     */
    public List<String> findAllDistinctMonths(SimpleDateFormat dateFormat, Date startDate, Date endDate) {
        Cursor cursor = null;
        try {
            String selectionArgs = "";
            if (startDate != null) {
                selectionArgs = COLUMN_DAY + " >= " + startDate.getTime();
            }

            if (endDate != null) {
                if (!TextUtils.isEmpty(selectionArgs)) {
                    selectionArgs = selectionArgs + " AND ";
                }

                selectionArgs = selectionArgs + COLUMN_DAY + " <= " + endDate.getTime();
            }

            cursor = getReadableDatabase().query(true, TABLE_NAME,
                    new String[]{COLUMN_DAY},
                    selectionArgs, null, null, null, null, null);

            return getUniqueMonths(dateFormat, cursor);
        } catch (SQLException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return new ArrayList<>();
    }

    /**
     * Returns a list of unique months formatted in the provided {@link SimpleDateFormat}
     *
     * @param dateFormat The date format to format the months
     * @param cursor     Cursor to get the dates from
     * @return
     */
    private List<String> getUniqueMonths(SimpleDateFormat dateFormat, Cursor cursor) {
        List<String> months = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                Date curMonth = new Date(cursor.getLong(0));
                String month = dateFormat.format(curMonth);
                if (!months.contains(month)) {
                    months.add(month);
                }
            }
        }

        return months;
    }

    public Map<Long, List<Moh710DailyTally>> findTalliesInMonth(Date month) {
        Map<Long, List<Moh710DailyTally>> talliesFromMonth = new HashMap<>();
        Cursor cursor = null;
        try {
            HashMap<Long, MohIndicator> indicatorMap = KipApplication.getInstance()
                    .moh710IndicatorsRepository().findAll();

            Calendar startDate = Calendar.getInstance();
            startDate.setTime(month);
            startDate.set(Calendar.DAY_OF_MONTH, 1);
            startDate.set(Calendar.HOUR_OF_DAY, 0);
            startDate.set(Calendar.MINUTE, 0);
            startDate.set(Calendar.SECOND, 0);
            startDate.set(Calendar.MILLISECOND, 0);

            Calendar endDate = Calendar.getInstance();
            endDate.setTime(month);
            endDate.add(Calendar.MONTH, 1);
            endDate.set(Calendar.DAY_OF_MONTH, 1);
            endDate.set(Calendar.HOUR_OF_DAY, 23);
            endDate.set(Calendar.MINUTE, 59);
            endDate.set(Calendar.SECOND, 59);
            endDate.set(Calendar.MILLISECOND, 999);
            endDate.add(Calendar.DATE, -1);

            cursor = getReadableDatabase().query(TABLE_NAME, TABLE_COLUMNS,
                    getDayBetweenDatesSelection(startDate.getTime(), endDate.getTime()),
                    null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    Moh710DailyTally curTally = extractDailyTally(indicatorMap, cursor);
                    if (curTally != null) {
                        if (!talliesFromMonth.containsKey(curTally.getIndicator().getId())) {
                            talliesFromMonth.put(
                                    curTally.getIndicator().getId(),
                                    new ArrayList<Moh710DailyTally>());
                        }

                        talliesFromMonth.get(curTally.getIndicator().getId()).add(curTally);
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return talliesFromMonth;
    }

    public Map<Long, List<Moh710DailyTally>> findTallies(Date startDate, Date endDate) {
        Map<Long, List<Moh710DailyTally>> tallies = new HashMap<>();
        Cursor cursor = null;
        try {
            HashMap<Long, MohIndicator> indicatorMap = KipApplication.getInstance()
                    .moh710IndicatorsRepository().findAll();

            cursor = getReadableDatabase().query(TABLE_NAME, TABLE_COLUMNS,
                    getDayBetweenDatesSelection(startDate, endDate),
                    null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    Moh710DailyTally curTally = extractDailyTally(indicatorMap, cursor);
                    if (curTally != null) {
                        if (!tallies.containsKey(curTally.getIndicator().getId())) {
                            tallies.put(
                                    curTally.getIndicator().getId(),
                                    new ArrayList<Moh710DailyTally>());
                        }

                        tallies.get(curTally.getIndicator().getId()).add(curTally);
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return tallies;
    }

    private String getDayBetweenDatesSelection(Date startDate, Date endDate) {
        return COLUMN_DAY + " >= " + String.valueOf(startDate.getTime()) +
                " AND " + COLUMN_DAY + " <= " + String.valueOf(endDate.getTime());
    }


    private Moh710DailyTally extractDailyTally(HashMap<Long, MohIndicator> indicatorMap, Cursor cursor) {
        long indicatorId = cursor.getLong(cursor.getColumnIndex(COLUMN_INDICATOR_ID));
        if (indicatorMap.containsKey(indicatorId)) {
            MohIndicator indicator = indicatorMap.get(indicatorId);
            if (!IGNORED_INDICATOR_CODES.contains(indicator.getIndicatorCode())) {
                Moh710DailyTally curTally = new Moh710DailyTally();
                curTally.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                curTally.setProviderId(
                        cursor.getString(cursor.getColumnIndex(COLUMN_PROVIDER_ID)));
                curTally.setIndicator(indicator);
                curTally.setValue(cursor.getString(cursor.getColumnIndex(COLUMN_VALUE)));
                curTally.setDay(
                        new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_DAY))));
                curTally.setUpdatedAt(
                        new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_UPDATED_AT)))
                );

                return curTally;
            }
        }

        return null;
    }

    private Long checkIfExists(long indicatorId, String day) {
        Cursor mCursor = null;
        try {
            String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_NAME +
                    " WHERE " + COLUMN_INDICATOR_ID + " = " + String.valueOf(indicatorId) + " COLLATE NOCASE "
                    + " AND " + COLUMN_DAY + "='" + day + "'";
            mCursor = getWritableDatabase().rawQuery(query, null);
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
}
