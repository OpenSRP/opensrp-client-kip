package org.smartregister.kip.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.viewpager.widget.ViewPager;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.domain.Setting;
import org.smartregister.domain.SyncStatus;
import org.smartregister.kip.R;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.contract.KipOpdProfileActivityContract;
import org.smartregister.kip.fragment.KipOpdProfileOverviewFragment;
import org.smartregister.kip.fragment.KipOpdProfileVisitsFragment;
import org.smartregister.kip.presenter.KipOpdProfileActivityPresenter;
import org.smartregister.kip.repository.KipOpdDetailsRepository;
import org.smartregister.kip.util.KipChildUtils;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.kip.util.KipJsonFormUtils;
import org.smartregister.kip.util.KipOpdConstants;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.opd.activity.BaseOpdProfileActivity;
import org.smartregister.opd.adapter.ViewPagerAdapter;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdJsonFormUtils;
import org.smartregister.opd.utils.OpdUtils;
import org.smartregister.repository.AllSettings;
import org.smartregister.util.FormUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

public class KipOpdProfileActivity extends BaseOpdProfileActivity implements KipOpdProfileActivityContract.View {
    KipJsonFormUtils kipJsonFormUtils = new KipJsonFormUtils();
    String vaccineDispenseForm = "";

    @Override
    protected void initializePresenter() {
        presenter = new KipOpdProfileActivityPresenter(this);
    }

    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        KipOpdProfileOverviewFragment profileOverviewFragment = KipOpdProfileOverviewFragment.newInstance(this.getIntent().getExtras());
        setSendActionListenerForProfileOverview(profileOverviewFragment);

        KipOpdProfileVisitsFragment profileVisitsFragment = KipOpdProfileVisitsFragment.newInstance(this.getIntent().getExtras());
        setSendActionListenerToVisitsFragment(profileVisitsFragment);

        adapter.addFragment(profileOverviewFragment, this.getString(org.smartregister.opd.R.string.overview));
        adapter.addFragment(profileVisitsFragment, this.getString(org.smartregister.opd.R.string.visits));

        viewPager.setAdapter(adapter);
        return viewPager;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OpdJsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(OpdConstants.JSON_FORM_EXTRA.JSON);
                Timber.d("JSON-Result : %s", jsonString);

                JSONObject form = new JSONObject(jsonString);
                String encounterType = form.getString(OpdJsonFormUtils.ENCOUNTER_TYPE);

