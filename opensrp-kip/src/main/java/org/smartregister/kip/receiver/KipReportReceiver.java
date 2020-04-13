package org.smartregister.kip.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.service.intent.Moh710IntentService;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class KipReportReceiver extends BroadcastReceiver {

    private static final String TAG = KipReportReceiver.class.getCanonicalName();

    private static final String serviceActionName = "org.smartregister.kip.action.START_SERVICE_ACTION";
    private static final String serviceTypeName = "serviceType";
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onReceive(Context context, Intent alarmIntent) {
        int serviceType = alarmIntent.getIntExtra(serviceTypeName, 0);
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

    /**
     * @param context
     * @param triggerIteration in minutes
     * @param taskType         a constant from pathconstants denoting the service type
     */
    public static void setAlarm(Context context, long triggerIteration, int taskType) {
        try {
            AlarmManager alarmManager;
            PendingIntent alarmIntent;

            long triggerAt;
            long triggerInterval;
            if (context == null) {
                throw new Exception("Unable to schedule service without app context");
            }

            // Otherwise schedule based on normal interval
            triggerInterval = TimeUnit.MINUTES.toMillis(triggerIteration);
            // set trigger time to be current device time + the interval (frequency). Probably randomize this a bit so that services not launch at exactly the same time
            triggerAt = System.currentTimeMillis() + triggerInterval;

            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent alarmReceiverIntent = new Intent(context, KipReportReceiver.class);

            alarmReceiverIntent.setAction(serviceActionName + taskType);
            alarmReceiverIntent.putExtra(serviceTypeName, taskType);
            alarmIntent = PendingIntent.getBroadcast(context, 0, alarmReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                alarmManager.cancel(alarmIntent);
            } catch (Exception e) {
                Log.logError(TAG, e.getMessage());
            }
            //Elapsed real time uses the "time since system boot" as a reference, and real time clock uses UTC (wall clock) time
            alarmManager.setRepeating(AlarmManager.RTC, triggerAt, triggerInterval, alarmIntent);
        } catch (Exception e) {
            Log.logError(TAG, "Error in setting service Alarm " + e.getMessage());
        }

    }

}
