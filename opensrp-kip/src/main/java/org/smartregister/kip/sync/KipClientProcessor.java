package org.smartregister.kip.sync;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.DateUtil;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.service.intent.WeightIntentService;
import org.smartregister.immunization.domain.ServiceRecord;
import org.smartregister.immunization.domain.ServiceSchedule;
import org.smartregister.immunization.domain.ServiceType;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.service.intent.RecurringIntentService;
import org.smartregister.immunization.service.intent.VaccineIntentService;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.sync.ClientProcessor;
import org.smartregister.sync.CloudantDataHandler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import util.KipConstants;
import util.MoveToMyCatchmentUtils;

public class KipClientProcessor extends ClientProcessor {

    private static final String TAG = "KipClientProcessor";
    private static final String detailsUpdated = "detailsUpdated";
    private static final String[] openmrs_gen_ids = {"zeir_id"};
    private static KipClientProcessor instance;

    private KipClientProcessor(Context context) {
        super(context);
    }

    public static KipClientProcessor getInstance(Context context) {
        if (instance == null) {
            instance = new KipClientProcessor(context);
        }
        return instance;
    }

    @Override
    public synchronized void processClient() throws Exception {
        CloudantDataHandler handler = CloudantDataHandler.getInstance(getContext());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
        long lastSyncTimeStamp = allSharedPreferences.fetchLastSyncDate(0);
        Date lastSyncDate = new Date(lastSyncTimeStamp);
        String clientClassificationStr = getFileContents("ec_client_classification.json");
        String clientVaccineStr = getFileContents("ec_client_vaccine.json");
        String clientWeightStr = getFileContents("ec_client_weight.json");
        String clientServiceStr = getFileContents("ec_client_service.json");

        //this seems to be easy for now cloudant json to events model is crazy
        List<JSONObject> events = handler.getUpdatedEventsAndAlerts(lastSyncDate);
        if (!events.isEmpty()) {
            List<JSONObject> unsyncEvents = new ArrayList<>();
            for (JSONObject event : events) {
                String type = event.has("eventType") ? event.getString("eventType") : null;
                if (type == null) {
                    continue;
                }

                if (type.equals(VaccineIntentService.EVENT_TYPE) || type.equals(VaccineIntentService.EVENT_TYPE_OUT_OF_CATCHMENT)) {
                    JSONObject clientVaccineClassificationJson = new JSONObject(clientVaccineStr);
                    if (isNullOrEmptyJSONObject(clientVaccineClassificationJson)) {
                        continue;
                    }

                    processVaccine(event, clientVaccineClassificationJson, type.equals(VaccineIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));
                } else if (type.equals(WeightIntentService.EVENT_TYPE) || type.equals(WeightIntentService.EVENT_TYPE_OUT_OF_CATCHMENT)) {
                    JSONObject clientWeightClassificationJson = new JSONObject(clientWeightStr);
                    if (isNullOrEmptyJSONObject(clientWeightClassificationJson)) {
                        continue;
                    }

                    processWeight(event, clientWeightClassificationJson, type.equals(WeightIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));
                } else if (type.equals(RecurringIntentService.EVENT_TYPE)) {
                    JSONObject clientServiceClassificationJson = new JSONObject(clientServiceStr);
                    if (isNullOrEmptyJSONObject(clientServiceClassificationJson)) {
                        continue;
                    }
                    processService(event, clientServiceClassificationJson);
                } else if (type.equals(MoveToMyCatchmentUtils.MOVE_TO_CATCHMENT_EVENT)) {
                    unsyncEvents.add(event);
                } else if (type.equals(KipConstants.EventType.DEATH)) {
                    unsyncEvents.add(event);
                } else {
                    JSONObject clientClassificationJson = new JSONObject(clientClassificationStr);
                    if (isNullOrEmptyJSONObject(clientClassificationJson)) {
                        continue;
                    }
                    //iterate through the events
                    processEvent(event, clientClassificationJson);
                }
            }

            // Unsync events that are should not be in this device
            if (!unsyncEvents.isEmpty()) {
                unSync(unsyncEvents);
            }
        }

        allSharedPreferences.saveLastSyncDate(lastSyncDate.getTime());
    }

