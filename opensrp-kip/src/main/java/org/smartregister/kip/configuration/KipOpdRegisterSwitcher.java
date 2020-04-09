package org.smartregister.kip.configuration;

import android.content.Context;
import android.support.annotation.NonNull;

import org.smartregister.child.domain.RegisterClickables;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.kip.activity.ChildImmunizationActivity;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.opd.configuration.OpdRegisterSwitcher;
import org.smartregister.opd.utils.OpdConstants;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-11-20
 */

public class KipOpdRegisterSwitcher implements OpdRegisterSwitcher {

    @Override
    public void switchFromOpdRegister(@NonNull CommonPersonObjectClient client, @NonNull Context context) {
        String registerType = client.getColumnmaps().get(OpdConstants.ColumnMapKey.REGISTER_TYPE);
        if (registerType != null) {
             if (registerType.equalsIgnoreCase(KipConstants.RegisterType.CHILD)) {
                ChildImmunizationActivity.launchActivity(context, client, new RegisterClickables());
            }
        }
    }


    @Override
    public boolean showRegisterSwitcher(@NonNull CommonPersonObjectClient client) {
        String registerType = client.getColumnmaps().get(OpdConstants.ColumnMapKey.REGISTER_TYPE);
        return !(registerType == null || registerType.equalsIgnoreCase(OpdConstants.RegisterType.OPD));
    }
}
