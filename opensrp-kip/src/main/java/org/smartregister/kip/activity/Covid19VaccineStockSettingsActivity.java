package org.smartregister.kip.activity;

import android.os.Bundle;
import android.view.View;

import org.smartregister.kip.R;
import org.smartregister.kip.contract.BaseSettingsContract;
import org.smartregister.kip.presenter.Covid19VaccineStockSettingsPresenter;
import org.smartregister.kip.presenter.SettingsPresenter;

public class Covid19VaccineStockSettingsActivity extends BaseSettingsActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mToolbar.findViewById(R.id.covid19_vaccine_stock_settings_toolbar_edit).setOnClickListener(this);
        presenter = new SettingsPresenter(this);
    }

    @Override
    protected BaseSettingsContract.BasePresenter getPresenter() {
        return new Covid19VaccineStockSettingsPresenter(this);
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.update_covid19_stock);
    }

    @Override
    public void onClick(View view) {
        presenter.launchCovid19VaccineStockSettingsForEdit();
    }
}
