package org.smartregister.kip.activity;

import android.content.Intent;

import org.smartregister.kip.fragment.AllClientsRegisterFragmentKip;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.kip.view.NavigationMenu;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class AllClientsRegisterActivity extends KipOpdRegisterActivity {
    private NavigationMenu navigationMenu;

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new AllClientsRegisterFragmentKip();
    }

    public void createDrawer() {
        navigationMenu = NavigationMenu.getInstance(this, null, null);
        if (navigationMenu != null) {
            navigationMenu.getNavigationAdapter().setSelectedView(KipConstants.DrawerMenu.ALL_CLIENTS);
            navigationMenu.runRegisterCount();
        }
    }

    @Override
    public NavigationMenu getNavigationMenu() {
        return navigationMenu;
    }

    @Override
    public void openDrawer() {
        if (navigationMenu != null) {
            navigationMenu.openDrawer();
        }
    }


    @Override
    public void switchToBaseFragment() {
        Intent intent = new Intent(this, AllClientsRegisterActivity.class);
        startActivity(intent);
        finish();
    }
}