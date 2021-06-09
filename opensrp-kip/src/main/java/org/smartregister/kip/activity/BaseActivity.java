package org.smartregister.kip.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.smartregister.kip.R;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.contract.Covid19VaccineStockSettingsContract;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.kip.util.KipJsonFormUtils;

import java.util.Map;

import timber.log.Timber;


public abstract class BaseActivity extends AppCompatActivity implements Covid19VaccineStockSettingsContract.View {
    protected ProgressDialog progressDialog;
    protected Covid19VaccineStockSettingsContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == KipJsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra("json");
                Timber.d("JSONResult %s", jsonString);
                presenter.saveCovid19VaccineStockSettings(jsonString);
            } catch (Exception e) {
                Timber.e(e);
            }

        }
    }

    @Override
    public void launchCovid19VaccineStockSettingsForm() {
        KipJsonFormUtils.launchCovid19VaccineStockSettingsForm(this);
    }

    public void showProgressDialog(int saveMessageStringIdentifier) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(getString(saveMessageStringIdentifier));
            progressDialog.setMessage(getString(R.string.please_wait_message));
        }
        if (!isFinishing()) progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void goToLastPage() {
        if (this instanceof Covid19VaccineStockSettingsActivity) {
            goToHomeRegisterPage();
        } else {
            goToSiteCharacteristicsExitPage();
        }
    }

    public void goToHomeRegisterPage() {
        Intent intent = new Intent(this, KipApplication.getInstance().getActivityConfiguration().getLandingPageActivityClass())
                .putExtra(KipConstants.IntentKeyUtils.IS_REMOTE_LOGIN,
                        getIntent().getBooleanExtra(KipConstants.IntentKeyUtils.IS_REMOTE_LOGIN, false));
        startActivity(intent);
        finish();
    }

    public void goToSiteCharacteristicsExitPage() {
        Intent intent = new Intent(this, Covid19VaccineStockSettingsExitActivity.class)
                .putExtra(KipConstants.IntentKeyUtils.IS_REMOTE_LOGIN,
                        getIntent().getBooleanExtra(KipConstants.IntentKeyUtils.IS_REMOTE_LOGIN, false));
        startActivity(intent);
        finish();
    }

    @Override
    public void launchCovid19VaccineStockSettingsFormForEdit(Map<String, String> characteristics) {
        String formMetadata = KipJsonFormUtils.getAutoPopulatedCovid19VaccineStockSettingsEditFormString(this, characteristics);
        try {
            KipJsonFormUtils.startFormForEdit(this, KipJsonFormUtils.REQUEST_CODE_GET_JSON, formMetadata);
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
