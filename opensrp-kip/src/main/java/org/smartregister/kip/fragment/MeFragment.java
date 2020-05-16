package org.smartregister.kip.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.kip.R;
import org.smartregister.kip.presenter.MePresenter;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.contract.MeContract;

public class MeFragment extends org.smartregister.view.fragment.MeFragment implements MeContract.View {

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
    protected void onViewClicked(View view) {
        int i = view.getId();
        if (i == R.id.logout_section) {
            DrishtiApplication.getInstance().logoutCurrentUser();
        }
    }

}