    @Override
    public synchronized void processClient(List<JSONObject> events) throws Exception {

        String clientClassificationStr = getFileContents("ec_client_classification.json");
        String clientVaccineStr = getFileContents("ec_client_vaccine.json");
        String clientWeightStr = getFileContents("ec_client_weight.json");
        String clientServiceStr = getFileContents("ec_client_service.json");

        if (!events.isEmpty()) {
            List<JSONObject> unsyncEvents = new ArrayList<>();
            for (JSONObject event : events) {

                String eventType = event.has("eventType") ? event.getString("eventType") : null;
                if (eventType == null) {
                    continue;
                }

                if (eventType.equals(VaccineIntentService.EVENT_TYPE) || eventType.equals(VaccineIntentService.EVENT_TYPE_OUT_OF_CATCHMENT)) {
                    JSONObject clientVaccineClassificationJson = new JSONObject(clientVaccineStr);
                    if (isNullOrEmptyJSONObject(clientVaccineClassificationJson)) {
                        continue;
                    }

                    processVaccine(event, clientVaccineClassificationJson, eventType.equals(VaccineIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));
                } else if (eventType.equals(WeightIntentService.EVENT_TYPE) || eventType.equals(WeightIntentService.EVENT_TYPE_OUT_OF_CATCHMENT)) {
                    JSONObject clientWeightClassificationJson = new JSONObject(clientWeightStr);
                    if (isNullOrEmptyJSONObject(clientWeightClassificationJson)) {
                        continue;
                    }

                    processWeight(event, clientWeightClassificationJson, eventType.equals(WeightIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));
                } else if (eventType.equals(RecurringIntentService.EVENT_TYPE)) {
                    JSONObject clientServiceClassificationJson = new JSONObject(clientServiceStr);
                    if (isNullOrEmptyJSONObject(clientServiceClassificationJson)) {
                        continue;
                    }
                    processService(event, clientServiceClassificationJson);
                } else if (eventType.equals(MoveToMyCatchmentUtils.MOVE_TO_CATCHMENT_EVENT)) {
                    unsyncEvents.add(event);
                } else if (eventType.equals(KipConstants.EventType.DEATH)) {
                    unsyncEvents.add(event);
                } else {
                    JSONObject clientClassificationJson = new JSONObject(clientClassificationStr);
                    if (isNullOrEmptyJSONObject(clientClassificationJson)) {
                        continue;
                    }
                    //iterate through the events
                    if (event.has("client")) {
                        processEvent(event, event.getJSONObject("client"), clientClassificationJson);
                    }
                }
            }

            // Unsync events that are should not be in this device
            if (!unsyncEvents.isEmpty()) {
                unSync(unsyncEvents);
            }
        }

    }

