package org.smartregister.kip.presenter;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.kip.R;
import org.smartregister.kip.contract.KipOpdProfileActivityContract;
import org.smartregister.kip.domain.KipServerSetting;
import org.smartregister.kip.helper.KipServerSettingHelper;
import org.smartregister.kip.repository.KipOpdDetailsRepository;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.kip.util.KipJsonFormUtils;
import org.smartregister.kip.util.KipLocationUtility;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.contract.OpdProfileActivityContract;
import org.smartregister.opd.interactor.OpdProfileInteractor;
import org.smartregister.opd.model.OpdProfileActivityModel;
import org.smartregister.opd.pojo.OpdMetadata;
import org.smartregister.opd.presenter.OpdProfileActivityPresenter;
import org.smartregister.opd.tasks.FetchRegistrationDataTask;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.opd.utils.OpdJsonFormUtils;
import org.smartregister.opd.utils.OpdUtils;
import org.smartregister.util.Utils;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

public class KipOpdProfileActivityPresenter extends OpdProfileActivityPresenter {
    public static final String ADMINISTRATION_DATE = "administration_date";
    public static final String WAITING_LIST = "waiting_list";
    public static final String ELIGIBILITY = "eligibility";
    private static final String SMS_REMINDER = "opd_sms_reminder";
    public static final String COVID_19_VACCINE_NEXT_NUMBER = "covid19_vaccine_next_number";
    private final OpdProfileActivityModel model;
    private final WeakReference<KipOpdProfileActivityContract.View> mProfileView;
    private final OpdProfileActivityContract.Interactor mProfileInteractor;

    public KipOpdProfileActivityPresenter(KipOpdProfileActivityContract.View profileView) {
        super(profileView);
        mProfileView = new WeakReference<>(profileView);
        mProfileInteractor = new OpdProfileInteractor(this);
        model = new OpdProfileActivityModel();
    }

    @Override
    public void startFormActivity(@Nullable JSONObject form, @NonNull String caseId, @NonNull String entityTable) {
        addAgeToTheForm(form);
        form = updateVaccineDispenseForm(form);
        super.startFormActivity(form, caseId, entityTable);
    }

    @NotNull
    private JSONObject updateVaccineDispenseForm(@Nullable JSONObject form) {
        JSONObject newForm = new JSONObject();
        try {
            getVaccineStock();
            if (form != null && form.has(KipJsonFormUtils.ENCOUNTER_TYPE) && form.getString(KipJsonFormUtils.ENCOUNTER_TYPE).equalsIgnoreCase(KipConstants.EventType.OPD_COVID_19_VACCINE_ADMINISTRATION)) {
                form = new JSONObject(getProfileView().getVaccineDispenseForm());
                addAgeToTheForm(form);
            }
            newForm = form;
        } catch (JSONException exception) {
            Timber.e(exception);
        }
        return newForm;
    }

    @Nullable
    @Override
    public KipOpdProfileActivityContract.View getProfileView() {
        if (mProfileView != null) {
            return mProfileView.get();
        }

        return null;
    }

