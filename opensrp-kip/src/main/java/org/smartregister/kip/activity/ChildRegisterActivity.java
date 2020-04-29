package org.smartregister.kip.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.View;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.smartregister.Context;
import org.smartregister.child.ChildLibrary;
import org.smartregister.child.activity.BaseChildRegisterActivity;
import org.smartregister.child.model.BaseChildRegisterModel;
import org.smartregister.child.presenter.BaseChildRegisterPresenter;
import org.smartregister.child.util.Constants;
import org.smartregister.child.util.JsonFormUtils;
import org.smartregister.child.util.Utils;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.kip.R;
import org.smartregister.kip.contract.NavigationMenuContract;
import org.smartregister.kip.event.LoginEvent;
import org.smartregister.kip.fragment.AdvancedSearchFragment;
import org.smartregister.kip.fragment.ChildRegisterFragment;
import org.smartregister.kip.fragment.MeFragment;
import org.smartregister.kip.presenter.ChildRegisterPresenter;
import org.smartregister.kip.util.KipChildUtils;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.kip.util.KipJsonFormUtils;
import org.smartregister.kip.util.KipLocationUtility;
import org.smartregister.kip.view.NavDrawerActivity;
import org.smartregister.kip.view.NavigationMenu;
import org.smartregister.listener.BottomNavigationListener;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.lang.ref.WeakReference;

public class ChildRegisterActivity extends BaseChildRegisterActivity implements NavDrawerActivity, NavigationMenuContract {
    private NavigationMenu navigationMenu;

    @Override
    public NavigationMenu getNavigationMenu() {
        return navigationMenu;
    }

    @Override
    protected void attachBaseContext(android.content.Context base) {
        // get language from prefs
        String lang = KipChildUtils.getLanguage(base.getApplicationContext());
        super.attachBaseContext(KipChildUtils.setAppLocale(base, lang));
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(org.smartregister.R.id.bottom_navigation);
        this.bottomNavigationView.setVisibility(ChildLibrary.getInstance().getProperties().getPropertyBoolean("feature.bottom.navigation.enabled") ? View.VISIBLE : View.GONE);
        if (bottomNavigationView != null) {
            if (isMeItemEnabled()) {
                bottomNavigationView.getMenu().add(Menu.NONE, org.smartregister.R.string.action_me, Menu.NONE, org.smartregister.R.string.me).setIcon(
                        bottomNavigationHelper.writeOnDrawable(org.smartregister.R.drawable.bottom_bar_initials_background, userInitials, getResources()));
            }

            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

            if (!isLibraryItemEnabled()) {
                bottomNavigationView.getMenu().removeItem(R.id.action_library);
            }

            BottomNavigationListener bottomNavigationListener = new BottomNavigationListener(this);
            bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationListener);
        }
    }

    @Override
    protected Fragment[] getOtherFragments() {
        ME_POSITION = 1;
        int positionCounter = getPositionCounter();

        Fragment[] fragments = new Fragment[positionCounter];
        if (isAdvancedSearchEnabled()) {
            fragments[BaseRegisterActivity.ADVANCED_SEARCH_POSITION - 1] = new AdvancedSearchFragment();
        }

        if (isMeItemEnabled()) {
            fragments[BaseRegisterActivity.ME_POSITION - 1] = new MeFragment();
        }

        return fragments;
    }

    private int getPositionCounter() {
        int positionCounter = 0;
        if (isAdvancedSearchEnabled()) {
            BaseRegisterActivity.ADVANCED_SEARCH_POSITION = ++positionCounter;
        }

        if (isMeItemEnabled()) {
            BaseRegisterActivity.ME_POSITION = ++positionCounter;
        }
        return positionCounter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public String getRegistrationForm() {
        return KipConstants.JSON_FORM.CHILD_ENROLLMENT;
    }

    @Override
    public void startNFCCardScanner() {
        // Todo
    }

    @NotNull
    private Form getForm() {
        Form form = new Form();
        form.setWizard(false);
        form.setHideSaveLabel(true);
        form.setNextLabel("");
        return form;
    }

    public boolean isMeItemEnabled() {
        return true;
    }

    public boolean isLibraryItemEnabled() {
        return true;
    }

    public boolean isAdvancedSearchEnabled() {
        return true;
    }

    @Override
    protected void initializePresenter() {
        presenter = new ChildRegisterPresenter(this, new BaseChildRegisterModel());
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        WeakReference<ChildRegisterFragment> childRegisterFragmentWeakReference = new WeakReference<>(
                new ChildRegisterFragment());

        return childRegisterFragmentWeakReference.get();
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        createDrawer();
    }

    private void createDrawer() {
        navigationMenu = NavigationMenu.getInstance(this, null, null);
        if (navigationMenu != null) {
            navigationMenu.getNavigationAdapter().setSelectedView(KipConstants.DrawerMenu.CHILD_CLIENTS);
            navigationMenu.runRegisterCount();
        }
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void showNfcNotInstalledDialog(LoginEvent event) {
        if (event != null) {
            KipChildUtils.removeStickyEvent(event);
            new Handler(Looper.getMainLooper()).post(() -> showNfcDialog());
        }
    }

    private void showNfcDialog() {
        KipChildUtils.showDialogMessage(this, R.string.nfc_sdk_missing, R.string.please_install_nfc_sdk);
    }

    public void refresh() {
        Intent intent = new Intent(ChildRegisterActivity.this, ChildRegisterActivity.class);
        getApplicationContext()
                .startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = new Intent(this, Utils.metadata().childFormActivity);
        if (jsonForm.has(KipConstants.KEY.ENCOUNTER_TYPE) && jsonForm.optString(KipConstants.KEY.ENCOUNTER_TYPE).equals(
                KipConstants.KEY.BIRTH_REGISTRATION)) {
            Context context = org.smartregister.login.task.RemoteLoginTask.getOpenSRPContext();
            KipLocationUtility.addChildRegLocHierarchyQuestions(jsonForm, context);
            KipJsonFormUtils.addRelationshipTypesQuestions(jsonForm);


        }
        intent.putExtra(Constants.INTENT_KEY.JSON, jsonForm.toString());
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, getForm());
        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    public void finishActivity() {
        finish();
    }

    @Override
    public void openDrawer() {
        if (navigationMenu != null) {
            navigationMenu.openDrawer();
        }
    }

    @Override
    public void closeDrawer() {
        if (navigationMenu != null) {
            NavigationMenu.closeDrawer();
        }
    }
}
