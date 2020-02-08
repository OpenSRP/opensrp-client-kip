package org.smartregister.kip.fragment;

import android.app.Activity;
import android.view.View;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.smartregister.anc.library.fragment.MeFragment;
import org.smartregister.kip.R;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.contract.NavigationMenuContract;
import org.smartregister.kip.listener.OnLocationChangeListener;
import org.smartregister.kip.util.KipChildUtils;
import org.smartregister.view.LocationPickerView;

public class KipMeFragment extends MeFragment implements OnLocationChangeListener {
    private View view;
    private KipMeFragment fragment;
    private LocationPickerView facilitySelection;

    @Override
    protected void setUpViews(View view) {
        this.fragment = this;
        super.setUpViews(view);
        this.view = view;
    }

    @Override
    protected void setClickListeners() {
        super.setClickListeners();
        if (view != null) {
            View locationLayout = view.findViewById(R.id.me_location_section);
            facilitySelection = view.findViewById(R.id.facility_selection);
            facilitySelection.setClickable(false);
            locationLayout.setOnClickListener(v -> {
                Activity activity = fragment.getActivity();
                if (activity instanceof NavigationMenuContract) {
                    KipChildUtils.showLocations(getActivity(), fragment, ((NavigationMenuContract) activity).getNavigationMenu());
                }
            });
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser)
            updateUi(KipApplication.getInstance().context().allSharedPreferences().fetchCurrentLocality());
    }

    @Override
    public void updateUi(@Nullable String location) {
        if (facilitySelection != null && StringUtils.isNotBlank(location)) {
            facilitySelection.setText(location);
        }
    }
}
