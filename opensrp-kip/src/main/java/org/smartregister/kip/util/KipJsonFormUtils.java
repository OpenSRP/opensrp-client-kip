package org.smartregister.kip.util;

import android.content.Context;
import android.util.Log;

import com.google.common.reflect.TypeToken;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.api.domain.Location;
import org.smartregister.child.ChildLibrary;
import org.smartregister.child.enums.LocationHierarchy;
import org.smartregister.child.util.Constants;
import org.smartregister.child.util.JsonFormUtils;
import org.smartregister.child.util.Utils;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.context.AllSettings;
import org.smartregister.kip.repository.KipLocationRepository;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.util.AssetHandler;
import org.smartregister.util.FormUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

import static org.smartregister.login.task.RemoteLoginTask.getOpenSRPContext;

public class KipJsonFormUtils extends JsonFormUtils {


    public static String getMetadataForEditForm(Context context, Map<String, String> childDetails, List<String> nonEditableFields) {
        try {
            JSONObject birthRegistrationForm = FormUtils.getInstance(context)
                    .getFormJson(Utils.metadata().childRegister.formName);
            updateRegistrationEventType(birthRegistrationForm);


            JsonFormUtils.addChildRegLocHierarchyQuestions(birthRegistrationForm, KipConstants.KEY.REGISTRATION_HOME_ADDRESS,
                    LocationHierarchy.ENTIRE_TREE);

            KipAddChildRegLocHierarchyQuestions(birthRegistrationForm, getOpenSRPContext());

            if (birthRegistrationForm != null) {
                birthRegistrationForm.put(JsonFormUtils.ENTITY_ID, childDetails.get(Constants.KEY.BASE_ENTITY_ID));
                birthRegistrationForm.put(JsonFormUtils.ENCOUNTER_TYPE, Utils.metadata().childRegister.updateEventType);
                birthRegistrationForm.put(JsonFormUtils.RELATIONAL_ID, childDetails.get(RELATIONAL_ID));
                birthRegistrationForm.put(JsonFormUtils.CURRENT_ZEIR_ID,
                        Utils.getValue(childDetails, KipConstants.KEY.MALAWI_ID, true).replace("-",
                                ""));
                birthRegistrationForm.put(JsonFormUtils.CURRENT_OPENSRP_ID,
                        Utils.getValue(childDetails, Constants.JSON_FORM_KEY.UNIQUE_ID, false));

                JSONObject metadata = birthRegistrationForm.getJSONObject(JsonFormUtils.METADATA);
                metadata.put(JsonFormUtils.ENCOUNTER_LOCATION,
                        ChildLibrary.getInstance().getLocationPickerView(context).getSelectedItem());

                //inject zeir id into the birthRegistrationForm
                JSONObject stepOne = birthRegistrationForm.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                updateFormDetailsForEdit(childDetails, jsonArray, nonEditableFields);
                return birthRegistrationForm.toString();
            }
        } catch (Exception e) {
            Timber.e(e, "KipJsonFormUtils --> getMetadataForEditForm");
        }

        return "";
    }


