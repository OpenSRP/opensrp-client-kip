package org.smartregister.kip.application;

import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.smartregister.CoreLibrary;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.repository.ZScoreRepository;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineNameRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.repository.VaccineTypeRepository;
import org.smartregister.immunization.util.VaccinateActionUtils;
import org.smartregister.immunization.util.VaccinatorUtils;
import org.smartregister.kip.BuildConfig;
import org.smartregister.kip.R;
import org.smartregister.kip.activity.LoginActivity;
import org.smartregister.kip.context.Context;
import org.smartregister.kip.receiver.KipSyncBroadcastReceiver;
import org.smartregister.kip.receiver.Moh710ServiceBroadcastReceiver;
import org.smartregister.kip.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.kip.repository.DailyTalliesRepository;
import org.smartregister.kip.repository.KipEventClientRepository;
import org.smartregister.kip.repository.KipRepository;
import org.smartregister.kip.repository.LocationRepository;
import org.smartregister.kip.repository.Moh710IndicatorsRepository;
import org.smartregister.kip.repository.MonthlyTalliesRepository;
import org.smartregister.kip.repository.StockRepository;
import org.smartregister.kip.repository.UniqueIdRepository;
import org.smartregister.kip.sync.KipUpdateActionsTask;
import org.smartregister.repository.Repository;
import org.smartregister.sync.DrishtiSyncScheduler;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.receiver.TimeChangedBroadcastReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.fabric.sdk.android.Fabric;
import util.KipConstants;

import static org.smartregister.util.Log.logError;
import static org.smartregister.util.Log.logInfo;

/**
 * Created by koros on 2/3/16.
 */
