package org.smartregister.kip.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.kip.R;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.domain.MohIndicator;
import org.smartregister.kip.domain.MonthlyTally;
import org.smartregister.kip.receiver.Moh710ServiceBroadcastReceiver;
import org.smartregister.kip.repository.Moh710IndicatorsRepository;
import org.smartregister.kip.repository.MonthlyTalliesRepository;
import org.smartregister.kip.service.Moh710Service;
import org.smartregister.kip.toolbar.LocationSwitcherToolbar;
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

/**
 * Created by keyman on 01/09/2017.
 */
public class Moh710ReportActivity extends BaseActivity implements Moh710ServiceBroadcastReceiver.Moh710ServiceListener {
    private static final String TAG = Moh710ReportActivity.class.getCanonicalName();
    private static final SimpleDateFormat MMMYYYY = new SimpleDateFormat("MMMM yyyy");

    //Global data variables
    private List<String> antigenList = new ArrayList<>();
    private Map<String, List<MohIndicator>> antigenMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBarDrawerToggle toggle = getDrawerToggle();
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setHomeAsUpIndicator(null);

        updateListViewHeader();
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
        Moh710ServiceBroadcastReceiver.getInstance().addMoh710ServiceListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        Moh710ServiceBroadcastReceiver.getInstance().removeMoh710ServiceListener(this);
    }

    private void update() {
        Utils.startAsyncTask(new GenerateReportTask(this), null);
    }

    private void updateListViewHeader() {
        // Add header
        ListView listView = (ListView) findViewById(R.id.list_view);
        View view = getLayoutInflater().inflate(R.layout.moh710_header, null);
        listView.addHeaderView(view);
    }

    private void updateReportDates(List<Date> dates) {
        if (dates != null && !dates.isEmpty()) {
            Spinner reportDateSpinner = (Spinner) findViewById(R.id.report_date_spinner);
            SpinnerAdapter dataAdapter = new SpinnerAdapter(this, R.layout.item_spinner_moh710, dates);
            dataAdapter.setDropDownViewResource(R.layout.item_spinner_drop_down_moh710);
            reportDateSpinner.setAdapter(dataAdapter);

            reportDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Object tag = view.getTag();
                    if (tag != null && tag instanceof Date) {
                        updateReportList((Date) tag);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

       /* View view = findViewById(R.id.custom_dates_value);
        if (view != null) {
            TextView customDatesValue = (TextView) view;
            customDatesValue.setPaintFlags(customDatesValue.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            customDatesValue.setText("14 Jun 2017 - 19 Jun 2017");
        }*/

    }

    private void updateReportList(List<Date> dates, List<String> antigenList, Map<String, List<MohIndicator>> antigenMap, List<MonthlyTally> monthlyTallies) {
        if (dates != null && !dates.isEmpty()) {
            this.antigenList = antigenList;
            this.antigenMap = antigenMap;
            updateReportList(monthlyTallies);
        }
    }

    private void updateReportList(Date date) {
        Date[] dates = {date};
        Utils.startAsyncTask(new UpdateReportTask(this), dates);
    }

    private void updateReportList(final List<MonthlyTally> monthlyTallies) {

        BaseAdapter baseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return antigenList.size();
            }

            @Override
            public Object getItem(int position) {
                return antigenList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;
                LayoutInflater inflater =
                        Moh710ReportActivity.this.getLayoutInflater();
                if (convertView == null) {
                    view = inflater.inflate(R.layout.moh710_item, null);
                } else {
                    view = convertView;
                }

                String antigen = antigenList.get(position);
                List<MohIndicator> indicators = antigenMap.get(antigen);
                if (indicators.isEmpty()) {
                    return view;
                }

                if (antigen.contains("_")) {
                    antigen = antigen.split("_")[0];
                }
                TextView antigenTextView = (TextView) view.findViewById(R.id.antigen);
                antigenTextView.setText(antigen);

                LinearLayout ageValueLayout = (LinearLayout) view.findViewById(R.id.age_value_layout);
                ageValueLayout.removeAllViews();

                for (int i = 0; i < indicators.size(); i++) {

                    View ageValueView = inflater.inflate(R.layout.moh710_item_age_value, null);
                    View divider = ageValueView.findViewById(R.id.adapter_divider_bottom);
                    if (i == 0) {
                        divider.setVisibility(View.GONE);
                    }

                    MohIndicator indicator = indicators.get(i);
                    TextView ageView = (TextView) ageValueView.findViewById(R.id.age);

                    // Remove double quotes
                    String age = indicator.getAge().replaceAll("^\"|\"$", "");
                    ageView.setText(age);

                    String[] noSource = {Moh710Service.MOH_044, Moh710Service.MOH_043, Moh710Service.MOH_040, Moh710Service.MOH_039, Moh710Service.MOH_038, Moh710Service.MOH_037, Moh710Service.MOH_036};
                    List<String> noSourceList = new ArrayList<>(Arrays.asList(noSource));

                    String value;
                    if (noSourceList.contains(indicator.getIndicatorCode())) {
                        value = getString(R.string.n_a);
                    } else {
                        value = retrieveValue(monthlyTallies, indicator);
                    }

                    TextView valueView = (TextView) ageValueView.findViewById(R.id.value);
                    valueView.setText(value);

                    ageValueLayout.addView(ageValueView);
                }


                return view;
            }
        };

        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(baseAdapter);
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

    private static String retrieveValue(List<MonthlyTally> monthlyTallies, MohIndicator mohIndicator) {
        String defaultValue = "N/A";
        if (mohIndicator == null || monthlyTallies == null) {
            return defaultValue;
        }

        for (MonthlyTally monthlyTally : monthlyTallies) {
            if (monthlyTally.getIndicator() != null && monthlyTally.getIndicator().getIndicatorCode()
                    .equalsIgnoreCase(mohIndicator.getIndicatorCode())) {
                return monthlyTally.getValue();
            }
        }

        return defaultValue;
    }

    @Override
    protected void showProgressDialog() {
        showProgressDialog(getString(R.string.updating_dialog_title), getString(R.string.please_wait_message));
    }

    @Override
    public void onServiceFinish(String actionType) {
        if (Moh710ServiceBroadcastReceiver.TYPE_GENERATE_DAILY_INDICATORS.equals(actionType)) {
            update();
        }
    }

    ////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////

    private class SpinnerAdapter extends ArrayAdapter<Date> {

        public SpinnerAdapter(Context context, int resource, List<Date> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = getLayoutInflater().inflate(R.layout.item_spinner_moh710, parent, false);
            } else {
                view = convertView;
            }

            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                Date date = getItem(position);

                String dateString = MMMYYYY.format(date);
                textView.setText(dateString);
                textView.setTag(date);
            }
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = getLayoutInflater().inflate(R.layout.item_spinner_drop_down_moh710, parent, false);
            } else {
                view = convertView;
            }

            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                Date date = getItem(position);

                String dateString = MMMYYYY.format(date);
                textView.setText(dateString);
                textView.setTag(date);
            }
            return view;
        }
    }

    public class GenerateReportTask extends AsyncTask<Void, Void, Map<String, NamedObject<?>>> {

        BaseActivity baseActivity;

        public GenerateReportTask(BaseActivity baseActivity) {
            this.baseActivity = baseActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            baseActivity.showProgressDialog();
        }

        @Override
        protected Map<String, NamedObject<?>> doInBackground
                (Void... params) {
            try {
                Moh710IndicatorsRepository moh710IndicatorsRepository = KipApplication.getInstance().moh710IndicatorsRepository();
                List<MohIndicator> indicators = moh710IndicatorsRepository.fetchAll();

                if (indicators == null || indicators.isEmpty()) {
                    return null;
                }

                List<String> antigenList = new ArrayList<>();
                Map<String, List<MohIndicator>> antigenMap = new HashMap<>();

                String[] customAntigens = {Moh710Service.MOH_026, Moh710Service.MOH_032, Moh710Service.MOH_033, Moh710Service.MOH_042, Moh710Service.MOH_043};
                List<String> customAntigenList = new ArrayList<>(Arrays.asList(customAntigens));

                String previousAntigen = null;
                String previousCustomAntigen = null;
                for (MohIndicator indicator : indicators) {
                    String antigen = indicator.getAntigen();

                    if (StringUtils.isNotBlank(antigen)) {
                        if (customAntigenList.contains(indicator.getIndicatorCode())) {
                            if (StringUtils.isNotBlank(previousAntigen) && StringUtils.isNotBlank(previousCustomAntigen) && previousAntigen.equals(antigen)) {
                                antigen = previousCustomAntigen;
                            } else {
                                previousAntigen = antigen;
                                antigen = antigen.concat("_" + Math.random());
                                previousCustomAntigen = antigen;
                            }
                        } else {
                            previousAntigen = antigen;
                            previousCustomAntigen = antigen;
                        }

                        if (antigenMap.containsKey(antigen)) {
                            List<MohIndicator> curIndicators = antigenMap.get(antigen);
                            curIndicators.add(indicator);
                        } else {
                            List<MohIndicator> newIndicators = new ArrayList<>();
                            newIndicators.add(indicator);
                            antigenMap.put(antigen, newIndicators);
                            antigenList.add(antigen);
                        }
                    }
                }

                MonthlyTalliesRepository monthlyTalliesRepository = KipApplication
                        .getInstance().monthlyTalliesRepository();
                Date startDate = new Date(0);
                Date endDate = new Date();

                List<Date> dates = monthlyTalliesRepository.findUneditedDraftMonths(startDate,
                        endDate);

                if (dates == null || dates.isEmpty()) {
                    return null;
                }

                Collections.sort(dates, new Comparator<Date>() {
                    @Override
                    public int compare(Date lhs, Date rhs) {
                        if (lhs.after(rhs))
                            return -1;
                        else if (lhs.equals(rhs))
                            return 0;
                        else
                            return 1;
                    }
                });

                List<MonthlyTally> monthlyTallies = monthlyTalliesRepository
                        .findDrafts(MonthlyTalliesRepository.DF_YYYYMM.format(dates.get(0)));

                Map<String, NamedObject<?>> map = new HashMap<>();
                NamedObject<List<Date>> datesNamedObject = new NamedObject<>(Date.class.getName(), dates);
                map.put(datesNamedObject.name, datesNamedObject);

                NamedObject<List<String>> antigenListNamedObject = new NamedObject<>(String.class.getName(), antigenList);
                map.put(antigenListNamedObject.name, antigenListNamedObject);

                NamedObject<Map<String, List<MohIndicator>>> antigenMapNamedObject = new NamedObject<>(MohIndicator.class.getName(), antigenMap);
                map.put(antigenMapNamedObject.name, antigenMapNamedObject);

                NamedObject<List<MonthlyTally>> monthlyTalliesNammedObject = new NamedObject<>(MonthlyTally.class.getName(), monthlyTallies);
                map.put(monthlyTalliesNammedObject.name, monthlyTalliesNammedObject);


                return map;

            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onPostExecute(Map<String, NamedObject<?>> map) {
            super.onPostExecute(map);
            baseActivity.hideProgressDialog();

            if (map == null || map.isEmpty()) {
                return;
            }

            List<Date> dates = new ArrayList<>();
            List<String> antigenList = new ArrayList<>();
            Map<String, List<MohIndicator>> antigenMap = new HashMap<>();
            List<MonthlyTally> monthlyTallies = new ArrayList<>();

            if (map.containsKey(Date.class.getName())) {
                NamedObject<?> namedObject = map.get(Date.class.getName());
                if (namedObject != null) {
                    dates = (List<Date>) namedObject.object;
                }

            }

            if (map.containsKey(String.class.getName())) {
                NamedObject<?> namedObject = map.get(String.class.getName());
                if (namedObject != null) {
                    antigenList = (List<String>) namedObject.object;
                }

            }

            if (map.containsKey(MohIndicator.class.getName())) {
                NamedObject<?> namedObject = map.get(MohIndicator.class.getName());
                if (namedObject != null) {
                    antigenMap = (Map<String, List<MohIndicator>>) namedObject.object;
                }

            }


            if (map.containsKey(MonthlyTally.class.getName())) {
                NamedObject<?> namedObject = map.get(MonthlyTally.class.getName());
                if (namedObject != null) {
                    monthlyTallies = (List<MonthlyTally>) namedObject.object;
                }

            }

            updateReportDates(dates);
            updateReportList(dates, antigenList, antigenMap, monthlyTallies);
        }
    }

    public class UpdateReportTask extends AsyncTask<Date, Void, List<MonthlyTally>> {

        BaseActivity baseActivity;

        public UpdateReportTask(BaseActivity baseActivity) {
            this.baseActivity = baseActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            baseActivity.showProgressDialog();
        }

        @Override
        protected List<MonthlyTally> doInBackground(Date... params) {

            if (params == null) {
                return new ArrayList<>();
            }

            Date date = params[0];
            MonthlyTalliesRepository monthlyTalliesRepository = KipApplication
                    .getInstance().monthlyTalliesRepository();

            return monthlyTalliesRepository
                    .findDrafts(MonthlyTalliesRepository.DF_YYYYMM.format(date));

        }

        @Override
        protected void onPostExecute(List<MonthlyTally> monthlyTallies) {
            super.onPostExecute(monthlyTallies);
            baseActivity.hideProgressDialog();
            updateReportList(monthlyTallies);

        }
    }

    private class NamedObject<T> {
        public final String name;
        public final T object;

        public NamedObject(String name, T object) {
            this.name = name;
            this.object = object;
        }
    }
}
