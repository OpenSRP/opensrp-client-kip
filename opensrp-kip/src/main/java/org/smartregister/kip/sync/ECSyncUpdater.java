package org.smartregister.kip.sync;

import android.content.Context;
import android.util.Log;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.domain.Response;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.repository.KipEventClientRepository;
import org.smartregister.service.HTTPAgent;
import org.smartregister.util.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import util.MoveToMyCatchmentUtils;

public class ECSyncUpdater {
    public static final String SEARCH_URL = "/rest/event/sync";

    private static final String LAST_SYNC_TIMESTAMP = "LAST_SYNC_TIMESTAMP";
    private static final String LAST_CHECK_TIMESTAMP = "LAST_SYNC_CHECK_TIMESTAMP";

    private final KipEventClientRepository db;
    private final Context context;

    private static ECSyncUpdater instance;

    public static ECSyncUpdater getInstance(Context context) {
        if (instance == null) {
            instance = new ECSyncUpdater(context);
        }
        return instance;
    }

    public ECSyncUpdater(Context context) {
        this.context = context;
        db = KipApplication.getInstance().eventClientRepository();
    }

    public JSONObject fetchPsmart() throws Exception {
        final Response[] resp = new Response[1];
        String motherBaseID = UUID.randomUUID().toString();
        JSONObject clientObject = new JSONObject();
        JSONObject motherObject = new JSONObject();
        JSONObject addressFields = new JSONObject();
        JSONObject relationships = new JSONObject();
        JSONObject addresses = new JSONObject();
        JSONObject addressesOut = new JSONObject();
        JSONArray addressesFieldsArray = new JSONArray();
        JSONObject physicalAddresses = new JSONObject();
        JSONObject openmrs_id_client = new JSONObject();
        JSONObject openmrs_id_mum = new JSONObject();
        SimpleDateFormat spf = new SimpleDateFormat("yyyyMMdd");
        final CountDownLatch latch = new CountDownLatch(1);
        Thread thread = new Thread(new Runnable() {



            HTTPAgent httpAgent = KipApplication.getInstance().context().getHttpAgent();
            @Override
            public void run() {
                try {


                    resp[0] = httpAgent.fetch("http://192.168.1.207/shr.json");
//                    resp[0] = httpAgent.fetch("http://10.20.25.219/shr.json");
                    latch.countDown();
                    if (resp[0].isFailure()) {
                        throw new Exception(SEARCH_URL + " not returned data");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        latch.await();

        JSONObject responseObj = new JSONObject((String) resp[0].payload());
        JSONObject clientDetails = responseObj.getJSONObject("PATIENT_IDENTIFICATION");
        JSONObject clientDetailsNames = clientDetails.getJSONObject("PATIENT_NAME");
        JSONObject clientDetailsIds = clientDetails.getJSONObject("EXTERNAL_PATIENT_ID");
        JSONObject clientDetailsAddresses = clientDetails.getJSONObject("PATIENT_ADDRESS");

        relationships.put("relationshipType", "8d91a210-c2cc-11de-8d13-0010c6dffd0f");
        relationships.put("relativeEntityId", "dd32df6c-32c9-4d08-a116-e1386e8c93c5");
//                    relationships.put("relativeEntityId", motherBaseID);

        motherObject.put("mother", relationships);
        clientObject.put("relationships", motherObject);
        Date birthdate = spf.parse(clientDetails.getString("DATE_OF_BIRTH"));
        spf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        clientObject.put("birthdate", spf.format(birthdate));
        clientObject.put("birthdateApprox", false);
        clientObject.put("deathdateApprox", false);
        if (clientDetailsNames.has("FIRST_NAME")) {
            clientObject.put("firstName", clientDetailsNames.getString("FIRST_NAME"));
        }
        if (clientDetailsNames.has("LAST_NAME")) {
            clientObject.put("lastName", clientDetailsNames.getString("LAST_NAME"));
        }
        if (clientDetails.getString("DATE_OF_BIRTH").startsWith("f")) {
            clientObject.put("gender", "Female");
        } else {
            clientObject.put("gender", "Male");
        }

        physicalAddresses = clientDetailsAddresses.getJSONObject("PHYSICAL_ADDRESS");
//                    addresses.put("address3",physicalAddresses.getString("VILLAGE"));
//                    addresses.put("address2",physicalAddresses.getString("NEAREST_LANDMARK"));
//                    addresses.put("address1",clientDetailsAddresses.getString("POSTAL_ADDRESS"));
        addresses.put("address3", "za");
        addresses.put("address4", "Za");
        addressFields.put("addressFields", addresses);

        addressesOut.put("addressType", "usual_residence");
//                    addressesOut.put("cityVillage",physicalAddresses.getString("WARD"));
        addressesOut.put("cityVillage", "CENTRAL SAKWA");
//                    addressesOut.put("countyDistrict",physicalAddresses.getString("COUNTY"));
        addressesOut.put("countyDistrict", "BONDO");
        addressesOut.put("stateProvince", "SIAYA");

        addressesFieldsArray.put(addressFields);
        addressesFieldsArray.put(addressesOut);
        clientObject.put("addresses", addressesFieldsArray);
        clientObject.put("baseEntityId", "dd32df6c-32c9-4d08-a116-e1386e8c93d1");
//                    clientObject.put("baseEntityId",clientDetailsIds.getString("ID"));
        openmrs_id_client.put("OPENMRS_ID", "MEKXJV");
        clientObject.put("identifiers", openmrs_id_client);

        clientObject.put("dateCreated", spf.format(new Date()));
        clientObject.put("type", "Client");


        JSONObject clientMotherObject = new JSONObject();

        clientMotherObject.put("birthdate", "2010-01-01T00:00:00.000Z");
        clientMotherObject.put("birthdateApprox", true);
        JSONObject motherClientDetailsNames = clientDetails.getJSONObject("MOTHER_DETAILS").getJSONObject("MOTHER_NAME");
        clientMotherObject.put("firstName", motherClientDetailsNames.getString("FIRST_NAME"));
        clientMotherObject.put("lastName", motherClientDetailsNames.getString("LAST_NAME"));
        clientMotherObject.put("gender", "Female");
        clientMotherObject.put("addresses", addressesFieldsArray);
        clientMotherObject.put("baseEntityId", motherBaseID);
        openmrs_id_mum.put("M_KIP_ID", "MEKXJV_mother");
        clientMotherObject.put("identifiers", openmrs_id_mum);
        clientMotherObject.put("dateCreated", spf.format(new Date()));
        clientMotherObject.put("type", "Client");

//                    addClient(motherBaseID, clientMotherObject);
//                    Log.i("ONBLEEE ", clientMotherObject.toString());

        addClient("dd32df6c-32c9-4d08-a116-e1386e8c93d1", clientObject);
        Log.i("ONBLEEE ", clientObject.toString());

        return clientObject;

    }


    private JSONObject fetchAsJsonObject(Map<String, String> params) throws Exception {
        HTTPAgent httpAgent = KipApplication.getInstance().context().getHttpAgent();
        String baseUrl = KipApplication.getInstance().context().
                configuration().dristhiBaseURL();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
        }

        Long lastSyncDatetime = getLastSyncTimeStamp();
        Log.i(ECSyncUpdater.class.getName(), "LAST SYNC DT :" + new DateTime(lastSyncDatetime));

        String url = baseUrl + SEARCH_URL + "?";

        for (String k : params.keySet()) {
            url += k + "=" + params.get(k) + "&";
        }
        url += "serverVersion=" + lastSyncDatetime;
        Log.i(ECSyncUpdater.class.getName(), "URL: " + url);

        if (httpAgent == null) {
            throw new Exception(SEARCH_URL + " http agent is null");
        }

        Response resp = httpAgent.fetch(url);
        if (resp.isFailure()) {


            throw new Exception(SEARCH_URL + " not returned data");
        }

        return new JSONObject((String) resp.payload());
    }

    public int fetchAllClientsAndEvents(Map<String, String> params) {
        try {

            JSONObject jsonObject = fetchAsJsonObject(params);

            int eventsCount = jsonObject.has("no_of_events") ? jsonObject.getInt("no_of_events") : 0;
            if (eventsCount == 0) {
                return eventsCount;
            }

            JSONArray events = jsonObject.has("events") ? jsonObject.getJSONArray("events") : new JSONArray();
            JSONArray clients = jsonObject.has("clients") ? jsonObject.getJSONArray("clients") : new JSONArray();

            long lastSyncTimeStamp = batchSave(events, clients);
            if (lastSyncTimeStamp > 0l) {
                updateLastSyncTimeStamp(lastSyncTimeStamp);
            }

            return eventsCount;

        } catch (Exception e) {
            Log.e(getClass().getName(), "Exception", e);
            return -1;
        }
    }

    public List<JSONObject> allEvents(long startSyncTimeStamp, long lastSyncTimeStamp) {
        try {
            return db.getEvents(startSyncTimeStamp, lastSyncTimeStamp);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Exception", e);
        }
        return new ArrayList<>();
    }

    public List<JSONObject> getAllEvents(Date lastSyncDate) {
        try {
            return db.getEvents(lastSyncDate);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Exception", e);
        }
        return new ArrayList<>();
    }

    public List<JSONObject> getEvents(Date lastSyncDate) {
        try {
            return db.getEvents(lastSyncDate);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Exception", e);
        }
        return new ArrayList<>();
    }

    public List<JSONObject> getEventsByBaseEnityId(String baseEntityId) {
        try {
            return db.getEventsByBaseEntityId(baseEntityId);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Exception", e);
        }
        return new ArrayList<>();
    }

    public List<JSONObject> getEvents(Date lastSyncDate, String syncStatus) {
        try {
            return db.getEvents(lastSyncDate, syncStatus);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Exception", e);
        }
        return new ArrayList<>();
    }

    public JSONObject getClient(String baseEntityId) {
        try {
            return db.getClientByBaseEntityId(baseEntityId);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Exception", e);
        }
        return null;
    }

    public void addClient(String baseEntityId, JSONObject jsonObject) {
        try {
            db.addorUpdateClient(baseEntityId, jsonObject);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Exception", e);
        }
    }

    public void addEvent(String baseEntityId, JSONObject jsonObject) {
        try {
            db.addEvent(baseEntityId, jsonObject);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Exception", e);
        }
    }

    public void addReport(JSONObject jsonObject) {
        try {
            db.addReport(jsonObject);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Exception", e);
        }
    }

    public long getLastSyncTimeStamp() {
        return Long.parseLong(Utils.getPreference(context, LAST_SYNC_TIMESTAMP, "0"));
    }

    private void updateLastSyncTimeStamp(long lastSyncTimeStamp) {
        Utils.writePreference(context, LAST_SYNC_TIMESTAMP, lastSyncTimeStamp + "");
    }

    public long getLastCheckTimeStamp() {
        return Long.parseLong(Utils.getPreference(context, LAST_CHECK_TIMESTAMP, "0"));
    }

    public void updateLastCheckTimeStamp(long lastSyncTimeStamp) {
        Utils.writePreference(context, LAST_CHECK_TIMESTAMP, lastSyncTimeStamp + "");
    }

    public long batchSave(JSONArray events, JSONArray clients) throws Exception {
        db.batchInsertClients(clients);
        return db.batchInsertEvents(events, getLastSyncTimeStamp());
    }

    public <T> T convert(JSONObject jo, Class<T> t) {
        return db.convert(jo, t);
    }

    public JSONObject convertToJson(Object object) {
        return db.convertToJson(object);
    }

    public boolean deleteClient(String baseEntityId) {
        return db.deleteClient(baseEntityId);
    }

    public boolean deleteEventsByBaseEntityId(String baseEntityId) {
        return db.deleteEventsByBaseEntityId(baseEntityId, MoveToMyCatchmentUtils.MOVE_TO_CATCHMENT_EVENT);
    }
}
