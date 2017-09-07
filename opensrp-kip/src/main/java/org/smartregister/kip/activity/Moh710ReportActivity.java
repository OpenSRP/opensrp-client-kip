package org.smartregister.kip.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.smartregister.kip.R;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.domain.MohIndicator;
import org.smartregister.kip.repository.Moh710IndicatorsRepository;
import org.smartregister.kip.repository.MonthlyTalliesRepository;
import org.smartregister.kip.toolbar.LocationSwitcherToolbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by keyman on 01/09/2017.
 */
public class Moh710ReportActivity extends BaseActivity {
    private static final SimpleDateFormat MMMYYYY = new SimpleDateFormat("MMMM yyyy");
    private static final int MONTH_SUGGESTION_LIMIT = 10;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBarDrawerToggle toggle = getDrawerToggle();
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setHomeAsUpIndicator(null);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateReportDates();
        updateReportList();
    }

    private void updateReportDates() {

        MonthlyTalliesRepository monthlyTalliesRepository = KipApplication
                .getInstance().monthlyTalliesRepository();
        Calendar startDate = Calendar.getInstance();
        startDate.set(Calendar.DAY_OF_MONTH, 1);
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);
        startDate.add(Calendar.MONTH, -1 * MONTH_SUGGESTION_LIMIT);

        Calendar endDate = Calendar.getInstance();
        endDate.set(Calendar.DAY_OF_MONTH, 1);// Set date to first day of this month
        endDate.set(Calendar.HOUR_OF_DAY, 23);
        endDate.set(Calendar.MINUTE, 59);
        endDate.set(Calendar.SECOND, 59);
        endDate.set(Calendar.MILLISECOND, 999);
        endDate.add(Calendar.DATE, -1);// Move the date to last day of last month

        List<Date> dates = monthlyTalliesRepository.findUneditedDraftMonths(startDate.getTime(),
                endDate.getTime());

        Spinner reportDateSpinner = (Spinner) findViewById(R.id.report_date_spinner);
        SpinnerAdapter dataAdapter = new SpinnerAdapter(this, R.layout.item_spinner_moh710, dates);
        dataAdapter.setDropDownViewResource(R.layout.item_spinner_drop_down_moh710);
        reportDateSpinner.setAdapter(dataAdapter);

        reportDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object tag = view.getTag();
                if (tag != null && tag instanceof Date) {
                    Toast.makeText(Moh710ReportActivity.this, tag.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


       /* View view = findViewById(R.id.custom_dates_value);
        if (view != null) {
            TextView customDatesValue = (TextView) view;
            customDatesValue.setPaintFlags(customDatesValue.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            customDatesValue.setText("14 Jun 2017 - 19 Jun 2017");
        }*/

    }

    private void updateReportList() {

        final Activity context = this;
        ListView listView = (ListView) findViewById(R.id.list_view);

        View view = getLayoutInflater().inflate(R.layout.moh710_header, null);
        listView.addHeaderView(view);

        final Moh710IndicatorsRepository moh710IndicatorsRepository = KipApplication.getInstance().moh710IndicatorsRepository();
        final List<String> antigens = moh710IndicatorsRepository.fetchDistinctAntigens();

        BaseAdapter baseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return antigens.size();
            }

            @Override
            public Object getItem(int position) {
                return antigens.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;
                LayoutInflater inflater =
                        context.getLayoutInflater();
                if (convertView == null) {
                    view = inflater.inflate(R.layout.moh710_item, null);
                } else {
                    view = convertView;
                }

                String antigen = antigens.get(position);
                List<MohIndicator> indicators = moh710IndicatorsRepository.findByAntigen(antigen);
                if (indicators.isEmpty()) {
                    return view;
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

                    TextView valueView = (TextView) ageValueView.findViewById(R.id.value);
                    valueView.setText(String.valueOf(new Random().nextInt(100 + 1)));

                    ageValueLayout.addView(ageValueView);
                }


                return view;
            }
        };

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
}
