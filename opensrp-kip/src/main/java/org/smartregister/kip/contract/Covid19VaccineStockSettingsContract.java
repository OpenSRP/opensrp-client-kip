package org.smartregister.kip.contract;

import org.json.JSONException;

import java.util.Map;

public interface Covid19VaccineStockSettingsContract {

    interface Presenter {
        Covid19VaccineStockSettingsContract.View getCovid19VaccineStockSettingsView();
        void launchCovid19VaccineStockSettingsForm();
        void launchCovid19VaccineStockSettingsForEdit();
        void onDestroy(boolean isChangingConfiguration);
        void saveCovid19VaccineStockSettings(String jsonString);
    }

    interface View {
        void launchCovid19VaccineStockSettingsForm();
        void showProgressDialog(int messageStringIdentifier);
        void hideProgressDialog();
        void goToLastPage();
        void launchCovid19VaccineStockSettingsFormForEdit(Map<String, String> characteristics);
    }

    interface Model {
        Map<String, String> processCovid19VaccineStockSettings(String jsonString);
    }

    interface Interactor {
        void saveCovid19VaccineStockSettings(Map<String, String> covid19VaccineStockSettingsMap) throws JSONException;
    }
}
