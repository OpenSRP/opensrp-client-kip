package org.smartregister.kip.context;


import org.smartregister.repository.SettingsRepository;

/**
 * Created by amosl on 8/22/17.
 */

public class KipContext extends org.smartregister.Context {

    private static KipContext kipContext = new KipContext();


    public static KipContext getInstance() {
        if (kipContext == null) {
            kipContext = new KipContext();
        }
        return kipContext;
    }


    public SettingsRepository kipSettingsRepository() {
        return super.settingsRepository();
    }

}
