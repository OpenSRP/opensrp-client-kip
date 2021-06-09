package org.smartregister.kip.util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.child.util.Constants;
import org.smartregister.child.util.Utils;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.domain.db.Client;
import org.smartregister.domain.db.Event;
import org.smartregister.domain.db.EventClient;
import org.smartregister.domain.db.Obs;
import org.smartregister.domain.form.FormLocation;
import org.smartregister.kip.BuildConfig;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.event.BaseEvent;
import org.smartregister.kip.listener.OnLocationChangeListener;
import org.smartregister.kip.repository.KipLocationRepository;
import org.smartregister.kip.view.NavigationMenu;
import org.smartregister.kip.widget.KipTreeViewDialog;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.reporting.job.RecurringIndicatorGeneratingJob;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.AssetHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class KipChildUtils extends Utils {
    private static Context context = KipApplication.getInstance().getApplicationContext();

    public static final ArrayList<String> ALLOWED_LEVELS;
    public static final String FACILITY = "Facility";
    public static final String DEFAULT_LOCATION_LEVEL = "Health Facility";
    public static final SimpleDateFormat DB_DF = new SimpleDateFormat("yyyy-MM-dd");
    public static final String LANGUAGE = "language";
    private static final String PREFERENCES_FILE = "lang_prefs";

    static {
        ALLOWED_LEVELS = new ArrayList<>();
        ALLOWED_LEVELS.add(DEFAULT_LOCATION_LEVEL);
        ALLOWED_LEVELS.add(FACILITY);
    }

    public static void showDialogMessage(Context context, int title, int message) {
        showDialogMessage(context, title > 0 ? context.getResources().getString(title) : "",
                message > 0 ? context.getResources().getString(message) : "");
    }

    public static void showDialogMessage(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)

                .setPositiveButton(android.R.string.ok, null)

                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static void saveLanguage(Context ctx, String language) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(LANGUAGE, language);
        editor.apply();
        saveGlobalLanguage(language, ctx);
    }

    public static void saveGlobalLanguage(String language, Context activity) {
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(PreferenceManager.getDefaultSharedPreferences(
                KipApplication.getInstance().getApplicationContext()));
        allSharedPreferences.saveLanguagePreference(language);
        setLocale(new Locale(language), activity);
    }

    public static void setLocale(Locale locale, Context activity) {
        Locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        KipApplication.getInstance().getApplicationContext().createConfigurationContext(configuration);
    }

    public static String getLanguage(Context ctx) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(LANGUAGE, "en");
    }

    public static Context setAppLocale(Context context, String language) {
        Context newContext = context;
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = newContext.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        newContext = newContext.createConfigurationContext(config);
        return newContext;
    }

    public static void postStickyEvent(
            BaseEvent event) {//Each Sticky event must be manually cleaned by calling GizUtils.removeStickyEvent
        // after
        // handling
        EventBus.getDefault().postSticky(event);
    }

    public static void removeStickyEvent(BaseEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
    }

    public static String childAgeLimitFilter() {
        return childAgeLimitFilter(KipConstants.KEY.DOB, KipConstants.KEY.FIVE_YEAR);
    }

    private static String childAgeLimitFilter(String dateColumn, int age) {
        return " ((( julianday('now') - julianday(" + dateColumn + "))/365.25) <" + age + ")";
    }

    public static boolean updateClientDeath(@NonNull EventClient eventClient) {
        Client client = eventClient.getClient();
        ContentValues values = new ContentValues();
        if (client != null) {
            if (client.getDeathdate() == null) {
                Timber.e(new Exception(), "Death event for %s cannot be processed because deathdate is NULL : %s"
                        , client.getFirstName() + " " + client.getLastName(), new Gson().toJson(eventClient));
                return false;
            }
            values.put(Constants.KEY.DOD, Utils.convertDateFormat(client.getDeathdate()));
            values.put(Constants.KEY.DATE_REMOVED, Utils.convertDateFormat(client.getDeathdate().toDate(), Utils.DB_DF));
            AllCommonsRepository allCommonsRepository = KipApplication.getInstance().context().allCommonsRepositoryobjects(KipConstants.TABLE_NAME.ALL_CLIENTS);
            if (allCommonsRepository != null) {
                allCommonsRepository.update(KipConstants.TABLE_NAME.ALL_CLIENTS, values, client.getBaseEntityId());
                allCommonsRepository.updateSearch(client.getBaseEntityId());
            }
            return true;
        }
        return false;
    }

    @NonNull
    public static Locale getLocale(Context context) {
        if (context == null) {
            return Locale.getDefault();
        } else {
            return context.getResources().getConfiguration().locale;
        }
    }

    @NonNull
    public static ArrayList<String> getLocationLevels() {
        return new ArrayList<>(Arrays.asList(BuildConfig.LOCATION_LEVELS));
    }

    @NonNull
    public static ArrayList<String> getHealthFacilityLevels() {
        return new ArrayList<>(Arrays.asList(BuildConfig.HEALTH_FACILITY_LEVELS));
    }

    @NonNull
    public static String getCurrentLocality() {
        String selectedLocation = KipApplication.getInstance().context().allSharedPreferences().fetchCurrentLocality();
        if (StringUtils.isBlank(selectedLocation)) {
            selectedLocation = LocationHelper.getInstance().getDefaultLocation();
            KipApplication.getInstance().context().allSharedPreferences().saveCurrentLocality(selectedLocation);
        }
        return selectedLocation;
    }

    public static void showLocations(@Nullable Activity context,
                                     @NonNull OnLocationChangeListener onLocationChangeListener,
                                     @Nullable NavigationMenu navigationMenu) {
        try {
            ArrayList<String> allLevels = getLocationLevels();
            ArrayList<String> healthFacilities = getHealthFacilityLevels();
            ArrayList<String> defaultLocation = (ArrayList<String>) LocationHelper.getInstance().generateDefaultLocationHierarchy(allLevels);
            List<FormLocation> upToFacilities = LocationHelper.getInstance().generateLocationHierarchyTree(false, healthFacilities);
            String upToFacilitiesString = AssetHandler.javaToJsonString(upToFacilities, new TypeToken<List<FormLocation>>() {
            }.getType());
            KipTreeViewDialog treeViewDialog = new KipTreeViewDialog(context,
                    new JSONArray(upToFacilitiesString), defaultLocation, new ArrayList<>());

            treeViewDialog.setCancelable(true);
            treeViewDialog.setCanceledOnTouchOutside(true);
            treeViewDialog.setOnDismissListener(dialog -> {
                ArrayList<String> treeViewDialogName = treeViewDialog.getName();
                if (!treeViewDialogName.isEmpty()) {
                    String newLocation = treeViewDialogName.get(treeViewDialogName.size() - 1);
                    KipApplication.getInstance().context().allSharedPreferences().saveCurrentLocality(newLocation);
                    onLocationChangeListener.updateUi(newLocation);
                    if (navigationMenu != null) {
                        navigationMenu.updateUi(newLocation);
                    }
                }

            });
            treeViewDialog.show();
        } catch (JSONException e) {
            Timber.e(e);
        }
    }


    public static void startReportJob(Context context) {
        String reportJobExecutionTime = KipApplication.getInstance().context().allSharedPreferences().getPreference("report_job_execution_time");
        if (StringUtils.isBlank(reportJobExecutionTime) || timeBetweenLastExecutionAndNow(30, reportJobExecutionTime)) {
            KipApplication.getInstance().context().allSharedPreferences().savePreference("report_job_execution_time", String.valueOf(System.currentTimeMillis()));
            Toast.makeText(context, "Reporting Job Has Started, It will take some time", Toast.LENGTH_LONG).show();
            RecurringIndicatorGeneratingJob.scheduleJobImmediately(RecurringIndicatorGeneratingJob.TAG);
        } else {
            Toast.makeText(context, "Reporting Job Has Already Been Started, Try again in 30 mins", Toast.LENGTH_LONG).show();
        }
    }

    public static boolean timeBetweenLastExecutionAndNow(int i, String reportJobExecutionTime) {
        try {
            long executionTime = Long.parseLong(reportJobExecutionTime);
            long now = System.currentTimeMillis();
            long diffNowExecutionTime = now - executionTime;
            return TimeUnit.MILLISECONDS.toMinutes(diffNowExecutionTime) > i;
        } catch (NumberFormatException e) {
            Timber.e(e);
            return false;
        }
    }

    public static boolean getSyncStatus() {
        String synComplete = KipApplication.getInstance().context().allSharedPreferences().getPreference("syncComplete");
        boolean isSyncComplete = false;
        if (StringUtils.isBlank(synComplete)) {
            KipApplication.getInstance().context().allSharedPreferences().savePreference("syncComplete", String.valueOf(false));
        } else {
            isSyncComplete = Boolean.parseBoolean(synComplete);
        }
        return isSyncComplete;
    }

    public static void updateSyncStatus(boolean isComplete) {
        KipApplication.getInstance().context().allSharedPreferences().savePreference("syncComplete", String.valueOf(isComplete));
    }


    public static HashMap<String, String> generateKeyValuesFromEvent(@NonNull Event event) {
        HashMap<String, String> keyValues = new HashMap<>();
        List<Obs> obs = event.getObs();
        for (Obs observation : obs) {
            String key = observation.getFormSubmissionField();
            List<Object> humanReadableValues = observation.getHumanReadableValues();
            if (humanReadableValues.size() > 0) {
                String value = (String) humanReadableValues.get(0);
                if (!TextUtils.isEmpty(value)) {
                    if (humanReadableValues.size() > 1) {
                        value = humanReadableValues.toString();
                    }
                    keyValues.put(key, value);
                    continue;
                }
            }
            List<Object> values = observation.getValues();
            if (values.size() > 0) {
                String value = (String) values.get(0);
                if (!TextUtils.isEmpty(value)) {
                    if (values.size() > 1) {
                        value = values.toString();
                    }
                    keyValues.put(key, value);
                }
            }
        }
        return keyValues;
    }

    public static JSONArray getMultiStepFormFields(@NonNull JSONObject jsonForm) {
        JSONArray fields = new JSONArray();
        try {
            if (jsonForm.has(JsonFormConstants.COUNT)) {
                int stepCount = Integer.parseInt(jsonForm.getString(JsonFormConstants.COUNT));
                for (int i = 0; i < stepCount; i++) {
                    String stepName = JsonFormConstants.STEP + (i + 1);
                    JSONObject step = jsonForm.optJSONObject(stepName);
                    if (step != null) {
                        JSONArray stepFields = step.optJSONArray(JsonFormConstants.FIELDS);
                        if (stepFields != null) {
                            for (int k = 0; k < stepFields.length(); k++) {
                                JSONObject field = stepFields.optJSONObject(k);
                                if (field != null) {
                                    fields.put(field);
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Timber.e(e, " --> getMultiStepFormFields()");
        }
        return fields;
    }

    private static void saveEvent(@NonNull org.smartregister.clientandeventmodel.Event event, @NonNull String baseEntityId) throws JSONException {
        JSONObject eventJson = new JSONObject(KipJsonFormUtils.gson.toJson(event));
        KipApplication.getInstance().getEcSyncHelper().addEvent(baseEntityId, eventJson);
    }

    public static void initiateEventProcessing(@NonNull List<String> formSubmissionIds) throws Exception {
        long lastSyncTimeStamp = Utils.getAllSharedPreferences().fetchLastUpdatedAtDate(0);
        Date lastSyncDate = new Date(lastSyncTimeStamp);
        KipApplication.getInstance().getClientProcessor()
                .processClient(
                        KipApplication.getInstance()
                                .getEcSyncHelper()
                                .getEvents(formSubmissionIds));

        Utils.getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
    }

    public static List<Map<String, String>> pupulateOpenMRSLocationsFromCSV(Context context, String csvFileName, Map<Integer, String> columns){

        List<Map<String, String>> result = new ArrayList<>();

        try {
            InputStream is = org.smartregister.util.Utils.getAssetFileInputStream(context, csvFileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    Map<String, String> csvValues = new HashMap<>();
                    String[] rowData = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    if (!TextUtils.isDigitsOnly(rowData[0])) {
                        continue;
                    }
                    for (Integer key : columns.keySet()) {
                        String value = rowData[key];
                        csvValues.put(columns.get(key), value);

                    }
                    result.add(csvValues);
                }
            } catch (IOException e) {
                Timber.e( "populateMohIndicatorsTableFromCSV: error reading csv file " + Log.getStackTraceString(e));

            } finally {
                try {
                    is.close();
                    reader.close();
                } catch (Exception e) {
                    Timber.e( "populateMohIndicatorsTableFromCSV: unable to close inputstream/bufferedreader " + Log.getStackTraceString(e));
                }
            }

        } catch (Exception e) {
            Timber.e( "populateMohIndicatorsTableFromCSV " + Log.getStackTraceString(e));
        }
        return result;
    }

    public static void saveOpenMrsLocation(SQLiteDatabase db){

        List<Map<String, String>> csvData = KipChildUtils.pupulateOpenMRSLocationsFromCSV(
                context, KipLocationRepository.LOCATIONS_CSV_FILE, KipLocationRepository.CSV_COLUMN_MAPPING);
        KipLocationRepository kipLocationRepository = KipApplication.getInstance().kipLocationRepository();
        kipLocationRepository.save(db, csvData);
    }
}
