package org.smartregister.kip.sync;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.domain.DownloadStatus;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Response;
import org.smartregister.growthmonitoring.service.intent.ZScoreRefreshIntentService;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.domain.Client;
import org.smartregister.kip.domain.Stock;
import org.smartregister.kip.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.kip.receiver.VaccinatorAlarmReceiver;
import org.smartregister.kip.repository.KipEventClientRepository;
import org.smartregister.kip.repository.StockRepository;
import org.smartregister.kip.service.intent.PullUniqueIdsIntentService;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.service.ActionService;
import org.smartregister.service.AllFormVersionSyncService;
import org.smartregister.service.HTTPAgent;
import org.smartregister.service.ImageUploadSyncService;
import org.smartregister.sync.AdditionalSyncService;
import org.smartregister.view.BackgroundAction;
import org.smartregister.view.LockingBackgroundTask;
import org.smartregister.view.ProgressIndicator;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.JsonFormUtils;
import util.KipConstants;
import util.NetworkUtils;

import static java.text.MessageFormat.format;
import static org.smartregister.domain.FetchStatus.fetched;
import static org.smartregister.domain.FetchStatus.fetchedFailed;
import static org.smartregister.domain.FetchStatus.nothingFetched;
import static org.smartregister.util.Log.logError;
import static org.smartregister.util.Log.logInfo;

public class KipUpdateActionsTask {
    private static final String EVENTS_SYNC_PATH = "/rest/event/add";
    private static final String REPORTS_SYNC_PATH = "/rest/report/add";
    private static final String STOCK_Add_PATH = "/rest/stockresource/add/";


    private static final String STOCK_SYNC_PATH = "rest/stockresource/sync/";
    private final LockingBackgroundTask task;
    private final ActionService actionService;
    private final Context context;
    private final AllFormVersionSyncService allFormVersionSyncService;
    private AdditionalSyncService additionalSyncService;
    private KipAfterFetchListener kipAfterFetchListener;
    private final HTTPAgent httpAgent;


    public KipUpdateActionsTask(Context context, ActionService actionService, ProgressIndicator progressIndicator,
                                AllFormVersionSyncService allFormVersionSyncService) {
        this.actionService = actionService;
        this.context = context;
        this.allFormVersionSyncService = allFormVersionSyncService;
        this.additionalSyncService = null;
        task = new LockingBackgroundTask(progressIndicator);
        this.httpAgent = KipApplication.getInstance().context().getHttpAgent();

    }

    public void setAdditionalSyncService(AdditionalSyncService additionalSyncService) {
        this.additionalSyncService = additionalSyncService;
    }

    public void updateFromServer(final KipAfterFetchListener kipAfterFetchListener) {
        this.kipAfterFetchListener = kipAfterFetchListener;

        sendSyncStatusBroadcastMessage(context, FetchStatus.fetchStarted);
        if (KipApplication.getInstance().context().IsUserLoggedOut()) {
            logInfo("Not updating from server as user is not logged in.");
            return;
        }

        task.doActionInBackground(new BackgroundAction<FetchStatus>() {
            public FetchStatus actionToDoInBackgroundThread() {
                if (NetworkUtils.isNetworkAvailable()) {
                    FetchStatus fetchStatusForForms = sync();
                    FetchStatus fetchStatusForActions = actionService.fetchNewActions();
                    kipAfterFetchListener.partialFetch(fetchStatusForActions);

                    startImageUploadIntentService(context);
                    startPullUniqueIdsIntentService(context);

                    FetchStatus fetchStatusAdditional = additionalSyncService == null ? nothingFetched : additionalSyncService.sync();

                    if (KipApplication.getInstance().context().configuration().shouldSyncForm()) {

                        allFormVersionSyncService.verifyFormsInFolder();
                        FetchStatus fetchVersionStatus = allFormVersionSyncService.pullFormDefinitionFromServer();
                        DownloadStatus downloadStatus = allFormVersionSyncService.downloadAllPendingFormFromServer();

                        if (downloadStatus == DownloadStatus.downloaded) {
                            allFormVersionSyncService.unzipAllDownloadedFormFile();
                        }

                        if (fetchVersionStatus == fetched || downloadStatus == DownloadStatus.downloaded) {
                            return fetched;
                        }
                    }

                    if (fetchStatusForActions == fetched || fetchStatusForForms == fetched || fetchStatusAdditional == fetched)
                        return fetched;

                    return fetchStatusForForms;
                }

                return FetchStatus.noConnection;
            }

            public void postExecuteInUIThread(FetchStatus result) {
                Intent intent = new Intent(context, ZScoreRefreshIntentService.class);
                context.startService(intent);
                if (result.equals(FetchStatus.nothingFetched) || result.equals(FetchStatus.fetched)) {
                    ECSyncUpdater ecSyncUpdater = ECSyncUpdater.getInstance(context);
                    ecSyncUpdater.updateLastCheckTimeStamp(Calendar.getInstance().getTimeInMillis());
                }
                kipAfterFetchListener.afterFetch(result);
                sendSyncStatusBroadcastMessage(context, result);
            }
        });
    }

