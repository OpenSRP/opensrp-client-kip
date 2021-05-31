package org.smartregister.kip.activity;

import android.os.Bundle;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.smartregister.kip.fragment.KipJsonFormFragment;
import org.smartregister.stock.activity.StockJsonFormActivity;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-07-11
 */

public class KipJsonFormReportsActivity extends StockJsonFormActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initializeFormFragment() {
        KipJsonFormFragment kipJsonFormFragment = KipJsonFormFragment.getFormFragment(JsonFormConstants.FIRST_STEP_NAME);
        getSupportFragmentManager().beginTransaction()
                .add(com.vijay.jsonwizard.R.id.container, kipJsonFormFragment).commit();
    }
}