    private Boolean processVaccine(JSONObject vaccine, JSONObject clientVaccineClassificationJson, boolean outOfCatchment) throws Exception {

        try {

            if (vaccine == null || vaccine.length() == 0) {
                return false;
            }

            if (clientVaccineClassificationJson == null || clientVaccineClassificationJson.length() == 0) {
                return false;
            }

            ContentValues contentValues = processCaseModel(vaccine, clientVaccineClassificationJson);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = simpleDateFormat.parse(contentValues.getAsString(VaccineRepository.DATE));

                VaccineRepository vaccineRepository = KipApplication.getInstance().vaccineRepository();
                Vaccine vaccineObj = new Vaccine();
                vaccineObj.setBaseEntityId(contentValues.getAsString(VaccineRepository.BASE_ENTITY_ID));
                vaccineObj.setName(contentValues.getAsString(VaccineRepository.NAME));
                if (contentValues.containsKey(VaccineRepository.CALCULATION)) {
                    vaccineObj.setCalculation(parseInt(contentValues.getAsString(VaccineRepository.CALCULATION)));
                }
                vaccineObj.setDate(date);
                vaccineObj.setAnmId(contentValues.getAsString(VaccineRepository.ANMID));
                vaccineObj.setLocationId(contentValues.getAsString(VaccineRepository.LOCATIONID));
                vaccineObj.setSyncStatus(VaccineRepository.TYPE_Synced);
                vaccineObj.setFormSubmissionId(vaccine.has(VaccineRepository.FORMSUBMISSION_ID) ? vaccine.getString(VaccineRepository.FORMSUBMISSION_ID) : null);
                vaccineObj.setEventId(vaccine.getString("id")); //FIXME hard coded id
                vaccineObj.setOutOfCatchment(outOfCatchment ? 1 : 0);

                vaccineRepository.add(vaccineObj);
            }
            return true;

        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            return null;
        }
    }

    private Boolean processWeight(JSONObject weight, JSONObject clientWeightClassificationJson, boolean outOfCatchment) throws Exception {

        try {

            if (weight == null || weight.length() == 0) {
                return false;
            }

            if (clientWeightClassificationJson == null || clientWeightClassificationJson.length() == 0) {
                return false;
            }

            ContentValues contentValues = processCaseModel(weight, clientWeightClassificationJson);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {
                Date date = DateUtil.getDateFromString(contentValues.getAsString(WeightRepository.DATE));
                if (date == null) {
                    try {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                        date = dateFormat.parse(contentValues.getAsString(WeightRepository.DATE));
                    } catch (Exception e) {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                        date = dateFormat.parse(contentValues.getAsString(WeightRepository.DATE));
                    }
                }

                WeightRepository weightRepository = KipApplication.getInstance().weightRepository();
                Weight weightObj = new Weight();
                weightObj.setBaseEntityId(contentValues.getAsString(WeightRepository.BASE_ENTITY_ID));
                if (contentValues.containsKey(WeightRepository.KG)) {
                    weightObj.setKg(parseFloat(contentValues.getAsString(WeightRepository.KG)));
                }
                weightObj.setDate(date);
                weightObj.setAnmId(contentValues.getAsString(WeightRepository.ANMID));
                weightObj.setLocationId(contentValues.getAsString(WeightRepository.LOCATIONID));
                weightObj.setSyncStatus(WeightRepository.TYPE_Synced);
                weightObj.setFormSubmissionId(weight.has(WeightRepository.FORMSUBMISSION_ID) ? weight.getString(WeightRepository.FORMSUBMISSION_ID) : null);
                weightObj.setEventId(weight.getString("id")); //FIXME hard coded id
                weightObj.setOutOfCatchment(outOfCatchment ? 1 : 0);


                weightRepository.add(weightObj);
            }
            return true;

        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            return null;
        }
    }

    private Boolean processService(JSONObject service, JSONObject clientVaccineClassificationJson) throws Exception {

        try {

            if (service == null || service.length() == 0) {
                return false;
            }

            if (clientVaccineClassificationJson == null || clientVaccineClassificationJson.length() == 0) {
                return false;
            }

            ContentValues contentValues = processCaseModel(service, clientVaccineClassificationJson);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {

                String name = contentValues.getAsString(RecurringServiceTypeRepository.NAME);
                if (StringUtils.isNotBlank(name)) {
                    name = name.replaceAll("_", " ").replace("dose", "").trim();
                }

                Date date = null;
                String eventDateStr = contentValues.getAsString(RecurringServiceRecordRepository.DATE);
                if (StringUtils.isNotBlank(eventDateStr)) {
                    date = DateUtil.getDateFromString(eventDateStr);
                    if (date == null) {
                        try {
                            date = DateUtil.parseDate(eventDateStr);
                        } catch (ParseException e) {
                            Log.e(TAG, e.toString(), e);
                        }
                    }
                }

                String value = null;

                if (StringUtils.containsIgnoreCase(name, "ITN")) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String itnDateString = contentValues.getAsString("itn_date");
                    if (StringUtils.isNotBlank(itnDateString)) {
                        date = simpleDateFormat.parse(itnDateString);
                    }


                    value = RecurringIntentService.ITN_PROVIDED;
                    if (contentValues.getAsString("itn_has_net") != null) {
                        value = RecurringIntentService.CHILD_HAS_NET;
                    }

                }

                RecurringServiceTypeRepository recurringServiceTypeRepository = KipApplication.getInstance().recurringServiceTypeRepository();
                List<ServiceType> serviceTypeList = recurringServiceTypeRepository.searchByName(name);
                if (serviceTypeList == null || serviceTypeList.isEmpty()) {
                    return false;
                }

                if (date == null) {
                    return false;
                }

                RecurringServiceRecordRepository recurringServiceRecordRepository = KipApplication.getInstance().recurringServiceRecordRepository();
                ServiceRecord serviceObj = new ServiceRecord();
                serviceObj.setBaseEntityId(contentValues.getAsString(RecurringServiceRecordRepository.BASE_ENTITY_ID));
                serviceObj.setName(name);
                serviceObj.setDate(date);
                serviceObj.setAnmId(contentValues.getAsString(RecurringServiceRecordRepository.ANMID));
                serviceObj.setLocationId(contentValues.getAsString(RecurringServiceRecordRepository.LOCATIONID));
                serviceObj.setSyncStatus(RecurringServiceRecordRepository.TYPE_Synced);
                serviceObj.setFormSubmissionId(service.has(RecurringServiceRecordRepository.FORMSUBMISSION_ID) ? service.getString(RecurringServiceRecordRepository.FORMSUBMISSION_ID) : null);
                serviceObj.setEventId(service.getString("id")); //FIXME hard coded id
                serviceObj.setValue(value);
                serviceObj.setRecurringServiceId(serviceTypeList.get(0).getId());

                recurringServiceRecordRepository.add(serviceObj);
            }
            return true;

        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            return null;
        }
    }

    @Override
    public Boolean processCaseModel(JSONObject event, JSONObject client, JSONArray createsCase) {
        try {

            if (createsCase == null || createsCase.length() == 0) {
                return false;
            }
            for (int openCase = 0; openCase < createsCase.length(); openCase++) {

                String clientType = createsCase.getString(openCase);

                JSONObject columnMappings = getColumnMappings(clientType);
                JSONArray columns = columnMappings.getJSONArray("columns");
                String baseEntityId = client.getString(baseEntityIdJSONKey);
                String expectedEncounterType =
                        event.has("eventType") ? event.getString("eventType") : null;

                ContentValues contentValues = new ContentValues();
                //Add the base_entity_id
                contentValues.put("base_entity_id", baseEntityId);
                contentValues.put("is_closed", 0);

                for (int i = 0; i < columns.length(); i++) {
                    JSONObject colObject = columns.getJSONObject(i);
                    String docType = colObject.getString("type");
                    String columnName = colObject.getString("column_name");
                    JSONObject jsonMapping = colObject.getJSONObject("json_mapping");
                    String dataSegment = null;
                    String fieldName = jsonMapping.getString("field");
                    String fieldValue = null;
                    String responseKey = null;

                    if (fieldName != null && fieldName.contains(".")) {
                        String fieldNameArray[] = fieldName.split("\\.");
                        dataSegment = fieldNameArray[0];
                        fieldName = fieldNameArray[1];
                        fieldValue = jsonMapping.has("concept") ? jsonMapping.getString("concept")
                                : (jsonMapping.has("formSubmissionField") ? jsonMapping
                                .getString("formSubmissionField") : null);
                        if (fieldValue != null) {
                            responseKey = VALUES_KEY;
                        }
                    }

                    JSONObject jsonDocument = docType.equalsIgnoreCase("Event") ? event : client;

                    Object jsonDocSegment;

                    if (dataSegment != null) {
                        // pick data from a specific section of the doc
                        jsonDocSegment =
                                jsonDocument.has(dataSegment) ? jsonDocument.get(dataSegment)
                                        : null;

                    } else {
                        // else the use the main doc as the doc segment
                        jsonDocSegment = jsonDocument;
                    }

                    // special handler needed to process address,
                    if (dataSegment != null && dataSegment.equalsIgnoreCase("addresses")) {
                        Map<String, String> addressMap = getClientAddressAsMap(client);
                        if (addressMap.containsKey(fieldName)) {
                            contentValues.put(columnName, addressMap.get(fieldName));
                        }
                        continue;
                    }

                    //special handler for relationalid
                    if (dataSegment != null && dataSegment.equalsIgnoreCase("relationships")) {
                        if(jsonDocument.has("relationships")) {
                            JSONObject relationshipsObject = jsonDocument.getJSONObject("relationships");
                            if(relationshipsObject.has(fieldName)) {
                                JSONObject relationship = relationshipsObject.getJSONObject(fieldName);
                                String relationalId = relationship.getString("relativeEntityId");
                                contentValues.put(columnName, relationalId);
                            }
                        }
                        continue;
                    }

                    //special handler for relationship types
                    if (dataSegment != null && dataSegment.equalsIgnoreCase("relationshipTypes")) {
                        if(jsonDocument.has("relationships")) {
                            JSONObject relationshipsObject = jsonDocument.getJSONObject("relationships");
                            if(relationshipsObject.has(fieldName)) {
                                JSONObject relationship = relationshipsObject.getJSONObject(fieldName);
                                String relationshipType = relationship.getString("relationshipType");
                                contentValues.put(columnName, relationshipType);
                            }
                        }
                        continue;
                    }

                    String encounterType =
                            jsonMapping.has("event_type") ? jsonMapping.getString("event_type")
                                    : null;

                    if (jsonDocSegment instanceof JSONArray) {

                        JSONArray jsonDocSegmentArray = (JSONArray) jsonDocSegment;

                        for (int j = 0; j < jsonDocSegmentArray.length(); j++) {
                            JSONObject jsonDocObject = jsonDocSegmentArray.getJSONObject(j);
                            String columnValue = null;

                            if (fieldValue == null) {
                                // This means field_value and response_key are null so pick the
                                // value from the json object for the field_name
                                if (jsonDocObject.has(fieldName)) {
                                    columnValue = jsonDocObject.getString(fieldName);
                                }
                            } else {
                                // this means field_value and response_key are not null e.g when
                                // retrieving some value in the events obs section
                                String expectedFieldValue = jsonDocObject.getString(fieldName);
                                // some events can only be differentiated by the event_type value
                                // eg pnc1,pnc2, anc1,anc2
                                // check if encountertype (the one in ec_client_fields.json) is
                                // null or it matches the encounter type from the ec doc we're
                                // processing
                                boolean encounterTypeMatches =
                                        (encounterType == null) || (encounterType != null
                                                && encounterType
                                                .equalsIgnoreCase(expectedEncounterType));

                                if (encounterTypeMatches && expectedFieldValue
                                        .equalsIgnoreCase(fieldValue)) {
                                    columnValue = getValues(jsonDocObject.get(responseKey)).get(0);
                                }
                            }

                            // after successfully retrieving the column name and value store it
                            // in Content value
                            if (columnValue != null) {
                                columnValue = getHumanReadableConceptResponse(columnValue,
                                        jsonDocObject);
                                contentValues.put(columnName, columnValue);
                            }
                        }

                    } else {
                        //e.g client attributes section
                        String columnValue = null;
                        JSONObject jsonDocSegmentObject = (JSONObject) jsonDocSegment;
                        columnValue = jsonDocSegmentObject.has(fieldName) ? jsonDocSegmentObject
                                .getString(fieldName) : "";

                        // after successfully retrieving the column name and value store it in
                        // Content value
                        if (columnValue != null) {
                            columnValue = getHumanReadableConceptResponse(columnValue,
                                    jsonDocSegmentObject);
                            contentValues.put(columnName, columnValue);
                        }
                    }
                }

                // Modify openmrs generated identifier, Remove hyphen if it exists
                updateIdenitifier(contentValues);

                // save the values to db
                Long id = executeInsertStatement(contentValues, clientType);
                updateFTSsearch(clientType, baseEntityId, contentValues);
                Long timestamp = getEventDate(event.get("eventDate"));
                addContentValuesToDetailsTable(contentValues, timestamp);
                updateClientDetailsTable(event, client);
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);

            return null;
        }

    }

    private ContentValues processCaseModel(JSONObject entity, JSONObject clientClassificationJson) {
        try {
            JSONArray columns = clientClassificationJson.getJSONArray("columns");

            ContentValues contentValues = new ContentValues();

            for (int i = 0; i < columns.length(); i++) {
                JSONObject colObject = columns.getJSONObject(i);
                String columnName = colObject.getString("column_name");
                JSONObject jsonMapping = colObject.getJSONObject("json_mapping");
                String dataSegment = null;
                String fieldName = jsonMapping.getString("field");
                String fieldValue = null;
                String responseKey = null;
                String valueField = jsonMapping.has("value_field") ? jsonMapping.getString("value_field") : null;
                if (fieldName != null && fieldName.contains(".")) {
                    String fieldNameArray[] = fieldName.split("\\.");
                    dataSegment = fieldNameArray[0];
                    fieldName = fieldNameArray[1];
                    fieldValue = jsonMapping.has("concept") ? jsonMapping.getString("concept") : (jsonMapping.has("formSubmissionField") ? jsonMapping.getString("formSubmissionField") : null);
                    if (fieldValue != null) {
                        responseKey = VALUES_KEY;
                    }
                }

                Object jsonDocSegment = null;

                if (dataSegment != null) {
                    //pick data from a specific section of the doc
                    jsonDocSegment = entity.has(dataSegment) ? entity.get(dataSegment) : null;

                } else {
                    //else the use the main doc as the doc segment
                    jsonDocSegment = entity;

                }

                if (jsonDocSegment instanceof JSONArray) {

                    JSONArray jsonDocSegmentArray = (JSONArray) jsonDocSegment;

                    for (int j = 0; j < jsonDocSegmentArray.length(); j++) {
                        JSONObject jsonDocObject = jsonDocSegmentArray.getJSONObject(j);
                        String columnValue = null;
                        if (fieldValue == null) {
                            //this means field_value and response_key are null so pick the value from the json object for the field_name
                            if (jsonDocObject.has(fieldName)) {
                                columnValue = jsonDocObject.getString(fieldName);
                            }
                        } else {
                            //this means field_value and response_key are not null e.g when retrieving some value in the events obs section
                            String expectedFieldValue = jsonDocObject.getString(fieldName);
                            //some events can only be differentiated by the event_type value eg pnc1,pnc2, anc1,anc2

                            if (expectedFieldValue.equalsIgnoreCase(fieldValue)) {
                                if (StringUtils.isNotBlank(valueField) && jsonDocObject.has(valueField)) {
                                    columnValue = jsonDocObject.getString(valueField);
                                } else {
                                    List<String> values = getValues(jsonDocObject.get(responseKey));
                                    if (!values.isEmpty()) {
                                        columnValue = values.get(0);
                                    }
                                }
                            }
                        }
                        // after successfully retrieving the column name and value store it in Content value
                        if (columnValue != null) {
                            columnValue = getHumanReadableConceptResponse(columnValue, jsonDocObject);
                            contentValues.put(columnName, columnValue);
                        }
                    }

                } else {
                    //e.g client attributes section
                    String columnValue = null;
                    JSONObject jsonDocSegmentObject = (JSONObject) jsonDocSegment;
                    columnValue = jsonDocSegmentObject.has(fieldName) ? jsonDocSegmentObject.getString(fieldName) : "";
                    // after successfully retrieving the column name and value store it in Content value
                    if (columnValue != null) {
                        columnValue = getHumanReadableConceptResponse(columnValue, jsonDocSegmentObject);
                        contentValues.put(columnName, columnValue);
                    }

                }


            }

            return contentValues;
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return null;
    }

    @Override
    public Boolean processEvent(JSONObject event, JSONObject client, JSONObject
            clientClassificationJson) throws Exception {

        try {
            String baseEntityId = event.getString(baseEntityIdJSONKey);
            if (event.has("creator")) {
                Log.i(TAG, "EVENT from openmrs");
            }
            // For data integrity check if a client exists, if not pull one from cloudant and
            // insert in drishti sqlite db

            if (isNullOrEmptyJSONObject(client)) {
                return false;
            }

            // Get the client type classification
            JSONArray clientClasses = clientClassificationJson
                    .getJSONArray("case_classification_rules");
            if (isNullOrEmptyJSONArray(clientClasses)) {
                return false;
            }

            // Check if child is deceased and skip
            if (client.has("deathdate") && !client.getString("deathdate").isEmpty()) {

                return false;
            }

            for (int i = 0; i < clientClasses.length(); i++) {
                JSONObject clientClass = clientClasses.getJSONObject(i);
                processClientClass(clientClass, event, client);
            }

            // Incase the details have not been updated
            boolean updated = event.has(detailsUpdated) && event.getBoolean(detailsUpdated);

            if (!updated) {
                updateClientDetailsTable(event, client);
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);

            return null;
        }
    }

    @Override
    public Boolean processClientClass(JSONObject clientClass, JSONObject event, JSONObject client) {

        try {
            if (clientClass == null || clientClass.length() == 0) {
                return false;
            }

            if (event == null || event.length() == 0) {
                return false;
            }

            if (client == null || client.length() == 0) {
                return false;
            }

            JSONObject ruleObject = clientClass.getJSONObject("rule");
            JSONArray fields = ruleObject.getJSONArray("fields");

            for (int i = 0; i < fields.length(); i++) {
                JSONObject fieldJson = fields.getJSONObject(i);
                processField(fieldJson, event, client);
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            return null;
        }
    }

    @Override
    public Boolean processField(JSONObject fieldJson, JSONObject event, JSONObject client) {

        try {
            if (fieldJson == null || fieldJson.length() == 0) {
                return false;
            }

            // keep checking if the event data matches the values expected by each rule, break the
            // moment the rule fails
            String dataSegment = null;
            String fieldName = fieldJson.has("field") ? fieldJson.getString("field") : null;
            String fieldValue =
                    fieldJson.has("field_value") ? fieldJson.getString("field_value") : null;
            String responseKey = null;

            if (fieldName != null && fieldName.contains(".")) {
                String fieldNameArray[] = fieldName.split("\\.");
                dataSegment = fieldNameArray[0];
                fieldName = fieldNameArray[1];
                String concept = fieldJson.has("concept") ? fieldJson.getString("concept") : null;

                if (concept != null) {
                    fieldValue = concept;
                    responseKey = VALUES_KEY;
                }
            }

            JSONArray createsCase =
                    fieldJson.has("creates_case") ? fieldJson.getJSONArray("creates_case") : null;
            JSONArray closesCase =
                    fieldJson.has("closes_case") ? fieldJson.getJSONArray("closes_case") : null;

            // some fields are in the main doc e.g event_type so fetch them from the main doc
            if (dataSegment != null && !dataSegment.isEmpty()) {

                JSONArray responseValue =
                        fieldJson.has(responseKey) ? fieldJson.getJSONArray(responseKey) : null;
                List<String> responseValues = getValues(responseValue);

                if (event.has(dataSegment)) {
                    JSONArray jsonDataSegment = event.getJSONArray(dataSegment);

                    // Iterate in the segment e.g obs segment
                    for (int j = 0; j < jsonDataSegment.length(); j++) {
                        JSONObject segmentJsonObject = jsonDataSegment.getJSONObject(j);
                        // let's discuss this further, to get the real value in the doc we've to
                        // use the keys 'fieldcode' and 'value'
                        String docSegmentFieldValue =
                                segmentJsonObject.has(fieldName) ? segmentJsonObject.get(fieldName)
                                        .toString() : "";
                        List<String> docSegmentResponseValues =
                                segmentJsonObject.has(responseKey) ? getValues(
                                        segmentJsonObject.get(responseKey)) : null;

                        if (docSegmentFieldValue.equalsIgnoreCase(fieldValue) && (!Collections
                                .disjoint(responseValues, docSegmentResponseValues))) {
                            // this is the event obs we're interested in put it in the respective
                            // bucket specified by type variable
                            processCaseModel(event, client, createsCase);
                            closeCase(client, closesCase);
                        }

                    }
                }

            } else {
                //fetch from the main doc
                String docSegmentFieldValue =
                        event.has(fieldName) ? event.get(fieldName).toString() : "";

                if (docSegmentFieldValue.equalsIgnoreCase(fieldValue)) {
                    processCaseModel(event, client, createsCase);
                    closeCase(client, closesCase);
                }

            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);

            return null;
        }
    }

    @Override
    public void updateFTSsearch(String tableName, String entityId, ContentValues contentValues) {
        super.updateFTSsearch(tableName, entityId, contentValues);

        if (contentValues != null && StringUtils.containsIgnoreCase(tableName, "child")) {
            String dob = contentValues.getAsString("dob");

            if (StringUtils.isBlank(dob)) {
                return;
            }

            DateTime birthDateTime = new DateTime(dob);
            VaccineSchedule.updateOfflineAlerts(entityId, birthDateTime, "child");
            ServiceSchedule.updateOfflineAlerts(entityId, birthDateTime);
        }
    }

    private boolean unSync(List<JSONObject> events) {
        try {

            if (events == null || events.isEmpty()) {
                return false;
            }

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
            String registeredAnm = allSharedPreferences.fetchRegisteredANM();

            String clientClassificationStr = getFileContents("ec_client_fields.json");
            JSONObject clientClassificationJson = new JSONObject(clientClassificationStr);
            JSONArray bindObjects = clientClassificationJson.getJSONArray("bindobjects");

            DetailsRepository detailsRepository = KipApplication.getInstance().context().detailsRepository();
            ECSyncUpdater ecUpdater = ECSyncUpdater.getInstance(getContext());

            for (JSONObject event : events) {
                unSync(ecUpdater, detailsRepository, bindObjects, event, registeredAnm);
            }

            return true;

        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }

        return false;
    }

    private boolean unSync(ECSyncUpdater ecUpdater, DetailsRepository detailsRepository, JSONArray bindObjects, JSONObject event, String registeredAnm) {
        try {
            String baseEntityId = event.getString(baseEntityIdJSONKey);
            String providerId = event.getString(providerIdJSONKey);

            if (providerId.equals(registeredAnm)) {
                boolean eventDeleted = ecUpdater.deleteEventsByBaseEntityId(baseEntityId);
                boolean clientDeleted = ecUpdater.deleteClient(baseEntityId);
                Log.d(getClass().getName(), "EVENT_DELETED: " + eventDeleted);
                Log.d(getClass().getName(), "ClIENT_DELETED: " + clientDeleted);

                boolean detailsDeleted = detailsRepository.deleteDetails(baseEntityId);
                Log.d(getClass().getName(), "DETAILS_DELETED: " + detailsDeleted);

                for (int i = 0; i < bindObjects.length(); i++) {

                    JSONObject bindObject = bindObjects.getJSONObject(i);
                    String tableName = bindObject.getString("name");

                    boolean caseDeleted = deleteCase(tableName, baseEntityId);
                    Log.d(getClass().getName(), "CASE_DELETED: " + caseDeleted);
                }

                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return false;
    }

    private Integer parseInt(String string) {
        try {
            return Integer.valueOf(string);
        } catch (NumberFormatException e) {
            Log.e(TAG, e.toString(), e);
        }
        return null;
    }

    private Float parseFloat(String string) {
        try {
            return Float.valueOf(string);
        } catch (NumberFormatException e) {
            Log.e(TAG, e.toString(), e);
        }
        return null;
    }

    /**
     * Update given identifier, removes hyphen
     *
     * @param values
     */
    private void updateIdenitifier(ContentValues values) {
        try {
            for (String identifier : openmrs_gen_ids) {
                Object value = values.get(identifier);
                if (value != null && value instanceof String) {
                    String sValue = value.toString();
                    if (StringUtils.isNotBlank(sValue)) {
                        values.remove(identifier);
                        values.put(identifier, sValue.replace("-", ""));
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    private long getEventDate(Object eventDate) {
        if (eventDate instanceof Long) {
            return (Long) eventDate;
        } else {
            Date date = DateUtil.toDate(eventDate);
            if (date != null) {
                return date.getTime();
            }
        }
        return new Date().getTime();
    }

    private boolean isNullOrEmptyJSONArray(JSONArray jsonArray) {
        return (jsonArray == null || jsonArray.length() == 0);
    }
}
