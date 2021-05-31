package org.smartregister.kip.presenter;

import org.smartregister.kip.contract.BaseSettingsContract;
import org.smartregister.kip.contract.SettingsContract;
import org.smartregister.kip.domain.KipServerSetting;

import java.lang.ref.WeakReference;
import java.util.List;


public abstract class BaseSettingsPresenter implements SettingsContract.Presenter {

    private WeakReference<BaseSettingsContract.View> view;
    private BaseSettingsContract.Interactor interactor;

    public BaseSettingsPresenter(BaseSettingsContract.View view) {
        this.view = new WeakReference<>(view);
        interactor = getInteractor();
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        view = null;//set to null on destroy

        // Inform interactor
        interactor.onDestroy(isChangingConfiguration);

        // Activity destroyed set interactor to null
        if (!isChangingConfiguration) {
            interactor = null;
        }
    }

    @Override
    public void renderView(List<KipServerSetting> data) {
        getView().renderSettings(data);
    }

    @Override
    public BaseSettingsContract.View getView() {
        if (view != null) {
            return view.get();
        } else {
            return null;
        }
    }

    @Override
    public void getSettings() {
        interactor.fetchSettings();
    }

    public abstract BaseSettingsContract.Interactor getInteractor();
}
