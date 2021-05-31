package org.smartregister.kip.fragment;

import android.os.Bundle;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;

import org.smartregister.kip.presenter.KipOpdFormFragmentPresenter;
import org.smartregister.opd.adapter.ClientLookUpListAdapter;
import org.smartregister.opd.fragment.BaseOpdFormFragment;
import org.smartregister.opd.interactor.OpdFormInteractor;
import org.smartregister.opd.presenter.OpdFormFragmentPresenter;

import java.lang.ref.WeakReference;


public class KipOpdFormFragment extends BaseOpdFormFragment implements ClientLookUpListAdapter.ClickListener {

    public static JsonWizardFormFragment getFormFragment(String stepName) {
        KipOpdFormFragment jsonFormFragment = new KipOpdFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString(JsonFormConstants.JSON_FORM_KEY.STEPNAME, stepName);
        jsonFormFragment.setArguments(bundle);
        return jsonFormFragment;
    }

    @Override
    public void onResume() {
        setShouldSkipStep(true);
        super.onResume();
    }

    @Override
    protected OpdFormFragmentPresenter createPresenter() {
        WeakReference kipChildFormFragmentWeakReference = new WeakReference<>(this);
        return new KipOpdFormFragmentPresenter((BaseOpdFormFragment) kipChildFormFragmentWeakReference.get(), OpdFormInteractor.getInstance());
    }
}
