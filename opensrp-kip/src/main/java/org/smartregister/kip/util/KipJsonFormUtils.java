package org.smartregister.kip.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.reflect.TypeToken;
import com.vijay.jsonwizard.activities.JsonWizardFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.child.ChildLibrary;
import org.smartregister.child.util.JsonFormUtils;
import org.smartregister.child.util.Constants;
import org.smartregister.child.util.Utils;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.Setting;
import org.smartregister.domain.form.FormLocation;
import org.smartregister.kip.activity.EditJsonFormActivity;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.context.AllSettings;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.util.AssetHandler;
import org.smartregister.util.FormUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.login.task.RemoteLoginTask.getOpenSRPContext;

public class KipJsonFormUtils extends JsonFormUtils {
    public static final String CONCEPT = "concept";
    public static final String VALUE = "value";
    public static final String VALUES = "values";
    public static final String FIELDS = "fields";
    public static final String KEY = "key";
    public static final String ENTITY_ID = "entity_id";
    public static final String STEP1 = "step1";
    public static final String SECTIONS = "sections";
    public static final String attributes = "attributes";
    public static final String ENCOUNTER = "encounter";
    public static final String
            AZ_OXFORD_VIALS_AMOUNT = "az_oxford_vials_amount";
    public static final String AZ_OXFORD_VIALS_LOT_NUMBER = "az_oxford_vials_lot_number";
    public static final String AZ_OXFORD_VIALS_EXPIRY = "az_oxford_vials_expiry";
    public static final String SINOPHARM_VIALS_AMOUNT = "sinopharm_vials_amount";
    public static final String SINOPHARM_VIALS_LOT_NUMBER = "sinopharm_vials_lot_number";
    public static final String SINOPHARM_VIALS_EXPIRY = "sinopharm_vials_expiry";
    public static final String SINOVAC_VIALS_AMOUNT = "sinovac_vials_amount";
    public static final String SINOVAC_VIALS_LOT_NUMBER = "sinovac_vials_lot_number";
    public static final String SINOVAC_VIALS_EXPIRY = "sinovac_vials_expiry";

    public static final String MODERNA_VIALS_AMOUNT = "moderna_vials_amount";
    public static final String MODERNA_VIALS_LOT_NUMBER = "moderna_vials_lot_number";
    public static final String MODERNA_VIALS_EXPIRY = "moderna_vials_expiry";

    public static final String PFIZER_VIALS_AMOUNT = "pfizer_vials_amount";
    public static final String PFIZER_VIALS_LOT_NUMBER = "pfizer_vials_lot_number";
    public static final String PFIZER_VIALS_EXPIRY = "pfizer_vials_expiry";

    public KipJsonFormUtils() {
    }