                switch (encounterType) {
                    case KipConstants.EventType.OPD_CALCULATE_RISK_FACTOR:
                        showProgressDialog(R.string.saving_dialog_title);
                        int riskFactor = kipJsonFormUtils.calculateCovid19RiskFactor(data);
                        saveRiskFactor(riskFactor);
                        ((KipOpdProfileActivityPresenter) presenter).saveCalculateRiskFactorForm(encounterType, data);
                        if (riskFactor <= 1) {
                            ((KipOpdProfileActivityPresenter) presenter).saveCovid19WaitingListForm(KipConstants.EventType.OPD_COVID19_WAITING_LIST, data, getClient(), riskFactor);
                        }
                        onResumption();
                        break;
                    case KipConstants.EventType.OPD_VACCINATION_ELIGIBILITY_CHECK:
                        showProgressDialog(R.string.saving_dialog_title);
                        ((KipOpdProfileActivityPresenter) presenter).saveCovid19EligibilityForm(encounterType, data);
                        onResumption();
                        break;
                    case KipConstants.EventType.OPD_COVID_19_VACCINE_ADMINISTRATION:
                        showProgressDialog(R.string.saving_dialog_title);
                        ((KipOpdProfileActivityPresenter) presenter).saveCovid19VaccineRecord(encounterType, data);
                        updateSettings(jsonString);
                        sendReminderSms();
                        onResumption();

                        break;
                    case KipConstants.EventType.OPD_SMS_REMINDER:
                        showProgressDialog(R.string.saving_dialog_title);
                        ((KipOpdProfileActivityPresenter) presenter).saveOpdSMSReminderForm(encounterType, data);
                        onResumption();
                        break;
                    case KipConstants.EventType.COVID_AEFI:
                        showProgressDialog(R.string.saving_dialog_title);
                        ((KipOpdProfileActivityPresenter) presenter).saveCovidAefiForm(encounterType, data);
                        onResumption();
                        break;
                    case KipConstants.EventType.OPD_COVID19_VACCINE_STOCK:
                        showProgressDialog(R.string.saving_dialog_title);
                        ((KipOpdProfileActivityPresenter) presenter).saveCovidVaccineStockForm(encounterType, data);
                        updateSettings(jsonString);
                        onResumption();
                        break;
                    case KipConstants.EventType.OPD_INFLUENZA_MEDIAL_CONDITION:
                        showProgressDialog(R.string.saving_dialog_title);
                        saveOpdMedicalCheck();
                        ((KipOpdProfileActivityPresenter) presenter).saveInfluenzaMedicalConditionForm(encounterType, data);
                        onResumption();
                        break;
                    case KipConstants.EventType.OPD_INFLUENZA_VACCINE_ADMINISTRATION:
                        showProgressDialog(R.string.saving_dialog_title);
                        ((KipOpdProfileActivityPresenter) presenter).saveInfluenzaVaccineAdministrationForm(encounterType, data);
                        onResumption();
                        break;
                    case KipConstants.EventType.INFLUENZA_VACCINE_AEFI:
                        showProgressDialog(R.string.saving_dialog_title);
                        ((KipOpdProfileActivityPresenter) presenter).saveInfluenzaAefiForm(encounterType, data);
                        onResumption();
                        break;
                    default:
                        break;
                }

            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateSettings(String jsonForm) {
        try {
            JSONObject settingJsonObject = new JSONObject();
            Setting covid19VaccineStockSettings = getCovid19Settings();
            JSONObject form = new JSONObject(jsonForm);
            JSONArray fields = KipJsonFormUtils.getMultiStepFormFields(form);
            JSONObject vaccineField = KipJsonFormUtils.getFieldJSONObject(fields, "covid19_antigens");
            if (vaccineField != null && vaccineField.has(KipConstants.VALUE)) {
                String value = vaccineField.optString(KipConstants.VALUE);
                if (StringUtils.isNoneBlank(value)) {
                    settingJsonObject = new JSONObject(covid19VaccineStockSettings.getValue());
                    JSONArray settingsMetadata = settingJsonObject.getJSONArray("settings");
                    for (int i = 0; i < settingsMetadata.length(); i++) {
                        JSONObject metadata = settingsMetadata.getJSONObject(i);
                        if (metadata != null && metadata.has(KipConstants.KeyUtils.KEY)) {
                            String settingsKey = metadata.getString(KipConstants.KeyUtils.KEY);
                            if (value.toLowerCase().equalsIgnoreCase("az_oxford") && settingsKey.equalsIgnoreCase(KipConstants.AZ_OXFORD_VIALS_AMOUNT)) {
                                setValues(metadata);
                                break;
                            } else if (value.toLowerCase().equalsIgnoreCase("sinopharm") && settingsKey.equalsIgnoreCase(KipConstants.SINOPHARM_VIALS_AMOUNT)) {
                                setValues(metadata);
                                break;
                            } else if (value.toLowerCase().equalsIgnoreCase("sinovac") && settingsKey.equalsIgnoreCase(KipConstants.SINOVAC_VIALS_AMOUNT)) {
                                setValues(metadata);
                                break;
                            }else if (value.toLowerCase().equalsIgnoreCase("pfizer") && settingsKey.equalsIgnoreCase(KipConstants.PFIZER_VIALS_AMOUNT)) {
                                setValues(metadata);
                                break;
                            } else if (value.toLowerCase().equalsIgnoreCase("moderna") && settingsKey.equalsIgnoreCase(KipConstants.MODERNA_VIALS_AMOUNT)) {
                                setValues(metadata);
                                break;
                            } else if (value.toLowerCase().equalsIgnoreCase("johnson_and_johnson") && settingsKey.equalsIgnoreCase(KipConstants.JOHNSON_AND_JOHNSON_VIALS_AMOUNT)) {
                                setValues(metadata);
                                break;
                            }
                        }
                    }
                }
            }

            covid19VaccineStockSettings.setValue(settingJsonObject.toString());
            covid19VaccineStockSettings.setKey(KipConstants.Settings.VACCINE_STOCK_IDENTIFIER);
            covid19VaccineStockSettings.setSyncStatus(SyncStatus.PENDING.name());
            getAllSettingsRepo().putSetting(covid19VaccineStockSettings);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    private void setValues(JSONObject metadata) throws JSONException {
        int settingValue = Integer.parseInt(metadata.getString(KipConstants.KeyUtils.VALUE));
        metadata.put(KipConstants.KeyUtils.VALUE, String.valueOf(settingValue - 1));
    }

    private Setting getCovid19Settings() {
        return getAllSettingsRepo().getSetting(KipConstants.Settings.VACCINE_STOCK_IDENTIFIER);
    }

    protected AllSettings getAllSettingsRepo() {
        return KipApplication.getInstance().getContext().allSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(org.smartregister.opd.R.menu.menu_opd_profile_activity, menu);

        if (KipConstants.RegisterType.OPD.equalsIgnoreCase(getRegisterType())) {
            MenuItem closeMenu = menu.findItem(R.id.opd_menu_item_close_client);
            MenuItem adverseEffect = menu.findItem(R.id.opd_menu_item_adverse_event);
            MenuItem sendSmsReminder = menu.findItem(R.id.opd_menu_item_send_sms);
            MenuItem influenzaAdverseEffect = menu.findItem(R.id.opd_menu_item_influenza_adverse_event);
            if (closeMenu != null) {
                closeMenu.setEnabled(true);
                adverseEffect.setVisible(false);
                sendSmsReminder.setVisible(false);

                toggleViews(adverseEffect, sendSmsReminder, influenzaAdverseEffect);
            }
        }

        return true;
    }

    public int getAge() {
        return Integer.parseInt(((KipOpdProfileActivityPresenter) this.presenter).getAge(getClient()));
    }

    private void toggleViews(MenuItem adverseEffect, MenuItem sendSmsReminder, MenuItem influenzaAdverseEffect) {
        if (checkCovid19Vaccination()) {
            adverseEffect.setEnabled(true);
            adverseEffect.setVisible(true);

        }
        if (checkReminder()) {
            sendSmsReminder.setEnabled(true);
            sendSmsReminder.setVisible(true);
        }

        if (checkInfluenzaVaccination()){
            influenzaAdverseEffect.setEnabled(true);
            influenzaAdverseEffect.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.opd_menu_item_adverse_event) {
            ((KipOpdProfileActivityPresenter) this.presenter).startForm(KipOpdConstants.KipForms.OPD_ADVERSE_EVENT, Objects.requireNonNull(getClient()));
        }
        if (item.getItemId() == R.id.opd_menu_item_send_sms){
            sendReminderSms();
        }
        if (item.getItemId() == R.id.opd_menu_item_influenza_adverse_event) {
            ((KipOpdProfileActivityPresenter) this.presenter).startForm(KipOpdConstants.KipForms.OPD_INFLUENZA_ADVERSE_EVENT, Objects.requireNonNull(getClient()));
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendReminderSms(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
                Map<String, String> details = getClient().getDetails();
                String client = details.get("first_name");
                String messageTxt = formatMessage().trim();
                String phone = phoneNumber().trim();
                sendSmsReminder(messageTxt, phone);
                org.smartregister.child.util.Utils.showToast(this, "Reminder Message Successfully Sent to "+ client);
                ((KipOpdProfileActivityPresenter) this.presenter).startForm(KipOpdConstants.KipForms.OPD_SMS_REMINDER, Objects.requireNonNull(getClient()));
            } else {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 1);
            }
        }
    }

    @Override
    public void startForm(String formName) {
        ((KipOpdProfileActivityPresenter) this.presenter).startForm(formName, Objects.requireNonNull(getClient()));
    }

    private void saveRiskFactor(int riskFactor) {
        Map<String, String> details = getClient().getDetails();
        String baseEntityId = details.get(KipConstants.KEY.ID_LOWER_CASE);
        KipOpdDetailsRepository.updateRiskStatus(baseEntityId, riskFactor);
    }

    private void saveOpdMedicalCheck(){
        Map<String, String> details = getClient().getDetails();
        String baseEntityId = details.get(KipConstants.KEY.ID_LOWER_CASE);
        KipOpdDetailsRepository.updateOpdMedicalCheck(baseEntityId);
    }

    public String getOpdMedicalCheck(){
        Map<String, String> details = getPatientDetails();
        return details.get("opd_medical_check");
    }

    public String getRiskFactor() {
        Map<String, String> details = getPatientDetails();
        return details.get(KipConstants.COVID_19_RISK_FACTOR);
    }

    public boolean checkEligibility() {
        boolean isEligible = false;
        Map<String, String> details = getPatientDetails();
        String eligibility = details.get(KipConstants.COVID_19_VACCINE_ELIGIBILITY);
        if (StringUtils.isNotEmpty(eligibility) && eligibility.equalsIgnoreCase("1")) {
            isEligible = true;
        }
        return isEligible;
    }

    private Map<String, String> getPatientDetails() {
        return OpdUtils.getClientDemographicDetails(getClient().getDetails().get(KipConstants.KEY.ID_LOWER_CASE));
    }

    public boolean checkCovid19Vaccination() {
        boolean isVaccinated = false;
        Map<String, String> details = getPatientDetails();
        String vaccinated = details.get(KipConstants.COVID_19_VACCINE_GIVEN);
        if (StringUtils.isNotEmpty(vaccinated) && vaccinated.equalsIgnoreCase("1")) {
            isVaccinated = true;
        }
        return isVaccinated;
    }

    public boolean checkInfluenzaVaccination() {
        boolean isVaccinated = false;
        Map<String, String> details = getPatientDetails();
        String vaccinated = details.get(KipConstants.INFLUENZA_VACCINE_GIVEN);
        if (StringUtils.isNotEmpty(vaccinated) && vaccinated.equalsIgnoreCase("1")) {
            isVaccinated = true;
        }
        return isVaccinated;
    }

    public String phoneNumber(){
        Map<String, String> details = getClient().getDetails();
        String phoneNum = details.get("phone_number");
        return phoneNum;
    }

    public boolean checkReminder(){
        boolean isEnrolledInSmsMessages = false;
        Map<String, String> details = getClient().getDetails();
        String nextVisit = details.get("covid19_vaccine_next_date");
        String enrolled = details.get("reminders");
        String phonenumber = details.get("phone_number");
        if (enrolled != null && phonenumber != null && nextVisit != null){
            isEnrolledInSmsMessages = true;
        }
        return isEnrolledInSmsMessages;
    }

    @Override
    public String getVaccineDispenseForm() {
        return vaccineDispenseForm;
    }

    public void setVaccineDispenseForm(String vaccineDispenseForm) {
        this.vaccineDispenseForm = vaccineDispenseForm;
    }

    @Override
    public void updateVaccineStockForm(Map<String, String> vaccineStock) {
        try {
            JSONObject form = FormUtils.getInstance(this).getFormJson(KipConstants.JSON_FORM.OPD_COVID19_VACCINE_ADMINISTRATION_FORM);
            JSONArray fields = KipJsonFormUtils.getMultiStepFormFields(form);
            KipJsonFormUtils.appendFormValues(vaccineStock, fields);
            setVaccineDispenseForm(form.toString());
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public boolean getDifferenceInDate(){
        boolean isDateDiffThree = false;
        Map<String, String> details = getClient().getDetails();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        Date todayDate = new Date();
        String thisDate = currentDate.format(todayDate);
        String nextVisitDate = details.get("covid19_vaccine_next_date");

        try {
            Date toDay = currentDate.parse(thisDate);
            Date nextDate = currentDate.parse(nextVisitDate);
            long difference = toDay.getTime() - nextDate.getTime();
            float daysBetween = (difference/(1000*60*24));

            if (daysBetween >=3){
                isDateDiffThree = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return isDateDiffThree;
    }


    public void openCovid19Forms(String form) {
        ((KipOpdProfileActivityPresenter) this.presenter).startForm(form, Objects.requireNonNull(getClient()));
    }

    public void openInfluenzaMedicalConditionForm(){
        ((KipOpdProfileActivityPresenter) this.presenter).startForm(KipConstants.JSON_FORM.OPD_INFLUENZA_MEDICAL_CONDITION, Objects.requireNonNull(getClient()));
    }

    public void openInfluenzaImmunizationForm(){
        ((KipOpdProfileActivityPresenter) this.presenter).startForm(KipConstants.JSON_FORM.OPD_INFLUENZA_VACCINE_ADMINISTRATION, Objects.requireNonNull(getClient()));
    }

    private String formatMessage(){
        Map<String, String> details = getClient().getDetails();
        String firstName = details.get("first_name");
        String facilliTy = LocationHelper.getInstance().getOpenMrsReadableName(KipChildUtils.getCurrentLocality());
        String date = details.get("covid19_vaccine_next_date");
        return "Dear, "+" " + firstName + " "+ KipConstants.TXT_SMS_REMINDER + date + " " + " at  " + facilliTy;
    }

    public void sendSmsReminder(String messageTxt, String phone){
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone,null, messageTxt, null, null);
        } catch (Exception e){
            Timber.e(e, "Message could not be sent");
        }
    }
}
