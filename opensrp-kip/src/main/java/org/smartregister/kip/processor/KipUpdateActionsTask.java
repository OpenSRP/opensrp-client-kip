package org.smartregister.kip.processor;

import android.content.Context;
import org.smartregister.kip.receiver.KipReportReceiver;
import org.smartregister.kip.util.KipConstants;


public class KipUpdateActionsTask {

    public static void setAlarms(Context context) {
        KipReportReceiver.setAlarm(context, 2, KipConstants.ServiceType.DAILY_TALLIES_GENERATION);
    }

}
