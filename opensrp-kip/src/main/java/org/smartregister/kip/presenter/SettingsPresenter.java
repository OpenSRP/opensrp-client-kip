package org.smartregister.kip.presenter;


import org.smartregister.kip.R;
import org.smartregister.kip.contract.Covid19VaccineStockSettingsContract;
import org.smartregister.kip.domain.KipServerSetting;
import org.smartregister.kip.helper.KipServerSettingHelper;
import org.smartregister.kip.interactor.SettingsInteractor;
import org.smartregister.kip.model.Covid19VaccineStockSettingsModel;
import org.smartregister.kip.util.KipConstants;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;


public class SettingsPresenter implements Covid19VaccineStockSettingsContract.Presenter {

    private WeakReference<Covid19VaccineStockSettingsContract.View> view;
    private final Covid19VaccineStockSettingsContract.Interactor interactor;
    private final Covid19VaccineStockSettingsContract.Model model;

    public SettingsPresenter(Covid19VaccineStockSettingsContract.View view) {
        this.view = new WeakReference<>(view);
        interactor = new SettingsInteractor();
        model = new Covid19VaccineStockSettingsModel();
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        this.view = null;
    }

    @Override
    public void launchCovid19VaccineStockSettingsForm() {
        getCovid19VaccineStockSettingsView().launchCovid19VaccineStockSettingsForm();
    }

    @Override
    public void launchCovid19VaccineStockSettingsForEdit() {
        Map<String, String> settings = getSCovid19SettingsMapByType();
        getCovid19VaccineStockSettingsView().launchCovid19VaccineStockSettingsFormForEdit(settings);
    }

    protected Map<String, String> getSCovid19SettingsMapByType() {
        List<KipServerSetting> characteristicList = KipServerSettingHelper.fetchServerSettingsByTypeKey(KipConstants.Settings.VACCINE_STOCK_IDENTIFIER);

        Map<String, String> settingsMap = new HashMap<>();
        for (KipServerSetting characteristic : characteristicList) {
            settingsMap.put(characteristic.getKey(), characteristic.getValue() + ":" + characteristic.getDescription());
        }
        return settingsMap;
    }

    @Override
    public Covid19VaccineStockSettingsContract.View getCovid19VaccineStockSettingsView() {
        if (this.view != null) {
            return this.view.get();
        } else {
            return null;
        }
    }

    @Override
    public void saveCovid19VaccineStockSettings(String jsonString) {
        getCovid19VaccineStockSettingsView().showProgressDialog(R.string.saving_dialog_title);
        Map<String, String> settings = model.processCovid19VaccineStockSettings(jsonString);
        try {
            interactor.saveCovid19VaccineStockSettings(settings);
        } catch (Exception e) {
            Timber.e(e);
        }

        getCovid19VaccineStockSettingsView().hideProgressDialog();
        getCovid19VaccineStockSettingsView().goToLastPage();
    }
}
