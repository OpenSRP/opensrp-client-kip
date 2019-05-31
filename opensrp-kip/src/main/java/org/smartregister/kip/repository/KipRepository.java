package org.smartregister.kip.repository;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.repository.ZScoreRepository;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineNameRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.repository.VaccineTypeRepository;
import org.smartregister.immunization.util.IMDatabaseUtils;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.repository.AlertRepository;
import org.smartregister.repository.Repository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.KipConstants;

public class KipRepository extends Repository {

    private static final String TAG = KipRepository.class.getCanonicalName();
    private SQLiteDatabase readableDatabase;
    private SQLiteDatabase writableDatabase;
    private final Context context;

    public KipRepository(Context context, org.smartregister.Context opensrpContext) {
        super(context, KipConstants.DATABASE_NAME, KipConstants.DATABASE_VERSION, opensrpContext.session(), KipApplication.createCommonFtsObject(), opensrpContext.sharedRepositoriesArray());
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        super.onCreate(database);
        KipEventClientRepository.createTable(database, KipEventClientRepository.Table.client, KipEventClientRepository.client_column.values());
        KipEventClientRepository.createTable(database, KipEventClientRepository.Table.address, KipEventClientRepository.address_column.values());
        KipEventClientRepository.createTable(database, KipEventClientRepository.Table.event, KipEventClientRepository.event_column.values());
        KipEventClientRepository.createTable(database, KipEventClientRepository.Table.obs, KipEventClientRepository.obs_column.values());
        UniqueIdRepository.createTable(database);
        WeightRepository.createTable(database);
        VaccineRepository.createTable(database);
        onUpgrade(database, 1, KipConstants.DATABASE_VERSION);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(KipRepository.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 2:
                    upgradeToVersion2(db);
                    break;
                case 3:
                    upgradeToVersion3(db);
                    break;
                case 4:
                    upgradeToVersion4(db);
                    break;
                case 5:
                    upgradeToVersion5(db);
                    break;
                case 6:
                    upgradeToVersion6(db);
                    break;
                case 7:
                    upgradeToVersion7Stock(db);
                    upgradeToVersion7Hia2(db);
                    break;
                case 8:
                    upgradeToVersion8RecurringServiceUpdate(db);
                    upgradeToVersion8ReportDeceased(db);
                    break;
                case 9:
                    upgradeToVersion9(db);
                    break;
                case 10:
                    upgradeToVersion10(db);
                    break;
                case 11:
                    upgradeToVersion11(db);
                    break;
                default:
                    break;
            }
            upgradeTo++;
        }
    }

    private void upgradeToVersion7Stock(SQLiteDatabase db) {
        try {
//            db.execSQL("DROP TABLE IF EXISTS  ");
            StockRepository.createTable(db);
            VaccineNameRepository.createTable(db);
            VaccineTypeRepository.createTable(db);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion7Stock " + e.getMessage());
        }
    }


    @Override
    public SQLiteDatabase getReadableDatabase() {
        return getReadableDatabase(KipApplication.getInstance().getPassword());
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return getWritableDatabase(KipApplication.getInstance().getPassword());
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase(String password) {
        try {
            if (readableDatabase == null || !readableDatabase.isOpen()) {
                if (readableDatabase != null) {
                    readableDatabase.close();
                }
                readableDatabase = super.getReadableDatabase(password);
            }
            return readableDatabase;
        } catch (Exception e) {
            Log.e(TAG, "Database Error. " + e.getMessage());
            return null;
        }

    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase(String password) {
        if (writableDatabase == null || !writableDatabase.isOpen()) {
            if (writableDatabase != null) {
                writableDatabase.close();
            }
            writableDatabase = super.getWritableDatabase(password);
        }
        return writableDatabase;
    }

    @Override
    public synchronized void close() {
        if (readableDatabase != null) {
            readableDatabase.close();
        }

        if (writableDatabase != null) {
            writableDatabase.close();
        }
        super.close();
    }

    /**
     * Version 2 added some columns to the ec_child table
     *
     * @param database
     */
    private void upgradeToVersion2(SQLiteDatabase database) {
        try {
            // Run insert query
            ArrayList<String> newlyAddedFields = new ArrayList<>();
            newlyAddedFields.add("BCG_2");
            newlyAddedFields.add("inactive");
            newlyAddedFields.add("lost_to_follow_up");

            addFieldsToFTSTable(database, KipConstants.CHILD_TABLE_NAME, newlyAddedFields);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion2 " + Log.getStackTraceString(e));
        }
    }

    private void upgradeToVersion3(SQLiteDatabase db) {
        try {
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_EVENT_ID_COL);
            db.execSQL(VaccineRepository.EVENT_ID_INDEX);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_EVENT_ID_COL);
            db.execSQL(WeightRepository.EVENT_ID_INDEX);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL);
            db.execSQL(VaccineRepository.FORMSUBMISSION_INDEX);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL);
            db.execSQL(WeightRepository.FORMSUBMISSION_INDEX);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion3 " + Log.getStackTraceString(e));
        }
    }

    private void upgradeToVersion4(SQLiteDatabase db) {
        try {
            db.execSQL(AlertRepository.ALTER_ADD_OFFLINE_COLUMN);
            db.execSQL(AlertRepository.OFFLINE_INDEX);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion4" + Log.getStackTraceString(e));
        }
    }

    private void upgradeToVersion5(SQLiteDatabase db) {
        try {
            RecurringServiceTypeRepository.createTable(db);
            RecurringServiceRecordRepository.createTable(db);

            RecurringServiceTypeRepository recurringServiceTypeRepository = KipApplication.getInstance().recurringServiceTypeRepository();
            IMDatabaseUtils.populateRecurringServices(context, db, recurringServiceTypeRepository);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion5 " + Log.getStackTraceString(e));
        }
    }

    private void upgradeToVersion6(SQLiteDatabase db) {
        try {
            ZScoreRepository.createTable(db);
            db.execSQL(WeightRepository.ALTER_ADD_Z_SCORE_COLUMN);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion6" + Log.getStackTraceString(e));
        }
    }

    private void upgradeToVersion7Hia2(SQLiteDatabase db) {
        try {
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX);
            DailyTalliesRepository.createTable(db);
            MonthlyTalliesRepository.createTable(db);
            KipEventClientRepository.createTable(db, KipEventClientRepository.Table.path_reports, KipEventClientRepository.report_column.values());
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_HIA2_STATUS_COL);

        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion7Hia2 " + e.getMessage());
        }
    }

    private void upgradeToVersion8RecurringServiceUpdate(SQLiteDatabase db) {
        try {
            db.execSQL(MonthlyTalliesRepository.INDEX_UNIQUE);

            // Recurring service json changed. update
            RecurringServiceTypeRepository recurringServiceTypeRepository = KipApplication.getInstance().recurringServiceTypeRepository();
            IMDatabaseUtils.populateRecurringServices(context, db, recurringServiceTypeRepository);

        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion8RecurringServiceUpdate " + Log.getStackTraceString(e));
        }
    }

    private void upgradeToVersion8ReportDeceased(SQLiteDatabase database) {
        try {

            String ALTER_ADD_DEATHDATE_COLUMN = "ALTER TABLE " + KipConstants.CHILD_TABLE_NAME + " Add COLUMN " + KipConstants.EC_CHILD_TABLE.DOD + " VARCHAR";
            database.execSQL(ALTER_ADD_DEATHDATE_COLUMN);

            ArrayList<String> newlyAddedFields = new ArrayList<>();
            newlyAddedFields.add(KipConstants.EC_CHILD_TABLE.DOD);

            addFieldsToFTSTable(database, KipConstants.CHILD_TABLE_NAME, newlyAddedFields);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion8ReportDeceased " + e.getMessage());
        }
    }

    private void upgradeToVersion9(SQLiteDatabase database) {
        try {
            LocationRepository.createTable(database);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion9 " + e.getMessage());
        }
    }

    private void upgradeToVersion10(SQLiteDatabase database) {
        try {
            Moh710IndicatorsRepository.createTable(database);
            dumpMOH710IndicatorsCSV(database);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion10 " + e.getMessage());
        }
    }

    private void upgradeToVersion11(SQLiteDatabase database) {
        try {
            ArrayList<String> newlyAddedFields = new ArrayList<>();
            newlyAddedFields.add(KipConstants.EC_CHILD_TABLE.GENDER);
            newlyAddedFields.add(KipConstants.EC_CHILD_TABLE.DUE_DATE);

            addFieldsToFTSTable(database, KipConstants.CHILD_TABLE_NAME, newlyAddedFields);

            String ALTER_ADD_CHW_NAME_COLUMN = "ALTER TABLE " + KipConstants.CHILD_TABLE_NAME + " ADD COLUMN " + KipConstants.EC_CHILD_TABLE.CHW_NAME + " VARCHAR";
            database.execSQL(ALTER_ADD_CHW_NAME_COLUMN);

            String ALTER_ADD_CHW_PHONE_NUMBER_COLUMN = "ALTER TABLE " + KipConstants.CHILD_TABLE_NAME + " ADD COLUMN " + KipConstants.EC_CHILD_TABLE.CHW_PHONE_NUMBER + " VARCHAR";
            database.execSQL(ALTER_ADD_CHW_PHONE_NUMBER_COLUMN);
        } catch (Exception e) {
            Log.e(TAG, "upgradeToVersion11 " + e.getMessage());
        }
    }

    private void addFieldsToFTSTable(SQLiteDatabase database, String originalTableName, List<String> newlyAddedFields) {

        // Create the new ec_child table

        String newTableNameSuffix = "_v2";

        Set<String> searchColumns = new LinkedHashSet<>();
        searchColumns.add(CommonFtsObject.idColumn);
        searchColumns.add(CommonFtsObject.relationalIdColumn);
        searchColumns.add(CommonFtsObject.phraseColumn);
        searchColumns.add(CommonFtsObject.isClosedColumn);

        String[] mainConditions = this.commonFtsObject.getMainConditions(originalTableName);
        if (mainConditions != null)
            for (String mainCondition : mainConditions) {
                if (!mainCondition.equals(CommonFtsObject.isClosedColumnName))
                    searchColumns.add(mainCondition);
            }

        String[] sortFields = this.commonFtsObject.getSortFields(originalTableName);
        if (sortFields != null) {
            for (String sortValue : sortFields) {
                if (sortValue.startsWith("alerts.")) {
                    sortValue = sortValue.split("\\.")[1];
                }
                searchColumns.add(sortValue);
            }
        }

        String joinedSearchColumns = StringUtils.join(searchColumns, ",");

        String searchSql = "create virtual table "
                + CommonFtsObject.searchTableName(originalTableName) + newTableNameSuffix
                + " using fts4 (" + joinedSearchColumns + ");";
        Log.d(TAG, "Create query is\n---------------------------\n" + searchSql);

        database.execSQL(searchSql);

        ArrayList<String> oldFields = new ArrayList<>();

        for (String curColumn : searchColumns) {
            curColumn = curColumn.trim();
            if (curColumn.contains(" ")) {
                String[] curColumnParts = curColumn.split(" ");
                curColumn = curColumnParts[0];
            }

            if (!newlyAddedFields.contains(curColumn)) {
                oldFields.add(curColumn);
            } else {
                Log.d(TAG, "Skipping field " + curColumn + " from the select query");
            }
        }

        String insertQuery = "insert into "
                + CommonFtsObject.searchTableName(originalTableName) + newTableNameSuffix
                + " (" + StringUtils.join(oldFields, ", ") + ")"
                + " select " + StringUtils.join(oldFields, ", ") + " from "
                + CommonFtsObject.searchTableName(originalTableName);

        Log.d(TAG, "Insert query is\n---------------------------\n" + insertQuery);
        database.execSQL(insertQuery);

        // Run the drop query
        String dropQuery = "drop table " + CommonFtsObject.searchTableName(originalTableName);
        Log.d(TAG, "Drop query is\n---------------------------\n" + dropQuery);
        database.execSQL(dropQuery);

        // Run rename query
        String renameQuery = "alter table "
                + CommonFtsObject.searchTableName(originalTableName) + newTableNameSuffix
                + " rename to " + CommonFtsObject.searchTableName(originalTableName);
        Log.d(TAG, "Rename query is\n---------------------------\n" + renameQuery);
        database.execSQL(renameQuery);

    }

    private void dumpMOH710IndicatorsCSV(SQLiteDatabase db) {
        List<Map<String, String>> csvData = util.Utils.populateMohIndicatorsTableFromCSV(
                context,
                Moh710IndicatorsRepository.INDICATORS_CSV_FILE,
                Moh710IndicatorsRepository.CSV_COLUMN_MAPPING);
        Moh710IndicatorsRepository moh710IndicatorsRepository = KipApplication.getInstance().moh710IndicatorsRepository();
        moh710IndicatorsRepository.save(db, csvData);
    }
}
