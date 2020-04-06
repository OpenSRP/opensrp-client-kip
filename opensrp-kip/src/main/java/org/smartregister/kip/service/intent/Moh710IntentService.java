package org.smartregister.kip.service.intent;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.receiver.Moh710ServiceBroadcastReceiver;
import org.smartregister.kip.repository.DailyTalliesRepository;
import org.smartregister.kip.service.Moh710Service;
import org.smartregister.repository.EventClientRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Moh710IntentService extends IntentService {
    private static final String TAG = Moh710IntentService.class.getCanonicalName();
    private DailyTalliesRepository dailyTalliesRepository;
    private EventClientRepository kipEventClientRepository;
    private Moh710Service moh710Service;

    public Moh710IntentService() {
        super("Moh710IntentService");
    }

    /**
     * Build indicators,save them to the db and generate report
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Started MOH 710 service");
        try {
            // Generate daily MOH 710 indicators
            generateDailyMohIndicators();

            // Send broadcast message
            sendBroadcastMessage(Moh710ServiceBroadcastReceiver.TYPE_GENERATE_DAILY_INDICATORS);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "Finishing MOH 710 service");
    }

    private void sendBroadcastMessage(String type) {
        Intent intent = new Intent();
        intent.setAction(Moh710ServiceBroadcastReceiver.ACTION_SERVICE_DONE);
        intent.putExtra(Moh710ServiceBroadcastReceiver.TYPE, type);
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dailyTalliesRepository = KipApplication.getInstance().dailyTalliesRepository();
        kipEventClientRepository = KipApplication.getInstance().eventClientRepository();
        moh710Service = new Moh710Service();
        return super.onStartCommand(intent, flags, startId);
    }

    private void generateDailyMohIndicators() {
        try {
            generateVaccineIndicators();
            generateRecurringServiceIndicators();
            generateAdverseEffect();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    private void generateVaccineIndicators() {
        final String eventDateColumn = "event_date";
        final String updatedAtColumn = "updated_at";
        final String orderByUpdatedAtColumn = " order by " + updatedAtColumn + " asc";
        final String PREVIOUS_VACCINE_REPORT_DATES_QUERY = "select distinct strftime('%Y-%m-%d',datetime(date/1000, 'unixepoch')) as " + eventDateColumn + ", " + updatedAtColumn + " from vaccines ";
        final String MOH710_VACCINE_LAST_PROCESSED_DATE = "MOH710_VACCINE_LAST_PROCESSED_DATE";
        String lastUpdatedAt = null;

        try {

            SQLiteDatabase db = KipApplication.getInstance().getRepository().getWritableDatabase();
            //get previous dates if shared preferences is null meaning reports for previous months haven't been generated
            String lastProcessedDate = KipApplication.getInstance().context().allSharedPreferences().getPreference(MOH710_VACCINE_LAST_PROCESSED_DATE);
            ArrayList<HashMap<String, String>> reportDates;
            if (lastProcessedDate == null || lastProcessedDate.isEmpty()) {
                reportDates = kipEventClientRepository.rawQuery(db, PREVIOUS_VACCINE_REPORT_DATES_QUERY.concat(orderByUpdatedAtColumn));

            } else {
                reportDates = kipEventClientRepository.rawQuery(db, PREVIOUS_VACCINE_REPORT_DATES_QUERY.concat(" where " + updatedAtColumn + " > " + lastProcessedDate + orderByUpdatedAtColumn));
            }

            if (reportDates == null || reportDates.isEmpty()) {
                return;
            }

            for (Map<String, String> dates : reportDates) {
                String date = dates.get(eventDateColumn);
                lastUpdatedAt = dates.get(updatedAtColumn);

                Map<String, Object> mohReport = moh710Service.generateVaccineIndicators(db, date);
                dailyTalliesRepository.save(date, mohReport);

            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (lastUpdatedAt != null) {
                KipApplication.getInstance().context().allSharedPreferences().savePreference(MOH710_VACCINE_LAST_PROCESSED_DATE, lastUpdatedAt);
            }
        }

    }

    private void generateRecurringServiceIndicators() {
        String lastUpdatedAt = null;
        final String eventDateColumn = "event_date";
        final String updatedAtColumn = "updated_at";
        final String orderByUpdatedAtColumn = " order by " + updatedAtColumn + " asc";
        final String PREVIOUS_VACCINE_REPORT_DATES_QUERY = "select distinct strftime('%Y-%m-%d',datetime(date/1000, 'unixepoch')) as " + eventDateColumn + ", " + updatedAtColumn + " from recurring_service_records ";
        final String MOH710_RECURRING_SERVICE_LAST_PROCESSED_DATE = "MOH710_RECURRING_SERVICE_LAST_PROCESSED_DATE";

        try {
            SQLiteDatabase db = KipApplication.getInstance().getRepository().getWritableDatabase();
            //get previous dates if shared preferences is null meaning reports for previous months haven't been generated
            String lastProcessedDate = KipApplication.getInstance().context().allSharedPreferences().getPreference(MOH710_RECURRING_SERVICE_LAST_PROCESSED_DATE);
            ArrayList<HashMap<String, String>> reportDates;
            if (lastProcessedDate == null || lastProcessedDate.isEmpty()) {
                reportDates = kipEventClientRepository.rawQuery(db, PREVIOUS_VACCINE_REPORT_DATES_QUERY.concat(orderByUpdatedAtColumn));

            } else {
                reportDates = kipEventClientRepository.rawQuery(db, PREVIOUS_VACCINE_REPORT_DATES_QUERY.concat(" where " + updatedAtColumn + " > " + lastProcessedDate + orderByUpdatedAtColumn));
            }

            if (reportDates == null || reportDates.isEmpty()) {
                return;
            }

            for (Map<String, String> dates : reportDates) {
                String date = dates.get(eventDateColumn);
                lastUpdatedAt = dates.get(updatedAtColumn);

                Map<String, Object> mohReport = moh710Service.generateRecurringServiceIndicators(db, date);
                dailyTalliesRepository.save(date, mohReport);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (lastUpdatedAt != null) {
                KipApplication.getInstance().context().allSharedPreferences().savePreference(MOH710_RECURRING_SERVICE_LAST_PROCESSED_DATE, lastUpdatedAt);
            }
        }

    }

    private void generateAdverseEffect() {
        String lastUpdatedAt = null;
        final String eventType = "'AEFI'";
        final String updatedAtColumn = EventClientRepository.event_column.updatedAt.toString();
        final String orderByUpdatedAtColumn = " order by " + updatedAtColumn + " asc";
        final String PREVIOUS_AEFI_REPORT_DATES_QUERY = "select distinct strftime('%Y-%m-%d'," + EventClientRepository.event_column.eventDate.toString() + ") as eventDate, " + updatedAtColumn + " from " + EventClientRepository.Table.event.name() + " where " + EventClientRepository.event_column.eventType.toString() + " = " + eventType;
        final String MOH710_AEFI_LAST_PROCESSED_DATE = "MOH710_AEFI_LAST_PROCESSED_DATE";

        try {
            SQLiteDatabase db = KipApplication.getInstance().getRepository().getWritableDatabase();
            //get previous dates if shared preferences is null meaning reports for previous months haven't been generated
            String lastProcessedDate = KipApplication.getInstance().context().allSharedPreferences().getPreference(MOH710_AEFI_LAST_PROCESSED_DATE);
            ArrayList<HashMap<String, String>> reportDates;
            if (lastProcessedDate == null || lastProcessedDate.isEmpty()) {
                reportDates = kipEventClientRepository.rawQuery(db, PREVIOUS_AEFI_REPORT_DATES_QUERY.concat(orderByUpdatedAtColumn));

            } else {
                reportDates = kipEventClientRepository.rawQuery(db, PREVIOUS_AEFI_REPORT_DATES_QUERY.concat(" where " + updatedAtColumn + " > " + lastProcessedDate + orderByUpdatedAtColumn));
            }

            if (reportDates == null || reportDates.isEmpty()) {
                return;
            }

            for (Map<String, String> dates : reportDates) {
                String date = dates.get(EventClientRepository.event_column.eventDate.toString());
                lastUpdatedAt = dates.get(updatedAtColumn);

                Map<String, Object> mohReport = moh710Service.generateAdverseEffectIndicators(db, date);
                dailyTalliesRepository.save(date, mohReport);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (lastUpdatedAt != null) {
                KipApplication.getInstance().context().allSharedPreferences().savePreference(MOH710_AEFI_LAST_PROCESSED_DATE, lastUpdatedAt);
            }
        }

    }


}