    private FetchStatus sync() {
        try {
            int totalCount = 0;
            pushToServer();
            ECSyncUpdater ecUpdater = ECSyncUpdater.getInstance(context);

            // Retrieve database host from preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
            String locatioId = allSharedPreferences.fetchDefaultLocalityId(allSharedPreferences.fetchRegisteredANM());
            while (true) {
                long startSyncTimeStamp = ecUpdater.getLastSyncTimeStamp();

                Map<String, String> params = new HashMap<>();
//                params.put(AllConstants.SyncFilters.FILTER_PROVIDER, allSharedPreferences.fetchRegisteredANM());
                params.put(AllConstants.SyncFilters.FILTER_LOCATION_ID, locatioId);

                int eCount = ecUpdater.fetchAllClientsAndEvents(params);
                totalCount += eCount;

                if (eCount <= 0) {
                    if (eCount < 0) totalCount = eCount;
                    break;
                }

                long lastSyncTimeStamp = ecUpdater.getLastSyncTimeStamp();
                KipClientProcessor.getInstance(context).processClient(ecUpdater.allEvents(startSyncTimeStamp, lastSyncTimeStamp));
                Log.i(getClass().getName(), "!!!!! Sync count:  " + eCount);
                kipAfterFetchListener.partialFetch(fetched);
            }
            pullStockFromServer();

            if (totalCount == 0) {
                return nothingFetched;
            } else if (totalCount < 0) {
                return fetchedFailed;
            } else {
                return fetched;
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "", e);
            return fetchedFailed;
        }

    }

    private void pushToServer() {
        pushECToServer();
        pushReportsToServer();
        pushStockToServer();
    }

