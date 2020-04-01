package org.smartregister.kip.presenter;

import android.app.Activity;

import org.smartregister.anc.library.contract.RegisterContract;
import org.smartregister.anc.library.presenter.RegisterPresenter;
import org.smartregister.kip.view.NavigationMenu;

import java.lang.ref.WeakReference;

public class KipAncRegisterPresenter extends RegisterPresenter {
    private WeakReference<RegisterContract.View> gizAncViewReference;

    public KipAncRegisterPresenter(RegisterContract.View view) {
        super(view);
        this.gizAncViewReference = new WeakReference(view);

    }

    @Override
    public void onRegistrationSaved(boolean isEdit) {
        super.onRegistrationSaved(isEdit);
        NavigationMenu navigationMenu = NavigationMenu.getInstance((Activity) gizAncViewReference.get(), null, null);
        if (navigationMenu != null) {
            navigationMenu.runRegisterCount();
        }
    }
}
