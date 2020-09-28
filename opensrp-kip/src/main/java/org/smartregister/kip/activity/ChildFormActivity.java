package org.smartregister.kip.activity;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.smartregister.child.activity.BaseChildFormActivity;
import org.smartregister.child.util.Constants;
import org.smartregister.child.util.MotherLookUpUtils;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.kip.fragment.KipChildFormFragment;

import java.util.Map;

public class ChildFormActivity extends BaseChildFormActivity {
    @Override
    public void initializeFormFragment() {
        initializeFormFragmentCore();
    }

    protected void initializeFormFragmentCore() {
        KipChildFormFragment gizChildFormFragment = (KipChildFormFragment) KipChildFormFragment.getFormFragment(JsonFormConstants.FIRST_STEP_NAME);
        getSupportFragmentManager().beginTransaction().add(com.vijay.jsonwizard.R.id.container, gizChildFormFragment).commit();
    }

}