    public static void KipAddChildRegLocHierarchyQuestions(JSONObject form, org.smartregister.Context context) {
        try {
            JSONArray questions = form.getJSONObject("step1").getJSONArray("fields");
            ArrayList<String> allLevels = new ArrayList<>();
            allLevels.add("Country");
            allLevels.add("County");
            allLevels.add("Sub County");
            allLevels.add("Ward");
            allLevels.add("Health Facility");

            JSONArray upToFacilities = generateLocationHierarchyTree(context, false, new ArrayList<>(allLevels.subList(4, 5)));
            JSONArray counties = generateLocationArray("County", context, true, new ArrayList<>(allLevels.subList(1, 2)));
            JSONArray subCounties = generateLocationArray("Sub County", context, true, new ArrayList<>(allLevels.subList(2, 3)));
            JSONArray wards = generateLocationArray("Ward", context, true, new ArrayList<>(allLevels.subList(3, 4)));

            for (int i = 0; i < questions.length(); i++) {
                if (questions.getJSONObject(i).getString("key").equals("Home_Facility")) {
                    if (upToFacilities.length() > 0) {
                        JSONObject facility = upToFacilities.getJSONObject(0);
                        if (facility != null && facility.has("name")) {
                            questions.getJSONObject(i).put("value", facility.getString("name"));
                        }
                    }
                } else if (questions.getJSONObject(i).getString("key").equals("Ce_County")) {
                    questions.getJSONObject(i).remove(JsonFormUtils.VALUES);
                    questions.getJSONObject(i).put("values", counties);

                } else if (questions.getJSONObject(i).getString("key").equals("Ce_Sub_County")) {
                    questions.getJSONObject(i).remove(JsonFormUtils.VALUES);
                    questions.getJSONObject(i).put("values", subCounties);

                } else if (questions.getJSONObject(i).getString("key").equals("Ce_Ward")) {
                    questions.getJSONObject(i).remove(JsonFormUtils.VALUES);
                    questions.getJSONObject(i).put("values", wards);

                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }


    private static JSONArray generateLocationArray(String locationTag, org.smartregister.Context context, boolean b, ArrayList<String> strings) throws
            JSONException {

        JSONArray jsonArray = new JSONArray();
        KipLocationRepository locationRepository = KipApplication.getInstance().locationRepository();

        List<Location> locations = locationRepository.getLocationsByTag(locationTag);

        if (locations != null && locations.size() > 0) {
            for (Location l : locations) {
                if (l.getTags() != null && l.getTags().contains(locationTag))
                    jsonArray.put(l.getName());
            }
            jsonArray.put("Other");
        } else {
            JSONArray array = generateLocationHierarchyTree(context, b, strings);
            for (int i = 0; i < array.length(); i++) {
                JSONObject jo = array.getJSONObject(i);
                if (jo.has("name") && StringUtils.isNotBlank(jo.getString("name"))) {
                    jsonArray.put(jo.getString("name"));
                }
            }
        }

        return jsonArray;
    }


    public static JSONArray generateLocationHierarchyTree(org.smartregister.Context context,
                                                          boolean withOtherOption, ArrayList<String> allowedLevels) {
        JSONArray array = new JSONArray();
        try {
            JSONObject locationData = new JSONObject(context.anmLocationController().get());
            if (locationData.has("locationsHierarchy")
                    && locationData.getJSONObject("locationsHierarchy").has("map")) {
                JSONObject map = locationData.getJSONObject("locationsHierarchy").getJSONObject("map");
                Iterator<String> keys = map.keys();
                while (keys.hasNext()) {
                    String curKey = keys.next();
                    getFormJsonData(array, map.getJSONObject(curKey), allowedLevels);
                }
            }

            array = sortTreeViewQuestionOptions(array);
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        if (withOtherOption) {
            try {
                JSONObject other = new JSONObject();
                other.put("name", "Other");
                other.put("key", "Other");
                other.put("level", "");
                array.put(other);
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        return array;
    }


    private static void getFormJsonData(JSONArray allLocationData, JSONObject
            openMrsLocationData, ArrayList<String> allowedLevels) throws JSONException {
        JSONObject jsonFormObject = new JSONObject();
        String name = openMrsLocationData.getJSONObject("node").getString("name");
        jsonFormObject.put("name", getOpenMrsReadableName(name));
        jsonFormObject.put("key", name);
        String level = "";
        try {
            level = openMrsLocationData.getJSONObject("node").getJSONArray("tags").getString(0);
        } catch (JSONException e) {
            Log.e(JsonFormUtils.class.getCanonicalName(), e.getMessage());
        }
        jsonFormObject.put("level", "");
        JSONArray children = new JSONArray();
        if (openMrsLocationData.has("children")) {
            Iterator<String> childIterator = openMrsLocationData.getJSONObject("children").keys();
            while (childIterator.hasNext()) {
                String curChildKey = childIterator.next();
                getFormJsonData(children, openMrsLocationData.getJSONObject("children").getJSONObject(curChildKey), allowedLevels);
            }
            if (allowedLevels.contains(level)) {
                jsonFormObject.put("nodes", children);
            } else {
                for (int i = 0; i < children.length(); i++) {
                    allLocationData.put(children.getJSONObject(i));
                }
            }
        }
        if (allowedLevels.contains(level)) {
            allLocationData.put(jsonFormObject);
        }
    }

    public static String getOpenMrsReadableName(String name) {
        String readableName = name;

        Pattern prefixPattern = Pattern.compile("^[a-z]{2} (.*)$");
        Matcher prefixMatcher = prefixPattern.matcher(readableName);
        if (prefixMatcher.find()) {
            readableName = prefixMatcher.group(1);
        }

        if (readableName.contains(":")) {
            String[] splitName = readableName.split(":");
            readableName = splitName[splitName.length - 1].trim();
        }

        return readableName;
    }


    private static JSONArray sortTreeViewQuestionOptions(JSONArray treeViewOptions) throws
            JSONException {
        JSONArray sortedTree = new JSONArray();

        HashMap<String, JSONObject> sortMap = new HashMap<>();
        for (int i = 0; i < treeViewOptions.length(); i++) {
            sortMap.put(treeViewOptions.getJSONObject(i).getString("name"), treeViewOptions.getJSONObject(i));
        }

        ArrayList<String> sortedKeys = new ArrayList<>(sortMap.keySet());
        Collections.sort(sortedKeys);

        for (String curOptionName : sortedKeys) {
            JSONObject curOption = sortMap.get(curOptionName);
            if (curOption.has("nodes")) {
                curOption.put("nodes", sortTreeViewQuestionOptions(curOption.getJSONArray("nodes")));
            }

            sortedTree.put(curOption);
        }

        return sortedTree;
    }


    private static void updateFormDetailsForEdit(Map<String, String> childDetails, JSONArray jsonArray, List<String> nonEditableFields)
            throws JSONException {
        String prefix;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            prefix = getPrefix(jsonObject);

            if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(Constants.KEY.PHOTO)) {
                processPhoto(childDetails.get(Constants.KEY.BASE_ENTITY_ID), jsonObject);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(Constants.JSON_FORM_KEY.DOB_UNKNOWN)) {
                getDobUnknown(childDetails, jsonObject);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(Constants.JSON_FORM_KEY.AGE)) {
                processAge(Utils.getValue(childDetails, Constants.JSON_FORM_KEY.DOB, false), jsonObject);
            } else if (jsonObject.getString(JsonFormConstants.TYPE).equalsIgnoreCase(JsonFormConstants.DATE_PICKER)) {
                processDate(childDetails, prefix, jsonObject);
            } else if (jsonObject.getString(JsonFormUtils.OPENMRS_ENTITY).equalsIgnoreCase(JsonFormUtils.PERSON_INDENTIFIER)) {
                jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(childDetails,
                        jsonObject.getString(JsonFormUtils.OPENMRS_ENTITY_ID).toLowerCase(), true).replace("-", ""));
            } else if (jsonObject.getString(JsonFormUtils.OPENMRS_ENTITY).equalsIgnoreCase(JsonFormUtils.CONCEPT)) {
                jsonObject.put(JsonFormUtils.VALUE,
                        getMappedValue(jsonObject.getString(JsonFormUtils.KEY), childDetails));
            } else {
                jsonObject.put(JsonFormUtils.VALUE,
                        getMappedValue(prefix + jsonObject.getString(JsonFormUtils.OPENMRS_ENTITY_ID),
                                childDetails));
            }

            processLocationTree(childDetails, nonEditableFields, jsonObject);

            if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(KipConstants.KEY.MIDDLE_NAME)) {
                String middleName = Utils.getValue(childDetails, KipConstants.KEY.MIDDLE_NAME, true);
                jsonObject.put(JsonFormUtils.VALUE, middleName);
            }
            if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(KipConstants.KEY.MOTHER_NRC_NUMBER)) {
                String nidNumber = Utils.getValue(childDetails, KipConstants.KEY.MOTHER_NRC_NUMBER, true);
                jsonObject.put(JsonFormUtils.VALUE, nidNumber);
            }
            if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(KipConstants.KEY.MOTHER_SECOND_PHONE_NUMBER)) {
                String secondaryNumber = Utils.getValue(childDetails, KipConstants.KEY.MOTHER_SECOND_PHONE_NUMBER, true);
                jsonObject.put(JsonFormUtils.VALUE, secondaryNumber);
            }
        }
    }

