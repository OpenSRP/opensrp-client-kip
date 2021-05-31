package org.smartregister.kip.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.smartregister.kip.R;
import org.smartregister.kip.activity.Covid19VaccineStockSettingsActivity;
import org.smartregister.kip.presenter.MePresenter;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.contract.MeContract;

public class MeFragment extends org.smartregister.view.fragment.MeFragment implements MeContract.View {
    private RelativeLayout covid19VaccineStockSection;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_me, container, false);
    }

    protected void initializePresenter() {
        presenter = new MePresenter(this);
    }

    @Override
    protected void setUpViews(View view) {
        super.setUpViews(view);
        covid19VaccineStockSection = view.findViewById(R.id.covid19_vaccine_stock_section);
    }

    @Override
    protected void onViewClicked(View view) {
        int viewId = view.getId();
        if (viewId == R.id.logout_section) {
            DrishtiApplication.getInstance().logoutCurrentUser();
        } else if (viewId == R.id.covid19_vaccine_stock_section) {
            if (getContext() != null) {
                getContext().startActivity(new Intent(getContext(), Covid19VaccineStockSettingsActivity.class));
            }
        }
    }

    @Override
    protected void setClickListeners() {
        super.setClickListeners();
        covid19VaccineStockSection.setOnClickListener(meFragmentActionHandler);
    }
}
