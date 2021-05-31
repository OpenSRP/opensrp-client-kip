package org.smartregister.kip.presenter;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;

import org.smartregister.kip.R;
import org.smartregister.kip.activity.OpdFormActivity;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.domain.KipLocation;
import org.smartregister.kip.fragment.KipOpdFormFragment;
import org.smartregister.kip.repository.KipLocationRepository;
import org.smartregister.opd.fragment.BaseOpdFormFragment;
import org.smartregister.opd.presenter.OpdFormFragmentPresenter;

import java.util.List;

public class KipOpdFormFragmentPresenter extends OpdFormFragmentPresenter {

    private KipOpdFormFragment formFragment;

    public static final String CE_COUNTY = "Ce_County";
    public static final String CE_SUBCOUNTY = "Ce_Sub_County";
    public static final String CE_WARD = "Ce_Ward";

    public KipOpdFormFragmentPresenter(BaseOpdFormFragment formFragment, JsonFormInteractor jsonFormInteractor) {
        super(formFragment, jsonFormInteractor);
        this.formFragment = (KipOpdFormFragment) formFragment;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        super.onItemSelected(parent, view, position, id);
        KipLocationRepository locationRepository = KipApplication.getInstance().locationRepository();
        String key = (String) parent.getTag(R.id.key);


        if (key.equals(CE_COUNTY)){

            String value = null;

                MaterialSpinner spinnerCounty = (MaterialSpinner) ((OpdFormActivity) formFragment.getActivity()).getFormDataView(JsonFormConstants.STEP1 + ":" + CE_COUNTY);

                int selectedItemPos = spinnerCounty.getSelectedItemPosition();

                if (selectedItemPos > 0) {
                    selectedItemPos = selectedItemPos - 1;
                    value = (String) spinnerCounty.getAdapter().getItem(selectedItemPos);
                }

            MaterialSpinner spinnerSubCounty = (MaterialSpinner) ((OpdFormActivity) formFragment.getActivity()).getFormDataView(JsonFormConstants.STEP1 + ":" + CE_SUBCOUNTY);
                processSpinner(position,spinnerSubCounty,locationRepository,value);

        }

        if (key.equals(CE_SUBCOUNTY)){
            MaterialSpinner spinnerSubCountyForWard = (MaterialSpinner) ((OpdFormActivity) formFragment.getActivity()).getFormDataView(JsonFormConstants.STEP1 + ":" + CE_SUBCOUNTY);
            int selectedItemPosss = spinnerSubCountyForWard.getSelectedItemPosition();
            String value = null;
            if (selectedItemPosss > 0) {
                selectedItemPosss = selectedItemPosss - 1;
                value = (String) spinnerSubCountyForWard.getAdapter().getItem(selectedItemPosss);

            }
            MaterialSpinner spinnerWard = (MaterialSpinner) ((OpdFormActivity) formFragment.getActivity()).getFormDataView(JsonFormConstants.STEP1 + ":" + CE_WARD);

            processSpinner(position,spinnerWard,locationRepository,value);

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
