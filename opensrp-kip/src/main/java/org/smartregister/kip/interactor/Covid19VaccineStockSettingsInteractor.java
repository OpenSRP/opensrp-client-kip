package org.smartregister.kip.interactor;

import org.smartregister.kip.contract.BaseSettingsContract;
import org.smartregister.kip.contract.SettingsContract;
import org.smartregister.kip.task.FetchCovid19VaccineStockSettingsTask;

public class Covid19VaccineStockSettingsInteractor implements BaseSettingsContract.Interactor {
    private SettingsContract.Presenter presenter;

    public Covid19VaccineStockSettingsInteractor(SettingsContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        if (!isChangingConfiguration) {
            presenter = null;
        }
    }

    @Override
    public void fetchSettings() {
        new FetchCovid19VaccineStockSettingsTask(presenter).execute();
    }

}
