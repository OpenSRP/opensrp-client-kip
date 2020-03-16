package org.smartregister.kip.listener;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import org.smartregister.kip.activity.ChildRegisterActivity;
import org.smartregister.kip.adapter.NavigationAdapter;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.kip.view.NavDrawerActivity;
import org.smartregister.kip.view.NavigationMenu;

public class NavigationListener implements View.OnClickListener {

    private Activity activity;
    private NavigationAdapter navigationAdapter;

    public NavigationListener(Activity activity, NavigationAdapter adapter) {
        this.activity = activity;
        this.navigationAdapter = adapter;
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null && v.getTag() instanceof String) {
            String tag = (String) v.getTag();

            if (KipConstants.DrawerMenu.CHILD_CLIENTS.equals(tag)) {
                navigateToActivity(ChildRegisterActivity.class);
            }
            navigationAdapter.setSelectedView(tag);
        }
    }

    private void navigateToActivity(@NonNull Class<?> aClass) {
        NavigationMenu.closeDrawer();

        if (activity instanceof NavDrawerActivity) {
            ((NavDrawerActivity) activity).finishActivity();
        } else {
            activity.finish();
        }

        activity.startActivity(new Intent(activity, aClass));
    }
}
