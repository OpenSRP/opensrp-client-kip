package org.smartregister.kip.util;


import androidx.annotation.NonNull;

import org.smartregister.opd.configuration.BaseOpdRegisterProviderMetadata;
import org.smartregister.util.Utils;

import java.util.Map;

public class KipOpdRegisterProviderMetadata extends BaseOpdRegisterProviderMetadata {

    @Override
    public boolean isClientHaveGuardianDetails(@NonNull Map<String, String> columnMaps) {
        String registerType = getRegisterType(columnMaps);
        return registerType != null && registerType.contains("CHILD");
    }

    @NonNull
    @Override
    public String getGender(@NonNull Map<String, String> columnMaps) {
        String gender = Utils.getValue(columnMaps, KipConstants.KEY.GENDER, true);
        if(gender.startsWith("F") || gender.startsWith("f")){
            columnMaps.put(KipConstants.KEY.GENDER, "Female");
        } else {
            columnMaps.put(KipConstants.KEY.GENDER, "Male");
        }
        return super.getGender(columnMaps);
    }
}