    public Date syncDate(String stringDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            Date date = format.parse(stringDate);
            System.out.println(date);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String formatVaccines(String vaccine) {
        return vaccine.replaceAll("[^\\dA-Za-z ]", "").replaceAll("\\s+", "+").toUpperCase();


    }

    public void readFromPsmart(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        JsonFormUtils.savePsmartData(this, KipApplication.getInstance().context(), jsonString, new AllSharedPreferences(preferences).fetchRegisteredANM());

    }

    public void pushToPsmart() {

        KipEventClientRepository db = KipApplication.getInstance().eventClientRepository();
        JSONObject clientObject = new JSONObject();
        JSONObject internalIDSObject = new JSONObject();
        JSONObject idObject = new JSONObject();
        JSONArray idObjectArray = new JSONArray();
        JSONObject ids = new JSONObject();
        JSONArray jArray = null;
        JSONObject obsObject = null;
        JSONObject vaccineObject = null;
        JSONObject nameObject = new JSONObject();
        JSONObject patientNameObject = new JSONObject();
        JSONObject birthdateObject = new JSONObject();
        JSONArray jsonImmunizationArray = new JSONArray();
        JSONArray addressArray = new JSONArray();
        JSONObject immunizationObj = new JSONObject();
        JSONObject birthdateApproxObject = new JSONObject();
        JSONObject deathObject = new JSONObject();
        JSONObject deathIndicatorObject = new JSONObject();
        JSONObject patientAddressObject = new JSONObject();
        JSONObject addressObject = new JSONObject();
        JSONObject psmartaddressObject = new JSONObject();
        JSONObject general = new JSONObject();
        List<JSONObject> clients = db.getEvents(syncDate("2010-10-15T09:27:37Z"));
        String baseID = null;
        for (JSONObject clientsObject : clients) {
            try {
                if (clientsObject.getString("eventType").equalsIgnoreCase("Child Enrollment")) {
                    baseID = clientsObject.getString("baseEntityId");

                    clientObject = new JSONObject(db.getClientByBaseEntityId(baseID).toString());

                    internalIDSObject = new JSONObject(clientObject.getString("identifiers"));
                    idObject = new JSONObject();
                    if (internalIDSObject.has("OPENMRS_ID")) {
                        idObject.put("IDENTIFIER_TYPE", "OPENMRS_ID");
                        idObject.put("ID", internalIDSObject.getString("OPENMRS_ID"));
                        idObject.put("ASSIGNING_AUTHORITY", "OPENMRS");
                        idObjectArray.put(idObject);
                    }
                    idObject = new JSONObject();
                    if (internalIDSObject.has("OPENMRS_UUID")) {
                        idObject.put("IDENTIFIER_TYPE", "OPENMRS_UUID");
                        idObject.put("ID", internalIDSObject.getString("OPENMRS_UUID"));
                        idObject.put("ASSIGNING_AUTHORITY", "OPENMRS");
                        idObjectArray.put(idObject);
                    }

                    ids.put("INTERNAL_PATIENT_ID", idObjectArray);
                    List<JSONObject> pendingEvents = db.getEventsByBaseEntityId(baseID);
                    for (JSONObject jsonObject : pendingEvents) {
                        try {
                            if (jsonObject.getString("eventType").equalsIgnoreCase("Vaccination")) {
                                jArray = new JSONArray(jsonObject.getString("obs"));
                                for (int i = 0; i < jArray.length(); i++) {
                                    obsObject = new JSONObject(jArray.get(i).toString());
                                    if (obsObject.getString("fieldDataType").equalsIgnoreCase("date")) {
                                        vaccineObject = new JSONObject();
                                        vaccineObject.put("NAME", formatVaccines(obsObject.getString("formSubmissionField")));
                                        vaccineObject.put("DATE_ADMINISTERED", formatVaccines(new JSONArray(obsObject.getString("values")).get(0).toString()));
                                        jsonImmunizationArray.put(vaccineObject);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    if (clientObject.has("firstName")) {
                        nameObject.put("FIRST_NAME", clientObject.getString("firstName"));
                    }
                    if (clientObject.has("middleName")) {
                        nameObject.put("MIDDLE_NAME", clientObject.getString("middleName"));
                    }
                    if (clientObject.has("lastName")) {
                        nameObject.put("LAST_NAME", clientObject.getString("lastName"));
                    }
                    patientNameObject.put("PATIENT_NAME", nameObject);
                    birthdateObject.put("DATE_OF_BIRTH", clientObject.getString("birthdate"));
                    if (clientObject.getString("birthdateApprox").equalsIgnoreCase("false")) {
                        birthdateApproxObject.put("DATE_OF_BIRTH", "EXACT");
                    } else {
                        birthdateApproxObject.put("DATE_OF_BIRTH_PRECISION", "APPROX");

                    }

                    addressArray = new JSONArray(clientObject.getString("addresses"));
                    if (addressArray.length() > 0) {
                        patientAddressObject = new JSONObject(addressArray.get(0).toString());
                        if (patientAddressObject.has("stateProvince"))
                            addressObject.put("PROVINCE", patientAddressObject.getString("stateProvince"));
                        if (patientAddressObject.has("cityVillage"))
                            addressObject.put("VILLAGE", patientAddressObject.getString("cityVillage"));
                        if (patientAddressObject.has("countyDistrict"))
                            addressObject.put("DISTRICT", patientAddressObject.getString("countyDistrict"));
                    }


                    deathObject.put("DEATH_DATE", "");
                    deathIndicatorObject.put("DEATH_INDICATOR", "N");
                    psmartaddressObject.put("PATIENT_ADDRESS", addressObject);
                    birthdateObject.put("DATE_OF_BIRTH", clientObject.getString("birthdate"));
                    immunizationObj.put("IMMUNIZATION", jsonImmunizationArray);




//                    Log.i("PHASE-IMMUNIZATION", immunizationObj.toString());
//                    Log.i("PHASE-ids", ids.toString());
//                    Log.i("PHASE-patientAddress", patientAddressObject.toString());
                    general = new JSONObject(ids.toString() + birthdateObject.toString()
                    +birthdateApproxObject.toString());
                    Log.i("PHASE-patientAddress", ids.toString() + patientNameObject + birthdateObject+ birthdateApproxObject+ patientAddressObject.toString() + immunizationObj.toString());




                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }

    private void pushECToServer() {
        KipEventClientRepository db = KipApplication.getInstance().eventClientRepository();
        boolean keepSyncing = true;
        int limit = 50;
        try {
            // db.markAllAsUnSynced();

            while (keepSyncing) {
                Map<String, Object> pendingEvents = null;
                pendingEvents = db.getUnSyncedEvents(limit);

                if (pendingEvents.isEmpty()) {
                    return;
                }

                String baseUrl = KipApplication.getInstance().context().configuration().dristhiBaseURL();
                if (baseUrl.endsWith("/")) {
                    baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
                }
                // create request body
                JSONObject request = new JSONObject();
                if (pendingEvents.containsKey("clients")) {
                    request.put("clients", pendingEvents.get("clients"));
                }
                if (pendingEvents.containsKey("events")) {
                    request.put("events", pendingEvents.get("events"));
                }
                String jsonPayload = request.toString();
                Response<String> response = httpAgent.post(
                        format("{0}/{1}",
                                baseUrl,
                                EVENTS_SYNC_PATH),
                        jsonPayload);
                if (response.isFailure()) {
                    Log.e(getClass().getName(), baseUrl + " " + EVENTS_SYNC_PATH + " " + jsonPayload);
                    Log.e(getClass().getName(), "Events sync failed.");

                    Log.e(getClass().getName(), response.toString());
                    return;
                }
                db.markEventsAsSynced(pendingEvents);
                Log.i(getClass().getName(), "Events synced successfully.");
            }
        } catch (JSONException e) {
            Log.e(getClass().getName(), e.getMessage());
        }

    }

    private void pullStockFromServer() {
        final String LAST_STOCK_SYNC = "last_stock_sync";
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
        String anmId = allSharedPreferences.fetchRegisteredANM();
        String baseUrl = KipApplication.getInstance().context().configuration().dristhiBaseURL();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
        }

        while (true) {
            long timestamp = preferences.getLong(LAST_STOCK_SYNC, 0);
            String timeStampString = String.valueOf(timestamp);
            String uri = format("{0}/{1}?providerid={2}&serverVersion={3}",
                    baseUrl,
                    STOCK_SYNC_PATH,
                    anmId,
                    timeStampString
            );
            Response<String> response = httpAgent.fetch(uri);
            if (response.isFailure()) {
                logError(format("Stock pull failed."));
                return;
            }
            String jsonPayload = response.payload();
            ArrayList<Stock> Stock_arrayList = getStockFromPayload(jsonPayload);
            Long highestTimestamp = getHighestTimestampFromStockPayLoad(jsonPayload);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(LAST_STOCK_SYNC, highestTimestamp);
            editor.commit();
            if (Stock_arrayList.isEmpty()) {
                return;
            } else {
                StockRepository stockRepository = KipApplication.getInstance().stockRepository();
                for (int j = 0; j < Stock_arrayList.size(); j++) {
                    Stock fromServer = Stock_arrayList.get(j);
                    List<Stock> existingStock = stockRepository.findUniqueStock(fromServer.getVaccineTypeId(), fromServer.getTransactionType(), fromServer.getProviderid(),
                            String.valueOf(fromServer.getValue()), String.valueOf(fromServer.getDateCreated()), fromServer.getToFrom());
                    if (!existingStock.isEmpty()) {
                        for (Stock stock : existingStock) {
                            fromServer.setId(stock.getId());
                        }
                    }
                    stockRepository.add(fromServer);
                }

            }
        }
    }

    private Long getHighestTimestampFromStockPayLoad(String jsonPayload) {
        Long toreturn = 0l;
        try {
            JSONObject stockContainer = new JSONObject(jsonPayload);
            if (stockContainer.has("stocks")) {
                JSONArray stockArray = stockContainer.getJSONArray("stocks");
                for (int i = 0; i < stockArray.length(); i++) {

                    JSONObject stockObject = stockArray.getJSONObject(i);
                    if (stockObject.getLong("serverVersion") > toreturn) {
                        toreturn = stockObject.getLong("serverVersion");
                    }

                }
            }
        } catch (Exception e) {
            Log.e(getClass().getCanonicalName(), e.getMessage());
        }
        return toreturn;
    }

    private ArrayList<Stock> getStockFromPayload(String jsonPayload) {
        ArrayList<Stock> Stock_arrayList = new ArrayList<>();
        try {
            JSONObject stockcontainer = new JSONObject(jsonPayload);
            if (stockcontainer.has("stocks")) {
                JSONArray stockArray = stockcontainer.getJSONArray("stocks");
                for (int i = 0; i < stockArray.length(); i++) {
                    JSONObject stockObject = stockArray.getJSONObject(i);
                    Stock stock = new Stock(null,
                            stockObject.getString("transaction_type"),
                            stockObject.getString("providerid"),
                            stockObject.getInt("value"),
                            stockObject.getLong("date_created"),
                            stockObject.getString("to_from"),
                            BaseRepository.TYPE_Synced,
                            stockObject.getLong("date_updated"),
                            stockObject.getString("vaccine_type_id"));
                    Stock_arrayList.add(stock);
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getCanonicalName(), e.getMessage());
        }
        return Stock_arrayList;
    }

    private void pushStockToServer() {
        boolean keepSyncing = true;
        int limit = 50;

        try {

            while (keepSyncing) {
                StockRepository stockRepository = KipApplication.getInstance().stockRepository();
                ArrayList<Stock> stocks = (ArrayList<Stock>) stockRepository.findUnSyncedWithLimit(limit);
                JSONArray stocksarray = createJsonArrayFromStockArray(stocks);
                if (stocks.isEmpty()) {
                    return;
                }

                String baseUrl = KipApplication.getInstance().context().configuration().dristhiBaseURL();
                if (baseUrl.endsWith("/")) {
                    baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
                }
                // create request body
                JSONObject request = new JSONObject();
                request.put("stocks", stocksarray);

                String jsonPayload = request.toString();
                Response<String> response = httpAgent.post(
                        format("{0}/{1}",
                                baseUrl,
                                STOCK_Add_PATH),
                        jsonPayload);
                if (response.isFailure()) {
                    Log.e(getClass().getName(), "Stocks sync failed.");
                    return;
                }
                stockRepository.markEventsAsSynced(stocks);
                Log.i(getClass().getName(), "Stocks synced successfully.");
            }
        } catch (JSONException e) {
            Log.e(getClass().getName(), e.getMessage());
        }
    }

    private JSONArray createJsonArrayFromStockArray(ArrayList<Stock> stocks) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < stocks.size(); i++) {
            JSONObject stock = new JSONObject();
            try {
                stock.put("identifier", stocks.get(i).getId());
                stock.put("vaccine_type_id", stocks.get(i).getVaccineTypeId());
                stock.put("transaction_type", stocks.get(i).getTransactionType());
                stock.put("providerid", stocks.get(i).getProviderid());
                stock.put("date_created", stocks.get(i).getDateCreated());
                stock.put("value", stocks.get(i).getValue());
                stock.put("to_from", stocks.get(i).getToFrom());
                stock.put("date_updated", stocks.get(i).getUpdatedAt());
                array.put(stock);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array;
    }

    private void pushReportsToServer() {
        KipEventClientRepository db = KipApplication.getInstance().eventClientRepository();
        try {
            boolean keepSyncing = true;
            int limit = 50;
            while (keepSyncing) {
                List<JSONObject> pendingReports = null;
                pendingReports = db.getUnSyncedReports(limit);

                if (pendingReports.isEmpty()) {
                    return;
                }

                String baseUrl = KipApplication.getInstance().context().configuration().dristhiBaseURL();
                if (baseUrl.endsWith("/")) {
                    baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
                }
                // create request body
                JSONObject request = new JSONObject();

                request.put("reports", pendingReports);
                String jsonPayload = request.toString();
                Response<String> response = httpAgent.post(
                        MessageFormat.format("{0}/{1}",
                                baseUrl,
                                REPORTS_SYNC_PATH),
                        jsonPayload);
                if (response.isFailure()) {
                    Log.e(getClass().getName(), "Reports sync failed.");
                    return;
                }
                db.markReportsAsSynced(pendingReports);
                Log.i(getClass().getName(), "Reports synced successfully.");
            }
        } catch (JSONException e) {
            Log.e(getClass().getName(), e.getMessage());
        }
    }

    private void startImageUploadIntentService(Context context) {
        Intent intent = new Intent(context, ImageUploadSyncService.class);
        context.startService(intent);
    }

    private void startPullUniqueIdsIntentService(Context context) {
        Intent intent = new Intent(context, PullUniqueIdsIntentService.class);
        context.startService(intent);
    }


    private void sendSyncStatusBroadcastMessage(Context context, FetchStatus fetchStatus) {
        Intent intent = new Intent();
        intent.setAction(SyncStatusBroadcastReceiver.ACTION_SYNC_STATUS);
        intent.putExtra(SyncStatusBroadcastReceiver.EXTRA_FETCH_STATUS, fetchStatus);
        context.sendBroadcast(intent);
    }

    public static void setAlarms(Context context) {
        VaccinatorAlarmReceiver.setAlarm(context, 2, KipConstants.ServiceType.DAILY_TALLIES_GENERATION);
        VaccinatorAlarmReceiver.setAlarm(context, 2, KipConstants.ServiceType.WEIGHT_SYNC_PROCESSING);
        VaccinatorAlarmReceiver.setAlarm(context, 2, KipConstants.ServiceType.VACCINE_SYNC_PROCESSING);
        VaccinatorAlarmReceiver.setAlarm(context, 2, KipConstants.ServiceType.RECURRING_SERVICES_SYNC_PROCESSING);
    }

}
