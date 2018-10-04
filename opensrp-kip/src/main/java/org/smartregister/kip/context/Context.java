package org.smartregister.kip.context;


/**
 * Created by amosl on 8/22/17.
 */

public class Context extends org.smartregister.Context {

    private static Context context = new Context();

    private AllSettings allSettings;

    public static Context getInstance() {
        if (context == null){
            context = new Context();
        }
        return context;
    }

    public AllSettings allSettings() {
        initRepository();
        if (allSettings == null) {
            allSettings = new AllSettings(allSharedPreferences(), settingsRepository());
        }
        return allSettings;
    }

}
