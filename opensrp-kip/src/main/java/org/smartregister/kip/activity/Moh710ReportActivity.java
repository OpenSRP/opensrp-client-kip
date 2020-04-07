package org.smartregister.kip.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.smartregister.child.activity.BaseActivity;
import org.smartregister.child.toolbar.LocationSwitcherToolbar;
import org.smartregister.kip.R;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.domain.MohIndicator;
import org.smartregister.kip.domain.MonthlyTally;
import org.smartregister.kip.fragment.CustomDateRangeDialogFragment;
import org.smartregister.kip.helper.SpinnerHelper;
import org.smartregister.kip.listener.DateRangeActionListener;
import org.smartregister.kip.model.ReportGroupingModel;
import org.smartregister.kip.receiver.Moh710ServiceBroadcastReceiver;
import org.smartregister.kip.repository.Moh710IndicatorsRepository;
import org.smartregister.kip.repository.MonthlyTalliesRepository;
import org.smartregister.kip.service.Moh710Service;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.kip.view.NavigationMenu;
import org.smartregister.util.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Moh710ReportActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

            ListView listView = findViewById(R.id.lv_reportRegister_groupings);
            TextView titleTv = findViewById(R.id.title);

                if (titleTv != null) {
                titleTv.setText(R.string.dhis2_reports);
            }

            final ArrayList<ReportGroupingModel.ReportGrouping> reportGroupings = getReportGroupings();
                listView.setAdapter(new ArrayAdapter<>(this, R.layout.report_grouping_list_item, reportGroupings));
                listView.setOnItemClickListener((parent, view, position, id) -> {
                Intent intent = new Intent(Moh710ReportActivity.this, HIA2ReportsActivity.class);
                intent.putExtra(KipConstants.IntentKey.REPORT_GROUPING, reportGroupings.get(position).getGrouping());
                startActivity(intent);
            });
        }

    @Override
    protected int getContentView() {
        return R.layout.activity_report_register;
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

    protected ArrayList<ReportGroupingModel.ReportGrouping> getReportGroupings() {
        return (new ReportGroupingModel(this)).getReportGroupings();
    }

    public void onClickReport(View view) {
        switch (view.getId()) {
            case R.id.btn_back_to_home:

                NavigationMenu navigationMenu = NavigationMenu.getInstance(this, null, null);
                if (navigationMenu != null) {
                    navigationMenu.getDrawer()
                            .openDrawer(GravityCompat.START);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onUniqueIdFetched(Triple<String, String, String> triple, String s) {
        // Nothing to happen here
    }

    @Override
    public void onNoUniqueId() {
        // Nothing to happen here
    }

    @Override
    public void onRegistrationSaved(boolean b) {
        // Nothing to happen here
    }

    @Override
    protected void onResume() {
        super.onResume();
        createDrawer();
    }

    public void createDrawer() {
        NavigationMenu navigationMenu = NavigationMenu.getInstance(this, null, null);
        if (navigationMenu != null) {
            navigationMenu.getNavigationAdapter().setSelectedView(null);
            navigationMenu.runRegisterCount();
        }
    }

}
