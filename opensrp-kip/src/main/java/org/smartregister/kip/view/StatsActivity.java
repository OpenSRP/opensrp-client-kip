package org.smartregister.kip.view;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import org.smartregister.child.adapter.ViewPagerAdapter;
import org.smartregister.kip.R;
import org.smartregister.kip.contract.StatsContract;
import org.smartregister.kip.fragment.StatsFragment;

//import org.smartregister.family.adapter.ViewPagerAdapter;

public class StatsActivity extends AppCompatActivity implements StatsContract.View {
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stats);
        ImageView backBtnImg = findViewById(R.id.back_button);
        if (backBtnImg != null) {
            backBtnImg.setImageResource(R.drawable.ic_back);
        }

//        Toolbar toolbar = this.findViewById(R.id.summary_toolbar);
//        toolbar.setTitle(R.string.return_to_register);
////        this.setSupportActionBar(toolbar);
//        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

        setupViews();
    }

    protected void setupViews() {
        ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
    }

    protected ViewPager setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());


        StatsFragment statsFragment = StatsFragment.newInstance(this.getIntent().getExtras());
        adapter.addFragment(statsFragment, this.getString(R.string.summary_forms).toUpperCase());


        viewPager.setAdapter(adapter);

        return viewPager;
    }

    @Override
    public void showProgressDialog(int titleIdentifier) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(titleIdentifier);
        progressDialog.setMessage(getString(R.string.please_wait_message));
        if (!isFinishing())
            progressDialog.show();
    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void onClickStats(View view) {
        switch (view.getId()) {
            case R.id.refresh_back:

                NavigationMenu navigationMenu = NavigationMenu.getInstance(this, null, null);
                if (navigationMenu != null) {
                    navigationMenu.getDrawer()
                            .openDrawer(GravityCompat.START);
                }
                break;
            default:
                break;
        }
    }

}
