package org.smartregister.kip.fragment;

import android.os.Bundle;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;

import org.smartregister.child.fragment.ChildFormFragment;
import org.smartregister.child.presenter.ChildFormFragmentPresenter;
import org.smartregister.kip.interactor.ChildFormInteractor;
import org.smartregister.kip.presenter.KipChildFormFragmentPresenter;

import java.lang.ref.WeakReference;

public class KipChildFormFragment extends ChildFormFragment {

    private OnReactionVaccineSelected OnReactionVaccineSelected;

    public static KipChildFormFragment getFormFragment(String stepName) {
        KipChildFormFragment jsonFormFragment = new KipChildFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString(JsonFormConstants.JSON_FORM_KEY.STEPNAME, stepName);
        jsonFormFragment.setArguments(bundle);
        return jsonFormFragment;
    }

    public OnReactionVaccineSelected getOnReactionVaccineSelected() {
        return OnReactionVaccineSelected;
    }

    public void setOnReactionVaccineSelected(OnReactionVaccineSelected onReactionVaccineSelected) {
        this.OnReactionVaccineSelected = onReactionVaccineSelected;
    }

    @Override
    protected ChildFormFragmentPresenter createPresenter() {
        WeakReference kipChildFormFragmentWeakReference = new WeakReference<>(this);
        return new KipChildFormFragmentPresenter((JsonFormFragment) kipChildFormFragmentWeakReference.get(), ChildFormInteractor.getChildInteractorInstance());
    }

    public interface OnReactionVaccineSelected {
        void updateDatePicker(String date);
    }

    @Override
    public void onDestroy() {
        setOnReactionVaccineSelected(null);
        super.onDestroy();
    }
}
