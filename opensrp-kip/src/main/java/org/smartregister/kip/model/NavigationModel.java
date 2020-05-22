package org.smartregister.kip.model;

import org.smartregister.kip.R;
import org.smartregister.kip.contract.NavigationContract;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class NavigationModel implements NavigationContract.Model {
    private static NavigationModel instance;
    private static List<NavigationOption> navigationOptions = new ArrayList<>();

    public static NavigationModel getInstance() {
        if (instance == null)
            instance = new NavigationModel();

        return instance;
    }

    @Override
    public List<NavigationOption> getNavigationItems() {
        if (navigationOptions.size() == 0) {

            NavigationOption allClientsOption = new NavigationOption(R.mipmap.sidemenu_children
                , R.mipmap.sidemenu_children_active, R.string.all_clients, KipConstants.DrawerMenu.ALL_CLIENTS, 0, true);

            if (allClientsOption.isEnabled()) {
                navigationOptions.add(allClientsOption);
            }

            NavigationOption childNavigationOption = new NavigationOption(R.mipmap.sidemenu_children,
                    R.mipmap.sidemenu_children_active, R.string.menu_child_clients, KipConstants.DrawerMenu.CHILD_CLIENTS,
                    0, true);
            if (childNavigationOption.isEnabled()) {
                navigationOptions.add(childNavigationOption);
            }
        }

        return navigationOptions;
    }

    @Override
    public String getCurrentUser() {
        String prefferedName = "";
        try {
            prefferedName = Utils.getPrefferedName().split(" ")[0];
        } catch (Exception e) {
            Timber.e(e, "NavigationModel --> getCurrentUser");
        }

        return prefferedName;
    }
}

