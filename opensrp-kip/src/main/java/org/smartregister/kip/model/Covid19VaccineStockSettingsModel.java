package org.smartregister.kip.model;

import org.smartregister.kip.contract.Covid19VaccineStockSettingsContract;
import org.smartregister.kip.util.KipJsonFormUtils;

import java.util.Map;

public class Covid19VaccineStockSettingsModel implements Covid19VaccineStockSettingsContract.Model {

    @Override
    public Map<String, String> processCovid19VaccineStockSettings(String jsonString) {
        return KipJsonFormUtils.processCovid19VaccineStockSettings(jsonString);
    }

}
