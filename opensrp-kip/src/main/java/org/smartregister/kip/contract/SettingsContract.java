package org.smartregister.kip.contract;

public interface SettingsContract {
    interface Presenter extends BaseSettingsContract.BasePresenter {
        BaseSettingsContract.View getView();
    }

    interface View extends BaseSettingsContract.View {
    }

    interface Interactor extends BaseSettingsContract.Interactor {
    }
}
