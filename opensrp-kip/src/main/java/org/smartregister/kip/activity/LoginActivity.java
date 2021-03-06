package org.smartregister.kip.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.smartregister.domain.LoginResponse;
import org.smartregister.domain.Response;
import org.smartregister.domain.ResponseStatus;
import org.smartregister.domain.TimeStatus;
import org.smartregister.event.Listener;
import org.smartregister.growthmonitoring.service.intent.ZScoreRefreshIntentService;
import org.smartregister.immunization.util.IMDatabaseUtils;
import org.smartregister.kip.R;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.context.Context;
import org.smartregister.kip.service.intent.LocationsIntentService;
import org.smartregister.kip.service.intent.PullUniqueIdsIntentService;
import org.smartregister.kip.sync.SaveRelationshipTypesTask;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.sync.DrishtiSyncScheduler;
import org.smartregister.util.Log;
import org.smartregister.view.BackgroundAction;
import org.smartregister.view.LockingBackgroundTask;
import org.smartregister.view.ProgressIndicator;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import util.KipConstants;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;
import static org.smartregister.domain.LoginResponse.NO_INTERNET_CONNECTIVITY;
import static org.smartregister.domain.LoginResponse.SUCCESS;
import static org.smartregister.domain.LoginResponse.UNAUTHORIZED;
import static org.smartregister.domain.LoginResponse.UNKNOWN_RESPONSE;
import static org.smartregister.util.Log.logError;
import static org.smartregister.util.Log.logVerbose;

public class LoginActivity extends AppCompatActivity {
    private EditText userNameEditText;
    private EditText passwordEditText;
    private ProgressDialog progressDialog;
    public static final String ENGLISH_LOCALE = "en";
    private static final String URDU_LOCALE = "ur";
    private static final String ENGLISH_LANGUAGE = "English";
    private static final String URDU_LANGUAGE = "Urdu";
    private android.content.Context appContext;
    private RemoteLoginTask remoteLoginTask;
    private SaveRelationshipTypesTask saveRelationshipTypesTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logVerbose("Initializing ...");
        try {
            AllSharedPreferences allSharedPreferences = new AllSharedPreferences(getDefaultSharedPreferences(this));
            String preferredLocale = allSharedPreferences.fetchLanguagePreference();
            Resources res = getOpenSRPContext().applicationContext().getResources();
            // Change locale settings in the app.
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = new Locale(preferredLocale);
            res.updateConfiguration(conf, dm);
        } catch (Exception e) {
            logError("Error onCreate: " + e);

        }

