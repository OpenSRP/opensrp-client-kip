package org.smartregister.kip.interactor;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.domain.Setting;
import org.smartregister.domain.SyncStatus;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.contract.Covid19VaccineStockSettingsContract;
import org.smartregister.kip.util.Covid19VaccineStockSettingsFormUtils;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.kip.util.KipJsonFormUtils;
import org.smartregister.repository.AllSettings;
import org.smartregister.util.Utils;

import java.util.Map;

import timber.log.Timber;

/**
 * Created by ndegwamartin on 13/07/2018.
 */
public class SettingsInteractor implements Covid19VaccineStockSettingsContract.Interactor {

    @Override
    public void saveCovid19VaccineStockSettings(Map<String, String> covid19VaccineStockSettingsMap) throws JSONException {
        JSONArray localSettings;
        JSONObject settingObject;
        Context context = KipApplication.getInstance().getApplicationContext();
        Setting covid19VaccineStockSettings = getAllSettingsRepo().getSetting(KipConstants.Settings.VACCINE_STOCK_IDENTIFIER);
        boolean canSaveInitialSetting = getPropertyForInitialSaveAction(context);
        if (covid19VaccineStockSettings == null) {

            if (canSaveInitialSetting) {
                try {
                    settingObject = Covid19VaccineStockSettingsFormUtils.structureFormForRequest(context);
                    covid19VaccineStockSettings = new Setting();
                } catch (Exception e) {
                    Timber.e(e);
                    return;
                }
            } else {
                return;
            }
        } else {
            settingObject = new JSONObject(covid19VaccineStockSettings.getValue());
        }
        localSettings = settingObject.has(AllConstants.SETTINGS) ? settingObject.getJSONArray(AllConstants.SETTINGS) : null;
        if (localSettings != null) {
            for (int i = 0; i < localSettings.length(); i++) {
                JSONObject localSetting = localSettings.getJSONObject(i);
                if (localSetting.getString(KipConstants.KeyUtils.KEY).equalsIgnoreCase(KipConstants.AZ_OXFORD_VIALS_AMOUNT)) {
                    updateSettings(covid19VaccineStockSettingsMap, localSetting, KipConstants.AZ_OXFORD_VIALS_AMOUNT);
                } else if (localSetting.getString(KipConstants.KeyUtils.KEY).equalsIgnoreCase(KipConstants.SINOPHARM_VIALS_AMOUNT)) {
                    updateSettings(covid19VaccineStockSettingsMap, localSetting, KipConstants.SINOPHARM_VIALS_AMOUNT);
                } else if (localSetting.getString(KipConstants.KeyUtils.KEY).equalsIgnoreCase(KipConstants.SINOVAC_VIALS_AMOUNT)) {
                    updateSettings(covid19VaccineStockSettingsMap, localSetting, KipConstants.SINOVAC_VIALS_AMOUNT);
                } else if (localSetting.getString(KipConstants.KeyUtils.KEY).equalsIgnoreCase(KipConstants.PFIZER_VIALS_AMOUNT)) {
                    updateSettings(covid19VaccineStockSettingsMap, localSetting, KipConstants.PFIZER_VIALS_AMOUNT);
                } else {
                    updateSettings(covid19VaccineStockSettingsMap, localSetting, KipConstants.MODERNA_VIALS_AMOUNT);
                }
            }
        }

        settingObject.put(AllConstants.SETTINGS, localSettings);
        covid19VaccineStockSettings.setValue(settingObject.toString());
        covid19VaccineStockSettings.setKey(KipConstants.Settings.VACCINE_STOCK_IDENTIFIER);
        covid19VaccineStockSettings.setSyncStatus(SyncStatus.PENDING.name());
        getAllSettingsRepo().putSetting(covid19VaccineStockSettings);
        KipApplication.getInstance().populateGlobalSettings();
    }

    private void updateSettings(Map<String, String> covid19VaccineStockSettingsMap, JSONObject localSetting, String key) throws JSONException {
        localSetting.put(KipConstants.KeyUtils.VALUE, addTheStock(localSetting.optString(KipConstants.KeyUtils.VALUE, "0"), KipJsonFormUtils.splitValue(covid19VaccineStockSettingsMap.get(key))[2]));
        localSetting.put(KipConstants.KeyUtils.DESCRIPTION, KipJsonFormUtils.splitValue(covid19VaccineStockSettingsMap.get(key))[1] + ":" + KipJsonFormUtils.splitValue(covid19VaccineStockSettingsMap.get(key))[0]);
    }

    protected AllSettings getAllSettingsRepo() {
        return KipApplication.getInstance().getContext().allSettings();
    }

    private Boolean getPropertyForInitialSaveAction(Context context) {
        String value = Utils.getProperties(context).getProperty(KipConstants.Properties.CAN_SAVE_SITE_INITIAL_SETTING, "false");
        return Boolean.valueOf(value);
    }

    private String addTheStock(String previousValue, String currentValue) {
        int previous = Integer.parseInt(previousValue);
        int current = Integer.parseInt(currentValue);
        int total = previous != current ? previous + current : current;

        return String.valueOf(total);
    }
}