    public static String getMetadataForEditForm(Context context, Map<String, String> childDetails, List<String> nonEditableFields) {
        try {
            JSONObject birthRegistrationForm = FormUtils.getInstance(context)
                    .getFormJson(Utils.metadata().childRegister.formName);
            updateRegistrationEventType(birthRegistrationForm, childDetails);
            JsonFormUtils.addChildRegLocHierarchyQuestions(birthRegistrationForm);
            KipLocationUtility.addChildRegLocHierarchyQuestions(birthRegistrationForm, getOpenSRPContext());
            KipJsonFormUtils.addRelationshipTypesQuestions(birthRegistrationForm);

            if (birthRegistrationForm != null) {
                birthRegistrationForm.put(JsonFormUtils.ENTITY_ID, childDetails.get(Constants.KEY.BASE_ENTITY_ID));
                birthRegistrationForm.put(JsonFormUtils.ENCOUNTER_TYPE, Utils.metadata().childRegister.updateEventType);
                birthRegistrationForm.put(JsonFormUtils.RELATIONAL_ID, childDetails.get(RELATIONAL_ID));
                birthRegistrationForm.put(KipConstants.KEY.FATHER_RELATIONAL_ID, childDetails.get(KipConstants.KEY.FATHER_RELATIONAL_ID));
                birthRegistrationForm.put(JsonFormUtils.CURRENT_ZEIR_ID, Utils.getValue(childDetails, KipConstants.KEY.MALAWI_ID, true).replace("-", ""));
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

    private static void updateFormDetailsForEdit(Map<String, String> childDetails, JSONArray jsonArray, List<String> nonEditableFields)
            throws JSONException {
        String prefix;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            prefix = getPrefix(jsonObject);

            if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(Constants.KEY.PHOTO)) {
                processPhoto(childDetails.get(Constants.KEY.BASE_ENTITY_ID), jsonObject);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("dob_unknown")) {
                getDobUnknown(childDetails, jsonObject);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(Constants.JSON_FORM_KEY.AGE)) {
                processAge(Utils.getValue(childDetails, Constants.KEY.DOB, false), jsonObject);
            } else if (jsonObject.getString(JsonFormConstants.TYPE).equalsIgnoreCase(JsonFormConstants.DATE_PICKER)) {
                processDate(childDetails, prefix, jsonObject);
            } else if (jsonObject.getString(JsonFormUtils.OPENMRS_ENTITY).equalsIgnoreCase(JsonFormUtils.PERSON_INDENTIFIER)) {
                jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(childDetails,
                        jsonObject.getString(JsonFormUtils.OPENMRS_ENTITY_ID).toLowerCase(), true).replace("-", ""));
            } else if (jsonObject.getString(JsonFormUtils.OPENMRS_ENTITY).equalsIgnoreCase(JsonFormUtils.CONCEPT)) {
                jsonObject.put(JsonFormUtils.VALUE,
                        getMappedValue(jsonObject.getString(JsonFormUtils.KEY), childDetails));
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(KipConstants.KEY.MIDDLE_NAME)) {
                String middleName = Utils.getValue(childDetails, KipConstants.KEY.MIDDLE_NAME, true);
                jsonObject.put(JsonFormUtils.VALUE, middleName);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(KipConstants.KEY.FATHER_FIRST_NAME)) {
                String fatherFirstName = Utils.getValue(childDetails, KipConstants.KEY.FATHER_FIRST_NAME, true);
                jsonObject.put(JsonFormUtils.VALUE, fatherFirstName);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(KipConstants.KEY.FATHER_LAST_NAME)) {
                String fatherLastName = Utils.getValue(childDetails, KipConstants.KEY.FATHER_LAST_NAME, true);
                jsonObject.put(JsonFormUtils.VALUE, fatherLastName);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(KipConstants.KEY.FATHER_PHONE)) {
                String fatherPhoneNumber = Utils.getValue(childDetails, "father_phone_number", true);
                jsonObject.put(JsonFormUtils.VALUE, fatherPhoneNumber);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(KipConstants.KEY.MOTHER_NRC_NUMBER)) {
                String nidNumber = Utils.getValue(childDetails, KipConstants.KEY.MOTHER_NRC_NUMBER, true);
                jsonObject.put(JsonFormUtils.VALUE, nidNumber);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(KipConstants.KEY.MOTHER_SECOND_PHONE_NUMBER)) {
                String secondaryNumber = Utils.getValue(childDetails, KipConstants.KEY.MOTHER_SECOND_PHONE_NUMBER, true);
                jsonObject.put(JsonFormUtils.VALUE, secondaryNumber);
            } else if (jsonObject.has(JsonFormConstants.TREE)) {
                processLocationTree(childDetails, jsonObject);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("mother_guardian_first_name")) {
                String secondaryNumber = Utils.getValue(childDetails, KipConstants.KEY.MOTHER_FIRST_NAME, true);
                jsonObject.put(JsonFormUtils.VALUE, secondaryNumber);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("mother_guardian_last_name")) {
                String secondaryNumber = Utils.getValue(childDetails, KipConstants.KEY.MOTHER_LAST_NAME, true);
                jsonObject.put(JsonFormUtils.VALUE, secondaryNumber);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Sex")) {
                jsonObject.put(JsonFormUtils.VALUE,
                        childDetails.get(JsonFormUtils.GENDER));
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Ce_County")) {
                String county = Utils.getValue(childDetails, "stateProvince", true);
                jsonObject.put(JsonFormUtils.VALUE, county);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Ce_Sub_County")) {
                String subcounty = Utils.getValue(childDetails, "countyDistrict", true);
                jsonObject.put(JsonFormUtils.VALUE, subcounty);
            } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Ce_Ward")) {
                String ward = Utils.getValue(childDetails, "cityVillage", true);
                jsonObject.put(JsonFormUtils.VALUE, ward);
            } else {
                jsonObject.put(JsonFormUtils.VALUE,
                        childDetails.get(jsonObject.optString(JsonFormUtils.KEY)));
            }
            jsonObject.put(JsonFormUtils.READ_ONLY, nonEditableFields.contains(jsonObject.getString(JsonFormUtils.KEY)));
        }
    }

    private static void getDobUnknown(Map<String, String> childDetails, JSONObject jsonObject) throws JSONException {
        JSONObject optionsObject = jsonObject.getJSONArray(Constants.JSON_FORM_KEY.OPTIONS).getJSONObject(0);
        optionsObject.put(JsonFormUtils.VALUE,
                Utils.getValue(childDetails, "dob_unknown", false));
    }

    @NotNull
    private static String getPrefix(JSONObject jsonObject) throws JSONException {
        String prefix = "";
        if (jsonObject.has(JsonFormUtils.ENTITY_ID)) {
            String entityId = jsonObject.getString(JsonFormUtils.ENTITY_ID);
            if (!TextUtils.isEmpty(entityId) && entityId.equalsIgnoreCase(Constants.KEY.MOTHER))
                prefix = "mother_";
            else if (!TextUtils.isEmpty(entityId) && entityId.equalsIgnoreCase(Constants.KEY.FATHER))
                prefix = "father_";
        }
        return prefix;
    }

    private static void processLocationTree(Map<String, String> childDetails, JSONObject jsonObject) throws JSONException {
        updateHomeFacilityHierarchy(childDetails, jsonObject);
    }

    public static void tagEventSyncMetadata(Event event) {
        tagSyncMetadata(event);
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
                            KipConstants.KEY.HOME_FACILITY, false), false);
            String homeFacilityHierarchyString = AssetHandler
                    .javaToJsonString(homeFacilityHierarchy, new TypeToken<List<String>>() {
                    }.getType());
            ArrayList<String> allLevels = KipChildUtils.getHealthFacilityLevels();
            List<FormLocation> entireTree = LocationHelper.getInstance().generateLocationHierarchyTree(true, allLevels);
            String entireTreeString = AssetHandler.javaToJsonString(entireTree, new TypeToken<List<FormLocation>>() {
            }.getType());
            if (StringUtils.isNotBlank(homeFacilityHierarchyString)) {
                jsonObject.put(JsonFormUtils.VALUE, homeFacilityHierarchyString);
                jsonObject.put(JsonFormConstants.TREE, new JSONArray(entireTreeString));
            }
        }
    }

    private static void addNonEditableFields(List<String> nonEditableFields, JSONObject jsonObject) throws JSONException {
        jsonObject.put(JsonFormUtils.READ_ONLY,
                nonEditableFields.contains(jsonObject.getString(JsonFormUtils.KEY)));
    }

    private static void updateRegistrationEventType(JSONObject form, Map<String, String> childDetails) throws JSONException {
        if (form.has(JsonFormUtils.ENCOUNTER_TYPE) && form.getString(JsonFormUtils.ENCOUNTER_TYPE)
                .equals(Constants.EventType.BITRH_REGISTRATION)) {
            form.put(JsonFormUtils.ENCOUNTER_TYPE, Constants.EventType.UPDATE_BITRH_REGISTRATION);
        }

        if (form.has(JsonFormUtils.STEP1) && form.getJSONObject(JsonFormUtils.STEP1).has(KipConstants.KEY.TITLE) && form.getJSONObject(JsonFormUtils.STEP1).getString(KipConstants.KEY.TITLE)
                .equals(Constants.EventType.BITRH_REGISTRATION)) {
            form.getJSONObject(JsonFormUtils.STEP1).put(KipConstants.KEY.TITLE, KipConstants.FormTitleUtil.UPDATE_CHILD_FORM);
        }

        //Update father details if it exists or create a new one
        if (form.has(Constants.KEY.FATHER) && childDetails.containsKey(KipConstants.KEY.FATHER_RELATIONAL_ID)
                && childDetails.get(KipConstants.KEY.FATHER_RELATIONAL_ID) != null) {
            form.getJSONObject(Constants.KEY.FATHER).put(ENCOUNTER_TYPE, Constants.EventType.UPDATE_FATHER_DETAILS);
        }
        if (form.has(Constants.KEY.MOTHER)) {
            form.getJSONObject(Constants.KEY.MOTHER).put(ENCOUNTER_TYPE, Constants.EventType.UPDATE_MOTHER_DETAILS);
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

    public static void addRelationshipTypesQuestions(JSONObject form) {
        String UNIVERSAL_OPENMRS_RELATIONSHIP_TYPE_UUID = "8d91a210-c2cc-11de-8d13-0010c6dffd0f";

        try {
            JSONArray questions = form.getJSONObject("step1").getJSONArray("fields");
            JSONArray relationshipTypes = new JSONObject(AllSettings.fetchRelationshipTypes()).getJSONArray("relationshipTypes");

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

    public int calculateCovid19RiskFactor(@NonNull Intent data) {
        int riskFactor = 0;
        JSONObject form = getJsonForm(data);
        JSONArray formFields = KipJsonFormUtils.getMultiStepFormFields(form);
        JSONObject preExistingConditions = KipJsonFormUtils.getFieldJSONObject(formFields, KipConstants.DbConstants.Columns.CalculateRiskFactor.PRE_EXISTING_CONDITIONS);
        JSONObject occupation = KipJsonFormUtils.getFieldJSONObject(formFields, KipConstants.DbConstants.Columns.CalculateRiskFactor.OCCUPATION);
        JSONObject age = KipJsonFormUtils.getFieldJSONObject(formFields, KipConstants.DbConstants.Columns.CalculateRiskFactor.AGE);

        if (age != null && age.has(KipConstants.VALUE) && occupation != null && occupation.has(KipConstants.VALUE) && preExistingConditions != null && preExistingConditions.has(KipConstants.VALUE)) {
            int ageValue = Integer.parseInt(age.optString(KipConstants.VALUE, ""));
            String preExistingConditionsValue = preExistingConditions.optString(KipConstants.VALUE, "[]");
            String occupationValue = occupation.optString(KipConstants.VALUE, "[]");

            boolean riskCheck = !preExistingConditionsValue.contains(KipConstants.NONE) || !occupationValue.equalsIgnoreCase(KipConstants.NONE);
            boolean riskCheckMedium = preExistingConditionsValue.contains(KipConstants.NONE) && occupationValue.equalsIgnoreCase(KipConstants.NONE);
            if (ageValue > 55 && riskCheck) {
                riskFactor = 2;
            } else if (riskFactor != 2 && ((ageValue < 55 && riskCheck) || (ageValue > 55 && riskCheckMedium))) {
                riskFactor = 1;
            }
        }

        return riskFactor;
    }

    @NotNull
    private JSONObject getJsonForm(@NonNull Intent data) {
        JSONObject form = new JSONObject();
        try {
            String jsonString;
            form = new JSONObject();
            if (data != null) {
                jsonString = data.getStringExtra(OpdConstants.JSON_FORM_EXTRA.JSON);
                if (jsonString != null) {
                    form = new JSONObject(jsonString);
                }
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
        return form;
    }


    public static void launchCovid19VaccineStockSettingsForm(Activity activity) {
        try {
            Intent intent = new Intent(activity, JsonWizardFormActivity.class);
            JSONObject form = new com.vijay.jsonwizard.utils.FormUtils().getFormJsonFromRepositoryOrAssets(activity.getApplicationContext(), KipConstants.JSON_FORM.OPD_COVID_19_VACCINE_STOCK_TAKE_FORM);
            if (form != null) {
                form.put(KipConstants.JsonFormKeyUtils.ENTITY_ID,
                        activity.getIntent().getStringExtra(KipConstants.KeyUtils.BASE_ENTITY_ID));
                intent.putExtra(KipConstants.KeyUtils.JSON, form.toString());
                activity.startActivityForResult(intent, REQUEST_CODE_GET_JSON);
            }
        } catch (Exception e) {
            Timber.e(e, "JsonFormUtils --> launchSiteCharacteristicsForm");
        }
    }

    public static Map<String, String> processCovid19VaccineStockSettings(String jsonString) {
        try {
            Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString);
            if (!registrationFormParams.getLeft()) {
                return null;
            }

            Map<String, String> settings = new HashMap<>();
            JSONArray fields = returnCombinedFormValues(KipJsonFormUtils.getMultiStepFormFields(registrationFormParams.getMiddle()));

            for (int i = 0; i < fields.length(); i++) {
                if (!"label".equals(fields.getJSONObject(i).getString(KipConstants.KeyUtils.TYPE))) {
                    settings.put(fields.getJSONObject(i).getString(KipConstants.KeyUtils.KEY), StringUtils.isBlank(fields.getJSONObject(i).getString(KipConstants.KeyUtils.VALUE)) ? "0" : fields.getJSONObject(i).getString(KipConstants.KeyUtils.VALUE));
                }

            }

            return settings;
        } catch (Exception e) {
            Timber.e(e, "JsonFormUtils --> processSiteCharacteristics");
            return null;
        }
    }

    private static JSONArray returnCombinedFormValues(JSONArray fields) {
        JSONArray formFields = new JSONArray();
        if (fields != null) {
            formFields = extractValues(fields);
        }

        return formFields;
    }

    private static JSONArray extractValues(JSONArray fields) {
        StringBuilder azOxford = new StringBuilder();
        StringBuilder sinopharm = new StringBuilder();
        StringBuilder sinovac = new StringBuilder();
        StringBuilder mordana = new StringBuilder();
        StringBuilder pfizer = new StringBuilder();
        try {
            for (int i = 0; i < fields.length(); i++) {
                JSONObject field = fields.getJSONObject(i);
                if (field.has(KipConstants.KeyUtils.KEY)) {
                    String key = field.getString(KipConstants.KeyUtils.KEY);
                    if (key.contains("az_oxford_vials") && field.has(KipConstants.KeyUtils.VALUE)) {
                          azOxford.insert(0, field.optString(KipConstants.KeyUtils.VALUE) + ":");
                    }
                    if (key.contains("sinopharm_vials") && field.has(KipConstants.KeyUtils.VALUE)) {
                        sinopharm.insert(0, field.optString(KipConstants.KeyUtils.VALUE) + ":");
                    }
                    if (key.contains("sinovac_vials") && field.has(KipConstants.KeyUtils.VALUE)) {
                        sinovac.insert(0, field.optString(KipConstants.KeyUtils.VALUE) + ":");
                    }
                    if (key.contains("moderna_vials") && field.has(KipConstants.KeyUtils.VALUE)) {
                        sinovac.insert(0, field.optString(KipConstants.KeyUtils.VALUE) + ":");
                    }
                    if (key.contains("pfizer_vials") && field.has(KipConstants.KeyUtils.VALUE)) {
                        sinovac.insert(0, field.optString(KipConstants.KeyUtils.VALUE) + ":");
                    }
                }
            }
        } catch (JSONException exception) {
            Timber.e(exception);
        }

        return createNewFields(fields, azOxford.toString(), sinopharm.toString(), sinovac.toString(), mordana.toString(), pfizer.toString());
    }

    private static JSONArray createNewFields(JSONArray fields, String azOxford, String sinopharm, String sinovac, String morderna, String pfzer) {
        JSONArray newFormFields = new JSONArray();
        try {
            for (int i = 0; i < fields.length(); i++) {
                JSONObject field = fields.getJSONObject(i);
                if (field.has(KipConstants.KeyUtils.KEY)) {
                    String key = field.getString(KipConstants.KeyUtils.KEY);
                    if (key.equalsIgnoreCase(KipConstants.AZ_OXFORD_VIALS_AMOUNT)) {
                        field.put(KipConstants.KeyUtils.VALUE, azOxford);
                        newFormFields.put(field);
                    }
                    if (key.equalsIgnoreCase(KipConstants.SINOPHARM_VIALS_AMOUNT)) {
                        field.put(KipConstants.KeyUtils.VALUE, sinopharm);
                        newFormFields.put(field);
                    }
                    if (key.equalsIgnoreCase(KipConstants.SINOVAC_VIALS_AMOUNT)) {
                        field.put(KipConstants.KeyUtils.VALUE, sinovac);
                        newFormFields.put(field);
                    }
                    if (key.equalsIgnoreCase(KipConstants.MODERNA_VIALS_AMOUNT)) {
                        field.put(KipConstants.KeyUtils.VALUE, morderna);
                        newFormFields.put(field);
                    }
                    if (key.equalsIgnoreCase(KipConstants.PFIZER_VIALS_AMOUNT)) {
                        field.put(KipConstants.KeyUtils.VALUE, pfzer);
                        newFormFields.put(field);
                    }
                }
            }
        } catch (JSONException exception) {
            Timber.e(exception);
        }

        return newFormFields;
    }

    public static String getAutoPopulatedCovid19VaccineStockSettingsEditFormString(Context context,
                                                                                   Map<String, String> characteristics) {
        try {
            JSONObject form = FormUtils.getInstance(context).getFormJson(KipConstants.JSON_FORM.OPD_COVID_19_VACCINE_STOCK_TAKE_FORM);
            Timber.d("Form is %s", form.toString());
            if (form != null) {
                form.put(ENCOUNTER_TYPE, KipConstants.EventType.OPD_COVID_19_VACCINE_STOCK_TAKE);
                JSONArray jsonArray = KipJsonFormUtils.getMultiStepFormFields(form);

                Setting covid19VaccineStockSettings = getAllSettingsRepo().getSetting(KipConstants.Settings.VACCINE_STOCK_IDENTIFIER);
                if (covid19VaccineStockSettings != null) {
                    appendFormValues(characteristics, jsonArray);
                }

                return form.toString();
            }
        } catch (Exception e) {
            Timber.e(e, "JsonFormUtils --> getAutoPopulatedSiteCharacteristicsEditFormString");
        }

        return "";
    }

    protected static org.smartregister.repository.AllSettings getAllSettingsRepo() {
        return KipApplication.getInstance().getContext().allSettings();
    }

    public static void appendFormValues(Map<String, String> settings, JSONArray fields) {
        try {
            String[] aZOxford = settings.get(KipConstants.AZ_OXFORD_VIALS_AMOUNT).split(":");
            String[] sinopharm = settings.get(KipConstants.SINOPHARM_VIALS_AMOUNT).split(":");
            String[] sinovac = settings.get(KipConstants.SINOVAC_VIALS_AMOUNT).split(":");
            String[] moderna = settings.get(KipConstants.MODERNA_VIALS_AMOUNT).split(":");
            String[] pfizer = settings.get(KipConstants.PFIZER_VIALS_AMOUNT).split(":");

            for (int i = 0; i < fields.length(); i++) {
                JSONObject field = fields.getJSONObject(i);
                if (field.has(KipConstants.KeyUtils.KEY)) {
                    String fieldKey = field.getString(KipConstants.KeyUtils.KEY);
                    if (fieldKey.equalsIgnoreCase(AZ_OXFORD_VIALS_AMOUNT)) {
                        field.put(KipConstants.VALUE, aZOxford[0]);
                    } else if (fieldKey.equalsIgnoreCase(AZ_OXFORD_VIALS_LOT_NUMBER)) {
                        field.put(KipConstants.VALUE, aZOxford[1]);
                    } else if (fieldKey.equalsIgnoreCase(AZ_OXFORD_VIALS_EXPIRY)) {
                        field.put(KipConstants.VALUE, aZOxford[2]);
                    } else if (fieldKey.equalsIgnoreCase(SINOPHARM_VIALS_AMOUNT)) {
                        field.put(KipConstants.VALUE, sinopharm[0]);
                    } else if (fieldKey.equalsIgnoreCase(SINOPHARM_VIALS_LOT_NUMBER)) {
                        field.put(KipConstants.VALUE, sinopharm[1]);
                    } else if (fieldKey.equalsIgnoreCase(SINOPHARM_VIALS_EXPIRY)) {
                        field.put(KipConstants.VALUE, sinopharm[2]);
                    } else if (fieldKey.equalsIgnoreCase(SINOVAC_VIALS_AMOUNT)) {
                        field.put(KipConstants.VALUE, sinovac[0]);
                    } else if (fieldKey.equalsIgnoreCase(SINOVAC_VIALS_LOT_NUMBER)) {
                        field.put(KipConstants.VALUE, sinovac[1]);
                    } else if (fieldKey.equalsIgnoreCase(SINOVAC_VIALS_EXPIRY)) {
                        field.put(KipConstants.VALUE, sinovac[2]);
                    } else if (fieldKey.equalsIgnoreCase(MODERNA_VIALS_AMOUNT)) {
                        field.put(KipConstants.VALUE, moderna[0]);
                    } else if (fieldKey.equalsIgnoreCase(MODERNA_VIALS_LOT_NUMBER)) {
                        field.put(KipConstants.VALUE, moderna[1]);
                    } else if (fieldKey.equalsIgnoreCase(MODERNA_VIALS_EXPIRY)) {
                        field.put(KipConstants.VALUE, moderna[2]);
                    } else if (fieldKey.equalsIgnoreCase(PFIZER_VIALS_AMOUNT)) {
                        field.put(KipConstants.VALUE, pfizer[0]);
                    } else if (fieldKey.equalsIgnoreCase(PFIZER_VIALS_LOT_NUMBER)) {
                        field.put(KipConstants.VALUE, pfizer[1]);
                    } else if (fieldKey.equalsIgnoreCase(PFIZER_VIALS_EXPIRY)) {
                        field.put(KipConstants.VALUE, pfizer[2]);
                    }

                }
            }

        } catch (JSONException exception) {
            Timber.e(exception);
        }

    }

    public static void startFormForEdit(Activity context,
                                        int jsonFormActivityRequestCode, String metaData) {
        Intent intent = new Intent(context, EditJsonFormActivity.class);
        intent.putExtra(KipConstants.KeyUtils.JSON, metaData);
        Timber.d("form is %s", metaData);
        context.startActivityForResult(intent, jsonFormActivityRequestCode);
    }

    public static String[] splitValue(String value) {
        String[] settingValues;
        settingValues = value.split(":");
        return settingValues;
    }
}