package org.smartregister.kip.activity;

import android.os.Bundle;

import com.vijay.jsonwizard.activities.JsonWizardFormActivity;

import org.smartregister.kip.R;


public class EditJsonFormActivity extends JsonWizardFormActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setConfirmCloseMessage(getString(R.string.any_changes_you_make));
        super.onCreate(savedInstanceState);
    }
}
