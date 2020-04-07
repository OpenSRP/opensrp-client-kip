package org.smartregister.kip.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.smartregister.kip.R;
import org.smartregister.kip.activity.Moh710Activity;
import org.smartregister.kip.fragment.DailyTalliesFragment;
import org.smartregister.kip.fragment.DraftMonthlyFragment;
import org.smartregister.kip.fragment.SentMonthlyFragment;

public class Moh710ReportssectionsPagrAdapter extends FragmentPagerAdapter {

    private Moh710Activity moh710Activity;

    public Moh710ReportssectionsPagrAdapter(Moh710Activity moh710Activity, FragmentManager fm) {
        super(fm);
        this.moh710Activity = moh710Activity;

    }


    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0:
                return DailyTalliesFragment.newInstance(moh710Activity.getReportGrouping());
            case 1:
                return DraftMonthlyFragment.newInstance(moh710Activity.getReportGrouping());
            default:
                break;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return moh710Activity.getString(R.string.hia2_daily_tallies);
            case 1:
                return moh710Activity.getString(R.string.hia2_draft_monthly);
            default:
                break;
        }
        return null;
    }
}
