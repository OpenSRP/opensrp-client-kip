package org.smartregister.kip.activity;

import android.os.Bundle;
import android.view.View;

import org.smartregister.kip.R;
import org.smartregister.kip.presenter.SettingsPresenter;

public class Covid19VaccineStockSettingsEnterActivity extends BaseActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covid19_vaccine_stock_enter);
        findViewById(R.id.btn_covid19_vaccine_stock_settings).setOnClickListener(this);
        presenter = new SettingsPresenter(this);
    }

    @Override
    public void onClick(View view) {
        presenter.launchCovid19VaccineStockSettingsForm();
    }
}