        setContentView(R.layout.login);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.black)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.black));
        }
        appContext = this;
        positionViews();
        initializeLoginFields();
        initializeBuildDetails();
        setDoneActionHandlerOnPasswordField();
        initializeProgressDialog();

        setLanguage();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add("Settings");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equalsIgnoreCase("Settings")) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeBuildDetails() {
        TextView buildDetailsTextView = (TextView) findViewById(org.smartregister.R.id.login_build);
        try {
            buildDetailsTextView.setText("Version " + getVersion() + ", Built on: " + getBuildDate());
        } catch (Exception e) {
            logError("Error fetching build details: " + e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!getOpenSRPContext().IsUserLoggedOut()) {
            goToHome(false);
        }
    }

    public void login(final View view) {
        login(view, !getOpenSRPContext().allSharedPreferences().fetchForceRemoteLogin());
    }

    private void login(final View view, boolean localLogin) {
        android.util.Log.i(getClass().getName(), "Hiding Keyboard " + DateTime.now().toString());
        hideKeyboard();
        view.setClickable(false);

        final String userName = userNameEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password)) {
            if (localLogin) {
                localLogin(view, userName, password);
            } else {
                remoteLogin(view, userName, password);
            }
        } else {
            showErrorDialog(getResources().getString(R.string.unauthorized));
            view.setClickable(true);
        }

        android.util.Log.i(getClass().getName(), "Login result finished " + DateTime.now().toString());
    }

    private void initializeLoginFields() {
        userNameEditText = (EditText) findViewById(org.smartregister.R.id.login_userNameText);
        passwordEditText = (EditText) findViewById(org.smartregister.R.id.login_passwordText);
        passwordEditText.setTypeface(userNameEditText.getTypeface());
    }

    private void setDoneActionHandlerOnPasswordField() {
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login(findViewById(org.smartregister.R.id.login_loginButton));
                }
                return false;
            }
        });
    }

    private void initializeProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(org.smartregister.R.string.loggin_in_dialog_title));
        progressDialog.setMessage(getString(org.smartregister.R.string.loggin_in_dialog_message));
    }

    private void localLogin(View view, String userName, String password) {
        view.setClickable(true);
        if (getOpenSRPContext().userService().isUserInValidGroup(userName, password)
                && (!KipConstants.TIME_CHECK || TimeStatus.OK.equals(getOpenSRPContext().userService().validateStoredServerTimeZone()))) {
            localLoginWith(userName, password);
        } else {

            login(findViewById(org.smartregister.R.id.login_loginButton), false);
        }
    }


    private void remoteLogin(final View view, final String userName, final String password) {
        tryRemoteLogin(userName, password, new Listener<LoginResponse>() {
            public void onEvent(LoginResponse loginResponse) {
                view.setClickable(true);
                if (loginResponse == SUCCESS) {
                    if (getOpenSRPContext().userService().isUserInPioneerGroup(userName)) {
                        TimeStatus timeStatus = getOpenSRPContext().userService().validateDeviceTime(
                                loginResponse.payload(), KipConstants.MAX_SERVER_TIME_DIFFERENCE);
                        if (!KipConstants.TIME_CHECK || timeStatus.equals(TimeStatus.OK)) {
                            remoteLoginWith(userName, password, loginResponse.payload());
                            Intent intent = new Intent(appContext, PullUniqueIdsIntentService.class);
                            Intent lIntent = new Intent(appContext, LocationsIntentService.class);
                            lIntent.putExtra("userInfo", loginResponse.payload());
                            appContext.startService(intent);
                            appContext.startService(lIntent);
                        } else {
                            if (timeStatus.equals(TimeStatus.TIMEZONE_MISMATCH)) {
                                TimeZone serverTimeZone = getOpenSRPContext().userService()
                                        .getServerTimeZone(loginResponse.payload());
                                showErrorDialog(getString(timeStatus.getMessage(),
                                        serverTimeZone.getDisplayName()));
                            } else {
                                showErrorDialog(getString(timeStatus.getMessage()));
                            }
                        }
                    } else { // Valid user from wrong group trying to log in
                        showErrorDialog(getResources().getString(R.string.unauthorized_group));
                    }
                } else {
                    if (loginResponse == null) {
                        showErrorDialog("Login failed. Unknown reason. Try Again");
                    } else {
                        if (loginResponse == NO_INTERNET_CONNECTIVITY) {
                            showErrorDialog(getResources().getString(R.string.no_internet_connectivity));
                        } else if (loginResponse == UNKNOWN_RESPONSE) {
                            showErrorDialog(getResources().getString(R.string.unknown_response));
                        } else if (loginResponse == UNAUTHORIZED) {
                            showErrorDialog(getResources().getString(R.string.unauthorized));
                        }
//                        showErrorDialog(loginResponse.message());
                    }
                }
            }
        });
    }

    private void showErrorDialog(String message) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(org.smartregister.R.string.login_failed_dialog_title))
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create();
        dialog.show();
    }

    private void showMessageDialog(String message, DialogInterface.OnClickListener ok, DialogInterface.OnClickListener cancel) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(org.smartregister.R.string.login_failed_dialog_title))
                .setMessage(message)
                .setPositiveButton("OK", ok)
                .setNegativeButton("Cancel", cancel)
                .create();

        dialog.show();
    }

    private void getLocation() {
        tryGetLocation(new Listener<Response<String>>() {
            @Override
            public void onEvent(Response<String> data) {
                if (data.status() == ResponseStatus.success) {
                    getOpenSRPContext().userService().saveAnmLocation(data.payload());
                }
            }
        });
    }

    private void tryGetLocation(final Listener<Response<String>> afterGet) {
        LockingBackgroundTask task = new LockingBackgroundTask(new ProgressIndicator() {
            @Override
            public void setVisible() {
            }

            @Override
            public void setInvisible() {
                Log.logInfo("Successfully get location");
            }
        });

        task.doActionInBackground(new BackgroundAction<Response<String>>() {
            @Override
            public Response<String> actionToDoInBackgroundThread() {
                return getOpenSRPContext().userService().getLocationInformation();
            }

            @Override
            public void postExecuteInUIThread(Response<String> result) {
                afterGet.onEvent(result);
            }
        });
    }

    private void tryRemoteLogin(final String userName, final String password, final Listener<LoginResponse> afterLoginCheck) {
        if (remoteLoginTask != null && !remoteLoginTask.isCancelled()) {
            remoteLoginTask.cancel(true);
        }

        remoteLoginTask = new RemoteLoginTask(userName, password, afterLoginCheck);
        remoteLoginTask.execute();
    }

    private void fillUserIfExists() {
        if (getOpenSRPContext().userService().hasARegisteredUser()) {
            userNameEditText.setText(getOpenSRPContext().allSharedPreferences().fetchRegisteredANM());
            userNameEditText.setEnabled(false);
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), HIDE_NOT_ALWAYS);
    }

    private void localLoginWith(String userName, String password) {
        getOpenSRPContext().userService().localLogin(userName, password);
        goToHome(false);
        startZScoreIntentService();
        new Thread(new Runnable() {
            @Override
            public void run() {
                android.util.Log.i(getClass().getName(), "Starting DrishtiSyncScheduler " + DateTime.now().toString());
                DrishtiSyncScheduler.startOnlyIfConnectedToNetwork(getApplicationContext());
                android.util.Log.i(getClass().getName(), "Started DrishtiSyncScheduler " + DateTime.now().toString());
            }
        }).start();
    }

    private void startZScoreIntentService() {
        Intent intent = new Intent(this, ZScoreRefreshIntentService.class);
        startService(intent);
    }

    private void remoteLoginWith(String userName, String password, String userInfo) {
        getOpenSRPContext().userService().remoteLogin(userName, password, userInfo);
        saveRelationshipTypes(userInfo);
        goToHome(true);
        DrishtiSyncScheduler.startOnlyIfConnectedToNetwork(getApplicationContext());
    }

    private void saveRelationshipTypes(String userInfo) {
        if(saveRelationshipTypesTask == null)
            saveRelationshipTypesTask = new SaveRelationshipTypesTask(getOpenSRPContext().allSettings());

        try{
            JSONObject userInfoObject = new JSONObject(userInfo);
            if (userInfoObject.has("relationshipTypes")) {
                String relationshipTypes = userInfoObject.getString("relationshipTypes");
                saveRelationshipTypesTask.save(relationshipTypes);
            }
        }catch(Exception e){
            logError(e.getMessage());
        }
    }

    private void goToHome(boolean remote) {
        if (!remote) startZScoreIntentService();
        KipApplication.setCrashlyticsUser(getOpenSRPContext());
        Intent intent = new Intent(this, ChildSmartRegisterActivity.class);
        intent.putExtra(BaseRegisterActivity.IS_REMOTE_LOGIN, remote);
        startActivity(intent);
        IMDatabaseUtils.accessAssetsAndFillDataBaseForVaccineTypes(this, null);

        finish();
    }

    private String getVersion() throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        return packageInfo.versionName;
    }

    private String getBuildDate() throws PackageManager.NameNotFoundException, IOException {
        ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), 0);
        ZipFile zf = new ZipFile(applicationInfo.sourceDir);
        ZipEntry ze = zf.getEntry("classes.dex");
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new java.util.Date(ze.getTime()));
    }

    public static void setLanguage() {
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(getDefaultSharedPreferences(getOpenSRPContext().applicationContext()));
        String preferredLocale = allSharedPreferences.fetchLanguagePreference();
        Resources res = getOpenSRPContext().applicationContext().getResources();
        // Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.locale = new Locale(preferredLocale);
        res.updateConfiguration(conf, dm);

    }

    public static String switchLanguagePreference() {
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(getDefaultSharedPreferences(getOpenSRPContext().applicationContext()));

        String preferredLocale = allSharedPreferences.fetchLanguagePreference();
        if (URDU_LOCALE.equals(preferredLocale)) {
            allSharedPreferences.saveLanguagePreference(URDU_LOCALE);
            Resources res = getOpenSRPContext().applicationContext().getResources();
            // Change locale settings in the app.
            DisplayMetrics dm = res.getDisplayMetrics();
            android.content.res.Configuration conf = res.getConfiguration();
            conf.locale = new Locale(URDU_LOCALE);
            res.updateConfiguration(conf, dm);
            return URDU_LANGUAGE;
        } else {
            allSharedPreferences.saveLanguagePreference(ENGLISH_LOCALE);
            Resources res = getOpenSRPContext().applicationContext().getResources();
            // Change locale settings in the app.
            DisplayMetrics dm = res.getDisplayMetrics();
            android.content.res.Configuration conf = res.getConfiguration();
            conf.locale = new Locale(ENGLISH_LOCALE);
            res.updateConfiguration(conf, dm);
            return ENGLISH_LANGUAGE;
        }
    }

    private void positionViews() {
        final ScrollView canvasSV = (ScrollView) findViewById(R.id.canvasSV);
        final RelativeLayout canvasRL = (RelativeLayout) findViewById(R.id.canvasRL);
        final LinearLayout logoCanvasLL = (LinearLayout) findViewById(R.id.logoCanvasLL);
        final LinearLayout credentialsCanvasLL = (LinearLayout) findViewById(R.id.credentialsCanvasLL);

        canvasSV.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                canvasSV.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int windowHeight = canvasSV.getHeight();
                int topMargin = (windowHeight / 2)
                        - (credentialsCanvasLL.getHeight() / 2)
                        - logoCanvasLL.getHeight();
                topMargin = topMargin / 2;

                RelativeLayout.LayoutParams logoCanvasLP = (RelativeLayout.LayoutParams) logoCanvasLL.getLayoutParams();
                logoCanvasLP.setMargins(0, topMargin, 0, 0);
                logoCanvasLL.setLayoutParams(logoCanvasLP);

                canvasRL.setMinimumHeight(windowHeight);
            }
        });
    }

    public static Context getOpenSRPContext() {
        return KipApplication.getInstance().context();
    }

    ////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////
    private class RemoteLoginTask extends AsyncTask<Void, Void, LoginResponse> {
        private final String userName;
        private final String password;
        private final Listener<LoginResponse> afterLoginCheck;

        private RemoteLoginTask(String userName, String password, Listener<LoginResponse> afterLoginCheck) {
            this.userName = userName;
            this.password = password;
            this.afterLoginCheck = afterLoginCheck;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected LoginResponse doInBackground(Void... params) {
            return getOpenSRPContext().userService().isValidRemoteLogin(userName, password);
        }

        @Override
        protected void onPostExecute(LoginResponse loginResponse) {
            super.onPostExecute(loginResponse);
            progressDialog.dismiss();
            afterLoginCheck.onEvent(loginResponse);
        }
    }
}
