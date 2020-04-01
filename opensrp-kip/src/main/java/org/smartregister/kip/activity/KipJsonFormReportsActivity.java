package org.smartregister.kip.activity;

import android.os.Bundle;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.kip.R;
import org.smartregister.kip.fragment.KipJsonFormFragment;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.stock.activity.StockJsonFormActivity;

import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-07-11
 */

public class KipJsonFormReportsActivity extends StockJsonFormActivity {

    private KipJsonFormFragment kipJsonFormFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initializeFormFragment() {
        kipJsonFormFragment = KipJsonFormFragment.getFormFragment(JsonFormConstants.FIRST_STEP_NAME);
        getSupportFragmentManager().beginTransaction()
                .add(com.vijay.jsonwizard.R.id.container, kipJsonFormFragment).commit();
    }

    @Override
    public void writeValue(String stepName, String key, String value, String openMrsEntityParent, String openMrsEntity, String openMrsEntityId) throws JSONException {
        super.writeValue(stepName, key, value, openMrsEntityParent, openMrsEntity, openMrsEntityId);
        refreshCalculateLogic(key, value);
    }

    public boolean checkIfBalanceNegative() {
        boolean balanceCheck = true;
        String balanceString = kipJsonFormFragment.getRelevantTextViewString(getString(R.string.balance));

        if (balanceString.contains(getString(R.string.new_balance)) && StringUtils.isNumeric(balanceString)) {
            int balance = Integer.parseInt(balanceString.replace(getString(R.string.new_balance), "").trim());
            if (balance < 0) {
                balanceCheck = false;
            }
        }

        return balanceCheck;
    }

    public boolean checkIfAtLeastOneServiceGiven() {
        JSONObject object = getStep("step1");
        try {
            if (object.getString(KipConstants.KEY.TITLE).contains("Record out of catchment area service")) {
                JSONArray fields = object.getJSONArray(KipConstants.KEY.FIELDS);
                for (int i = 0; i < fields.length(); i++) {
                    JSONObject vaccineGroup = fields.getJSONObject(i);
                    if (vaccineGroup.has(KipConstants.KEY.KEY) && vaccineGroup.has(KipConstants.KEY.IS_VACCINE_GROUP)) {
                        if (vaccineGroup.getBoolean(KipConstants.KEY.IS_VACCINE_GROUP) && vaccineGroup.has(KipConstants.KEY.OPTIONS)) {
                            JSONArray vaccineOptions = vaccineGroup.getJSONArray(KipConstants.KEY.OPTIONS);
                            for (int j = 0; j < vaccineOptions.length(); j++) {
                                JSONObject vaccineOption = vaccineOptions.getJSONObject(j);
                                if (vaccineOption.has(KipConstants.KEY.VALUE) && vaccineOption.getBoolean(KipConstants.KEY.VALUE)) {
                                    return true;
                                }
                            }
                        }
                    } else if (vaccineGroup.has(KipConstants.KEY.KEY) && vaccineGroup.getString(KipConstants.KEY.KEY).equals("Weight_Kg")
                            && vaccineGroup.has(KipConstants.KEY.VALUE) && vaccineGroup.getString(KipConstants.KEY.VALUE).length() > 0) {
                        return true;
                    }
                }
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        return false;
    }

}
