package org.smartregister.kip.configuration;

import android.content.Context;
import androidx.annotation.NonNull;

import org.smartregister.child.domain.RegisterClickables;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.kip.activity.ChildImmunizationActivity;
import org.smartregister.kip.util.KipOpdConstants;
import org.smartregister.opd.configuration.OpdRegisterSwitcher;

public class KipOpdRegisterSwitcher implements OpdRegisterSwitcher {
    @Override
    public void switchFromOpdRegister(@NonNull CommonPersonObjectClient client, @NonNull Context context) {
        String registerType = client.getColumnmaps().get(KipOpdConstants.ColumnMapKey.REGISTER_TYPE);
        if (registerType != null) {
             if (registerType.equalsIgnoreCase(KipOpdConstants.RegisterType.CHILD)) {
                ChildImmunizationActivity.launchActivity(context, client, new RegisterClickables());
            }
        }
    }

    @Override
    public boolean showRegisterSwitcher(@NonNull CommonPersonObjectClient client) {
        String registerType = client.getColumnmaps().get(KipOpdConstants.ColumnMapKey.REGISTER_TYPE);
        return !(registerType == null || registerType.equalsIgnoreCase(KipOpdConstants.RegisterType.OPD));
    }
}