public class KipApplication extends DrishtiApplication
        implements TimeChangedBroadcastReceiver.OnTimeChangedListener {

    private static final String TAG = "KipApplication";
    private Locale locale = null;
    private Context context;
    private static CommonFtsObject commonFtsObject;
    private UniqueIdRepository uniqueIdRepository;
    private DailyTalliesRepository dailyTalliesRepository;
    private MonthlyTalliesRepository monthlyTalliesRepository;
    private KipEventClientRepository eventClientRepository;
    private StockRepository stockRepository;
    private boolean lastModified;
    private LocationRepository locationRepository;
    private Moh710IndicatorsRepository moh710IndicatorsRepository;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        context = Context.getInstance();
        context.updateApplicationContext(getApplicationContext());
        context.updateCommonFtsObject(createCommonFtsObject());

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        DrishtiSyncScheduler.setReceiverClass(KipSyncBroadcastReceiver.class);

        Moh710ServiceBroadcastReceiver.init(this);
        SyncStatusBroadcastReceiver.init(this);
        TimeChangedBroadcastReceiver.init(this);
        TimeChangedBroadcastReceiver.getInstance().addOnTimeChangedListener(this);

        applyUserLanguagePreference();
        cleanUpSyncState();
        initOfflineSchedules();
        setCrashlyticsUser(context);
        KipUpdateActionsTask.setAlarms(this);

        //Initialize Modules
        CoreLibrary.init(context());
        GrowthMonitoringLibrary.init(context(), getRepository());
        ImmunizationLibrary.init(context(), getRepository(), createCommonFtsObject());

    }

    public static synchronized KipApplication getInstance() {
        return (KipApplication) mInstance;
    }

    @Override
    public void logoutCurrentUser() {

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getApplicationContext().startActivity(intent);
        context.userService().logoutSession();
    }

    protected void cleanUpSyncState() {
        DrishtiSyncScheduler.stop(getApplicationContext());
        context.allSharedPreferences().saveIsSyncInProgress(false);
    }


    @Override
    public void onTerminate() {
        logInfo("Application is terminating. Stopping Bidan Sync scheduler and resetting isSyncInProgress setting.");
        cleanUpSyncState();
        SyncStatusBroadcastReceiver.destroy(this);
        TimeChangedBroadcastReceiver.destroy(this);
        super.onTerminate();
    }

    protected void applyUserLanguagePreference() {
        Configuration config = getBaseContext().getResources().getConfiguration();

        String lang = context.allSharedPreferences().fetchLanguagePreference();
        if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
            locale = new Locale(lang);
            updateConfiguration(config);
        }
    }

    private void updateConfiguration(Configuration config) {
        config.locale = locale;
        Locale.setDefault(locale);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    private static String[] getFtsSearchFields(String tableName) {
        if (tableName.equals(KipConstants.CHILD_TABLE_NAME)) {
            return new String[]{"zeir_id", "epi_card_number", "first_name", "last_name"};
        } else if (tableName.equals(KipConstants.MOTHER_TABLE_NAME)) {
            return new String[]{"zeir_id", "epi_card_number", "first_name", "last_name", "father_name", "husband_name", "contact_phone_number"};
        }
        return null;
    }

    private static String[] getFtsSortFields(String tableName) {


        if (tableName.equals(KipConstants.CHILD_TABLE_NAME)) {
            ArrayList<VaccineRepo.Vaccine> vaccines = VaccineRepo.getVaccines("child");
            List<String> names = new ArrayList<>();
            names.add("first_name");
            names.add("dob");
            names.add("zeir_id");
            names.add("last_interacted_with");
            names.add("inactive");
            names.add("lost_to_follow_up");
            names.add(KipConstants.EC_CHILD_TABLE.DOD);

            for (VaccineRepo.Vaccine vaccine : vaccines) {
                names.add("alerts." + VaccinateActionUtils.addHyphen(vaccine.display()));
            }

            return names.toArray(new String[names.size()]);
        } else if (tableName.equals(KipConstants.MOTHER_TABLE_NAME)) {
            return new String[]{"first_name", "dob", "zeir_id", "last_interacted_with"};
        }
        return null;
    }

    private static String[] getFtsTables() {
        return new String[]{KipConstants.CHILD_TABLE_NAME, KipConstants.MOTHER_TABLE_NAME};
    }

    private static Map<String, Pair<String, Boolean>> getAlertScheduleMap() {
        ArrayList<VaccineRepo.Vaccine> vaccines = VaccineRepo.getVaccines("child");
        Map<String, Pair<String, Boolean>> map = new HashMap<>();
        for (VaccineRepo.Vaccine vaccine : vaccines) {
            map.put(vaccine.display(), Pair.create(KipConstants.CHILD_TABLE_NAME, false));
        }
        return map;
    }

    public static CommonFtsObject createCommonFtsObject() {
        if (commonFtsObject == null) {
            commonFtsObject = new CommonFtsObject(getFtsTables());
            for (String ftsTable : commonFtsObject.getTables()) {
                commonFtsObject.updateSearchFields(ftsTable, getFtsSearchFields(ftsTable));
                commonFtsObject.updateSortFields(ftsTable, getFtsSortFields(ftsTable));
            }
        }
        commonFtsObject.updateAlertScheduleMap(getAlertScheduleMap());
        return commonFtsObject;
    }

    /**
     * This method sets the Crashlytics user to whichever username was used to log in last. It only
     * does so if the app is not built for debugging
     *
     * @param context The user's context
     */
    public static void setCrashlyticsUser(Context context) {
        if (!BuildConfig.DEBUG
                && context != null && context.userService() != null
                && context.userService().getAllSharedPreferences() != null) {
            Crashlytics.setUserName(context.userService().getAllSharedPreferences().fetchRegisteredANM());
        }
    }

    @Override
    public Repository getRepository() {
        try {
            if (repository == null) {
                repository = new KipRepository(getInstance().getApplicationContext(), context());
                uniqueIdRepository();
                dailyTalliesRepository();
                monthlyTalliesRepository();
                eventClientRepository();
                stockRepository();
                locationRepository();
            }
        } catch (UnsatisfiedLinkError e) {
            logError("Error on getRepository: " + e);

        }
        return repository;
    }


    public WeightRepository weightRepository() {
        return GrowthMonitoringLibrary.getInstance().weightRepository();
    }

    public Context context() {
        return context;
    }

    public VaccineRepository vaccineRepository() {
        return ImmunizationLibrary.getInstance().vaccineRepository();
    }

    public ZScoreRepository zScoreRepository() {
        return GrowthMonitoringLibrary.getInstance().zScoreRepository();
    }

    public UniqueIdRepository uniqueIdRepository() {
        if (uniqueIdRepository == null) {
            uniqueIdRepository = new UniqueIdRepository((KipRepository) getRepository());
        }
        return uniqueIdRepository;
    }

    public DailyTalliesRepository dailyTalliesRepository() {
        if (dailyTalliesRepository == null) {
            dailyTalliesRepository = new DailyTalliesRepository((KipRepository) getRepository());
        }
        return dailyTalliesRepository;
    }

    public MonthlyTalliesRepository monthlyTalliesRepository() {
        if (monthlyTalliesRepository == null) {
            monthlyTalliesRepository = new MonthlyTalliesRepository((KipRepository) getRepository());
        }

        return monthlyTalliesRepository;
    }

    public RecurringServiceTypeRepository recurringServiceTypeRepository() {
        return ImmunizationLibrary.getInstance().recurringServiceTypeRepository();
    }

    public RecurringServiceRecordRepository recurringServiceRecordRepository() {
        return ImmunizationLibrary.getInstance().recurringServiceRecordRepository();
    }

    public KipEventClientRepository eventClientRepository() {
        if (eventClientRepository == null) {
            eventClientRepository = new KipEventClientRepository(getRepository());
        }
        return eventClientRepository;
    }

    public StockRepository stockRepository() {
        if (stockRepository == null) {
            stockRepository = new StockRepository((KipRepository) getRepository());
        }
        return stockRepository;
    }

    public LocationRepository locationRepository() {
        if (locationRepository == null) {
            locationRepository = new LocationRepository((KipRepository) getRepository());
        }
        return locationRepository;
    }

    public Moh710IndicatorsRepository moh710IndicatorsRepository() {
        if (moh710IndicatorsRepository == null) {
            moh710IndicatorsRepository = new Moh710IndicatorsRepository((KipRepository) getRepository());
        }
        return moh710IndicatorsRepository;
    }

    public VaccineTypeRepository vaccineTypeRepository() {
        return ImmunizationLibrary.getInstance().vaccineTypeRepository();
    }

    public VaccineNameRepository vaccineNameRepository() {
        return ImmunizationLibrary.getInstance().vaccineNameRepository();
    }

    public boolean isLastModified() {
        return lastModified;
    }

    public void setLastModified(boolean lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public void onTimeChanged() {
        Toast.makeText(this, R.string.device_time_changed, Toast.LENGTH_LONG).show();
        context.userService().forceRemoteLogin();
        logoutCurrentUser();
    }

    @Override
    public void onTimeZoneChanged() {
        Toast.makeText(this, R.string.device_timezone_changed, Toast.LENGTH_LONG).show();
        context.userService().forceRemoteLogin();
        logoutCurrentUser();
    }

    private void initOfflineSchedules() {
        try {
            JSONArray childVaccines = new JSONArray(VaccinatorUtils.getSupportedVaccines(this));
            JSONArray specialVaccines = new JSONArray(VaccinatorUtils.getSpecialVaccines(this));
            VaccineSchedule.init(childVaccines, specialVaccines, "child");
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

}
