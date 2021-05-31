package org.smartregister.kip.contract;

import org.smartregister.kip.domain.KipServerSetting;

import java.util.List;

public interface BaseSettingsContract {
    interface BasePresenter {
        void onDestroy(boolean isChangingConfiguration);
        void renderView(List<KipServerSetting> data);
        void getSettings();
        Interactor getInteractor();
    }


    interface View {
        void renderSettings(List<KipServerSetting> settings);
    }

    interface Interactor {
        void onDestroy(boolean isChangingConfiguration);
        void fetchSettings();
    }
}
