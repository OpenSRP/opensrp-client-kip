package org.smartregister.kip.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;

import org.smartregister.domain.LoginResponse;
import org.smartregister.kip.R;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.presenter.LoginPresenter;
import org.smartregister.kip.service.intent.LocationsIntentService;
import org.smartregister.kip.util.KipChildUtils;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.task.SaveTeamLocationsTask;
import org.smartregister.view.activity.BaseLoginActivity;
import org.smartregister.view.contract.BaseLoginContract;

import static org.smartregister.domain.LoginResponse.SUCCESS;

public class LoginActivity extends BaseLoginActivity implements BaseLoginContract.View {
    Context context = KipApplication.getInstance().getApplicationContext();

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLoginPresenter.processViewCustomizations();
        if (!mLoginPresenter.isUserLoggedOut()) {
            goToHome(false);
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_login;
    }

    @Override
    protected void initializePresenter() {
        mLoginPresenter = new LoginPresenter(this);
    }

    @Override
    public void goToHome(boolean remote) {
        if (remote) {
            org.smartregister.util.Utils.startAsyncTask(new SaveTeamLocationsTask(), null);
        }
        goToCovid19VaccineStockSettings(remote);
        finish();
    }

//    private void gotToHomeRegister(boolean remote) {
//        LoginResponse loginResponse = SUCCESS;
//        String jsonPayload = new Gson().toJson(loginResponse.payload());
//        Intent intent = new Intent(this, ChildRegisterActivity.class);
//        intent.putExtra(KipConstants.IntentKeyUtils.IS_REMOTE_LOGIN, remote);
//        Intent rIntent = new Intent(this, LocationsIntentService.class);
//        rIntent.putExtra("userInfo", jsonPayload);
//        startService(rIntent);
//        startActivity(intent);
//        finish();
//    }

    private void goToCovid19VaccineStockSettings(boolean remote) {
        Intent intent = new Intent(this, Covid19VaccineStockSettingsEnterActivity.class);
        intent.putExtra(KipConstants.IntentKeyUtils.IS_REMOTE_LOGIN, remote);
        startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context base) {
        // get language from prefs
        String lang = KipChildUtils.getLanguage(base.getApplicationContext());
        super.attachBaseContext(KipChildUtils.setAppLocale(base, lang));
    }

}
