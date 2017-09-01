package org.smartregister.kip.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.smartregister.kip.R;
import org.smartregister.kip.toolbar.LocationSwitcherToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keyman on 01/09/2017.
 */
public class Moh710ReportActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBarDrawerToggle toggle = getDrawerToggle();
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setHomeAsUpIndicator(null);

        Spinner reportDateSpinner = (Spinner) findViewById(R.id.report_date_spinner);
        List<String> list = new ArrayList<>();
        list.add("list 1");
        list.add("list 2");
        list.add("list 3");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                R.layout.item_spinner_moh710, list);
        dataAdapter.setDropDownViewResource(R.layout.item_spinner_drop_down_moh710);
        reportDateSpinner.setAdapter(dataAdapter);


    }

    @Override
    protected int getContentView() {
        return R.layout.activity_moh710_report;
    }

    @Override
    protected int getDrawerLayoutId() {
        return R.id.drawer_layout;
    }

    @Override
    protected int getToolbarId() {
        return LocationSwitcherToolbar.TOOLBAR_ID;
    }

    @Override
    protected Class onBackActivity() {
        return null;
    }

}
