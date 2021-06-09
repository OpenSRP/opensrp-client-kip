package org.smartregister.kip.processor;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.kip.repository.KipOpdDetailsRepository;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.kip.util.KipJsonFormUtils;
import org.smartregister.kip.util.KipOpdConstants;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.configuration.OpdFormProcessor;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.opd.utils.OpdJsonFormUtils;
import org.smartregister.opd.utils.OpdUtils;
import org.smartregister.util.JsonFormUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.kip.presenter.KipOpdProfileActivityPresenter.ELIGIBILITY;
import static org.smartregister.opd.utils.OpdJsonFormUtils.METADATA;

public class KipCovid19MiniProcessor implements OpdFormProcessor<List<Event>> {
    /***
     * This method creates an event for each step in the Opd Diagnose and treatment form
     * @param jsonFormObject {@link JSONObject}
     * @param data {@link Intent}
     * @return {@link List}
     */
    @Nullable
    @Override
    public List<Event> processForm(@NonNull JSONObject jsonFormObject, @NonNull Intent data) {
        String entityId = OpdUtils.getIntentValue(data, OpdConstants.IntentKey.BASE_ENTITY_ID);

        if (StringUtils.isNotBlank(entityId)) {
            Map<String, String> opdCheckInMap = OpdLibrary.getInstance().getCheckInRepository().getLatestCheckIn(entityId);
            FormTag formTag = OpdJsonFormUtils.formTag(OpdUtils.getAllSharedPreferences());

            if (opdCheckInMap != null && !opdCheckInMap.isEmpty()) {
                String visitId = opdCheckInMap.get(OpdDbConstants.Column.OpdCheckIn.VISIT_ID);
                String visitDate = opdCheckInMap.get("date");
                String steps = jsonFormObject.optString(JsonFormConstants.COUNT);
                String encounterType = jsonFormObject.optString(JsonFormConstants.ENCOUNTER_TYPE);
                int numOfSteps = Integer.parseInt(steps);
                List<Event> eventList = new ArrayList<>();

                for (int j = 0; j < numOfSteps; j++) {
                    JSONObject step = jsonFormObject.optJSONObject(JsonFormConstants.STEP.concat(String.valueOf(j + 1)));
                    String bindType = step.optString(OpdConstants.BIND_TYPE);
                    JSONArray fields = step.optJSONArray(OpdJsonFormUtils.FIELDS);
                    JSONArray multiSelectListValueJsonArray = null;

                    Event baseEvent = JsonFormUtils.createEvent(fields, jsonFormObject.optJSONObject(METADATA), formTag, entityId, encounterType, bindType);
                    OpdJsonFormUtils.tagSyncMetadata(baseEvent);
                    baseEvent.addDetails(KipOpdConstants.VISIT_ID, visitId);
                    baseEvent.addDetails(KipOpdConstants.VISIT_DATE, visitDate);
                    baseEvent.setEntityType(encounterType);
                    baseEvent.setEventType(encounterType);

                    if (multiSelectListValueJsonArray != null) {
                        baseEvent.addDetails(OpdConstants.KEY.VALUE, multiSelectListValueJsonArray.toString());
                    }
                    eventList.add(baseEvent);
                }

                if (encounterType.equalsIgnoreCase(KipConstants.EventType.OPD_COVID_19_VACCINE_ADMINISTRATION)) {
                    closeOpdVisit(entityId, formTag, visitId, eventList);
                }

                if (encounterType.equals(KipConstants.EventType.OPD_VACCINATION_ELIGIBILITY_CHECK) && getEligibility(jsonFormObject.toString()).equalsIgnoreCase("0")) {
                    closeOpdVisit(entityId, formTag, visitId, eventList);
                    KipOpdDetailsRepository.resetCovid19VaccineSchedule(entityId);
                }

                if (encounterType.equals(KipConstants.EventType.OPD_INFLUENZA_VACCINE_ADMINISTRATION)){
                    closeOpdVisit(entityId,formTag,visitId,eventList);
                }

                return eventList;
            } else {
                Timber.e("Corresponding OpdCheckIn record for EntityId %s is missing", entityId);
                return null;
            }
        }
        return null;
    }

    private void closeOpdVisit(String entityId, FormTag formTag, String visitId, List<Event> eventList) {
        Event closeOpdVisit = JsonFormUtils.createEvent(new JSONArray(), new JSONObject(), formTag, entityId, OpdConstants.EventType.CLOSE_OPD_VISIT, "");
        closeOpdVisit.setEventType(OpdConstants.EventType.CLOSE_OPD_VISIT);
        closeOpdVisit.setEntityType(OpdConstants.EventType.CLOSE_OPD_VISIT);
        OpdJsonFormUtils.tagSyncMetadata(closeOpdVisit);
        closeOpdVisit.addDetails(OpdConstants.JSON_FORM_KEY.VISIT_ID, visitId);
        closeOpdVisit.addDetails(OpdConstants.JSON_FORM_KEY.VISIT_END_DATE, OpdUtils.convertDate(new Date(), OpdConstants.DateFormat.YYYY_MM_DD_HH_MM_SS));
        eventList.add(closeOpdVisit);
    }

    private String getEligibility(String form) {
        String eligibility;
        JSONObject eligibilityObject = getFormField(form, ELIGIBILITY);
        eligibility = eligibilityObject.optString(KipConstants.VALUE, "");
        return eligibility;
    }

    @org.jetbrains.annotations.Nullable
    private JSONObject getFormField(String form, String key) {
        JSONArray fields = new JSONArray();
        try {
            JSONObject jsonObject = new JSONObject(form);
            fields = KipJsonFormUtils.getSingleStepFormfields(jsonObject);
        } catch (JSONException exception) {
            Timber.e(exception);
        }
        return KipJsonFormUtils.getFieldJSONObject(fields, key);
    }

}
