package org.smartregister.kip.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.smartregister.stock.activity.StockControlActivity;
import org.smartregister.kip.R;
import org.smartregister.util.Utils;

public class KipStockActivity extends org.smartregister.stock.activity.StockActivity {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_stock);
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.location_switching_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //return to previous activity
                finish();
            }
        });

        TextView nameInitials = (TextView) findViewById(R.id.name_inits);
        nameInitials.setText(getLoggedInUserInitials());

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView drawer = getNavigationView();
        if (drawer != null) {
            DrawerLayout.LayoutParams lp = new DrawerLayout.LayoutParams(
                    getNavigationViewWith(), LinearLayout.LayoutParams.MATCH_PARENT);
            lp.gravity = Gravity.START;
            drawerLayout.addView(drawer, lp);
            final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.setDrawerListener(toggle);
            toggle.syncState();

            drawer.setNavigationItemSelectedListener(getNavigationViewListener());
        }
        nameInitials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer != null && !drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(GravityCompat.START);
                } else
                    finish();
            }
        });
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.stock_title));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    protected String getLoggedInUserInitials() {
        return Utils.getUserInitials();
    }

    @Override
    protected NavigationView getNavigationView() {
//        NavigationView view = (NavigationView) getLayoutInflater().inflate(R.layout.nav_view, null);
//        view.findViewById(R.id.logout_b).setOnClickListener(customNavbarListener);
//        view.findViewById(R.id.child_register).setOnClickListener(customNavbarListener);
//        view.findViewById(R.id.stock_control).setOnClickListener(customNavbarListener);
        return null;
    }

    @Override
    protected NavigationView.OnNavigationItemSelectedListener getNavigationViewListener() {
        return new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                return true;
            }
        };
    }

    @Override
    protected int getNavigationViewWith() {
        return (int) getResources().getDimension(R.dimen.nav_view_width);
    }

    @Override
    protected Class getControlActivity() {
        return StockControlActivity.class;
    }
}
