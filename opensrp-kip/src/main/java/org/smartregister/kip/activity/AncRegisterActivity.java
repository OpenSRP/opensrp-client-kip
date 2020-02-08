package org.smartregister.kip.activity;

import android.support.v4.app.Fragment;

import org.smartregister.anc.library.activity.BaseHomeRegisterActivity;
import org.smartregister.kip.contract.NavigationMenuContract;
import org.smartregister.kip.fragment.AncRegisterFragment;
import org.smartregister.kip.fragment.KipMeFragment;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.kip.view.NavDrawerActivity;
import org.smartregister.kip.view.NavigationMenu;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.fragment.BaseRegisterFragment;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-09-09
 */

public class AncRegisterActivity extends BaseHomeRegisterActivity implements NavDrawerActivity, NavigationMenuContract {
    private org.smartregister.kip.view.NavigationMenu navigationMenu;

    @Override
    protected void registerBottomNavigation() {
        // Do nothing because the bottom navigation was removed and overriden by someone
    }

    @Override
    public BaseRegisterFragment getRegisterFragment() {
        return new AncRegisterFragment();
    }

    @Override
    protected Fragment[] getOtherFragments() {
        int posCounter = 0;
        if (this.isMeItemEnabled()) {
            ++posCounter;
            BaseRegisterActivity.ME_POSITION = 1;
        }

        Fragment[] fragments = new Fragment[posCounter];
        if (this.isMeItemEnabled()) {
            fragments[BaseRegisterActivity.ME_POSITION - 1] = new KipMeFragment();
        }

        return fragments;
    }

    @Override
    public void setSelectedBottomBarMenuItem(int itemId) {
        // Do nothing here
    }

    @Override
    public boolean isMeItemEnabled() {
        return true;
    }

    @Override
    public boolean isLibraryItemEnabled() {
        return false;
    }

    @Override
    public boolean isAdvancedSearchEnabled() {
        return false;
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        createDrawer();
    }

    public void createDrawer() {
        navigationMenu = NavigationMenu.getInstance(this, null, null);

        if (navigationMenu != null) {
            navigationMenu.getNavigationAdapter().setSelectedView(KipConstants.DrawerMenu.ANC_CLIENTS);
            navigationMenu.runRegisterCount();
        }
    }

    @Override
    public void finishActivity() {
        finish();
    }

    public void openDrawer() {
        if (navigationMenu != null) {
            navigationMenu.openDrawer();
        }
    }

    public void closeDrawer() {
        if (navigationMenu != null) {
            NavigationMenu.closeDrawer();
        }
    }

    @Override
    public NavigationMenu getNavigationMenu() {
        return navigationMenu;
    }
}
