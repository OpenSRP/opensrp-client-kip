package org.smartregister.kip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.smartregister.kip.service.intent.Moh710IntentService;

import java.util.ArrayList;
import java.util.List;

/**
 * Listens for broadcast responses from {@link Moh710IntentService}
 * service
 * Created by Jason Rogena - jrogena@ona.io on 10/07/2017.
 */

public class Moh710ServiceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = Moh710ServiceListener.class.getCanonicalName();
    public static final String TYPE = "TYPE";
    public static final String ACTION_SERVICE_DONE = "MOH710_SERVICE_DONE";
    public static final String TYPE_GENERATE_DAILY_INDICATORS = "GENERATE_DAILY_INDICATORS";
    private static Moh710ServiceBroadcastReceiver singleton;
    private final List<Moh710ServiceListener> listeners;

    public static void init(Context context) {
        if (singleton != null) {
            destroy(context);
        }

        singleton = new Moh710ServiceBroadcastReceiver();
        context.registerReceiver(singleton, new IntentFilter(ACTION_SERVICE_DONE));
    }

    private static void destroy(Context context) {
        try {
            if (singleton != null) {
                context.unregisterReceiver(singleton);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public static Moh710ServiceBroadcastReceiver getInstance() {
        return singleton;
    }

    public Moh710ServiceBroadcastReceiver() {
        this.listeners = new ArrayList<>();
    }

    public void addMoh710ServiceListener(Moh710ServiceListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeMoh710ServiceListener(Moh710ServiceListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra(TYPE);
        for (Moh710ServiceListener curListener : listeners) {
            curListener.onServiceFinish(type);
        }
    }

    public interface Moh710ServiceListener {
        void onServiceFinish(String actionType);
    }
}
