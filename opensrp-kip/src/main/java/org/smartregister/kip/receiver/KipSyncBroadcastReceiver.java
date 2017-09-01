package org.smartregister.kip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.sync.KipAfterFetchListener;
import org.smartregister.kip.sync.KipUpdateActionsTask;
import org.smartregister.sync.SyncProgressIndicator;

import static org.smartregister.util.Log.logInfo;

public class KipSyncBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        logInfo("Sync alarm triggered. Trying to Sync.");

        KipUpdateActionsTask kipUpdateActionsTask = new KipUpdateActionsTask(
                context,
                getOpenSRPContext().actionService(),
                new SyncProgressIndicator(),
                getOpenSRPContext().allFormVersionSyncService());

        kipUpdateActionsTask.updateFromServer(new KipAfterFetchListener());

    }

    public org.smartregister.Context getOpenSRPContext() {
        return KipApplication.getInstance().context();
    }


}

