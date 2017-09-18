package org.smartregister.kip.widgets;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.widgets.SpinnerFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.api.domain.Location;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.repository.LocationRepository;
import org.smartregister.util.StringUtil;

import java.util.Arrays;
import java.util.List;

import util.JsonFormUtils;

/**
 * Created by amosl on 6/13/17.
 */

public class KipSpinnerFactory extends SpinnerFactory {

    private static final String TAG = KipSpinnerFactory.class.getCanonicalName();

    @Override
    public List<View> getViewsFromJson(final String stepName, final Context context, final JsonFormFragment formFragment, JSONObject jsonObject, final CommonListener listener) throws Exception {

        final List<View> views = super.getViewsFromJson(stepName, context, formFragment, jsonObject, listener);

        if (jsonObject.has("key")) {

            final String key = jsonObject.getString("key");

            final MaterialSpinner spinner = (MaterialSpinner) views.get(0);

            if (key.equalsIgnoreCase("Ce_County") || key.equalsIgnoreCase("Ce_Sub_County") || key.equalsIgnoreCase("Ce_Ward")) {

                views.remove(spinner);
                spinner.setTag(key);

                if (key.equalsIgnoreCase("Ce_County") || key.equalsIgnoreCase("Ce_Sub_County")) {

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position >= 0) {

                                String value = (String) parent.getItemAtPosition(position);
                                String parentKey = (String) parent.getTag(com.vijay.jsonwizard.R.id.key);
                                String openMrsEntityParent = (String) parent.getTag(com.vijay.jsonwizard.R.id.openmrs_entity_parent);
                                String openMrsEntity = (String) parent.getTag(com.vijay.jsonwizard.R.id.openmrs_entity);
                                String openMrsEntityId = (String) parent.getTag(com.vijay.jsonwizard.R.id.openmrs_entity_id);
                                try {
                                    formFragment.getJsonApi().writeValue(stepName, parentKey, value, openMrsEntityParent, openMrsEntity,
                                            openMrsEntityId);
                                } catch (JSONException e) {
                                    Log.e(TAG, e.getMessage(), e);
                                    e.printStackTrace();
                                }

                                MaterialSpinner childSpinner = null;
                                View v = (View) parent.getParent();

                                if (key.equalsIgnoreCase("Ce_County")) {
                                    childSpinner = (MaterialSpinner) v.findViewWithTag("Ce_Sub_County");
                                } else if (key.equalsIgnoreCase("Ce_Sub_County")) {
                                    childSpinner = (MaterialSpinner) v.findViewWithTag("Ce_Ward");
                                }

                                if (childSpinner != null) {
                                    int selectedItemPosition = childSpinner.getSelectedItemPosition();
                                    String selectedItem = selectedItemPosition != -1 ? childSpinner.getSelectedItem().toString() : null;
                                    LocationRepository locationRepository = KipApplication.getInstance().locationRepository();
                                    Location location = locationRepository.getLocationByName(value);
                                    ArrayAdapter<String> adapter;
                                    Log.d(TAG, "Name: " + value);
                                    Log.d(TAG, "Location: " + location != null ? location.toString() : " null");
                                    String[] locs;

                                    if (location != null) {
                                        Log.i(TAG, "Parent location is not null: " + location.toString());
                                        List<Location> locations = locationRepository.getChildLocations(location.getLocationId());
                                        int size = locations.size();
                                        locs = new String[Math.max(1, size)];

                                        if (size > 0) {
                                            String locationName;
                                            for (int n = 0; n < size; n++) {
                                                locationName = locations.get(n).getName();
                                                locs[n] = locationName;
                                                if(locationName.equals(selectedItem)){
                                                    selectedItemPosition = n;
                                                } else {
                                                    selectedItemPosition = -1;
                                                }
                                            }
                                        } else {
                                            locs[0] = "Other";
                                        }
                                    } else {
                                        locs = new String[]{"Other"};
                                        Log.i(TAG, "Parent location is null");
                                    }

                                    adapter = new ArrayAdapter<>(context, com.vijay.jsonwizard.R.layout.simple_list_item_1, locs);
                                    childSpinner.setAdapter(adapter);
                                    if(selectedItemPosition != -1) {
                                        childSpinner.setSelection(selectedItemPosition);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                }
                views.add(spinner);
            }
        }

        return views;
    }

}
