package org.smartregister.kip.listener;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import org.smartregister.kip.adapter.NavigationAdapter;
import org.smartregister.kip.util.KipConstants;

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

            switch (tag) {
                case KipConstants.DrawerMenu.CHILD_CLIENTS:
                    break;
                case KipConstants.DrawerMenu.ALL_FAMILIES:
                    break;
                case KipConstants.DrawerMenu.ANC:
                    break;
                case KipConstants.DrawerMenu.LD:
                    Toast.makeText(activity.getApplicationContext(), KipConstants.DrawerMenu.LD, Toast.LENGTH_SHORT)
                            .show();
                    break;
                case KipConstants.DrawerMenu.PNC:
                    Toast.makeText(activity.getApplicationContext(), KipConstants.DrawerMenu.PNC, Toast.LENGTH_SHORT)
                            .show();
                    break;
                case KipConstants.DrawerMenu.FAMILY_PLANNING:
                    Toast.makeText(activity.getApplicationContext(), KipConstants.DrawerMenu.FAMILY_PLANNING,
                            Toast.LENGTH_SHORT).show();
                    break;
                case KipConstants.DrawerMenu.MALARIA:
                    Toast.makeText(activity.getApplicationContext(), KipConstants.DrawerMenu.MALARIA, Toast.LENGTH_SHORT)
                            .show();
                    break;
                default:
                    break;
            }
            navigationAdapter.setSelectedView(tag);
        }
    }

    /*private void startRegisterActivity(Class registerClass) {
        Intent intent = new Intent(activity, registerClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        activity.finish();
    }*/
}
