package org.smartregister.kip.activity;

import android.app.Activity;
import android.support.annotation.NonNull;

public class ActivityConfiguration {
    private Class<? extends ChildRegisterActivity> homeRegisterActivityClass;
    private Class<? extends Activity> landingPageActivityClass;

    public ActivityConfiguration() {
        setHomeRegisterActivityClass(ChildRegisterActivity.class);
        setLandingPageActivityClass(getHomeRegisterActivityClass());
    }

    public Class<? extends ChildRegisterActivity> getHomeRegisterActivityClass() {
        return homeRegisterActivityClass;
    }

    public void setHomeRegisterActivityClass(@NonNull Class<? extends ChildRegisterActivity> homeRegisterActivityClass) {
        this.homeRegisterActivityClass = homeRegisterActivityClass;
    }

    public Class<? extends Activity> getLandingPageActivityClass() {
        return landingPageActivityClass;
    }

    public void setLandingPageActivityClass(Class<? extends Activity> landingPageActivityClass) {
        this.landingPageActivityClass = landingPageActivityClass;
    }
}
