package org.smartregister.kip.util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.vijay.jsonwizard.customviews.TreeViewDialog;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.smartregister.child.util.Constants;
import org.smartregister.child.util.Utils;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.domain.db.Client;
import org.smartregister.domain.db.EventClient;
import org.smartregister.domain.form.FormLocation;
import org.smartregister.kip.BuildConfig;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.event.BaseEvent;
import org.smartregister.kip.listener.OnLocationChangeListener;
import org.smartregister.kip.view.NavigationMenu;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.AssetHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class KipChildUtils extends Utils {

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
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
            KipApplication.getInstance().getApplicationContext().createConfigurationContext(configuration);
        } else {
            configuration.locale = locale;
            resources.updateConfiguration(configuration, displayMetrics);
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
            newContext = newContext.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
        return newContext;
    }

    public static void postStickyEvent(
            BaseEvent event) {//Each Sticky event must be manually cleaned by calling KipUtils.removeStickyEvent
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

    public static boolean updateChildDeath(@NonNull EventClient eventClient) {
        Client client = eventClient.getClient();
        ContentValues values = new ContentValues();

        if (client.getDeathdate() == null) {
            Timber.e(new Exception(), "Death event for %s cannot be processed because deathdate is NULL : %s"
                    , client.getFirstName() + " " + client.getLastName(), new Gson().toJson(eventClient));
            return false;
        }

        values.put(Constants.KEY.DOD, Utils.convertDateFormat(client.getDeathdate()));
        values.put(Constants.KEY.DATE_REMOVED, Utils.convertDateFormat(client.getDeathdate().toDate(), Utils.DB_DF));
        String tableName = Utils.metadata().childRegister.tableName;
        AllCommonsRepository allCommonsRepository = KipApplication.getInstance().context().allCommonsRepositoryobjects(tableName);
        if (allCommonsRepository != null) {
            allCommonsRepository.update(tableName, values, client.getBaseEntityId());
            allCommonsRepository.updateSearch(client.getBaseEntityId());
        }

        return true;
    }
    @NonNull
    public static Locale getLocale(Context context){
        if (context == null) {
            return Locale.getDefault();
        } else {
            return context.getResources().getConfiguration().locale;
        }
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

    public static void showLocations(@Nullable Activity context, @NonNull OnLocationChangeListener onLocationChangeListener, @Nullable NavigationMenu navigationMenu) {
        try {
            ArrayList<String> allLevels = getLocationLevels();
            ArrayList<String> healthFacilities = getHealthFacilityLevels();
            ArrayList<String> defaultLocation = (ArrayList<String>) LocationHelper.getInstance().generateDefaultLocationHierarchy(allLevels);
            List<FormLocation> upToFacilities = LocationHelper.getInstance().generateLocationHierarchyTree(false, healthFacilities);
            String upToFacilitiesString = AssetHandler.javaToJsonString(upToFacilities, new TypeToken<List<FormLocation>>() {
            }.getType());
            TreeViewDialog treeViewDialog = new TreeViewDialog(context,
                    new JSONArray(upToFacilitiesString), defaultLocation, defaultLocation);
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

    @NonNull
    private static ArrayList<String> getLocationLevels() {
        return new ArrayList<>(Arrays.asList(BuildConfig.LOCATION_LEVELS));
    }

    @NonNull
    private static ArrayList<String> getHealthFacilityLevels() {
        return new ArrayList<>(Arrays.asList(BuildConfig.HEALTH_FACILITY_LEVELS));
    }

    /**
     * This method is only intended to be used for processing KIP_MOH_710_Report.csv
     *
     * @param csvFileName
     * @param columns     this map has the db column name as value and the csv column no as the key
     * @return each map is db row with key as the column name and value as the value from the csv file
     */
    public static List<Map<String, String>> populateMohIndicatorsTableFromCSV(Context context, String csvFileName, Map<Integer, String> columns) {
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
                Timber.e(e, "-->populateMohIndicatorsTableFromCSV: error reading csv file ");

            } finally {
                try {
                    is.close();
                    reader.close();
                } catch (Exception e) {
                    Timber.e(e, "-->populateMohIndicatorsTableFromCSV: unable to close inputstream/bufferedreader ");
                }
            }

        } catch (Exception e) {
            Timber.e(e, "-->populateMohIndicatorsTableFromCSV ");
        }
        return result;
    }

}
