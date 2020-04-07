package org.smartregister.kip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.service.intent.Moh710IntentService;
import org.smartregister.kip.util.KipConstants;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Moh710Receiver extends BroadcastReceiver {

    private static final String TAG = Moh710Receiver.class.getCanonicalName();

    private static final String serviceActionName = "org.smartregister.kip.action.START_SERVICE_ACTION";
    private static final String serviceTypeName = "serviceType";
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    public void onReceive(Context context, Intent intent) {

        int serviceType = intent.getIntExtra(serviceTypeName, 0);
        if (!KipApplication.getInstance().context().IsUserLoggedOut()) {
            Intent serviceIntent = null;
            switch (serviceType) {
                case KipConstants.ServiceType.DAILY_TALLIES_GENERATION:
                    android.util.Log.i(TAG, "Started DAILY_TALLIES_GENERATION service at: " + dateFormatter.format(new Date()));
                    serviceIntent = new Intent(context, Moh710IntentService.class);
                    break;
                case KipConstants.ServiceType.MONTHLY_TALLIES_GENERATION:
                    android.util.Log.i(TAG, "Started MONTHLY_TALLIES_GENERATION service at: " + dateFormatter.format(new Date()));
                    break;
            }

            if (serviceIntent != null)
                this.startService(context, serviceIntent, serviceType);
        }

    }

    private void startService(Context context, Intent serviceIntent, int serviceType) {
        context.startService(serviceIntent);
    }
}
