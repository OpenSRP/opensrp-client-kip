package org.smartregister.kip.presenter;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.child.presenter.ChildFormFragmentPresenter;
import org.smartregister.kip.R;
import org.smartregister.kip.activity.ChildFormActivity;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.domain.KipLocation;
import org.smartregister.kip.fragment.KipChildFormFragment;
import org.smartregister.kip.repository.KipLocationRepository;
import org.smartregister.kip.util.KipConstants;

import java.util.List;

import timber.log.Timber;

public class KipChildFormFragmentPresenter extends ChildFormFragmentPresenter {

    private KipChildFormFragment formFragment;

    public static final String CE_COUNTY = "Ce_County";
    public static final String CE_SUBCOUNTY = "Ce_Sub_County";
    public static final String CE_WARD = "Ce_Ward";

    public KipChildFormFragmentPresenter(JsonFormFragment formFragment, JsonFormInteractor jsonFormInteractor) {
        super(formFragment, jsonFormInteractor);
        this.formFragment = (KipChildFormFragment) formFragment;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        super.onItemSelected(parent, view, position, id);
        String key = (String) parent.getTag(R.id.key);
        KipLocationRepository locationRepository = KipApplication.getInstance().locationRepository();
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

        if (key.equals(CE_COUNTY)){

            String value = null;

            MaterialSpinner spinnerCounty = (MaterialSpinner) ((ChildFormActivity) formFragment.getActivity()).getFormDataView(JsonFormConstants.STEP1 + ":" + CE_COUNTY);

            int selectedItemPos = spinnerCounty.getSelectedItemPosition();

            if (selectedItemPos > 0) {
                selectedItemPos = selectedItemPos - 1;
                value = (String) spinnerCounty.getAdapter().getItem(selectedItemPos);
            }

            MaterialSpinner spinnerSubCounty = (MaterialSpinner) ((ChildFormActivity) formFragment.getActivity()).getFormDataView(JsonFormConstants.STEP1 + ":" + CE_SUBCOUNTY);
            processSpinner(position,spinnerSubCounty,locationRepository,value);

        }

        if (key.equals(CE_SUBCOUNTY)){
            MaterialSpinner spinnerSubCountyForWard = (MaterialSpinner) ((ChildFormActivity) formFragment.getActivity()).getFormDataView(JsonFormConstants.STEP1 + ":" + CE_SUBCOUNTY);
            int selectedItemPosss = spinnerSubCountyForWard.getSelectedItemPosition();
            String value = null;
            if (selectedItemPosss > 0) {
                selectedItemPosss = selectedItemPosss - 1;
                value = (String) spinnerSubCountyForWard.getAdapter().getItem(selectedItemPosss);

            }
            MaterialSpinner spinnerWard = (MaterialSpinner) ((ChildFormActivity) formFragment.getActivity()).getFormDataView(JsonFormConstants.STEP1 + ":" + CE_WARD);
            processSpinner(position,spinnerWard,locationRepository,value);

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

    private void processSpinner(int position, MaterialSpinner spinner, KipLocationRepository locationRepository, String value){
        if (position >= 0) {

            KipLocation location = locationRepository.getLocationByName(value);
            ArrayAdapter<String> wardAdapter;

            int indexToSelect = -1;
            String[] locs;
            if (location != null) {
                List<KipLocation> subCountyLocations = locationRepository.getChildLocations(location.getUuid());

                int size = subCountyLocations.size();

                locs = new String[Math.max(1, size)];
                if (size > 0) {
                    String locationName;
                    for (int n = 0; n < size; n++) {
                        locationName = subCountyLocations.get(n).getName();
                        locs[n] = locationName;
                    }
                } else {
                    locs[0] = "Other";
                }

            } else {
                locs = new String[]{"Other"};
            }
            wardAdapter = new ArrayAdapter<>(getView().getContext(), R.layout.native_form_simple_list_item_1, locs);
            spinner.setAdapter(wardAdapter);
            if (indexToSelect != -1) {
                spinner.setSelection(indexToSelect);
            }
        }
    }

}
