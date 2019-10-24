package org.smartregister.kip;


import org.smartregister.kip.application.KipApplication;

public class TestKipApplication extends KipApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.Theme_AppCompat); //or just R.style.Theme_AppCompat
    }
}
