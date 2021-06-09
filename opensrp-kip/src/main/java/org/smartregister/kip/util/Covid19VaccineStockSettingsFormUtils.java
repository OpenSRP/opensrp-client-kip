package org.smartregister.kip.util;

import android.content.Context;
import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.repository.AllSharedPreferences;

public class Covid19VaccineStockSettingsFormUtils {
    public static JSONObject structureFormForRequest(@NonNull Context context) throws Exception {
        AllSharedPreferences allSharedPreferences = KipApplication.getInstance().getContext().userService().getAllSharedPreferences();
        String providerId = allSharedPreferences.fetchRegisteredANM();
        String locationId = allSharedPreferences.fetchDefaultLocalityId(providerId);
        String team = allSharedPreferences.fetchDefaultTeam(providerId);
        String teamId = allSharedPreferences.fetchDefaultTeamId(providerId);

        JSONObject ancSiteCharacteristicsTemplate = TemplateUtils.getTemplateAsJson(context, KipConstants.Settings.VACCINE_STOCK_IDENTIFIER);
        if (ancSiteCharacteristicsTemplate != null) {
            ancSiteCharacteristicsTemplate.put(KipConstants.TemplateUtils.Settings.TEAM_ID, teamId);
            ancSiteCharacteristicsTemplate.put(KipConstants.TemplateUtils.Settings.TEAM, team);
            ancSiteCharacteristicsTemplate.put(KipConstants.TemplateUtils.Settings.LOCATION_ID, locationId);
            ancSiteCharacteristicsTemplate.put(KipConstants.TemplateUtils.Settings.PROVIDER_ID, providerId);
        }

        return ancSiteCharacteristicsTemplate;
    }
}
