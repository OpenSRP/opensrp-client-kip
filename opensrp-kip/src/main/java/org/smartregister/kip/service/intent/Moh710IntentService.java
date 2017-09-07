package org.smartregister.kip.service.intent;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.domain.MonthlyTally;
import org.smartregister.kip.domain.ReportHia2Indicator;
import org.smartregister.kip.receiver.Moh710ServiceBroadcastReceiver;
import org.smartregister.kip.repository.DailyTalliesRepository;
import org.smartregister.kip.repository.KipEventClientRepository;
import org.smartregister.kip.repository.MonthlyTalliesRepository;
import org.smartregister.kip.service.Moh710Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.ReportUtils;


/**
 * Created by onamacuser on 18/03/2016.
 */
public class Moh710IntentService extends IntentService {
    private static final String TAG = Moh710IntentService.class.getCanonicalName();
    public static final String GENERATE_REPORT = "GENERATE_REPORT";
    public static final String REPORT_MONTH = "REPORT_MONTH";
    private DailyTalliesRepository dailyTalliesRepository;
    private MonthlyTalliesRepository monthlyTalliesRepository;
    private KipEventClientRepository kipEventClientRepository;
    private Moh710Service moh710Service;

    //HIA2 Status
    private VaccineRepository vaccineRepository;
    private static final int DAYS_BEFORE_OVERDUE = 10;

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
            boolean generateReport = intent.getBooleanExtra(GENERATE_REPORT, false);
            if (!generateReport) {
                // Generate daily HIA2 indicators
                generateDailyHia2Indicators();

                // Send broadcast message
                sendBroadcastMessage(Moh710ServiceBroadcastReceiver.TYPE_GENERATE_DAILY_INDICATORS);
            } else {
                String monthString = intent.getStringExtra(REPORT_MONTH);
                if (!TextUtils.isEmpty(monthString)) {
                    Date month = Moh710Service.dfyymm.parse(monthString);
                    //generateMonthlyReport(month);
                    sendBroadcastMessage(Moh710ServiceBroadcastReceiver.TYPE_GENERATE_MONTHLY_REPORT);
                }
            }
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
        monthlyTalliesRepository = KipApplication.getInstance().monthlyTalliesRepository();
        kipEventClientRepository = KipApplication.getInstance().eventClientRepository();
        moh710Service = new Moh710Service();

        vaccineRepository = KipApplication.getInstance().vaccineRepository();

        return super.onStartCommand(intent, flags, startId);
    }

    private void generateDailyHia2Indicators() {
        try {
            SQLiteDatabase db = KipApplication.getInstance().getRepository().getWritableDatabase();
            //get previous dates if shared preferences is null meaning reports for previous months haven't been generated
            String lastProcessedDate = KipApplication.getInstance().context().allSharedPreferences().getPreference(Moh710Service.MOH710_LAST_PROCESSED_DATE);
            ArrayList<HashMap<String, String>> reportDates;
            if (lastProcessedDate == null || lastProcessedDate.isEmpty()) {
                reportDates = kipEventClientRepository.rawQuery(db, Moh710Service.PREVIOUS_REPORT_DATES_QUERY.concat(" order by eventDate asc"));

            } else {
                reportDates = kipEventClientRepository.rawQuery(db, Moh710Service.PREVIOUS_REPORT_DATES_QUERY.concat(" where " + KipEventClientRepository.event_column.updatedAt + " >'" + lastProcessedDate + "'" + " order by eventDate asc"));
            }

            for (Map<String, String> dates : reportDates) {
                String date = dates.get(KipEventClientRepository.event_column.eventDate.name());
                String updatedAt = dates.get(KipEventClientRepository.event_column.updatedAt.name());

                Map<String, Object> mohReport = moh710Service.generateIndicators(db, date);
                dailyTalliesRepository.save(date, mohReport);
                KipApplication.getInstance().context().allSharedPreferences().savePreference(Moh710Service.MOH710_LAST_PROCESSED_DATE, updatedAt);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    private void generateMonthlyReport(Date month) {
        try {
            if (month != null) {
                List<MonthlyTally> tallies = monthlyTalliesRepository
                        .find(MonthlyTalliesRepository.DF_YYYYMM.format(month));
                if (tallies != null) {
                    List<ReportHia2Indicator> tallyReports = new ArrayList<>();
                    for (MonthlyTally curTally : tallies) {
                        // tallyReports.add(curTally.getReportHia2Indicator());
                    }

                    ReportUtils.createReport(this, tallyReports, month, Moh710Service.REPORT_NAME);

                    for (MonthlyTally curTally : tallies) {
                        curTally.setDateSent(Calendar.getInstance().getTime());
                        monthlyTalliesRepository.save(curTally);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

}
