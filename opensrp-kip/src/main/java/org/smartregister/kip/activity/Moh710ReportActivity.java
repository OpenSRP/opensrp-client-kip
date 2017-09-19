package org.smartregister.kip.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.kip.R;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.domain.MohIndicator;
import org.smartregister.kip.domain.MonthlyTally;
import org.smartregister.kip.fragment.CustomDateRangeDialogFragment;
import org.smartregister.kip.helper.SpinnerHelper;
import org.smartregister.kip.listener.DateRangeActionListener;
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
public class Moh710ReportActivity extends BaseActivity implements Moh710ServiceBroadcastReceiver.Moh710ServiceListener, DateRangeActionListener {
    private static final String TAG = Moh710ReportActivity.class.getCanonicalName();
    private static final String DIALOG_TAG = Moh710ReportActivity.class.getCanonicalName().concat("DIALOG_TAG");
    private static final SimpleDateFormat MMMYYYY = new SimpleDateFormat("MMMM yyyy");
    private static final String UNDER_SCORE = "_";

    //Global data variables
    private List<String> antigenList = new ArrayList<>();
    private Map<String, List<MohIndicator>> antigenMap = new HashMap<>();
    private Holder holder = new Holder();

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
        generateReport();
        Moh710ServiceBroadcastReceiver.getInstance().addMoh710ServiceListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        Moh710ServiceBroadcastReceiver.getInstance().removeMoh710ServiceListener(this);
    }

    private void generateReport() {
        holder.setDefault(null);
        Utils.startAsyncTask(new GenerateReportTask(this), null);
        updateFilters();
    }

    private void refresh(boolean showProgressBar) {
        if (holder.isCustomRange() && (holder.getCustomStartDate() == null || holder.getCustomEndDate() == null)) {
            generateReport();
            return;
        } else if (!holder.isCustomRange() && holder.getSelectedMonth() == null) {
            generateReport();
            return;
        }

        if (holder.isCustomRange()) {
            Utils.startAsyncTask(new UpdateReportTask(this, showProgressBar), new Date[]{holder.getCustomStartDate(), holder.getCustomEndDate()});
        } else {
            Utils.startAsyncTask(new UpdateReportTask(this, showProgressBar), new Date[]{holder.getSelectedMonth()});
        }
    }

    private void updateFilters() {
        View defaultFilter = findViewById(R.id.default_filter);
        View customFilter = findViewById(R.id.custom_filter);
        if (holder.isCustomRange()) {
            customFilter.setVisibility(View.VISIBLE);
            defaultFilter.setVisibility(View.GONE);

            if (holder.getCustomStartDate() != null && holder.getCustomEndDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
                String text = dateFormat.format(holder.getCustomStartDate()) + " - " + dateFormat.format(holder.getCustomEndDate());
                TextView textView = (TextView) findViewById(R.id.custom_dates_value);
                textView.setText(text);
            }

            View clearCustomView = findViewById(R.id.clear_custom_date_range);
            if (clearCustomView != null) {
                Button clearCustom = (Button) clearCustomView;
                clearCustom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.setDefault(null);
                        refresh(true);
                        updateFilters();
                    }
                });
            }

            View customDateRangeView = findViewById(R.id.custom_date_range_layout);
            if (customDateRangeView != null) {
                customDateRangeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addCustomRangeDateDialogFragment();
                    }
                });
            }

        } else {
            defaultFilter.setVisibility(View.VISIBLE);
            customFilter.setVisibility(View.GONE);

            View customDateRangeView = findViewById(R.id.set_custom_date_range);
            if (customDateRangeView != null) {
                Button customDateRange = (Button) customDateRangeView;
                customDateRange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addCustomRangeDateDialogFragment();
                    }
                });
            }
        }


    }

    private void updateListViewHeader() {
        // Add header
        ListView listView = (ListView) findViewById(R.id.list_view);
        View view = getLayoutInflater().inflate(R.layout.moh710_header, null);
        listView.addHeaderView(view);
    }

    private void updateReportDates(List<Date> dates) {
        if (dates != null && !dates.isEmpty()) {
            View reportDateSpinnerView = findViewById(R.id.report_date_spinner);
            if (reportDateSpinnerView != null) {
                SpinnerHelper reportDateSpinner = new SpinnerHelper(reportDateSpinnerView);
                SpinnerAdapter dataAdapter = new SpinnerAdapter(this, R.layout.item_spinner_moh710, dates);
                dataAdapter.setDropDownViewResource(R.layout.item_spinner_drop_down_moh710);
                reportDateSpinner.setAdapter(dataAdapter);

                reportDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Object tag = view.getTag();
                        if (tag != null && tag instanceof Date) {
                            holder.setDefault((Date) tag);
                            refresh(true);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                holder.setDefault(dates.get(0));
            }
        }
    }

    private void updateReportList(List<String> antigenList, Map<String, List<MohIndicator>> antigenMap, List<MonthlyTally> monthlyTallies) {
        this.antigenList = antigenList;
        this.antigenMap = antigenMap;
        updateReportList(monthlyTallies);

    }


    private void updateReportList(final List<MonthlyTally> monthlyTallies) {
        if (monthlyTallies == null || monthlyTallies.isEmpty()) {
            return;
        }

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

                if (antigen.contains(UNDER_SCORE)) {
                    antigen = antigen.split(UNDER_SCORE)[0];
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
            if (monthlyTally.getIndicator() != null
                    && monthlyTally.getIndicator().getIndicatorCode().equalsIgnoreCase(mohIndicator.getIndicatorCode())) {
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
            refresh(false);
        }
    }

    public void addCustomRangeDateDialogFragment() {

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();
        Fragment prev = this.getFragmentManager().findFragmentByTag(DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);

        Date startDate = null;
        Date endDate = null;
        if (holder.isCustomRange()) {
            startDate = holder.getCustomStartDate();
            endDate = holder.getCustomEndDate();
        }
        CustomDateRangeDialogFragment customDateRangeDialogFragment = CustomDateRangeDialogFragment.newInstance(startDate, endDate);
        customDateRangeDialogFragment.show(ft, DIALOG_TAG);
    }

    @Override
    public void onDateRangeSelected(Date startDate, Date endDate) {
        holder.setCustom(true, startDate, endDate);
        refresh(true);
        updateFilters();

    }

    ////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////

    private class SpinnerAdapter extends ArrayAdapter<Date> {

        SpinnerAdapter(Context context, int resource, List<Date> objects) {
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
        protected Map<String, NamedObject<?>> doInBackground(Void... params) {
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
                                antigen = antigen.concat(UNDER_SCORE + Math.random());
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

                MonthlyTalliesRepository monthlyTalliesRepository = KipApplication.getInstance().monthlyTalliesRepository();

                List<Date> dates = monthlyTalliesRepository.findUneditedDraftMonths(new Date(0),
                        new Date());
                if (dates == null || dates.isEmpty()) {
                    return null;
                }

                Collections.sort(dates, new Comparator<Date>() {
                    @Override
                    public int compare(Date lhs, Date rhs) {
                        if (lhs.after(rhs)) {
                            return -1;
                        } else if (lhs.equals(rhs)) {
                            return 0;
                        } else {
                            return 1;
                        }
                    }
                });

                List<MonthlyTally> monthlyTallies = monthlyTalliesRepository.findDrafts(MonthlyTalliesRepository.DF_YYYYMM.format(dates.get(0)));

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
            updateReportList(antigenList, antigenMap, monthlyTallies);
        }
    }

    public class UpdateReportTask extends AsyncTask<Date, Void, List<MonthlyTally>> {

        private BaseActivity baseActivity;
        private boolean showProgressBar;

        public UpdateReportTask(BaseActivity baseActivity, boolean showProgressBar) {
            this.baseActivity = baseActivity;
            this.showProgressBar = showProgressBar;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (showProgressBar) {
                baseActivity.showProgressDialog();
            }
        }

        @Override
        protected List<MonthlyTally> doInBackground(Date... params) {

            if (params == null) {
                return new ArrayList<>();
            }

            if (params.length == 1) {
                Date date = params[0];
                MonthlyTalliesRepository monthlyTalliesRepository = KipApplication.getInstance().monthlyTalliesRepository();

                return monthlyTalliesRepository
                        .findDrafts(MonthlyTalliesRepository.DF_YYYYMM.format(date));
            } else if (params.length == 2) {
                Date startDate = params[0];
                Date endDate = params[1];
                MonthlyTalliesRepository monthlyTalliesRepository = KipApplication.getInstance().monthlyTalliesRepository();

                return monthlyTalliesRepository.findDrafts(startDate, endDate);
            }

            return new ArrayList<>();

        }

        @Override
        protected void onPostExecute(List<MonthlyTally> monthlyTallies) {
            super.onPostExecute(monthlyTallies);
            if (showProgressBar) {
                baseActivity.hideProgressDialog();
            }
            updateReportList(monthlyTallies);

        }
    }

    private class NamedObject<T> {
        public final String name;
        public final T object;

        NamedObject(String name, T object) {
            this.name = name;
            this.object = object;
        }
    }

    private class Holder {
        private boolean customRange;
        private Date customStartDate;
        private Date customEndDate;
        private Date selectedMonth;


        public void setCustom(boolean customRange, Date customStartDate, Date customEndDate) {
            this.customRange = customRange;
            this.customStartDate = customStartDate;
            this.customEndDate = customEndDate;
        }

        public void setDefault(Date selectedMonth) {
            this.customRange = false;
            if (selectedMonth != null) {
                this.selectedMonth = selectedMonth;
            }
        }

        public boolean isCustomRange() {
            return customRange;
        }

        public Date getCustomStartDate() {
            return customStartDate;
        }

        public Date getCustomEndDate() {
            return customEndDate;
        }

        public Date getSelectedMonth() {
            return selectedMonth;
        }
    }
}
