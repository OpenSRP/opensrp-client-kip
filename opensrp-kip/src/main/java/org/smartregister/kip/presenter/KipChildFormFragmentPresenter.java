package org.smartregister.kip.presenter;

import android.view.View;
import android.widget.AdapterView;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.child.presenter.ChildFormFragmentPresenter;
import org.smartregister.kip.R;
import org.smartregister.kip.activity.ChildFormActivity;
import org.smartregister.kip.fragment.KipChildFormFragment;
import org.smartregister.kip.util.KipConstants;

import timber.log.Timber;

public class KipChildFormFragmentPresenter extends ChildFormFragmentPresenter {

    private KipChildFormFragment formFragment;

    public KipChildFormFragmentPresenter(JsonFormFragment formFragment, JsonFormInteractor jsonFormInteractor) {
        super(formFragment, jsonFormInteractor);
        this.formFragment = (KipChildFormFragment) formFragment;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        super.onItemSelected(parent, view, position, id);
        String key = (String) parent.getTag(R.id.key);
        if (key.equals(KipConstants.MOTHER_TDV_DOSES)) {
            MaterialSpinner spinnerMotherTdvDoses = (MaterialSpinner) ((ChildFormActivity) formFragment.getActivity()).getFormDataView(JsonFormConstants.STEP1 + ":" + KipConstants.MOTHER_TDV_DOSES);
            MaterialSpinner spinnerProtectedAtBirth = (MaterialSpinner) ((ChildFormActivity) formFragment.getActivity()).getFormDataView(JsonFormConstants.STEP1 + ":" + KipConstants.PROTECTED_AT_BIRTH);
            if (spinnerMotherTdvDoses.getSelectedItemPosition() == 1) {
                spinnerProtectedAtBirth.setSelection(1, true);
            } else if (spinnerMotherTdvDoses.getSelectedItemPosition() != 0) {
                spinnerProtectedAtBirth.setSelection(2, true);
            } else {
                spinnerProtectedAtBirth.setSelection(0, true);
            }
        }

        try {
            if (key.equals(KipConstants.REACTION_VACCINE)) {
                MaterialSpinner spinnerReactionVaccine = (MaterialSpinner) ((ChildFormActivity) formFragment.getActivity()).getFormDataView(JsonFormConstants.STEP1 + ":" + KipConstants.REACTION_VACCINE);
                int selectedItemPos = spinnerReactionVaccine.getSelectedItemPosition();
                KipChildFormFragment.OnReactionVaccineSelected onReactionVaccineSelected = formFragment.getOnReactionVaccineSelected();
                if (selectedItemPos > 0) {
                    selectedItemPos = selectedItemPos - 1;
                    String reactionVaccine = (String) spinnerReactionVaccine.getAdapter().getItem(selectedItemPos);
                    if (StringUtils.isNotBlank(reactionVaccine) && (reactionVaccine.length() > 10)) {
                        String reactionVaccineDate = reactionVaccine.substring(reactionVaccine.length() - 11, reactionVaccine.length() - 1);
                        if (onReactionVaccineSelected != null) {
                            onReactionVaccineSelected.updateDatePicker(reactionVaccineDate);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

}
