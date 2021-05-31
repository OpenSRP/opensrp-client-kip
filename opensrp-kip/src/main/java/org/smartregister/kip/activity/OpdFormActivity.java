package org.smartregister.kip.activity;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.smartregister.kip.fragment.KipOpdFormFragment;
import org.smartregister.opd.activity.BaseOpdFormActivity;

public class OpdFormActivity extends BaseOpdFormActivity {


    @Override
    public void initializeFormFragment() {
        initializeFormFragmentCore();
    }

    protected void initializeFormFragmentCore() {
        KipOpdFormFragment kipOpdFormFragment = (KipOpdFormFragment) KipOpdFormFragment.getFormFragment(JsonFormConstants.FIRST_STEP_NAME);
        getSupportFragmentManager().beginTransaction().add(com.vijay.jsonwizard.R.id.container, kipOpdFormFragment).commit();
    }
}