    private static void getDobUnknown(Map<String, String> childDetails, JSONObject jsonObject) throws JSONException {
        JSONObject optionsObject = jsonObject.getJSONArray(Constants.JSON_FORM_KEY.OPTIONS).getJSONObject(0);
        optionsObject.put(JsonFormUtils.VALUE,
                Utils.getValue(childDetails, Constants.JSON_FORM_KEY.DOB_UNKNOWN, false));
    }

    @NotNull
    private static String getPrefix(JSONObject jsonObject) throws JSONException {
        String prefix;
        prefix = jsonObject.has(JsonFormUtils.ENTITY_ID) && jsonObject.getString(JsonFormUtils.ENTITY_ID)
                .equalsIgnoreCase(KipConstants.KEY.MOTHER) ? KipConstants.KEY.MOTHER_ : "";
        return prefix;
    }

    private static void processLocationTree(Map<String, String> childDetails, List<String> nonEditableFields, JSONObject jsonObject) throws JSONException {
        updateBirthFacilityHierarchy(childDetails, jsonObject);
        if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(KipConstants.KEY.BIRTH_FACILITY_NAME_OTHER)) {
            jsonObject
                    .put(JsonFormUtils.VALUE, Utils.getValue(childDetails,
                            KipConstants.KEY.BIRTH_FACILITY_NAME_OTHER, false));
            jsonObject.put(JsonFormUtils.READ_ONLY, true);
        }
        updateResidentialAreaHierarchy(childDetails, jsonObject);
        updateHomeFacilityHierarchy(childDetails, jsonObject);
        addNonEditableFields(nonEditableFields, jsonObject);
    }

    private static void updateBirthFacilityHierarchy(Map<String, String> childDetails, JSONObject jsonObject) throws JSONException {
        if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(KipConstants.KEY.BIRTH_FACILITY_NAME)) {
            jsonObject.put(JsonFormUtils.READ_ONLY, true);
            List<String> birthFacilityHierarchy = null;
            String birthFacilityName = Utils.getValue(childDetails, KipConstants.KEY.BIRTH_FACILITY_NAME, false);
            if (birthFacilityName != null) {
                if (birthFacilityName.equalsIgnoreCase(KipConstants.KEY.OTHER)) {
                    birthFacilityHierarchy = new ArrayList<>();
                    birthFacilityHierarchy.add(birthFacilityName);
                } else {
                    birthFacilityHierarchy = LocationHelper.getInstance()
                            .getOpenMrsLocationHierarchy(birthFacilityName, true);
                }
            }

            String birthFacilityHierarchyString = AssetHandler
                    .javaToJsonString(birthFacilityHierarchy, new TypeToken<List<String>>() {
                    }.getType());
            if (StringUtils.isNotBlank(birthFacilityHierarchyString)) {
                jsonObject.put(JsonFormUtils.VALUE, birthFacilityHierarchyString);
            }
        }
    }

    private static void updateResidentialAreaHierarchy(Map<String, String> childDetails, JSONObject jsonObject) throws JSONException {
        if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(KipConstants.KEY.RESIDENTIAL_AREA)) {
            List<String> residentialAreaHierarchy;
            String address3 = Utils.getValue(childDetails, KipConstants.KEY.ADDRESS_3, false);
            if (address3 != null && address3.equalsIgnoreCase(KipConstants.KEY.OTHER)) {
                residentialAreaHierarchy = new ArrayList<>();
                residentialAreaHierarchy.add(address3);
            } else {
                residentialAreaHierarchy = LocationHelper.getInstance()
                        .getOpenMrsLocationHierarchy(address3, true);
            }

            String residentialAreaHierarchyString = AssetHandler
                    .javaToJsonString(residentialAreaHierarchy, new TypeToken<List<String>>() {
                    }.getType());
            if (StringUtils.isNotBlank(residentialAreaHierarchyString)) {
                jsonObject.put(JsonFormUtils.VALUE, residentialAreaHierarchyString);
            }
        }
    }

    private static void updateHomeFacilityHierarchy(Map<String, String> childDetails, JSONObject jsonObject) throws JSONException {
        if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(KipConstants.KEY.HOME_FACILITY)) {
            List<String> homeFacilityHierarchy = LocationHelper.getInstance()
                    .getOpenMrsLocationHierarchy(Utils.getValue(childDetails,
                            KipConstants.KEY.HOME_FACILITY, false), true);
            String homeFacilityHierarchyString = AssetHandler
                    .javaToJsonString(homeFacilityHierarchy, new TypeToken<List<String>>() {
                    }.getType());
            if (StringUtils.isNotBlank(homeFacilityHierarchyString)) {
                jsonObject.put(JsonFormUtils.VALUE, homeFacilityHierarchyString);
            }
        }
    }

    private static void addNonEditableFields(List<String> nonEditableFields, JSONObject jsonObject) throws JSONException {
        jsonObject.put(JsonFormUtils.READ_ONLY,
                nonEditableFields.contains(jsonObject.getString(JsonFormUtils.KEY)));
    }

    private static void updateRegistrationEventType(JSONObject form) throws JSONException {
        if (form.has(JsonFormUtils.ENCOUNTER_TYPE) && form.getString(JsonFormUtils.ENCOUNTER_TYPE)
                .equals(Constants.EventType.BITRH_REGISTRATION)) {
            form.put(JsonFormUtils.ENCOUNTER_TYPE, Constants.EventType.UPDATE_BITRH_REGISTRATION);
        }

        if (form.has(JsonFormUtils.STEP1) && form.getJSONObject(JsonFormUtils.STEP1).has(KipConstants.KEY.TITLE) && form.getJSONObject(JsonFormUtils.STEP1).getString(KipConstants.KEY.TITLE)
                .equals(Constants.EventType.BITRH_REGISTRATION)) {
            form.getJSONObject(JsonFormUtils.STEP1).put(KipConstants.KEY.TITLE, Constants.EventType.UPDATE_BITRH_REGISTRATION);
        }
    }

    public static String getJsonString(JSONObject jsonObject, String field) {
        try {
            if (jsonObject != null && jsonObject.has(field)) {
                String string = jsonObject.getString(field);
                if (StringUtils.isBlank(string)) {
                    return "";
                }

                return string;
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        return "";
    }

    public static JSONObject getJsonObject(JSONObject jsonObject, String field) {
        try {
            if (jsonObject != null && jsonObject.has(field)) {
                return jsonObject.getJSONObject(field);
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        return null;
    }


    public void addRelationshipTypesQuestions(JSONObject form) {
        String UNIVERSAL_OPENMRS_RELATIONSHIP_TYPE_UUID = "8d91a210-c2cc-11de-8d13-0010c6dffd0f";

        try {
            JSONArray questions = form.getJSONObject("step1").getJSONArray("fields");
            JSONArray relationshipTypes = new JSONObject(AllSettings.fetchRelationshipTypes() ).getJSONArray("relationshipTypes");

            for (int i = 0; i < questions.length(); i++) {
                if (questions.getJSONObject(i).getString("key").equals("Mother_Guardian_Relationship")
                        || questions.getJSONObject(i).getString("key").equals("Father_Guardian_Relationship")) {

                    JSONArray values = new JSONArray();
                    String value = "";

                    if (relationshipTypes != null && relationshipTypes.length() > 0) {
                        for (int n = 0; n < relationshipTypes.length(); n++) {
                            JSONObject rType = new JSONObject(relationshipTypes.getString(n));
                            values.put(rType.getString("name"));
                            if (rType.has("key") && rType.get("key").equals(UNIVERSAL_OPENMRS_RELATIONSHIP_TYPE_UUID)) {
                                value = rType.getString("name");
                            }
                        }
                    }

                    questions.getJSONObject(i).remove(JsonFormUtils.VALUES);
                    questions.getJSONObject(i).put(JsonFormUtils.VALUES, values);
                    // Set the default relationship type.
                    questions.getJSONObject(i).remove(JsonFormUtils.VALUE);
                    questions.getJSONObject(i).put(JsonFormUtils.VALUE, value);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }
}
