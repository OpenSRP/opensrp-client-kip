package org.smartregister.kip.processor;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.google.gson.Gson;

import net.sqlcipher.database.SQLiteException;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.child.util.ChildDbUtils;
import org.smartregister.child.util.Constants;
import org.smartregister.child.util.JsonFormUtils;
import org.smartregister.child.util.MoveToMyCatchmentUtils;
import org.smartregister.child.util.Utils;
import org.smartregister.clientandeventmodel.DateUtil;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.db.Client;
import org.smartregister.domain.db.Event;
import org.smartregister.domain.db.EventClient;
import org.smartregister.domain.db.Obs;
import org.smartregister.domain.jsonmapping.ClientClassification;
import org.smartregister.domain.jsonmapping.ClientField;
import org.smartregister.domain.jsonmapping.Column;
import org.smartregister.domain.jsonmapping.Table;
import org.smartregister.growthmonitoring.domain.Height;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.repository.HeightRepository;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.service.intent.HeightIntentService;
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
import org.smartregister.kip.domain.MonthlyTally;
import org.smartregister.kip.exceptions.Covid19CalculateRiskFactorException;
import org.smartregister.kip.exceptions.Covid19VaccinationWaitingListException;
import org.smartregister.kip.exceptions.Covid19VaccineEligibilityException;
import org.smartregister.kip.pojo.OpdCovid19CalculateRiskFactorForm;
import org.smartregister.kip.pojo.OpdCovid19VaccinationEligibilityCheckForm;
import org.smartregister.kip.pojo.OpdCovid19VaccinationForm;
import org.smartregister.kip.pojo.OpdCovid19WaitingListForm;
import org.smartregister.kip.pojo.OpdInfluenzaVaccineAdministrationForm;
import org.smartregister.kip.pojo.OpdMedicalCheckForm;
import org.smartregister.kip.pojo.OpdSMSReminderForm;
import org.smartregister.kip.repository.MonthlyTalliesRepository;
import org.smartregister.kip.util.AppExecutors;
import org.smartregister.kip.util.KipChildUtils;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.exception.CheckInEventProcessException;
import org.smartregister.opd.pojo.OpdDetails;
import org.smartregister.opd.processor.OpdMiniClientProcessorForJava;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.opd.utils.OpdUtils;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.sync.MiniClientProcessorForJava;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class KipProcessorForJava extends OpdMiniClientProcessorForJava implements MiniClientProcessorForJava {

    private static KipProcessorForJava instance;

    private HashMap<String, MiniClientProcessorForJava> processorMap = new HashMap<>();
    private HashMap<MiniClientProcessorForJava, List<Event>> unsyncEventsPerProcessor = new HashMap<>();

    private HashMap<String, DateTime> clientsForAlertUpdates = new HashMap<>();
    private AppExecutors appExecutors = new AppExecutors();

    private List<String> coreProcessedEvents = Arrays.asList(Constants.EventType.BITRH_REGISTRATION, Constants.EventType.UPDATE_BITRH_REGISTRATION,
            Constants.EventType.NEW_WOMAN_REGISTRATION, OpdConstants.EventType.OPD_REGISTRATION, OpdConstants.EventType.UPDATE_OPD_REGISTRATION,
            Constants.EventType.AEFI, Constants.EventType.ARCHIVE_CHILD_RECORD, Constants.EventType.FATHER_REGISTRATION, Constants.EventType.UPDATE_FATHER_DETAILS, Constants.EventType.UPDATE_MOTHER_DETAILS);

    private SimpleDateFormat dateFormat = new SimpleDateFormat(OpdDbConstants.DATE_FORMAT, Locale.US);

    private KipProcessorForJava(Context context) {
        super(context);
        OpdMiniClientProcessorForJava opdMiniClientProcessorForJava = new OpdMiniClientProcessorForJava(context);
        addMiniProcessors(opdMiniClientProcessorForJava);
    }

    public void addMiniProcessors(MiniClientProcessorForJava... miniClientProcessorsForJava) {
        for (MiniClientProcessorForJava miniClientProcessorForJava : miniClientProcessorsForJava) {
            unsyncEventsPerProcessor.put(miniClientProcessorForJava, new ArrayList<>());

            HashSet<String> eventTypes = miniClientProcessorForJava.getEventTypes();

            for (String eventType : eventTypes) {
                processorMap.put(eventType, miniClientProcessorForJava);
            }
        }
    }

    public static KipProcessorForJava getInstance(Context context) {
        if (instance == null) {
            instance = new KipProcessorForJava(context);
        }
        return instance;
    }


    @Override
    public synchronized void processClient(List<EventClient> eventClients) throws Exception {

        ClientClassification clientClassification = assetJsonToJava("ec_client_classification.json",
                ClientClassification.class);
        Table vaccineTable = assetJsonToJava("ec_client_vaccine.json", Table.class);
        Table weightTable = assetJsonToJava("ec_client_weight.json", Table.class);
        Table heightTable = assetJsonToJava("ec_client_height.json", Table.class);
        Table serviceTable = assetJsonToJava("ec_client_service.json", Table.class);
        if (!eventClients.isEmpty()) {
            List<Event> unsyncEvents = new ArrayList<>();
            for (EventClient eventClient : eventClients) {
                Event event = eventClient.getEvent();
                if (event == null) {
                    return;
                }

                String eventType = event.getEventType();
                if (eventType == null) {
                    continue;
                }

                if (eventType.equals(VaccineIntentService.EVENT_TYPE) || eventType
                        .equals(VaccineIntentService.EVENT_TYPE_OUT_OF_CATCHMENT)) {
                    processVaccinationEvent(vaccineTable, eventClient);
                } else if (eventType.equals(WeightIntentService.EVENT_TYPE) || eventType
                        .equals(WeightIntentService.EVENT_TYPE_OUT_OF_CATCHMENT)) {
                    processWeightEvent(weightTable, heightTable, eventClient, eventType);
                } else if (eventType.equals(RecurringIntentService.EVENT_TYPE)) {
                    if (serviceTable == null) {
                        continue;
                    }
                    processService(eventClient, serviceTable);
                } else if (eventType.equals(JsonFormUtils.BCG_SCAR_EVENT)) {
                    processBCGScarEvent(eventClient);
                } else if (eventType.equals(MoveToMyCatchmentUtils.MOVE_TO_CATCHMENT_EVENT)) {
                    unsyncEvents.add(event);
                } else if (eventType.equalsIgnoreCase(Constants.EventType.DEATH)) {
                    if (processDeathEvent(eventClient, clientClassification)) {
                        unsyncEvents.add(event);
                    }
                }else if (eventType.equals(KipConstants.EventType.REPORT_CREATION)) {
                    processReport(event);
                    CoreLibrary.getInstance().context().getEventClientRepository().markEventAsProcessed(eventClient.getEvent().getFormSubmissionId());
                } else if (eventType.equals(KipConstants.EventType.OPD_CALCULATE_RISK_FACTOR)) {
                    processCalculateRiskFactor(eventClient, clientClassification);
                }else if (eventType.equals(KipConstants.EventType.OPD_INFLUENZA_MEDIAL_CONDITION)) {
                    processOpdMedicalCheck(eventClient, clientClassification);
                } else if (eventType.equals(KipConstants.EventType.OPD_VACCINATION_ELIGIBILITY_CHECK)) {
                    processVaccinationEligibility(eventClient, clientClassification);
                } else if (eventType.equals(KipConstants.EventType.OPD_COVID19_WAITING_LIST)) {
                    processVaccinationWaitingLists(eventClient, clientClassification);
                } else if (eventType.equals(KipConstants.EventType.OPD_SMS_REMINDER)) {
                    processSmsReminder(eventClient, clientClassification);
                } else if (eventType.equals(KipConstants.EventType.OPD_COVID_19_VACCINE_ADMINISTRATION)) {
                    processVaccinations(eventClient, clientClassification);
                } else if (eventType.equals(KipConstants.EventType.OPD_INFLUENZA_VACCINE_ADMINISTRATION)) {
                    processInfluenzaVaccinations(eventClient, clientClassification);
                } else if (coreProcessedEvents.contains(eventType)) {
                    processKipCoreEvents(clientClassification, eventClient, event, eventType);
                } else if (processorMap.containsKey(eventType)) {
                    try {
                        processEventUsingMiniprocessor(clientClassification, eventClient, eventType);
                    } catch (Exception ex) {
                        Timber.e(ex);
                    }
                } else if (event.getEventType().equals(OpdConstants.EventType.CLOSE_OPD_VISIT)) {
                    try {
                        processOpdCloseVisitEvent(event);
                        CoreLibrary.getInstance().context().getEventClientRepository().markEventAsProcessed(eventClient.getEvent().getFormSubmissionId());
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }

            }

            // Unsync events that are should not be in this device
            processUnsyncEvents(unsyncEvents);


            // Process alerts for clients
            Runnable runnable = () -> updateClientAlerts(clientsForAlertUpdates);

            appExecutors.diskIO().execute(runnable);
        }
    }

    private void processOpdCloseVisitEvent(@NonNull Event event) {
        Map<String, String> mapDetails = event.getDetails();
        //update visit end date
        if (mapDetails != null) {
            OpdDetails opdDetails = new OpdDetails(event.getBaseEntityId(), mapDetails.get(OpdConstants.JSON_FORM_KEY.VISIT_ID));
            opdDetails = OpdLibrary.getInstance().getOpdDetailsRepository().findOne(opdDetails);
            if (opdDetails != null) {
                opdDetails.setCurrentVisitEndDate(OpdUtils.convertStringToDate(OpdConstants.DateFormat.YYYY_MM_DD_HH_MM_SS, mapDetails.get(OpdConstants.JSON_FORM_KEY.VISIT_END_DATE)));
                boolean result = OpdLibrary.getInstance().getOpdDetailsRepository().saveOrUpdate(opdDetails);
                if (result) {
                    Timber.d("Opd processOpdCloseVisitEvent for %s saved", event.getBaseEntityId());
                    return;
                }
                Timber.e("Opd processOpdCloseVisitEvent for %s not saved", event.getBaseEntityId());
            } else {
                Timber.e("Opd Details for %s not found", mapDetails.toString());
            }
        } else {
            Timber.e("Opd Details for %s not found, event details is null", event.getBaseEntityId());
        }
    }

    public void processKipCoreEvents(ClientClassification clientClassification, EventClient eventClient, Event event, String eventType) throws Exception {
        if (eventType.equals(OpdConstants.EventType.OPD_REGISTRATION) && eventClient.getClient() != null) {
            KipApplication.getInstance().registerTypeRepository().addUnique(KipConstants.RegisterType.OPD, event.getBaseEntityId());
        } else if (eventType.equals(Constants.EventType.BITRH_REGISTRATION) && eventClient.getClient() != null &&
                (KipApplication.getInstance().context().getEventClientRepository().getEventsByBaseEntityIdAndEventType(event.getBaseEntityId(), Constants.EventType.ARCHIVE_CHILD_RECORD) == null)) {
            KipApplication.getInstance().registerTypeRepository().addUnique(KipConstants.RegisterType.CHILD, event.getBaseEntityId());
        } else if (eventType.equals(Constants.EventType.NEW_WOMAN_REGISTRATION) && eventClient.getClient() != null) {
            KipApplication.getInstance().registerTypeRepository().addUnique(KipConstants.RegisterType.OPD, event.getBaseEntityId());
        } else if (eventType.equals(Constants.EventType.FATHER_REGISTRATION) && eventClient.getClient() != null) {
            KipApplication.getInstance().registerTypeRepository().addUnique(KipConstants.RegisterType.OPD, event.getBaseEntityId());
        }
//        else if (eventType.equals(Constants.EventType.ARCHIVE_CHILD_RECORD) && eventClient.getClient() != null) {
//            KipApplication.getInstance().registerTypeRepository().removeAll(event.getBaseEntityId());
//        }


        if (clientClassification != null) {
            processEventClient(clientClassification, eventClient, event);
        }
    }

    private void updateClientAlerts(@NonNull HashMap<String, DateTime> clientsForAlertUpdates) {
        HashMap<String, DateTime> stringDateTimeHashMap = SerializationUtils.clone(clientsForAlertUpdates);
        for (String baseEntityId : stringDateTimeHashMap.keySet()) {
            DateTime birthDateTime = clientsForAlertUpdates.get(baseEntityId);
            if (birthDateTime != null) {
                updateOfflineAlerts(baseEntityId, birthDateTime);
            }
        }
        clientsForAlertUpdates.clear();
    }

    private boolean processDeathEvent(@NonNull EventClient eventClient, ClientClassification clientClassification) {
        try {
            KipApplication.getInstance().registerTypeRepository().removeAll(eventClient.getEvent().getBaseEntityId());
            processEvent(eventClient.getEvent(), eventClient.getClient(), clientClassification);
        } catch (Exception e) {
            Timber.e(e);
        }
        return KipChildUtils.updateClientDeath(eventClient);
    }

    private void processUnsyncEvents(@NonNull List<Event> unsyncEvents) {
        if (!unsyncEvents.isEmpty()) {
            unSync(unsyncEvents);
        }

        for (MiniClientProcessorForJava miniClientProcessorForJava : unsyncEventsPerProcessor.keySet()) {
            List<Event> processorUnsyncEvents = unsyncEventsPerProcessor.get(miniClientProcessorForJava);
            miniClientProcessorForJava.unSync(processorUnsyncEvents);
        }
    }

    private void processEventClient(@NonNull ClientClassification clientClassification, @NonNull EventClient eventClient, @NonNull Event event) {
        Client client = eventClient.getClient();
        if (client != null) {
            try {
                processEvent(event, client, clientClassification);
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    private void processReport(@NonNull Event event) {
        try {
            String reportJson = event.getDetails().get(KipConstants.ReportKeys.REPORT_JSON);
            if (StringUtils.isNotBlank(reportJson)) {
                JSONObject reportJsonObject = new JSONObject(reportJson);
                String reportMonth = reportJsonObject.optString(KipConstants.ReportKeys.REPORT_DATE);
                String reportGrouping = reportJsonObject.optString(KipConstants.ReportKeys.GROUPING);
                String providerId = reportJsonObject.optString(KipConstants.ReportKeys.PROVIDER_ID);
                String dateCreated = reportJsonObject.optString(KipConstants.ReportKeys.DATE_CREATED);
                DateTime dateSent = new DateTime(dateCreated);
                Date dReportMonth = MonthlyTalliesRepository.DF_YYYYMM.parse(reportMonth);
                JSONArray hia2Indicators = reportJsonObject.optJSONArray(KipConstants.ReportKeys.HIA2_INDICATORS);
                if (hia2Indicators != null) {
                    for (int i = 0; i < hia2Indicators.length(); i++) {
                        JSONObject jsonObject1 = hia2Indicators.optJSONObject(i);
                        String indicator = jsonObject1.optString(KipConstants.ReportKeys.INDICATOR_CODE);
                        String value = jsonObject1.optString(KipConstants.ReportKeys.VALUE);
                        MonthlyTally monthlyTally = new MonthlyTally();
                        monthlyTally.setEdited(false);
                        monthlyTally.setGrouping(reportGrouping);
                        monthlyTally.setIndicator(indicator);
                        monthlyTally.setProviderId(providerId);
                        monthlyTally.setValue(value);
                        monthlyTally.setDateSent(dateSent.toDate());
                        monthlyTally.setCreatedAt(dateSent.toDate());
                        monthlyTally.setMonth(dReportMonth);
                        KipApplication.getInstance().monthlyTalliesRepository().save(monthlyTally);
                    }
                }
            }

        } catch (JSONException e) {
            Timber.e(e);
        } catch (ParseException e) {
            Timber.e(e);
        }

    }

    private void processEventUsingMiniprocessor(ClientClassification clientClassification, EventClient eventClient, String eventType) throws Exception {
        MiniClientProcessorForJava miniClientProcessorForJava = processorMap.get(eventType);
        if (miniClientProcessorForJava != null) {
            List<Event> processorUnsyncEvents = unsyncEventsPerProcessor.get(miniClientProcessorForJava);
            if (processorUnsyncEvents == null) {
                processorUnsyncEvents = new ArrayList<Event>();
                unsyncEventsPerProcessor.put(miniClientProcessorForJava, processorUnsyncEvents);
            }

            completeProcessing(eventClient.getEvent());
            miniClientProcessorForJava.processEventClient(eventClient, processorUnsyncEvents, clientClassification);
        }
    }

    private void processWeightEvent(Table weightTable, Table heightTable, EventClient eventClient, String eventType) throws Exception {
        if (weightTable == null) {
            return;
        }

        if (heightTable == null) {
            return;
        }

        processWeight(eventClient, weightTable,
                eventType.equals(WeightIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));
        processHeight(eventClient, heightTable,
                eventType.equals(HeightIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));
    }

    private void processVaccinationEvent(Table vaccineTable, EventClient eventClient) throws Exception {
        if (vaccineTable != null) {

            Client client = eventClient.getClient();
            Event event = eventClient.getEvent();
            if (!childExists(client.getBaseEntityId())) {
                List<String> createCase = new ArrayList<>();
                createCase.add(KipConstants.TABLE_NAME.ALL_CLIENTS);
                processCaseModel(event, client, createCase);
            }

            processVaccine(eventClient, vaccineTable,
                    VaccineIntentService.EVENT_TYPE_OUT_OF_CATCHMENT.equals(event.getEventType()));

            scheduleUpdatingClientAlerts(client.getBaseEntityId(), client.getBirthdate());
        }
    }

    private boolean childExists(String entityId) {
        return KipApplication.getInstance().eventClientRepository().checkIfExists(EventClientRepository.Table.client, entityId);
    }

    private Boolean processVaccine(@Nullable EventClient vaccine, @Nullable Table vaccineTable, boolean outOfCatchment) {
        try {
            if (vaccine == null || vaccine.getEvent() == null) {
                return false;
            }

            if (vaccineTable == null) {
                return false;
            }

            Timber.d("Starting processVaccine table: %s", vaccineTable.name);

            ContentValues contentValues = processCaseModel(vaccine, vaccineTable);

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
                vaccineObj.setLocationId(contentValues.getAsString(VaccineRepository.LOCATION_ID));
                vaccineObj.setSyncStatus(VaccineRepository.TYPE_Synced);
                vaccineObj.setFormSubmissionId(vaccine.getEvent().getFormSubmissionId());
                vaccineObj.setEventId(vaccine.getEvent().getEventId());
                vaccineObj.setOutOfCatchment(outOfCatchment ? 1 : 0);

                String createdAtString = contentValues.getAsString(VaccineRepository.CREATED_AT);
                Date createdAt = getDate(createdAtString);
                vaccineObj.setCreatedAt(createdAt);

                Utils.addVaccine(vaccineRepository, vaccineObj);

                Timber.d("Ending processVaccine table: %s", vaccineTable.name);
            }
            return true;

        } catch (Exception e) {
            Timber.e(e, "Process Vaccine Error");
            return null;
        }
    }

    private Boolean processWeight(EventClient weight, Table weightTable, boolean outOfCatchment) throws Exception {

        try {

            if (weight == null || weight.getEvent() == null) {
                return false;
            }

            if (weightTable == null) {
                return false;
            }

            Timber.d("Starting processWeight table: %s", weightTable.name);

            ContentValues contentValues = processCaseModel(weight, weightTable);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {
                String eventDateStr = contentValues.getAsString(WeightRepository.DATE);
                Date date = getDate(eventDateStr);

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
                weightObj.setFormSubmissionId(weight.getEvent().getFormSubmissionId());
                weightObj.setEventId(weight.getEvent().getEventId());
                weightObj.setOutOfCatchment(outOfCatchment ? 1 : 0);

                if (contentValues.containsKey(WeightRepository.Z_SCORE)) {
                    String zscoreString = contentValues.getAsString(WeightRepository.Z_SCORE);
                    if (NumberUtils.isNumber(zscoreString)) {
                        weightObj.setZScore(Double.valueOf(zscoreString));
                    }
                }

                String createdAtString = contentValues.getAsString(WeightRepository.CREATED_AT);
                Date createdAt = getDate(createdAtString);
                weightObj.setCreatedAt(createdAt);

                weightRepository.add(weightObj);

                Timber.d("Ending processWeight table: %s", weightTable.name);
            }
            return true;

        } catch (Exception e) {
            Timber.e(e, "Process Weight Error");
            return null;
        }
    }

    private Boolean processHeight(@Nullable EventClient height, @Nullable Table heightTable, boolean outOfCatchment) {

        try {

            if (height == null || height.getEvent() == null) {
                return false;
            }

            if (heightTable == null) {
                return false;
            }

            Timber.d("Starting processWeight table: %s", heightTable.name);

            ContentValues contentValues = processCaseModel(height, heightTable);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {
                String eventDateStr = contentValues.getAsString(HeightRepository.DATE);
                Date date = getDate(eventDateStr);

                HeightRepository heightRepository = KipApplication.getInstance().heightRepository();
                Height heightObject = new Height();
                heightObject.setBaseEntityId(contentValues.getAsString(WeightRepository.BASE_ENTITY_ID));
                if (contentValues.containsKey(HeightRepository.CM)) {
                    heightObject.setCm(parseFloat(contentValues.getAsString(HeightRepository.CM)));
                }
                heightObject.setDate(date);
                heightObject.setAnmId(contentValues.getAsString(HeightRepository.ANMID));
                heightObject.setLocationId(contentValues.getAsString(HeightRepository.LOCATIONID));
                heightObject.setSyncStatus(HeightRepository.TYPE_Synced);
                heightObject.setFormSubmissionId(height.getEvent().getFormSubmissionId());
                heightObject.setEventId(height.getEvent().getEventId());
                heightObject.setOutOfCatchment(outOfCatchment ? 1 : 0);

                if (contentValues.containsKey(HeightRepository.Z_SCORE)) {
                    String zScoreString = contentValues.getAsString(HeightRepository.Z_SCORE);
                    if (NumberUtils.isNumber(zScoreString)) {
                        heightObject.setZScore(Double.valueOf(zScoreString));
                    }
                }

                String createdAtString = contentValues.getAsString(HeightRepository.CREATED_AT);
                Date createdAt = getDate(createdAtString);
                heightObject.setCreatedAt(createdAt);

                heightRepository.add(heightObject);

                Timber.d("Ending processWeight table: %s", heightTable.name);
            }
            return true;

        } catch (Exception e) {
            Timber.e(e, "Process Height Error");
            return null;
        }
    }

    private Boolean processService(EventClient service, Table serviceTable) {
        try {
            if (service == null || service.getEvent() == null) {
                return false;
            }

            if (serviceTable == null) {
                return false;
            }

            Timber.d("Starting processService table: %s", serviceTable.name);

            ContentValues contentValues = processCaseModel(service, serviceTable);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {
                String name = getServiceTypeName(contentValues);

                String eventDateStr = contentValues.getAsString(RecurringServiceRecordRepository.DATE);
                Date date = getDate(eventDateStr);

                String value = null;

                if (StringUtils.containsIgnoreCase(name, "ITN")) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String itnDateString = contentValues.getAsString("itn_date");
                    if (StringUtils.isNotBlank(itnDateString)) {
                        date = simpleDateFormat.parse(itnDateString);
                    }

                    value = getServiceValue(contentValues);

                }

                List<ServiceType> serviceTypeList = getServiceTypes(name);
                if (serviceTypeList == null || serviceTypeList.isEmpty()) {
                    return false;
                }

                if (date == null) {
                    return false;
                }

                recordServiceRecord(service, contentValues, name, date, value, serviceTypeList);

                Timber.d("Ending processService table: %s", serviceTable.name);
            }
            return true;

        } catch (Exception e) {
            Timber.e(e, "Process Service Error");
            return null;
        }
    }

    @NotNull
    private String getServiceValue(ContentValues contentValues) {
        String value;
        value = RecurringIntentService.ITN_PROVIDED;
        if (contentValues.getAsString("itn_has_net") != null) {
            value = RecurringIntentService.CHILD_HAS_NET;
        }
        return value;
    }

    @Nullable
    private String getServiceTypeName(ContentValues contentValues) {
        String name = contentValues.getAsString(RecurringServiceTypeRepository.NAME);
        if (StringUtils.isNotBlank(name)) {
            name = name.replaceAll("_", " ").replace("dose", "").trim();
        }
        return name;
    }

    private void recordServiceRecord(EventClient service, ContentValues contentValues, String name, Date date, String value, List<ServiceType> serviceTypeList) {
        RecurringServiceRecordRepository recurringServiceRecordRepository = KipApplication.getInstance()
                .recurringServiceRecordRepository();
        ServiceRecord serviceObj = getServiceRecord(service, contentValues, name, date, value, serviceTypeList);
        String createdAtString = contentValues.getAsString(RecurringServiceRecordRepository.CREATED_AT);
        Date createdAt = getDate(createdAtString);
        serviceObj.setCreatedAt(createdAt);

        recurringServiceRecordRepository.add(serviceObj);
    }

    private List<ServiceType> getServiceTypes(String name) {
        RecurringServiceTypeRepository recurringServiceTypeRepository = KipApplication.getInstance()
                .recurringServiceTypeRepository();
        return recurringServiceTypeRepository.searchByName(name);
    }

    private void processBCGScarEvent(EventClient bcgScarEventClient) {
        if (bcgScarEventClient == null || bcgScarEventClient.getEvent() == null) {
            return;
        }

        Event event = bcgScarEventClient.getEvent();
        String baseEntityId = event.getBaseEntityId();
        DateTime eventDate = event.getEventDate();
        long date = 0;
        if (eventDate != null) {
            date = eventDate.getMillis();
        }

        ChildDbUtils.updateChildDetailsValue(Constants.SHOW_BCG_SCAR, String.valueOf(date), baseEntityId);
    }

    @Override
    public boolean unSync(List<Event> events) {
        try {

            if (events == null || events.isEmpty()) {
                return false;
            }

            ClientField clientField = assetJsonToJava("ec_client_fields.json", ClientField.class);
            return clientField != null;

        } catch (Exception e) {
            Timber.e(e);
        }

        return false;
    }

    @VisibleForTesting
    ContentValues processCaseModel(EventClient eventClient, Table table) {
        try {
            List<Column> columns = table.columns;
            ContentValues contentValues = new ContentValues();

            for (Column column : columns) {
                processCaseModel(eventClient.getEvent(), eventClient.getClient(), column, contentValues);
            }

            return contentValues;
        } catch (Exception e) {
            Timber.e(e);
        }
        return null;
    }

    private Integer parseInt(String string) {
        try {
            return Integer.valueOf(string);
        } catch (NumberFormatException e) {
            Timber.e(e, e.toString());
        }
        return null;
    }

    @Nullable
    private Date getDate(@Nullable String eventDateStr) {
        Date date = null;
        if (StringUtils.isNotBlank(eventDateStr)) {
            try {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
                date = dateFormat.parse(eventDateStr);
            } catch (ParseException e) {
                try {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    date = dateFormat.parse(eventDateStr);
                } catch (ParseException pe) {
                    try {
                        date = DateUtil.parseDate(eventDateStr);
                    } catch (ParseException pee) {
                        Timber.e(e);
                    }
                }
            }
        }
        return date;
    }

    private Float parseFloat(String string) {
        try {
            return Float.valueOf(string);
        } catch (NumberFormatException e) {
            Timber.e(e);
        }
        return null;
    }

    @NotNull
    private ServiceRecord getServiceRecord(EventClient service, ContentValues contentValues, String name, Date date,
                                           String value, List<ServiceType> serviceTypeList) {
        ServiceRecord serviceObj = new ServiceRecord();
        serviceObj.setBaseEntityId(contentValues.getAsString(RecurringServiceRecordRepository.BASE_ENTITY_ID));
        serviceObj.setName(name);
        serviceObj.setDate(date);
        serviceObj.setAnmId(contentValues.getAsString(RecurringServiceRecordRepository.ANMID));
        serviceObj.setLocationId(contentValues.getAsString(RecurringServiceRecordRepository.LOCATION_ID));
        serviceObj.setSyncStatus(RecurringServiceRecordRepository.TYPE_Synced);
        serviceObj.setFormSubmissionId(service.getEvent().getFormSubmissionId());
        serviceObj.setEventId(service.getEvent().getEventId()); //FIXME hard coded id
        serviceObj.setValue(value);
        serviceObj.setRecurringServiceId(serviceTypeList.get(0).getId());
        return serviceObj;
    }

    @Override
    public void updateFTSsearch(String tableName, String entityId, ContentValues contentValues) {

        Timber.d("Starting updateFTSsearch table: %s", tableName);

        AllCommonsRepository allCommonsRepository = KipApplication.getInstance().context().
                allCommonsRepositoryobjects(tableName);

        if (allCommonsRepository != null) {
            allCommonsRepository.updateSearch(entityId);
        }

        boolean isInRegister = KipApplication.getInstance().registerTypeRepository().findByRegisterType(entityId, KipConstants.RegisterType.CHILD);

        if (contentValues != null &&
                KipConstants.TABLE_NAME.ALL_CLIENTS.equals(tableName) &&
                isInRegister) {
            String dobString = contentValues.getAsString(Constants.KEY.DOB);
            if (StringUtils.isNotBlank(dobString)) {
                DateTime birthDateTime = Utils.dobStringToDateTime(dobString);
                if (birthDateTime != null) {
                    updateOfflineAlerts(entityId, birthDateTime);
                }
            }
        }
        Timber.d("Finished updateFTSsearch table: %s", tableName);
    }

    protected void updateOfflineAlerts(@NonNull String entityId, @NonNull DateTime birthDateTime) {
        VaccineSchedule.updateOfflineAlerts(entityId, birthDateTime, KipConstants.RegisterType.CHILD);
        ServiceSchedule.updateOfflineAlerts(entityId, birthDateTime);
    }

    @Override
    public String[] getOpenmrsGenIds() {
        return new String[]{"zeir_id"};
    }

    private void scheduleUpdatingClientAlerts(@NonNull String baseEntityId, @NonNull DateTime dateTime) {
        if (!clientsForAlertUpdates.containsKey(baseEntityId)) {
            clientsForAlertUpdates.put(baseEntityId, dateTime);
        }
    }

    protected void processVaccinations(@NonNull EventClient eventClient, @NonNull ClientClassification clientClassification) throws Covid19VaccineEligibilityException {
        HashMap<String, String> keyValues = new HashMap<>();
        Event event = eventClient.getEvent();
        // Todo: This might not work as expected when openmrs_entity_ids are added
        generateKeyValuesFromEvent(event, keyValues);


        String visitId = event.getDetails().get(OpdConstants.VISIT_ID);
        String visitDateString = event.getDetails().get(OpdConstants.VISIT_DATE);
        String covid19Antigens = keyValues.get(KipConstants.DbConstants.Columns.VaccineRecord.COVID_19_ANTIGENS);
        String siteOfAdmin = keyValues.get(KipConstants.DbConstants.Columns.VaccineRecord.SITE_OF_ADMINISTRATION);
        String adminDate = keyValues.get(KipConstants.DbConstants.Columns.VaccineRecord.ADMINISTRATION_DATE);
        String adminRoute = keyValues.get(KipConstants.DbConstants.Columns.VaccineRecord.ADMINISTRATION_ROUTE);
        String lotNumber = keyValues.get(KipConstants.DbConstants.Columns.VaccineRecord.LOT_NUMBER);
        String vaccineExpiry = keyValues.get(KipConstants.DbConstants.Columns.VaccineRecord.VACCINE_EXPIRY);
        String age = keyValues.get(KipConstants.DbConstants.Columns.CalculateRiskFactor.AGE);
        String date = keyValues.get(KipConstants.DbConstants.Columns.CalculateRiskFactor.DATE);

        Date visitDate;
        try {
            visitDate = dateFormat.parse(visitDateString != null ? visitDateString : "");
        } catch (ParseException e) {
            Timber.e(e);
            visitDate = event.getEventDate().toDate();
        }

        if (visitDate != null && visitId != null) {
            // Start transaction
            OpdLibrary.getInstance().getRepository().getWritableDatabase().beginTransaction();
            boolean saved = saveVaccinations(event, visitId, covid19Antigens, siteOfAdmin, adminDate, adminRoute, lotNumber, vaccineExpiry, age, date);
            if (!saved) {
                abortTransaction();
                throw new Covid19VaccineEligibilityException(String.format("Visit (COVID19 Vaccine administration) with id %s could not be saved in the db. Fail operation failed", visitId));
            }

            // Update the last interacted with of the user
            try {
                updateLastInteractedWith(event, visitId);
            } catch (SQLiteException | CheckInEventProcessException ex) {
                abortTransaction();
                throw new Covid19VaccineEligibilityException("An error occurred saving last_interacted_with");
            }

            commitSuccessfulTransaction();
        } else {
            throw new Covid19VaccineEligibilityException(String.format("OPD COVID19 Vaccine administration event %s could not be processed because it the visitDate OR visitId is null", new Gson().toJson(event)));
        }
    }

    protected void processInfluenzaVaccinations(@NonNull EventClient eventClient, @NonNull ClientClassification clientClassification) throws Covid19VaccineEligibilityException {
        HashMap<String, String> keyValues = new HashMap<>();
        Event event = eventClient.getEvent();
        // Todo: This might not work as expected when openmrs_entity_ids are added
        generateKeyValuesFromEvent(event, keyValues);


        String visitId = event.getDetails().get(OpdConstants.VISIT_ID);
        String visitDateString = event.getDetails().get(OpdConstants.VISIT_DATE);
        String influenzaVaccines = keyValues.get(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.INFLUENZA_VACCINE);
        String siteOfAdmin = keyValues.get(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.SITE_OF_ADMINISTRATION);
        String adminDate = keyValues.get(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.ADMINISTRATION_DATE);
        String adminRoute = keyValues.get(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.ADMINISTRATION_ROUTE);
        String lotNumber = keyValues.get(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.LOT_NUMBER);
        String vaccineExpiry = keyValues.get(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.VACCINE_EXPIRY);
        String age = keyValues.get(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.AGE);
        String date = keyValues.get(KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.DATE);

        Date visitDate;
        try {
            visitDate = dateFormat.parse(visitDateString != null ? visitDateString : "");
        } catch (ParseException e) {
            Timber.e(e);
            visitDate = event.getEventDate().toDate();
        }

        if (visitDate != null && visitId != null) {
            // Start transaction
            OpdLibrary.getInstance().getRepository().getWritableDatabase().beginTransaction();
            boolean saved = saveInfluenzaVaccinations(event, visitId, influenzaVaccines, siteOfAdmin, adminDate, adminRoute, lotNumber, vaccineExpiry, age, date);
            if (!saved) {
                abortTransaction();
                throw new Covid19VaccineEligibilityException(String.format("Visit (COVID19 Vaccine administration) with id %s could not be saved in the db. Fail operation failed", visitId));
            }

            try {
                processEvent(event, eventClient.getClient(), clientClassification);
            } catch (Exception e) {
                Timber.e(e);
            }

            // Update the last interacted with of the user
            try {
                updateLastInteractedWith(event, visitId);
            } catch (SQLiteException | CheckInEventProcessException ex) {
                abortTransaction();
                throw new Covid19VaccineEligibilityException("An error occurred saving last_interacted_with");
            }

            commitSuccessfulTransaction();
        } else {
            throw new Covid19VaccineEligibilityException(String.format("OPD COVID19 Vaccine administration event %s could not be processed because it the visitDate OR visitId is null", new Gson().toJson(event)));
        }
    }

    private boolean saveVaccinations(Event event, String visitId, String covid19Antigens, String siteOfAdmin, String adminDate, String adminRoute, String lotNumber, String vaccineExpiry, String age, String date) {
        OpdCovid19VaccinationForm opdCovid19VaccinationForm = new OpdCovid19VaccinationForm();
        opdCovid19VaccinationForm.setVisitId(visitId);
        opdCovid19VaccinationForm.setId(visitId);
        opdCovid19VaccinationForm.setBaseEntityId(event.getBaseEntityId());
        opdCovid19VaccinationForm.setCovid19Antigens(covid19Antigens);
        opdCovid19VaccinationForm.setAdministrationDate(adminDate);
        opdCovid19VaccinationForm.setAdministrationRoute(adminRoute);
        opdCovid19VaccinationForm.setSiteOfAdministration(siteOfAdmin);
        opdCovid19VaccinationForm.setLotNumber(lotNumber);
        opdCovid19VaccinationForm.setVaccineExpiry(vaccineExpiry);
        opdCovid19VaccinationForm.setAge(age);
        opdCovid19VaccinationForm.setCreatedAt(OpdUtils.convertDate(event.getEventDate().toLocalDate().toDate(), OpdDbConstants.DATE_FORMAT));
        opdCovid19VaccinationForm.setDate(date);

        return KipApplication.getInstance().opdCovid19VaccinationRepository().saveOrUpdate(opdCovid19VaccinationForm);
    }

    private boolean saveInfluenzaVaccinations(Event event, String visitId, String influenzaVaccine, String siteOfAdmin, String adminDate, String adminRoute, String lotNumber, String vaccineExpiry, String age, String date) {
        OpdInfluenzaVaccineAdministrationForm opdInfluenzaVaccineAdministrationForm = new OpdInfluenzaVaccineAdministrationForm();
        opdInfluenzaVaccineAdministrationForm.setVisitId(visitId);
        opdInfluenzaVaccineAdministrationForm.setId(visitId);
        opdInfluenzaVaccineAdministrationForm.setBaseEntityId(event.getBaseEntityId());
        opdInfluenzaVaccineAdministrationForm.setInfluenzaVaccines(influenzaVaccine);
        opdInfluenzaVaccineAdministrationForm.setInfluenzaAdministrationDate(adminDate);
        opdInfluenzaVaccineAdministrationForm.setInfluenzaAdministrationRoute(adminRoute);
        opdInfluenzaVaccineAdministrationForm.setInfluenzaSiteOfAdministration(siteOfAdmin);
        opdInfluenzaVaccineAdministrationForm.setInfluenzaLotNumber(lotNumber);
        opdInfluenzaVaccineAdministrationForm.setInfluenzaVaccineExpiry(vaccineExpiry);
        opdInfluenzaVaccineAdministrationForm.setAge(age);
        opdInfluenzaVaccineAdministrationForm.setCreatedAt(OpdUtils.convertDate(event.getEventDate().toLocalDate().toDate(), OpdDbConstants.DATE_FORMAT));
        opdInfluenzaVaccineAdministrationForm.setDate(date);

        return KipApplication.getInstance().opdInfluenzaVaccineAdministrationFormRepository().saveOrUpdate(opdInfluenzaVaccineAdministrationForm);
    }

    protected void processVaccinationWaitingLists(@NonNull EventClient eventClient, @NonNull ClientClassification clientClassification) throws Covid19VaccinationWaitingListException {
        HashMap<String, String> keyValues = new HashMap<>();
        Event event = eventClient.getEvent();
        // Todo: This might not work as expected when openmrs_entity_ids are added
        generateKeyValuesFromEvent(event, keyValues);


        String visitId = event.getDetails().get(OpdConstants.VISIT_ID);
        String visitDateString = event.getDetails().get(OpdConstants.VISIT_DATE);
        String waitingList = keyValues.get(KipConstants.DbConstants.Columns.WaitingList.WAITING_LIST);
        String age = keyValues.get(KipConstants.DbConstants.Columns.CalculateRiskFactor.AGE);
        String date = keyValues.get(KipConstants.DbConstants.Columns.CalculateRiskFactor.DATE);

        Date visitDate;
        try {
            visitDate = dateFormat.parse(visitDateString != null ? visitDateString : "");
        } catch (ParseException e) {
            Timber.e(e);
            visitDate = event.getEventDate().toDate();
        }

        if (visitDate != null && visitId != null) {
            // Start transaction
            OpdLibrary.getInstance().getRepository().getWritableDatabase().beginTransaction();
            boolean saved = saveVaccineWaitingList(event, visitId, waitingList, age, date);
            if (!saved) {
                abortTransaction();
                throw new Covid19VaccinationWaitingListException(String.format("Visit (COVID19 Waiting List) with id %s could not be saved in the db. Fail operation failed", visitId));
            }

            // Update the last interacted with of the user
            try {
                updateLastInteractedWith(event, visitId);
            } catch (SQLiteException | CheckInEventProcessException ex) {
                abortTransaction();
                throw new Covid19VaccinationWaitingListException("An error occurred saving last_interacted_with");
            }

            commitSuccessfulTransaction();
        } else {
            throw new Covid19VaccinationWaitingListException(String.format("OPD COVID19 Waiting List event %s could not be processed because it the visitDate OR visitId is null", new Gson().toJson(event)));
        }
    }

    private boolean saveVaccineWaitingList(Event event, String visitId, String waitingList, String age, String date) {
        OpdCovid19WaitingListForm opdCovid19WaitingListForm = new OpdCovid19WaitingListForm();
        opdCovid19WaitingListForm.setVisitId(visitId);
        opdCovid19WaitingListForm.setId(visitId);
        opdCovid19WaitingListForm.setBaseEntityId(event.getBaseEntityId());
        opdCovid19WaitingListForm.setWaitingList(waitingList);
        opdCovid19WaitingListForm.setAge(age);
        opdCovid19WaitingListForm.setCreatedAt(OpdUtils.convertDate(event.getEventDate().toLocalDate().toDate(), OpdDbConstants.DATE_FORMAT));
        opdCovid19WaitingListForm.setDate(date);

        return KipApplication.getInstance().opdCovid19WaitingListRepository().saveOrUpdate(opdCovid19WaitingListForm);
    }

    protected void processVaccinationEligibility(@NonNull EventClient eventClient, @NonNull ClientClassification clientClassification) throws Covid19VaccineEligibilityException {
        HashMap<String, String> keyValues = new HashMap<>();
        Event event = eventClient.getEvent();
        // Todo: This might not work as expected when openmrs_entity_ids are added
        generateKeyValuesFromEvent(event, keyValues);


        String visitId = event.getDetails().get(OpdConstants.VISIT_ID);
        String visitDateString = event.getDetails().get(OpdConstants.VISIT_DATE);
        String temperature = keyValues.get(KipConstants.DbConstants.Columns.VaccinationEligibility.TEMPERATURE);
        String covid19History = keyValues.get(KipConstants.DbConstants.Columns.VaccinationEligibility.COVID_19_HISTORY);
        String oralConfirmation = keyValues.get(KipConstants.DbConstants.Columns.VaccinationEligibility.ORAL_CONFIRMATION);
        String respiratorySymptoms = keyValues.get(KipConstants.DbConstants.Columns.VaccinationEligibility.RESPIRATORY_SYMPTOMS);
        String otherRespiratorySymptoms = keyValues.get(KipConstants.DbConstants.Columns.VaccinationEligibility.OTHER_RESPIRATORY_SYMPTOMS);
        String allergies = keyValues.get(KipConstants.DbConstants.Columns.VaccinationEligibility.ALLERGIES);
        String otherAllergies = keyValues.get(KipConstants.DbConstants.Columns.VaccinationEligibility.OTHER_ALLERGIES);
        String age = keyValues.get(KipConstants.DbConstants.Columns.CalculateRiskFactor.AGE);

        Date visitDate;
        try {
            visitDate = dateFormat.parse(visitDateString != null ? visitDateString : "");
        } catch (ParseException e) {
            Timber.e(e);
            visitDate = event.getEventDate().toDate();
        }

        if (visitDate != null && visitId != null) {
            // Start transaction
            OpdLibrary.getInstance().getRepository().getWritableDatabase().beginTransaction();
            boolean saved = saveVaccineEligibility(event, visitId, temperature, covid19History, oralConfirmation, respiratorySymptoms, otherRespiratorySymptoms, allergies, otherAllergies, age, visitDateString);
            if (!saved) {
                abortTransaction();
                throw new Covid19VaccineEligibilityException(String.format("Visit (OVID19 Eligibility check) with id %s could not be saved in the db. Fail operation failed", visitId));
            }
            // Update the last interacted with of the user
            try {
                updateLastInteractedWith(event, visitId);
            } catch (SQLiteException | CheckInEventProcessException ex) {
                abortTransaction();
                throw new Covid19VaccineEligibilityException("An error occurred saving last_interacted_with");
            }

            commitSuccessfulTransaction();
        } else {
            throw new Covid19VaccineEligibilityException(String.format("OPD COVID19 Eligibility check event %s could not be processed because it the visitDate OR visitId is null", new Gson().toJson(event)));
        }
    }

    private boolean saveVaccineEligibility(Event event, String visitId, String temperature, String covid19History, String oralConfirmation, String respiratorySymptoms, String otherRespiratorySymptoms, String allergies, String otherAllergies, String age, String date) {
        OpdCovid19VaccinationEligibilityCheckForm opdCovid19VaccinationEligibilityCheckForm = new OpdCovid19VaccinationEligibilityCheckForm();
        opdCovid19VaccinationEligibilityCheckForm.setVisitId(visitId);
        opdCovid19VaccinationEligibilityCheckForm.setId(visitId);
        opdCovid19VaccinationEligibilityCheckForm.setBaseEntityId(event.getBaseEntityId());
        opdCovid19VaccinationEligibilityCheckForm.setTemperature(temperature);
        opdCovid19VaccinationEligibilityCheckForm.setCovid19History(covid19History);
        opdCovid19VaccinationEligibilityCheckForm.setOralConfirmation(oralConfirmation);
        opdCovid19VaccinationEligibilityCheckForm.setRespiratorySymptoms(respiratorySymptoms);
        opdCovid19VaccinationEligibilityCheckForm.setOtherRespiratorySymptoms(otherRespiratorySymptoms);
        opdCovid19VaccinationEligibilityCheckForm.setAllergies(allergies);
        opdCovid19VaccinationEligibilityCheckForm.setOtherAllergies(otherAllergies);
        opdCovid19VaccinationEligibilityCheckForm.setAge(age);
        opdCovid19VaccinationEligibilityCheckForm.setCreatedAt(String.valueOf(new Date()));
        opdCovid19VaccinationEligibilityCheckForm.setDate(date);

        return KipApplication.getInstance().opdCovid19VaccinationEligibilityRepository().saveOrUpdate(opdCovid19VaccinationEligibilityCheckForm);
    }

    protected void processCalculateRiskFactor(@NonNull EventClient eventClient, @NonNull ClientClassification clientClassification) throws
            CheckInEventProcessException, Covid19CalculateRiskFactorException {
        HashMap<String, String> keyValues = new HashMap<>();
        Event event = eventClient.getEvent();
        // Todo: This might not work as expected when openmrs_entity_ids are added
        generateKeyValuesFromEvent(event, keyValues);


        String visitId = event.getDetails().get(OpdConstants.VISIT_ID);
        String visitDateString = event.getDetails().get(OpdConstants.VISIT_DATE);
        String preExistingConditionString = keyValues.get(KipConstants.DbConstants.Columns.CalculateRiskFactor.PRE_EXISTING_CONDITIONS);
        String otherPreExistingConditionString = keyValues.get(KipConstants.DbConstants.Columns.CalculateRiskFactor.OTHER_PRE_EXISTING_CONDITIONS);
        String occupation = keyValues.get(KipConstants.DbConstants.Columns.CalculateRiskFactor.OCCUPATION);
        String otherOccupation = keyValues.get(KipConstants.DbConstants.Columns.CalculateRiskFactor.OTHER_OCCUPATION);
        String age = keyValues.get(KipConstants.DbConstants.Columns.CalculateRiskFactor.AGE);
        String date = keyValues.get(KipConstants.DbConstants.Columns.CalculateRiskFactor.DATE);

        Date visitDate;
        try {
            visitDate = dateFormat.parse(visitDateString != null ? visitDateString : "");
        } catch (ParseException e) {
            Timber.e(e);
            visitDate = event.getEventDate().toDate();
        }

        if (visitDate != null && visitId != null) {
            // Start transaction
            OpdLibrary.getInstance().getRepository().getWritableDatabase().beginTransaction();
            boolean saved = saveRiskFactor(event, visitId, preExistingConditionString, otherPreExistingConditionString, occupation, otherOccupation, age, date);
            if (!saved) {
                abortTransaction();
                throw new Covid19CalculateRiskFactorException(String.format("Visit (COVID19 Calculate risk factor) with id %s could not be saved in the db. Fail operation failed", visitId));
            }

            // Update the last interacted with of the user
            try {
                updateLastInteractedWith(event, visitId);
            } catch (SQLiteException ex) {
                abortTransaction();
                throw new Covid19CalculateRiskFactorException("An error occurred saving last_interacted_with");
            }

            commitSuccessfulTransaction();
        } else {
            throw new Covid19CalculateRiskFactorException(String.format("OPD COVID19 Calculate risk factor event %s could not be processed because it the visitDate OR visitId is null", new Gson().toJson(event)));
        }
    }

    protected void processOpdMedicalCheck(@NonNull EventClient eventClient, @NonNull ClientClassification clientClassification) throws
            CheckInEventProcessException, Covid19CalculateRiskFactorException {
        HashMap<String, String> keyValues = new HashMap<>();
        Event event = eventClient.getEvent();
        // Todo: This might not work as expected when openmrs_entity_ids are added
        generateKeyValuesFromEvent(event, keyValues);


        String visitId = event.getDetails().get(OpdConstants.VISIT_ID);
        String visitDateString = event.getDetails().get(OpdConstants.VISIT_DATE);
        String temperature = keyValues.get(KipConstants.DbConstants.Columns.OpdMedicalCheck.TEMPERATURE);
        String preExistingConditionString = keyValues.get(KipConstants.DbConstants.Columns.OpdMedicalCheck.PRE_EXISTING_CONDITIONS);
        String otherPreExistingConditionString = keyValues.get(KipConstants.DbConstants.Columns.OpdMedicalCheck.OTHER_PRE_EXISTING_CONDITIONS);
        String allergies = keyValues.get(KipConstants.DbConstants.Columns.OpdMedicalCheck.ALLERGIES);
        String otherAllergies= keyValues.get(KipConstants.DbConstants.Columns.OpdMedicalCheck.OTHER_ALLERGIES);
        String age = keyValues.get(KipConstants.DbConstants.Columns.OpdMedicalCheck.AGE);
        String date = keyValues.get(KipConstants.DbConstants.Columns.OpdMedicalCheck.DATE);

        Date visitDate;
        try {
            visitDate = dateFormat.parse(visitDateString != null ? visitDateString : "");
        } catch (ParseException e) {
            Timber.e(e);
            visitDate = event.getEventDate().toDate();
        }

        if (visitDate != null && visitId != null) {
            // Start transaction
            OpdLibrary.getInstance().getRepository().getWritableDatabase().beginTransaction();
            boolean saved = saveOpdMedicalCheck(event, visitId,temperature, preExistingConditionString, otherPreExistingConditionString, allergies, otherAllergies, age, date);
            if (!saved) {
                abortTransaction();
                throw new Covid19CalculateRiskFactorException(String.format("Visit (COVID19 Calculate risk factor) with id %s could not be saved in the db. Fail operation failed", visitId));
            }

            try {
                processEvent(event, eventClient.getClient(), clientClassification);
            } catch (Exception e) {
                Timber.e(e);
            }

            // Update the last interacted with of the user
            try {
                updateLastInteractedWith(event, visitId);
            } catch (SQLiteException ex) {
                abortTransaction();
                throw new Covid19CalculateRiskFactorException("An error occurred saving last_interacted_with");
            }

            commitSuccessfulTransaction();
        } else {
            throw new Covid19CalculateRiskFactorException(String.format("OPD COVID19 Calculate risk factor event %s could not be processed because it the visitDate OR visitId is null", new Gson().toJson(event)));
        }
    }

    private boolean saveRiskFactor(Event event, String visitId, String preExistingConditionString, String otherPreExistingConditionString, String occupation, String otherOccupation, String age, String date) {
        OpdCovid19CalculateRiskFactorForm opdCovid19CalculateRiskFactorForm = new OpdCovid19CalculateRiskFactorForm();
        opdCovid19CalculateRiskFactorForm.setVisitId(visitId);
        opdCovid19CalculateRiskFactorForm.setId(visitId);
        opdCovid19CalculateRiskFactorForm.setBaseEntityId(event.getBaseEntityId());
        opdCovid19CalculateRiskFactorForm.setPreExistingConditions(preExistingConditionString);
        opdCovid19CalculateRiskFactorForm.setOtherPreExistingConditions(otherPreExistingConditionString);
        opdCovid19CalculateRiskFactorForm.setOccupation(occupation);
        opdCovid19CalculateRiskFactorForm.setOtherOccupation(otherOccupation);
        opdCovid19CalculateRiskFactorForm.setAge(age);
        opdCovid19CalculateRiskFactorForm.setCreatedAt(String.valueOf(new Date()));
        opdCovid19CalculateRiskFactorForm.setDate(date);

        return KipApplication.getInstance().opdCovid19CalculateRiskRepository().saveOrUpdate(opdCovid19CalculateRiskFactorForm);
    }

    private boolean saveOpdMedicalCheck(Event event, String visitId, String temperature, String preExistingConditionString, String otherPreExistingConditionString, String allergies, String otherAllergies, String age, String date) {
        OpdMedicalCheckForm opdMedicalCheckForm = new OpdMedicalCheckForm();
        opdMedicalCheckForm.setVisitId(visitId);
        opdMedicalCheckForm.setId(visitId);
        opdMedicalCheckForm.setBaseEntityId(event.getBaseEntityId());
        opdMedicalCheckForm.setTemperature(temperature);
        opdMedicalCheckForm.setPreExistingConditions(preExistingConditionString);
        opdMedicalCheckForm.setOtherPreExistingConditions(otherPreExistingConditionString);
        opdMedicalCheckForm.setAllergies(allergies);
        opdMedicalCheckForm.setOtherAllergies(otherAllergies);
        opdMedicalCheckForm.setAge(age);
        opdMedicalCheckForm.setCreatedAt(String.valueOf(new Date()));
        opdMedicalCheckForm.setDate(date);

        return KipApplication.getInstance().opdMedicalCheckFormRepository().saveOrUpdate(opdMedicalCheckForm);
    }

    protected void processSmsReminder(@NonNull EventClient eventClient, @NonNull ClientClassification clientClassification) throws Covid19VaccinationWaitingListException {
        HashMap<String, String> keyValues = new HashMap<>();
        Event event = eventClient.getEvent();
        // Todo: This might not work as expected when openmrs_entity_ids are added
        generateKeyValuesFromEvent(event, keyValues);


        String visitId = event.getDetails().get(OpdConstants.VISIT_ID);
        String visitDateString = event.getDetails().get(OpdConstants.VISIT_DATE);
        String smsReminder = keyValues.get(KipConstants.DbConstants.Columns.SmsReminder.SMS_REMINDER);
        String date = keyValues.get(KipConstants.DbConstants.Columns.VaccineRecord.DATE);

        Date visitDate;
        try {
            visitDate = dateFormat.parse(visitDateString != null ? visitDateString : "");
        } catch (ParseException e) {
            Timber.e(e);
            visitDate = event.getEventDate().toDate();
        }

        if (visitDate != null && visitId != null) {
            // Start transaction
            OpdLibrary.getInstance().getRepository().getWritableDatabase().beginTransaction();
            boolean saved = saveSMSReminder(event, visitId, smsReminder, date);
            if (!saved) {
                abortTransaction();
                throw new Covid19VaccinationWaitingListException(String.format("Visit (COVID19 Waiting List) with id %s could not be saved in the db. Fail operation failed", visitId));
            }

            try {
                processEvent(event, eventClient.getClient(), clientClassification);
            } catch (Exception e) {
                Timber.e(e);
            }

            // Update the last interacted with of the user
            try {
                updateLastInteractedWith(event, visitId);
            } catch (SQLiteException | CheckInEventProcessException ex) {
                abortTransaction();
                throw new Covid19VaccinationWaitingListException("An error occurred saving last_interacted_with");
            }

            commitSuccessfulTransaction();
        } else {
            throw new Covid19VaccinationWaitingListException(String.format("OPD COVID19 Waiting List event %s could not be processed because it the visitDate OR visitId is null", new Gson().toJson(event)));
        }
    }

    private boolean saveSMSReminder(Event event, String visitId, String smsReminder, String date){
        OpdSMSReminderForm opdSMSReminderForm = new OpdSMSReminderForm();
        opdSMSReminderForm.setVisitId(visitId);
        opdSMSReminderForm.setId(visitId);
        opdSMSReminderForm.setBaseEntityId(event.getBaseEntityId());
        opdSMSReminderForm.setSmsReminder(smsReminder);
        opdSMSReminderForm.setCreatedAt(String.valueOf(new Date()));
        opdSMSReminderForm.setDate(date);

        return KipApplication.getInstance().opdSMSReminderFormRepository().saveOrUpdate(opdSMSReminderForm);
    }


    private void abortTransaction() {
        if (OpdLibrary.getInstance().getRepository().getWritableDatabase().inTransaction()) {
            OpdLibrary.getInstance().getRepository().getWritableDatabase().endTransaction();
        }
    }

    private void commitSuccessfulTransaction() {
        if (OpdLibrary.getInstance().getRepository().getWritableDatabase().inTransaction()) {
            OpdLibrary.getInstance().getRepository().getWritableDatabase().setTransactionSuccessful();
            OpdLibrary.getInstance().getRepository().getWritableDatabase().endTransaction();
        }
    }

    private void updateLastInteractedWith(@NonNull Event event, @NonNull String visitId) throws
            CheckInEventProcessException {
        String tableName = OpdUtils.metadata().getTableName();

        String lastInteractedWithDate = String.valueOf(new Date().getTime());

        ContentValues contentValues = new ContentValues();
        contentValues.put("last_interacted_with", lastInteractedWithDate);

        int recordsUpdated = OpdLibrary.getInstance().getRepository().getWritableDatabase()
                .update(tableName, contentValues, "base_entity_id = ?", new String[]{event.getBaseEntityId()});

        if (recordsUpdated < 1) {
            abortTransaction();
            throw new CheckInEventProcessException(String.format("Updating last interacted with for visit %s for base_entity_id %s in table %s failed"
                    , visitId
                    , event.getBaseEntityId()
                    , tableName));
        }

        // Update FTS
        CommonRepository commonrepository = OpdLibrary.getInstance().context().commonrepository(tableName);

        ContentValues contentValues1 = new ContentValues();
        contentValues1.put("last_interacted_with", lastInteractedWithDate);

        boolean isUpdated = false;

        if (commonrepository.isFts()) {
            recordsUpdated = OpdLibrary.getInstance().getRepository().getWritableDatabase()
                    .update(CommonFtsObject.searchTableName(tableName), contentValues, CommonFtsObject.idColumn + " = ?", new String[]{event.getBaseEntityId()});
            isUpdated = recordsUpdated > 0;
        }

        if (!isUpdated) {
            abortTransaction();
            throw new CheckInEventProcessException(String.format("Updating last interacted with for visit %s for base_entity_id %s in table %s failed"
                    , visitId
                    , event.getBaseEntityId()
                    , tableName));
        }
    }

    @VisibleForTesting
    Date getDate() {
        return new Date();
    }

    private void generateKeyValuesFromEvent(@NonNull Event
                                                    event, @NonNull HashMap<String, String> keyValues) {
        List<Obs> obs = event.getObs();

        for (Obs observation : obs) {
            String key = observation.getFormSubmissionField();
            List<Object> values = observation.getValues();

            if (values.size() > 0) {
                String value = (String) values.get(0);

                if (!TextUtils.isEmpty(value)) {

                    if (values.size() > 1) {
                        value = values.toString();
                    }

                    keyValues.put(key, value);
                    continue;
                }
            }

            List<Object> humanReadableValues = observation.getHumanReadableValues();
            if (humanReadableValues.size() > 0) {
                String value = (String) humanReadableValues.get(0);

                if (!TextUtils.isEmpty(value)) {

                    if (values.size() > 1) {
                        value = values.toString();
                    }

                    keyValues.put(key, value);
                    continue;
                }
            }
        }
    }


}