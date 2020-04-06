package org.smartregister.kip.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.domain.DailyTally;
import org.smartregister.kip.domain.MohIndicator;
import org.smartregister.reporting.util.Constants;
import org.smartregister.repository.BaseRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-07-11
 */

public class DailyTalliesRepository extends BaseRepository {

    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    private static final String TABLE_NAME = Constants.DailyIndicatorCountRepository.INDICATOR_DAILY_TALLY_TABLE;
    private static final String COLUMN_DAY = Constants.DailyIndicatorCountRepository.DAY;

    private static final String MOH710_TABLE_NAME = "daily_tallies";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_PROVIDER_ID = "provider_id";
    private static final String COLUMN_INDICATOR_ID = "indicator_id";
    private static final String COLUMN_VALUE = "value";
    private static final String COLUMN_UPDATED_AT = "updated_at";
    private static final String[] TABLE_COLUMNS = {
            COLUMN_ID, COLUMN_INDICATOR_ID, COLUMN_PROVIDER_ID,
            COLUMN_VALUE, COLUMN_DAY, COLUMN_UPDATED_AT
    };


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
        return findAllDistinctMonths(dateFormat, startDate, endDate, null);
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
    public List<String> findAllDistinctMonths(SimpleDateFormat dateFormat, Date startDate, Date endDate, @Nullable String grouping) {
        Cursor cursor = null;
        List<String> months = new ArrayList<>();

        try {
            String selectionArgs = "";
            if (startDate != null) {
                selectionArgs = COLUMN_DAY + " >= '" + DAY_FORMAT.format(startDate) + "'";
            }

            if (endDate != null) {
                if (!TextUtils.isEmpty(selectionArgs)) {
                    selectionArgs = selectionArgs + " AND ";
                }

                selectionArgs = selectionArgs + COLUMN_DAY + " <= '" + DAY_FORMAT.format(endDate) +"'";
            }

            selectionArgs += " AND " + Constants.DailyIndicatorCountRepository.INDICATOR_GROUPING + (grouping == null ? " IS NULL" : " = '" + grouping + "'");

            cursor = getReadableDatabase().query(true, TABLE_NAME,
                    new String[]{COLUMN_DAY},
                    selectionArgs, null, null, null, null, null);

            months = getUniqueMonths(dateFormat, cursor);
        } catch (SQLException | ParseException e) {
            Timber.e(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return months;
    }

    /**
     * Returns a list of unique months formatted in the provided {@link SimpleDateFormat}
     *
     * @param dateFormat The date format to format the months
     * @param cursor     Cursor to get the dates from
     * @return
     */
    private List<String> getUniqueMonths(SimpleDateFormat dateFormat, Cursor cursor) throws ParseException {
        List<String> months = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                Date curMonth = DAY_FORMAT.parse((cursor.getString(0)));
                String month = dateFormat.format(curMonth);
                if (!months.contains(month)) {
                    months.add(month);
                }
            }
        }

        return months;
    }

    /**
     * Daily tally for MOH 710 Report
     */

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE " + MOH710_TABLE_NAME + "(" +
            COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            COLUMN_INDICATOR_ID + " INTEGER NOT NULL," +
            COLUMN_PROVIDER_ID + " VARCHAR NOT NULL," +
            COLUMN_VALUE + " VARCHAR NOT NULL," +
            COLUMN_DAY + " DATETIME NOT NULL," +
            COLUMN_UPDATED_AT + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)";
    private static final String INDEX_PROVIDER_ID = "CREATE INDEX " + MOH710_TABLE_NAME + "_" + COLUMN_PROVIDER_ID + "_index" +
            " ON " + MOH710_TABLE_NAME + "(" + COLUMN_PROVIDER_ID + " COLLATE NOCASE);";
    private static final String INDEX_INDICATOR_ID = "CREATE INDEX " + MOH710_TABLE_NAME + "_" + COLUMN_INDICATOR_ID + "_index" +
            " ON " + MOH710_TABLE_NAME + "(" + COLUMN_INDICATOR_ID + " COLLATE NOCASE);";
    private static final String INDEX_UPDATED_AT = "CREATE INDEX " + MOH710_TABLE_NAME + "_" + COLUMN_UPDATED_AT + "_index" +
            " ON " + MOH710_TABLE_NAME + "(" + COLUMN_UPDATED_AT + ");";
    private static final String INDEX_DAY = "CREATE INDEX " + MOH710_TABLE_NAME + "_" + COLUMN_DAY + "_index" +
            " ON " + MOH710_TABLE_NAME + "(" + COLUMN_DAY + ");";
    private static final String INDEX_UNIQUE = "CREATE UNIQUE INDEX " + MOH710_TABLE_NAME + "_" + COLUMN_INDICATOR_ID + "_" + COLUMN_DAY + "_index" +
            " ON " + MOH710_TABLE_NAME + "(" + COLUMN_INDICATOR_ID + "," + COLUMN_DAY + ");";
    public static final ArrayList<String> IGNORED_INDICATOR_CODES;

