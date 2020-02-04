package org.smartregister.kip.activity;

import android.content.Intent;
import android.os.Bundle;

import org.smartregister.kip.R;
import org.smartregister.kip.presenter.LoginPresenter;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.kip.util.KipChildUtils;
import org.smartregister.task.SaveTeamLocationsTask;
import org.smartregister.view.activity.BaseLoginActivity;
import org.smartregister.view.contract.BaseLoginContract;

public class LoginActivity extends BaseLoginActivity implements BaseLoginContract.View {

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

        Intent intent = new Intent(this, ChildRegisterActivity.class);
        intent.putExtra(KipConstants.IntentKeyUtil.IS_REMOTE_LOGIN, remote);
        startActivity(intent);
        finish();
    }

    @Override
    protected void attachBaseContext(android.content.Context base) {
        // get language from prefs
        String lang = KipChildUtils.getLanguage(base.getApplicationContext());
        super.attachBaseContext(KipChildUtils.setAppLocale(base, lang));
    }
}