    @Override
    public void onUpdateRegistrationBtnCLicked(@NonNull String baseEntityId) {
        if (getProfileView() != null) {
            Utils.startAsyncTask(new FetchRegistrationDataTask(new WeakReference<>(getProfileView()), jsonForm -> {
                OpdMetadata metadata = OpdUtils.metadata();

                OpdProfileActivityContract.View profileView = getProfileView();
                if (profileView != null && metadata != null && jsonForm != null) {
                    Context context = profileView.getContext();
                    Intent intent = new Intent(context, metadata.getOpdFormActivity());
                    Form formParam = new Form();
                    formParam.setWizard(false);
                    formParam.setHideSaveLabel(true);
                    formParam.setNextLabel("");
                    JSONObject jsonFormObject = null;
                    try {
                        jsonFormObject = new JSONObject(jsonForm);
                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }

                    String encounterType = jsonFormObject.optString(OpdJsonFormUtils.ENCOUNTER_TYPE);
                    if (encounterType.equals(OpdConstants.EventType.UPDATE_OPD_REGISTRATION)) {
                        updateRegistrationDetails(jsonFormObject);
                        org.smartregister.Context openSRPContext = org.smartregister.login.task.RemoteLoginTask.getOpenSRPContext();
                        KipLocationUtility.addChildRegLocHierarchyQuestions(jsonFormObject, openSRPContext);
                    }

                    intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, formParam);
                    intent.putExtra(JsonFormConstants.JSON_FORM_KEY.JSON, jsonFormObject.toString());
                    profileView.startActivityForResult(intent, OpdJsonFormUtils.REQUEST_CODE_GET_JSON);
                }
            }), new String[]{baseEntityId});
        }
    }

    public JSONObject updateRegistrationDetails(@NonNull JSONObject form) {
        JSONObject jsonForm = new JSONObject();
        CommonPersonObjectClient client = getProfileView().getClient();
        Timber.i(client.toString());
        return jsonForm;
    }

    private void addAgeToTheForm(@Nullable JSONObject form) {
        try {
            if (form != null && form.has(KipConstants.KEY.ENCOUNTER_TYPE)) {
                JSONArray formFields = OpdJsonFormUtils.getMultiStepFormFields(form);
                JSONObject ageJsonObject = OpdJsonFormUtils.getFieldJSONObject(formFields, KipConstants.AGE);
                if (ageJsonObject != null) {
                    CommonPersonObjectClient commonPersonObjectClient = Objects.requireNonNull(getProfileView()).getClient();
                    if (commonPersonObjectClient != null) {
                        ageJsonObject.put(KipConstants.VALUE, getAge(commonPersonObjectClient));

                    }
                }
            }
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }

    public String getAge(CommonPersonObjectClient commonPersonObjectClient) {
        Map<String, String> details = commonPersonObjectClient.getDetails();
        String dateOfBirth = details.get(OpdConstants.KEY.DOB);
        String translatedYearInitial = getProfileView().getString(R.string.abbrv_years);
        return OpdUtils.getClientAge(Utils.getDuration(dateOfBirth), Objects.requireNonNull(translatedYearInitial));
    }


    public void saveCalculateRiskFactorForm(@NonNull String eventType, @Nullable Intent data) {
        String jsonString = null;
        if (data != null) {
            jsonString = data.getStringExtra(OpdConstants.JSON_FORM_EXTRA.JSON);
        }

        if (jsonString == null) {
            return;
        }

        if (eventType.equals(KipConstants.EventType.OPD_CALCULATE_RISK_FACTOR)) {
            try {
                List<Event> opdCalculateRiskFactorEvent = OpdLibrary.getInstance().processOpdForm(jsonString, data);
                mProfileInteractor.saveEvents(opdCalculateRiskFactorEvent, this);

            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        onOpdEventSaved();
    }

    public void saveCovid19EligibilityForm(@NonNull String eventType, @Nullable Intent data) {
        String jsonString = null;
        if (data != null) {
            jsonString = data.getStringExtra(OpdConstants.JSON_FORM_EXTRA.JSON);
        }

        if (jsonString == null) {
            return;
        }
        List<Event> opdCovid19EligibilityEvent = null;
        if (eventType.equals(KipConstants.EventType.OPD_VACCINATION_ELIGIBILITY_CHECK)) {
            try {
                opdCovid19EligibilityEvent = OpdLibrary.getInstance().processOpdForm(jsonString, data);
                mProfileInteractor.saveEvents(opdCovid19EligibilityEvent, this);
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        KipOpdDetailsRepository.updateCovid19Eligibility(opdCovid19EligibilityEvent.get(0).getBaseEntityId(), getEligibility(jsonString));
        onOpdEventSaved();
    }

    public void saveCovid19VaccineRecord(@NonNull String eventType, @Nullable Intent data) {
        String jsonString = null;
        if (data != null) {
            jsonString = data.getStringExtra(OpdConstants.JSON_FORM_EXTRA.JSON);
        }

        if (jsonString == null) {
            return;
        }
        List<Event> opdCovid19EligibilityEvent = null;
        if (eventType.equals(KipConstants.EventType.OPD_COVID_19_VACCINE_ADMINISTRATION)) {
            try {
                opdCovid19EligibilityEvent = OpdLibrary.getInstance().processOpdForm(jsonString, data);
                mProfileInteractor.saveEvents(opdCovid19EligibilityEvent, this);
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
        String baseEntityId = opdCovid19EligibilityEvent.get(0).getBaseEntityId();

        KipOpdDetailsRepository.updateCovid19VaccineDispense(baseEntityId);
        KipOpdDetailsRepository.updateCovid19NextVaccineDetails(baseEntityId, calculateNextVaccineDate(jsonString), updateNextNumber(baseEntityId));
        KipOpdDetailsRepository.resetCovid19VaccineSchedule(baseEntityId);
        onOpdEventSaved();
    }

    private String updateNextNumber(String baseEntityId) {
        String number = "";
        if (StringUtils.isNoneBlank(baseEntityId)) {
            Map<String, String> details = OpdUtils.getClientDemographicDetails(baseEntityId);
            String nextNumber = details.get(COVID_19_VACCINE_NEXT_NUMBER);
            if (StringUtils.isBlank(nextNumber) || nextNumber.equalsIgnoreCase("1")) {
                number = "2";
            }
        }
        return number;
    }

    private String calculateNextVaccineDate(@NonNull String form) {
        String date = "";
        try {
            JSONObject jsonObject = new JSONObject(form);
            JSONArray fields = KipJsonFormUtils.getSingleStepFormfields(jsonObject);
            JSONObject administrationDate = KipJsonFormUtils.getFieldJSONObject(fields, ADMINISTRATION_DATE);
            String value = administrationDate.optString(KipConstants.VALUE, "");

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            Date adminDate = simpleDateFormat.parse(value);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(adminDate);
            calendar.add(Calendar.DAY_OF_MONTH, 28);
            adminDate = calendar.getTime();

            date = OpdUtils.convertDate(adminDate, OpdDbConstants.DATE_FORMAT);
        } catch (JSONException | ParseException exception) {
            Timber.e(exception);
        }
        return date;
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

    public void saveCovid19WaitingListForm(@NonNull String eventType, @Nullable Intent data, @NonNull CommonPersonObjectClient commonPersonObjectClient, int riskFactor) {
        String jsonString = loadForm(KipConstants.JSON_FORM.OPD_COVID19_WAITING_LIST_FORM, commonPersonObjectClient);

        if (jsonString == null) {
            return;
        }

        JSONObject form = new JSONObject();
        try {
            form = new JSONObject(jsonString);
            JSONArray fields = KipJsonFormUtils.getSingleStepFormfields(form);
            JSONObject waitingList = KipJsonFormUtils.getFieldJSONObject(fields, WAITING_LIST);
            waitingList.put(KipConstants.VALUE, riskFactor);
            addAgeToTheForm(form);
        } catch (JSONException e) {
            Timber.e(e);
        }

        List<Event> opdCovid19WaitingListEvent = null;
        if (eventType.equals(KipConstants.EventType.OPD_COVID19_WAITING_LIST)) {
            try {

                opdCovid19WaitingListEvent = OpdLibrary.getInstance().processOpdForm(form.toString(), data);
                mProfileInteractor.saveEvents(opdCovid19WaitingListEvent, this);
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        KipOpdDetailsRepository.updateCovid19WaitingList(opdCovid19WaitingListEvent.get(0).getBaseEntityId(), String.valueOf(riskFactor));
        onOpdEventSaved();
    }

    public void saveOpdSMSReminderForm(@NonNull String eventType, @Nullable Intent data) {
        String jsonString = null;
        if (data != null) {
            jsonString = data.getStringExtra(OpdConstants.JSON_FORM_EXTRA.JSON);
        }

        if (jsonString == null) {
            return;
        }

        List<Event> opdSmsReminder = null;
        if (eventType.equals(KipConstants.EventType.OPD_SMS_REMINDER)) {
            try {
                opdSmsReminder = OpdLibrary.getInstance().processOpdForm(jsonString, data);
                mProfileInteractor.saveEvents(opdSmsReminder, this);
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        KipOpdDetailsRepository.updateSmsReminder(opdSmsReminder.get(0).getBaseEntityId());
    }

    public void saveCovidAefiForm(@NonNull String eventType, @Nullable Intent data) {
        String jsonString = null;
        if (data != null) {
            jsonString = data.getStringExtra(OpdConstants.JSON_FORM_EXTRA.JSON);
        }

        if (jsonString == null) {
            return;
        }

        if (eventType.equals(KipConstants.EventType.COVID_AEFI)) {
            try {
                List<Event> covidEafi = OpdLibrary.getInstance().processOpdForm(jsonString, data);
                mProfileInteractor.saveEvents(covidEafi, this);
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

    }

    public void saveInfluenzaAefiForm(@NonNull String eventType, @Nullable Intent data) {
        String jsonString = null;
        if (data != null) {
            jsonString = data.getStringExtra(OpdConstants.JSON_FORM_EXTRA.JSON);
        }

        if (jsonString == null) {
            return;
        }

        if (eventType.equals(KipConstants.EventType.INFLUENZA_VACCINE_AEFI)) {
            try {
                List<Event> influenzaAefi = OpdLibrary.getInstance().processOpdForm(jsonString, data);
                mProfileInteractor.saveEvents(influenzaAefi, this);
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

    }

    public void saveInfluenzaMedicalConditionForm(@NonNull String eventType, @Nullable Intent data) {
        String jsonString = null;
        if (data != null) {
            jsonString = data.getStringExtra(OpdConstants.JSON_FORM_EXTRA.JSON);
        }

        if (jsonString == null) {
            return;
        }

        if (eventType.equals(KipConstants.EventType.OPD_INFLUENZA_MEDIAL_CONDITION)) {
            try {
                List<Event> influenzaMedicalConditionEvent = OpdLibrary.getInstance().processOpdForm(jsonString, data);
                mProfileInteractor.saveEvents(influenzaMedicalConditionEvent, this);
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
        onOpdEventSaved();
    }

    public void saveInfluenzaVaccineAdministrationForm(@NonNull String eventType, @Nullable Intent data) {
        String jsonString = null;
        if (data != null) {
            jsonString = data.getStringExtra(OpdConstants.JSON_FORM_EXTRA.JSON);
        }

        if (jsonString == null) {
            return;
        }


        List<Event> influenzaVaccinationAdministrationEvent = null;
        if (eventType.equals(KipConstants.EventType.OPD_INFLUENZA_VACCINE_ADMINISTRATION)) {
            try {
                influenzaVaccinationAdministrationEvent = OpdLibrary.getInstance().processOpdForm(jsonString, data);
                mProfileInteractor.saveEvents(influenzaVaccinationAdministrationEvent, this);
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        String baseEntityId = influenzaVaccinationAdministrationEvent.get(0).getBaseEntityId();
        KipOpdDetailsRepository.restInfluenzaSchedule(baseEntityId);
        KipOpdDetailsRepository.updateInfluenzaVaccineStatus(baseEntityId);
        onOpdEventSaved();

    }

    public void saveCovidVaccineStockForm(@NonNull String eventType, @Nullable Intent data) {
        String jsonString = null;
        if (data != null) {
            jsonString = data.getStringExtra(OpdConstants.JSON_FORM_EXTRA.JSON);
        }

        if (jsonString == null) {
            return;
        }

        if (eventType.equals(KipConstants.EventType.OPD_COVID19_VACCINE_STOCK)) {
            try {
                List<Event> covidEafi = OpdLibrary.getInstance().processOpdForm(jsonString, data);
                mProfileInteractor.saveEvents(covidEafi, this);
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

    }

    public String loadForm(@NonNull String formName, @NonNull CommonPersonObjectClient commonPersonObjectClient) {
        String form = "";
        try {
            HashMap<String, String> injectedValues = this.getInjectedFields(formName, commonPersonObjectClient.getCaseId());
            String locationId = OpdUtils.context().allSharedPreferences().getPreference("CURRENT_LOCATION_ID");
            form = String.valueOf(model.getFormAsJson(formName, commonPersonObjectClient.getCaseId(), locationId, injectedValues));
        } catch (JSONException exception) {
            Timber.e(exception);
        }
        return form;
    }

    public void getVaccineStock() {
        Map<String, String> settings = getSettingsMapByType();
        getProfileView().updateVaccineStockForm(settings);
    }

    protected Map<String, String> getSettingsMapByType() {
        List<KipServerSetting> serverSettings = KipServerSettingHelper.fetchServerSettingsByTypeKey(KipConstants.Settings.VACCINE_STOCK_IDENTIFIER);

        Map<String, String> settingsMap = new HashMap<>();
        for (KipServerSetting serverSetting : serverSettings) {
            settingsMap.put(serverSetting.getKey(), serverSetting.getValue() + ":" + serverSetting.getDescription());
        }
        return settingsMap;
    }

    @Override
    public void onOpdEventSaved() {
        KipOpdProfileActivityContract.View view = getProfileView();
        if (view != null) {
            view.getActionListenerForProfileOverview().onActionReceive();
            view.getActionListenerForVisitFragment().onActionReceive();
            view.hideProgressDialog();
        }
    }
}