    static {
        IGNORED_INDICATOR_CODES = new ArrayList<>();
    }

    protected static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_QUERY);
        database.execSQL(INDEX_PROVIDER_ID);
        database.execSQL(INDEX_INDICATOR_ID);
        database.execSQL(INDEX_UPDATED_AT);
        database.execSQL(INDEX_DAY);
        database.execSQL(INDEX_UNIQUE);
    }

    public List<String> findAllDistinctMOH710Months(SimpleDateFormat dateFormat, Date startDate, Date endDate) {
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
            Timber.e(e,"--> findAllDistinctMOH710Months");
        } catch (ParseException e) {
            Timber.e(e,"--> findAllDistinctMOH710Months");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return new ArrayList<>();
    }

    private String getDayBetweenDatesSelection(Date startDate, Date endDate) {
        return COLUMN_DAY + " >= " + String.valueOf(startDate.getTime()) +
                " AND " + COLUMN_DAY + " <= " + String.valueOf(endDate.getTime());
    }

    private DailyTally extractDailyTally(HashMap<Long, MohIndicator> indicatorMap, Cursor cursor) {
        long indicatorId = cursor.getLong(cursor.getColumnIndex(COLUMN_INDICATOR_ID));
        if (indicatorMap.containsKey(indicatorId)) {
            MohIndicator indicator = indicatorMap.get(indicatorId);
            if (!IGNORED_INDICATOR_CODES.contains(indicator.getIndicatorCode())) {
                DailyTally curTally = new DailyTally();
                curTally.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                curTally.setProviderId(
                        cursor.getString(cursor.getColumnIndex(COLUMN_PROVIDER_ID)));
                curTally.setMohIndicator(indicator);
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

    public Map<Long, List<DailyTally>> findTalliesInMonth(Date month) {
        Map<Long, List<DailyTally>> talliesFromMonth = new HashMap<>();
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

            cursor = getReadableDatabase().query(MOH710_TABLE_NAME, TABLE_COLUMNS,
                    getDayBetweenDatesSelection(startDate.getTime(), endDate.getTime()),
                    null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    DailyTally curTally = extractDailyTally(indicatorMap, cursor);
                    if (curTally != null) {
                        if (!talliesFromMonth.containsKey(curTally.getMohIndicator().getId())) {
                            talliesFromMonth.put(
                                    curTally.getMohIndicator().getId(),
                                    new ArrayList<DailyTally>());
                        }

                        talliesFromMonth.get(curTally.getMohIndicator().getId()).add(curTally);
                    }
                }
            }
        } catch (SQLException e) {
            Timber.e(e,"--> findTalliesInMonth");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return talliesFromMonth;
    }

    public Map<Long, List<DailyTally>> findTallies(Date startDate, Date endDate) {
        Map<Long, List<DailyTally>> tallies = new HashMap<>();
        Cursor cursor = null;
        try {
            HashMap<Long, MohIndicator> indicatorMap = KipApplication.getInstance()
                    .moh710IndicatorsRepository().findAll();

            cursor = getReadableDatabase().query(TABLE_NAME, TABLE_COLUMNS,
                    getDayBetweenDatesSelection(startDate, endDate),
                    null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    DailyTally curTally = extractDailyTally(indicatorMap, cursor);
                    if (curTally != null) {
                        if (!tallies.containsKey(curTally.getMohIndicator().getId())) {
                            tallies.put(
                                    curTally.getMohIndicator().getId(),
                                    new ArrayList<DailyTally>());
                        }

                        tallies.get(curTally.getMohIndicator().getId()).add(curTally);
                    }
                }
            }
        } catch (SQLException e) {
            Timber.e(e,"--> findTallies");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return tallies;
    }


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
                    cv.put(DailyTalliesRepository.COLUMN_INDICATOR_ID, indicator.getId());
                    cv.put(DailyTalliesRepository.COLUMN_VALUE, indicatorValue);
                    cv.put(DailyTalliesRepository.COLUMN_PROVIDER_ID, userName);
                    cv.put(DailyTalliesRepository.COLUMN_DAY, DAY_FORMAT.parse(day).getTime());
                    cv.put(DailyTalliesRepository.COLUMN_UPDATED_AT, Calendar.getInstance().getTimeInMillis());

                    database.insertWithOnConflict(TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
                }
            }
            database.setTransactionSuccessful();
        } catch (SQLException e) {
            Timber.e(e,"--> Save MOH Daily Tally");
        } catch (ParseException e) {
            Timber.e(e,"--> Save MOH Daily Tally");
        } finally {
            database.endTransaction();
        }
    }
}