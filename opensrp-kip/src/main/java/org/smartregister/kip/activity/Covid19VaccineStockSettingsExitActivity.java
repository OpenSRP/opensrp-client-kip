package org.smartregister.kip.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.smartregister.kip.R;
import org.smartregister.kip.presenter.SettingsPresenter;
import org.smartregister.location.helper.LocationHelper;


public class Covid19VaccineStockSettingsExitActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covid19_vaccine_stock_exit);

        findViewById(R.id.btn_covid19_vaccine_stock_settings_home_register).setOnClickListener(this);
        findViewById(R.id.btn_back_to_home).setOnClickListener(this);
        findViewById(R.id.txt_title_label).setOnClickListener(this);

        presenter = new SettingsPresenter(this);

        String defaultLocation =
                LocationHelper.getInstance().getOpenMrsLocationName(LocationHelper.getInstance().getDefaultLocation());
        TextView textView = findViewById(R.id.covid19_vaccine_stock_settings_facility_name);
        textView.setText(getString(R.string.your_covid19_vaccine_stock_settings_for_facility_name, defaultLocation));


    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_covid19_vaccine_stock_settings_home_register) {
            goToHomeRegisterPage();
        } else if (view.getId() == R.id.txt_title_label) {
            presenter.launchCovid19VaccineStockSettingsForEdit();
        } else {
            presenter.launchCovid19VaccineStockSettingsForEdit();
        }

    }
}
