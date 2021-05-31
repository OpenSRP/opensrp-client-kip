package org.smartregister.kip.presenter;

import org.smartregister.kip.contract.BaseSettingsContract;
import org.smartregister.kip.interactor.Covid19VaccineStockSettingsInteractor;


public class Covid19VaccineStockSettingsPresenter extends BaseSettingsPresenter {

    public Covid19VaccineStockSettingsPresenter(BaseSettingsContract.View view) {
        super(view);
    }

    @Override
    public BaseSettingsContract.Interactor getInteractor() {
        return new Covid19VaccineStockSettingsInteractor(this);
    }
}
