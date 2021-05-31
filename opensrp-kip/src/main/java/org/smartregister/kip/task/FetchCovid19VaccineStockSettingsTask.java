package org.smartregister.kip.task;

import android.os.AsyncTask;

import org.smartregister.kip.contract.BaseSettingsContract;
import org.smartregister.kip.contract.SettingsContract;
import org.smartregister.kip.domain.KipServerSetting;
import org.smartregister.kip.helper.KipServerSettingHelper;
import org.smartregister.kip.util.KipConstants;

import java.util.List;

public class FetchCovid19VaccineStockSettingsTask extends AsyncTask<Void, Void, List<KipServerSetting>> {

    private BaseSettingsContract.BasePresenter presenter;

    public FetchCovid19VaccineStockSettingsTask(SettingsContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected List<KipServerSetting> doInBackground(final Void... params) {
        KipServerSettingHelper helper = new KipServerSettingHelper(KipConstants.Settings.VACCINE_STOCK_IDENTIFIER);
        return helper.getServerSettings();
    }

    @Override
    protected void onPostExecute(final List<KipServerSetting> result) {
        presenter.renderView(result);
    }
}
