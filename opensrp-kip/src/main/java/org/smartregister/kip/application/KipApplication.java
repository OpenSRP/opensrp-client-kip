package org.smartregister.kip.application;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatDelegate;

import android.util.DisplayMetrics;
import android.util.Pair;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.evernote.android.job.JobManager;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.child.ChildLibrary;
import org.smartregister.child.domain.ChildMetadata;
import org.smartregister.child.util.DBConstants;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.configurableviews.helper.JsonSpecHelper;
import org.smartregister.domain.Setting;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.growthmonitoring.repository.HeightRepository;
import org.smartregister.growthmonitoring.repository.HeightZScoreRepository;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.repository.WeightZScoreRepository;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.jsonmapping.Vaccine;
import org.smartregister.immunization.domain.jsonmapping.VaccineGroup;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.VaccinateActionUtils;
import org.smartregister.immunization.util.VaccinatorUtils;
import org.smartregister.kip.BuildConfig;
import org.smartregister.kip.activity.ActivityConfiguration;
import org.smartregister.kip.activity.ChildFormActivity;
import org.smartregister.kip.activity.ChildImmunizationActivity;
import org.smartregister.kip.activity.ChildProfileActivity;
import org.smartregister.kip.activity.ChildRegisterActivity;
import org.smartregister.kip.activity.KipOpdProfileActivity;
import org.smartregister.kip.activity.LoginActivity;
import org.smartregister.kip.activity.OpdFormActivity;
import org.smartregister.kip.configuration.KipOpdRegisterRowOptions;
import org.smartregister.kip.configuration.KipOpdRegisterSwitcher;
import org.smartregister.kip.configuration.OpdRegisterQueryProvider;
import org.smartregister.kip.job.KipJobCreator;
import org.smartregister.kip.processor.KipCovid19MiniProcessor;
import org.smartregister.kip.processor.KipProcessorForJava;
import org.smartregister.kip.processor.TripleResultProcessor;
import org.smartregister.kip.repository.ChildAlertUpdatedRepository;
import org.smartregister.kip.repository.ClientRegisterTypeRepository;
import org.smartregister.kip.repository.DailyTalliesRepository;
import org.smartregister.kip.repository.HIA2IndicatorsRepository;
import org.smartregister.kip.repository.KipChildRegisterQueryProvider;
import org.smartregister.kip.repository.KipLocationRepository;
import org.smartregister.kip.repository.KipOpdVisitSummaryRepository;
import org.smartregister.kip.repository.KipRepository;
import org.smartregister.kip.repository.Moh510SummaryReportRepository;
import org.smartregister.kip.repository.MonthlyTalliesRepository;
import org.smartregister.kip.repository.OpdCovid19CalculateRiskRepository;
import org.smartregister.kip.repository.OpdCovid19VaccinationEligibilityRepository;
import org.smartregister.kip.repository.OpdCovid19VaccinationRepository;
import org.smartregister.kip.repository.OpdCovid19WaitingListRepository;
import org.smartregister.kip.repository.OpdInfluenzaVaccineAdministrationFormRepository;
import org.smartregister.kip.repository.OpdMedicalCheckFormRepository;
import org.smartregister.kip.repository.OpdSMSReminderFormRepository;
import org.smartregister.kip.repository.SmsEnrolledClientRepository;
import org.smartregister.kip.repository.StockHelperRepository;
import org.smartregister.kip.util.AppExecutors;
import org.smartregister.kip.util.KipChildUtils;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.kip.util.KipOpdRegisterProviderMetadata;
import org.smartregister.kip.util.VaccineDuplicate;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.configuration.OpdConfiguration;
import org.smartregister.opd.pojo.OpdMetadata;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.reporting.ReportingLibrary;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Hia2ReportRepository;
import org.smartregister.repository.Repository;
import org.smartregister.stock.StockLibrary;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.DrishtiSyncScheduler;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.receiver.TimeChangedBroadcastReceiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class KipApplication extends DrishtiApplication implements TimeChangedBroadcastReceiver.OnTimeChangedListener {

    private static CommonFtsObject commonFtsObject;
    private static JsonSpecHelper jsonSpecHelper;
    private static List<VaccineGroup> vaccineGroups;
    private KipLocationRepository kipLocationRepository;
    private String password;
    private boolean lastModified;
    private ECSyncHelper ecSyncHelper;
    private EventClientRepository eventClientRepository;
    private KipLocationRepository locationRepository;
    private ClientRegisterTypeRepository registerTypeRepository;
    private HIA2IndicatorsRepository hia2IndicatorsRepository;
    private DailyTalliesRepository dailyTalliesRepository;
    private MonthlyTalliesRepository monthlyTalliesRepository;
    private Hia2ReportRepository hia2ReportRepository;
    private AppExecutors appExecutors;
    private ChildAlertUpdatedRepository childAlertUpdatedRepository;
    private OpdCovid19CalculateRiskRepository opdCovid19CalculateRiskRepository;
    private OpdCovid19VaccinationEligibilityRepository opdCovid19VaccinationEligibilityRepository;
    private OpdCovid19VaccinationRepository opdCovid19VaccinationRepository;
    private OpdCovid19WaitingListRepository opdCovid19WaitingListRepository;
    private JSONObject defaultContactFormGlobals = new JSONObject();
    private KipOpdVisitSummaryRepository kipOpdVisitSummaryRepository;
    private OpdSMSReminderFormRepository opdSMSReminderFormRepository;
    private OpdMedicalCheckFormRepository opdMedicalCheckAndVaccinateFormRepository;
    private OpdInfluenzaVaccineAdministrationFormRepository opdInfluenzaVaccineAdministrationFormRepository;
    private SmsEnrolledClientRepository smsEnrolledClientRepository;
    private Moh510SummaryReportRepository moh510SummaryReportRepository;


    public static JsonSpecHelper getJsonSpecHelper() {
        return jsonSpecHelper;
    }

    public static CommonFtsObject createCommonFtsObject(android.content.Context context) {
        if (commonFtsObject == null) {
            commonFtsObject = new CommonFtsObject(getFtsTables());
            for (String ftsTable : commonFtsObject.getTables()) {
                commonFtsObject.updateSearchFields(ftsTable, getFtsSearchFields(ftsTable));
                commonFtsObject.updateSortFields(ftsTable, getFtsSortFields(ftsTable, context));
            }
        }
        commonFtsObject.updateAlertScheduleMap(getAlertScheduleMap(context));

        return commonFtsObject;
    }

    private static String[] getFtsTables() {
        return new String[]{DBConstants.RegisterTable.CHILD_DETAILS, DBConstants.RegisterTable.MOTHER_DETAILS, DBConstants.RegisterTable.FATHER_DETAILS, DBConstants.RegisterTable.CLIENT};
    }

    private static String[] getFtsSearchFields(String tableName) {
        if (tableName.equals(DBConstants.RegisterTable.CLIENT)) {
            return new String[]{DBConstants.KEY.ZEIR_ID, DBConstants.KEY.FIRST_NAME, DBConstants.KEY.LAST_NAME};
        } else if (tableName.equals(DBConstants.RegisterTable.CHILD_DETAILS)) {
            return new String[]{DBConstants.KEY.LOST_TO_FOLLOW_UP, DBConstants.KEY.INACTIVE};
        } else if (tableName.equals(DBConstants.RegisterTable.MOTHER_DETAILS)) {
            return new String[]{KipConstants.KEY.MOTHER_GUARDIAN_NUMBER};
        } else if (tableName.equals(DBConstants.RegisterTable.FATHER_DETAILS)) {
            return new String[]{KipConstants.KEY.FATHER_PHONE};
        }
        return null;
    }

    private static String[] getFtsSortFields(String tableName, android.content.Context context) {
        if (tableName.equals(KipConstants.TABLE_NAME.ALL_CLIENTS)) {
            List<String> names = new ArrayList<>();
            names.add(KipConstants.KEY.FIRST_NAME);
            names.add(OpdDbConstants.KEY.LAST_NAME);
            names.add(KipConstants.KEY.DOB);
            names.add(KipConstants.KEY.ZEIR_ID);
            names.add(KipConstants.KEY.LAST_INTERACTED_WITH);
            names.add(KipConstants.KEY.DOD);
            names.add(KipConstants.KEY.DATE_REMOVED);
            return names.toArray(new String[0]);
        } else if (tableName.equals(DBConstants.RegisterTable.CHILD_DETAILS)) {
            List<VaccineGroup> vaccineList = VaccinatorUtils.getVaccineGroupsFromVaccineConfigFile(context, VaccinatorUtils.vaccines_file);
            List<String> names = new ArrayList<>();
            names.add(DBConstants.KEY.INACTIVE);
            names.add("relational_id");
            names.add("father_relational_id");
            names.add(DBConstants.KEY.LOST_TO_FOLLOW_UP);

            for (VaccineGroup vaccineGroup : vaccineList) {
                populateAlertColumnNames(vaccineGroup.vaccines, names);
            }

            return names.toArray(new String[0]);
        }

        return null;
    }

    private static void populateAlertColumnNames(List<Vaccine> vaccines, List<String> names) {
        for (Vaccine vaccine : vaccines)
            if (vaccine.getVaccineSeparator() != null && vaccine.getName().contains(vaccine.getVaccineSeparator().trim())) {
                String[] individualVaccines = vaccine.getName().split(vaccine.getVaccineSeparator().trim());

                List<Vaccine> vaccineList = new ArrayList<>();
                for (String individualVaccine : individualVaccines) {
                    Vaccine vaccineClone = new Vaccine();
                    vaccineClone.setName(individualVaccine.trim());
                    vaccineList.add(vaccineClone);

                }
                populateAlertColumnNames(vaccineList, names);
            } else {
                names.add("alerts." + VaccinateActionUtils.addHyphen(vaccine.getName()));
            }
    }


    private static void populateAlertScheduleMap(List<Vaccine> vaccines, Map<String, Pair<String, Boolean>> map) {
        for (Vaccine vaccine : vaccines)
            if (vaccine.getVaccineSeparator() != null && vaccine.getName().contains(vaccine.getVaccineSeparator().trim())) {
                String[] individualVaccines = vaccine.getName().split(vaccine.getVaccineSeparator().trim());

                List<Vaccine> vaccineList = new ArrayList<>();
                for (String individualVaccine : individualVaccines) {
                    Vaccine vaccineClone = new Vaccine();
                    vaccineClone.setName(individualVaccine.trim());
                    vaccineList.add(vaccineClone);

                }
                populateAlertScheduleMap(vaccineList, map);
            } else {
                map.put(vaccine.name, Pair.create("ec_child_details", false));
            }
    }

    private static Map<String, Pair<String, Boolean>> getAlertScheduleMap(android.content.Context context) {
        List<VaccineGroup> vaccines = getVaccineGroups(context);
        Map<String, Pair<String, Boolean>> map = new HashMap<>();

        for (VaccineGroup vaccineGroup : vaccines) {
            populateAlertScheduleMap(vaccineGroup.vaccines, map);
        }
        return map;
    }

    public static List<VaccineGroup> getVaccineGroups(android.content.Context context) {
        if (vaccineGroups == null) {
            vaccineGroups = VaccinatorUtils.getVaccineGroupsFromVaccineConfigFile(context, VaccinatorUtils.vaccines_file);
        }
        return vaccineGroups;
    }

    public static synchronized KipApplication getInstance() {
        return (KipApplication) mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        context = Context.getInstance();

        String lang = KipChildUtils.getLanguage(getApplicationContext());
        Locale locale = new Locale(lang);
        Resources res = getApplicationContext().getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);

        context.updateApplicationContext(getApplicationContext());
        context.updateCommonFtsObject(createCommonFtsObject(context.applicationContext()));

        //Initialize Modules
        CoreLibrary.init(context, new KipSyncConfiguration(), BuildConfig.BUILD_TIMESTAMP);

        GrowthMonitoringLibrary.init(context, getRepository(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        GrowthMonitoringLibrary.getInstance().setGrowthMonitoringSyncTime(3, TimeUnit.MINUTES);
        ImmunizationLibrary.init(context, getRepository(), createCommonFtsObject(context.applicationContext()), BuildConfig.VERSION_CODE,
                BuildConfig.DATABASE_VERSION);
        ImmunizationLibrary.getInstance().setVaccineSyncTime(3, TimeUnit.MINUTES);
        fixHardcodedVaccineConfiguration();

        ConfigurableViewsLibrary.init(context);
        ChildLibrary.init(context, getRepository(), getMetadata(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);

        // Init Reporting library
        ReportingLibrary.init(context, getRepository(), null, BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        ReportingLibrary.getInstance().addMultiResultProcessor(new TripleResultProcessor());

        //Initialize and pass optional stock helper repository for external db functions
        StockLibrary.init(context, getRepository(), new StockHelperRepository(getRepository()));

        setupOpdLibrary();

        Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build());

        initRepositories();
        initOfflineSchedules();

        SyncStatusBroadcastReceiver.init(this);
        LocationHelper.init(KipChildUtils.ALLOWED_LEVELS, KipChildUtils.DEFAULT_LOCATION_LEVEL);
        jsonSpecHelper = new JsonSpecHelper(this);

        //init Job Manager
        JobManager.create(this).addJobCreator(new KipJobCreator());
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

    }

    private void setupOpdLibrary() {
        OpdMetadata opdMetadata = new OpdMetadata(OpdConstants.JSON_FORM_KEY.NAME, OpdDbConstants.KEY.TABLE,
                OpdConstants.EventType.OPD_REGISTRATION, OpdConstants.EventType.UPDATE_OPD_REGISTRATION,
                OpdConstants.CONFIG, OpdFormActivity.class, KipOpdProfileActivity.class, true);

        opdMetadata.setFieldsWithLocationHierarchy(new HashSet<>(Collections.singletonList("village")));
        opdMetadata.setLookUpQueryForOpdClient(String.format("select id as _id, %s, %s, %s, %s, %s, %s, %s, national_id from " + OpdDbConstants.KEY.TABLE + " where [condition] ", OpdConstants.KEY.RELATIONALID, OpdConstants.KEY.FIRST_NAME,
                OpdConstants.KEY.LAST_NAME, OpdConstants.KEY.GENDER, OpdConstants.KEY.DOB, OpdConstants.KEY.BASE_ENTITY_ID, OpdDbConstants.KEY.OPENSRP_ID));
        OpdConfiguration opdConfiguration = new OpdConfiguration.Builder(OpdRegisterQueryProvider.class)
                .setOpdMetadata(opdMetadata)
                .setOpdRegisterProviderMetadata(KipOpdRegisterProviderMetadata.class)
                .setOpdRegisterRowOptions(KipOpdRegisterRowOptions.class)
                .setOpdRegisterSwitcher(KipOpdRegisterSwitcher.class)
                .addOpdFormProcessingClass(KipConstants.EventType.OPD_CALCULATE_RISK_FACTOR, new KipCovid19MiniProcessor())
                .addOpdFormProcessingClass(KipConstants.EventType.OPD_VACCINATION_ELIGIBILITY_CHECK, new KipCovid19MiniProcessor())
                .addOpdFormProcessingClass(KipConstants.EventType.OPD_COVID19_WAITING_LIST, new KipCovid19MiniProcessor())
                .addOpdFormProcessingClass(KipConstants.EventType.OPD_SMS_REMINDER, new KipCovid19MiniProcessor())
                .addOpdFormProcessingClass(KipConstants.EventType.COVID_AEFI, new KipCovid19MiniProcessor())
                .addOpdFormProcessingClass(KipConstants.EventType.OPD_COVID_19_VACCINE_ADMINISTRATION, new KipCovid19MiniProcessor())
                .addOpdFormProcessingClass(KipConstants.EventType.OPD_COVID19_VACCINE_STOCK, new KipCovid19MiniProcessor())
                .addOpdFormProcessingClass(KipConstants.EventType.OPD_INFLUENZA_MEDIAL_CONDITION, new KipCovid19MiniProcessor())
                .addOpdFormProcessingClass(KipConstants.EventType.OPD_INFLUENZA_VACCINE_ADMINISTRATION, new KipCovid19MiniProcessor())
                .addOpdFormProcessingClass(KipConstants.EventType.INFLUENZA_VACCINE_AEFI, new KipCovid19MiniProcessor())
                .build();

        OpdLibrary.init(context, getRepository(), opdConfiguration, BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
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

    @Override
    public Repository getRepository() {
        try {
            if (repository == null) {
                repository = new KipRepository(getInstance().getApplicationContext(), context);
                kipLocationRepository();
            }
        } catch (UnsatisfiedLinkError e) {
            Timber.e(e, "KipApplication --> getRepository");
        }
        return repository;
    }

//    public String getPassword() {
//        if (password == null) {
//            String username = getContext().userService().getAllSharedPreferences().fetchRegisteredANM();
//            password = getContext().userService().getGroupId(username);
//        }
//        return password;
//    }

    public Context getContext() {
        return context;
    }

    @NotNull
    @Override
    public ClientProcessorForJava getClientProcessor() {
        return KipProcessorForJava.getInstance(this);
    }

    @Override
    public void onTerminate() {
        Timber.i("Application is terminating. Stopping sync scheduler and resetting isSyncInProgress setting.");
        cleanUpSyncState();
        TimeChangedBroadcastReceiver.destroy(this);
        SyncStatusBroadcastReceiver.destroy(this);
        super.onTerminate();
    }

    protected void cleanUpSyncState() {
        try {
            DrishtiSyncScheduler.stop(getApplicationContext());
            context.allSharedPreferences().saveIsSyncInProgress(false);
        } catch (Exception e) {
            Timber.e(e, "KipApplication --> cleanUpSyncState");
        }
    }

    private ChildMetadata getMetadata() {
        ChildMetadata metadata = new ChildMetadata(ChildFormActivity.class, ChildProfileActivity.class,
                ChildImmunizationActivity.class, ChildRegisterActivity.class, true, new KipChildRegisterQueryProvider());
        metadata.updateChildRegister(KipConstants.JSON_FORM.CHILD_ENROLLMENT, KipConstants.TABLE_NAME.ALL_CLIENTS,
                KipConstants.TABLE_NAME.ALL_CLIENTS, KipConstants.EventType.CHILD_REGISTRATION,
                KipConstants.EventType.UPDATE_CHILD_REGISTRATION, KipConstants.EventType.OUT_OF_CATCHMENT, KipConstants.CONFIGURATION.CHILD_REGISTER,
                KipConstants.RELATIONSHIP.MOTHER, KipConstants.JSON_FORM.OUT_OF_CATCHMENT_SERVICE);
        metadata.setupFatherRelation(KipConstants.TABLE_NAME.ALL_CLIENTS, KipConstants.RELATIONSHIP.FATHER);
        return metadata;
    }

    public KipLocationRepository kipLocationRepository() {
        if (kipLocationRepository == null) {
            kipLocationRepository = new KipLocationRepository();
        }
        return kipLocationRepository;
    }

    private void initRepositories() {
        weightRepository();
        heightRepository();
        vaccineRepository();
        weightZScoreRepository();
        heightZScoreRepository();
    }

    private void initOfflineSchedules() {
        try {
            List<VaccineGroup> childVaccines = VaccinatorUtils.getSupportedVaccines(this);
            List<Vaccine> specialVaccines = VaccinatorUtils.getSpecialVaccines(this);
            VaccineSchedule.init(childVaccines, specialVaccines, KipConstants.KEY.CHILD);
            //  VaccineSchedule.vaccineSchedules.get(KipConstants.KEY.CHILD).remove("BCG 2");
        } catch (Exception e) {
            Timber.e(e, "KipApplication --> initOfflineSchedules");
        }
    }

    public WeightRepository weightRepository() {
        return GrowthMonitoringLibrary.getInstance().weightRepository();
    }

    public HeightRepository heightRepository() {
        return GrowthMonitoringLibrary.getInstance().heightRepository();
    }

    public VaccineRepository vaccineRepository() {
        return ImmunizationLibrary.getInstance().vaccineRepository();
    }

    public WeightZScoreRepository weightZScoreRepository() {
        return GrowthMonitoringLibrary.getInstance().weightZScoreRepository();
    }

    public HeightZScoreRepository heightZScoreRepository() {
        return GrowthMonitoringLibrary.getInstance().heightZScoreRepository();
    }

    @Override
    public void onTimeChanged() {
        String username = getContext().userService().getAllSharedPreferences().fetchRegisteredANM();
        context.userService().forceRemoteLogin(username);
        logoutCurrentUser();
    }

    @Override
    public void onTimeZoneChanged() {
        String username = getContext().userService().getAllSharedPreferences().fetchRegisteredANM();
        context.userService().forceRemoteLogin(username);
        logoutCurrentUser();
    }

    public Context context() {
        return context;
    }

    public EventClientRepository eventClientRepository() {
        if (eventClientRepository == null) {
            eventClientRepository = new EventClientRepository();
        }
        return eventClientRepository;
    }

    public RecurringServiceTypeRepository recurringServiceTypeRepository() {
        return ImmunizationLibrary.getInstance().recurringServiceTypeRepository();
    }

    public RecurringServiceRecordRepository recurringServiceRecordRepository() {
        return ImmunizationLibrary.getInstance().recurringServiceRecordRepository();
    }

    public boolean isLastModified() {
        return lastModified;
    }

    public void setLastModified(boolean lastModified) {
        this.lastModified = lastModified;
    }

    public ECSyncHelper getEcSyncHelper() {
        if (ecSyncHelper == null) {
            ecSyncHelper = ECSyncHelper.getInstance(getApplicationContext());
        }
        return ecSyncHelper;
    }

    @VisibleForTesting
    protected void fixHardcodedVaccineConfiguration() {
        VaccineRepo.Vaccine[] vaccines = ImmunizationLibrary.getInstance().getVaccines();

        HashMap<String, VaccineDuplicate> replacementVaccines = new HashMap<>();
        replacementVaccines.put("MR 2", new VaccineDuplicate("MR 2", VaccineRepo.Vaccine.mr1, -1, 548, 183, "child"));
        replacementVaccines.put("BCG 2", new VaccineDuplicate("BCG 2", VaccineRepo.Vaccine.bcg, 1825, 0, 42, "child"));

        for (VaccineRepo.Vaccine vaccine : vaccines) {
            if (replacementVaccines.containsKey(vaccine.display())) {
                VaccineDuplicate vaccineDuplicate = replacementVaccines.get(vaccine.display());

                vaccine.setCategory(vaccineDuplicate.category());
                vaccine.setExpiryDays(vaccineDuplicate.expiryDays());
                vaccine.setMilestoneGapDays(vaccineDuplicate.milestoneGapDays());
                vaccine.setPrerequisite(vaccineDuplicate.prerequisite());
                vaccine.setPrerequisiteGapDays(vaccineDuplicate.prerequisiteGapDays());
            }
        }

        ImmunizationLibrary.getInstance().setVaccines(vaccines);
    }

    public DailyTalliesRepository dailyTalliesRepository() {
        if (dailyTalliesRepository == null) {
            dailyTalliesRepository = new DailyTalliesRepository();
        }
        return dailyTalliesRepository;
    }

    public HIA2IndicatorsRepository hIA2IndicatorsRepository() {
        if (hia2IndicatorsRepository == null) {
            hia2IndicatorsRepository = new HIA2IndicatorsRepository();
        }
        return hia2IndicatorsRepository;
    }

    public MonthlyTalliesRepository monthlyTalliesRepository() {
        if (monthlyTalliesRepository == null) {
            monthlyTalliesRepository = new MonthlyTalliesRepository();
        }

        return monthlyTalliesRepository;
    }

    public Hia2ReportRepository hia2ReportRepository() {
        if (hia2ReportRepository == null) {
            hia2ReportRepository = new Hia2ReportRepository();
        }
        return hia2ReportRepository;
    }

    public KipLocationRepository locationRepository() {
        if (locationRepository == null) {
            locationRepository = new KipLocationRepository();
        }
        return locationRepository;
    }

    public OpdCovid19CalculateRiskRepository opdCovid19CalculateRiskRepository() {
        if (opdCovid19CalculateRiskRepository == null) {
            opdCovid19CalculateRiskRepository = new OpdCovid19CalculateRiskRepository();
        }
        return opdCovid19CalculateRiskRepository;
    }

    public OpdCovid19VaccinationEligibilityRepository opdCovid19VaccinationEligibilityRepository() {
        if (opdCovid19VaccinationEligibilityRepository == null) {
            opdCovid19VaccinationEligibilityRepository = new OpdCovid19VaccinationEligibilityRepository();
        }
        return opdCovid19VaccinationEligibilityRepository;
    }


    public OpdCovid19VaccinationRepository opdCovid19VaccinationRepository() {
        if (opdCovid19VaccinationRepository == null) {
            opdCovid19VaccinationRepository = new OpdCovid19VaccinationRepository();
        }
        return opdCovid19VaccinationRepository;
    }

    public OpdCovid19WaitingListRepository opdCovid19WaitingListRepository() {
        if (opdCovid19WaitingListRepository == null) {
            opdCovid19WaitingListRepository = new OpdCovid19WaitingListRepository();
        }
        return opdCovid19WaitingListRepository;
    }

    public OpdSMSReminderFormRepository opdSMSReminderFormRepository(){
        if (opdSMSReminderFormRepository == null){
            opdSMSReminderFormRepository = new OpdSMSReminderFormRepository();
        }
        return opdSMSReminderFormRepository;
    }

    public OpdMedicalCheckFormRepository opdMedicalCheckFormRepository(){
        if (opdMedicalCheckAndVaccinateFormRepository == null){
            opdMedicalCheckAndVaccinateFormRepository = new OpdMedicalCheckFormRepository();
        }
        return opdMedicalCheckAndVaccinateFormRepository;
    }

    public OpdInfluenzaVaccineAdministrationFormRepository opdInfluenzaVaccineAdministrationFormRepository(){
        if (opdInfluenzaVaccineAdministrationFormRepository == null){
            opdInfluenzaVaccineAdministrationFormRepository = new OpdInfluenzaVaccineAdministrationFormRepository();
        }
        return opdInfluenzaVaccineAdministrationFormRepository;
    }

    public SmsEnrolledClientRepository smsEnrolledClientRepository(){
        if (smsEnrolledClientRepository == null){
            smsEnrolledClientRepository = new SmsEnrolledClientRepository();
        }
        return smsEnrolledClientRepository;
    }

    public Moh510SummaryReportRepository moh510SummaryReportRepository(){
        if (moh510SummaryReportRepository == null){
            moh510SummaryReportRepository = new Moh510SummaryReportRepository();
        }
        return moh510SummaryReportRepository;
    }

    public void populateGlobalSettings() {
        Setting setting = getSettings(KipConstants.Settings.VACCINE_STOCK_IDENTIFIER);
        populateGlobalSettingsCore(setting);
    }

    public Setting getSettings(String characteristics) {
        return KipApplication.getInstance().getContext().allSettings().getSetting(characteristics);
    }

    private void populateGlobalSettingsCore(Setting setting) {
        try {
            JSONObject settingObject = setting != null ? new JSONObject(setting.getValue()) : null;
            if (settingObject != null) {
                JSONArray settingArray = settingObject.getJSONArray(AllConstants.SETTINGS);
                if (settingArray != null) {
                    for (int i = 0; i < settingArray.length(); i++) {
                        JSONObject jsonObject = settingArray.getJSONObject(i);
                        Boolean value = jsonObject.optBoolean(JsonFormConstants.VALUE, false);
                        JSONObject nullObject = null;
                        if (value != null && !value.equals(nullObject)) {
                            defaultContactFormGlobals.put(jsonObject.getString(JsonFormConstants.KEY), value);
                        } else {
                            defaultContactFormGlobals.put(jsonObject.getString(JsonFormConstants.KEY), false);
                        }
                    }


                }
            }
        } catch (JSONException e) {
            Timber.e(" --> populateGlobalSettingsCore");
        }
    }

    public ActivityConfiguration getActivityConfiguration() {
        return new ActivityConfiguration();
    }

    public KipOpdVisitSummaryRepository kipOpdVisitSummaryRepository(){
        if (kipOpdVisitSummaryRepository == null){
            kipOpdVisitSummaryRepository = new KipOpdVisitSummaryRepository();
        }
        return kipOpdVisitSummaryRepository;
    }

    public AppExecutors getAppExecutors() {
        if (appExecutors == null) {
            appExecutors = new AppExecutors();
        }
        return appExecutors;
    }

    @VisibleForTesting
    public void setVaccineGroups(List<VaccineGroup> vaccines) {
        vaccineGroups = vaccines;
    }

    public ClientRegisterTypeRepository registerTypeRepository() {
        if (registerTypeRepository == null) {
            this.registerTypeRepository = new ClientRegisterTypeRepository();
        }
        return this.registerTypeRepository;
    }

    public ChildAlertUpdatedRepository alertUpdatedRepository() {
        if (childAlertUpdatedRepository == null) {
            this.childAlertUpdatedRepository = new ChildAlertUpdatedRepository();
        }
        return this.childAlertUpdatedRepository;
    }
}

