package org.smartregister.kip.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.smartregister.kip.R;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.domain.MohIndicator;
import org.smartregister.kip.repository.Moh710IndicatorsRepository;
import org.smartregister.kip.toolbar.LocationSwitcherToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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


    }

    @Override
    protected void onResume() {
        super.onResume();
        updateReportDates();
        updateReportList();
    }

    private void updateReportDates() {

        Spinner reportDateSpinner = (Spinner) findViewById(R.id.report_date_spinner);
        List<String> list = new ArrayList<>();
        list.add("list 1");
        list.add("list 2");
        list.add("list 3");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                R.layout.item_spinner_moh710, list);
        dataAdapter.setDropDownViewResource(R.layout.item_spinner_drop_down_moh710);
        reportDateSpinner.setAdapter(dataAdapter);

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

}
