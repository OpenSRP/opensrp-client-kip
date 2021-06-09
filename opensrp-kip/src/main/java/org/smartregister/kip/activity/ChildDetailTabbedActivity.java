package org.smartregister.kip.activity;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.child.activity.BaseChildDetailTabbedActivity;
import org.smartregister.child.fragment.StatusEditDialogFragment;
import org.smartregister.child.task.LoadAsyncTask;
import org.smartregister.child.util.ChildDbUtils;
import org.smartregister.kip.R;
import org.smartregister.kip.fragment.ChildRegistrationDataFragment;
import org.smartregister.kip.repository.KipOpdDetailsRepository;
import org.smartregister.kip.util.KipChildUtils;
import org.smartregister.kip.util.KipJsonFormUtils;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.opd.utils.OpdUtils;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.kip.util.KipChildUtils.setAppLocale;
import static org.smartregister.kip.util.KipConstants.KEPI_SMS_REMINDER;


/**
 * Created by ndegwamartin on 06/03/2019.
 */
public class ChildDetailTabbedActivity extends BaseChildDetailTabbedActivity {
    private static List<String> nonEditableFields = Arrays.asList("Sex", "zeir_id", "Birth_Weight", "Birth_Height");

    @Override
    protected void attachBaseContext(android.content.Context base) {
        // get language from prefs
        String lang = KipChildUtils.getLanguage(base.getApplicationContext());
        super.attachBaseContext(setAppLocale(base, lang));
    }

    @Override
    public void onUniqueIdFetched(Triple<String, Map<String, String>, String> triple, String s) {

    }

    @Override
    public void onNoUniqueId() {
        // Todo
    }

    public ChildRegistrationDataFragment getChildRegistrationDataFragment() {
        return new ChildRegistrationDataFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        overflow.findItem(org.smartregister.child.R.id.register_card).setVisible(false);
        overflow.findItem(org.smartregister.child.R.id.write_to_card).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        detailsMap = ChildDbUtils.fetchChildDetails(getChildDetails().entityId());
        detailsMap.putAll(ChildDbUtils.fetchChildFirstGrowthAndMonitoring(getChildDetails().entityId()));

        switch (item.getItemId()) {
            case R.id.registration_data:
                String populatedForm = KipJsonFormUtils.getMetadataForEditForm(this, detailsMap, nonEditableFields);
                startFormActivity(populatedForm);
                // User chose the "Settings" item, show the app settings UI...
                return true;
            case R.id.immunization_data:
                if (viewPager.getCurrentItem() != 1) {
                    viewPager.setCurrentItem(1);
                }
                Utils.startAsyncTask(
                        new LoadAsyncTask(org.smartregister.child.enums.Status.EDIT_VACCINE, detailsMap, getChildDetails(), this, getChildDataFragment(), getChildUnderFiveFragment(), getOverflow()),
                        null);
                saveButton.setVisibility(View.VISIBLE);
                for (int i = 0; i < overflow.size(); i++) {
                    overflow.getItem(i).setVisible(false);
                }
                return true;

            case R.id.recurring_services_data:
                if (viewPager.getCurrentItem() != 1) {
                    viewPager.setCurrentItem(1);
                }
                Utils.startAsyncTask(
                        new LoadAsyncTask(org.smartregister.child.enums.Status.EDIT_SERVICE, detailsMap, getChildDetails(), this, getChildDataFragment(), getChildUnderFiveFragment(), getOverflow()),
                        null);
                saveButton.setVisibility(View.VISIBLE);
                for (int i = 0; i < overflow.size(); i++) {
                    overflow.getItem(i).setVisible(false);
                }
                return true;
            case R.id.weight_data:
                if (viewPager.getCurrentItem() != 1) {
                    viewPager.setCurrentItem(1);
                }
                Utils.startAsyncTask(new LoadAsyncTask(org.smartregister.child.enums.Status.EDIT_GROWTH, detailsMap, getChildDetails(), this, getChildDataFragment(), getChildUnderFiveFragment(), getOverflow()), null);
                saveButton.setVisibility(View.VISIBLE);
                for (int i = 0; i < overflow.size(); i++) {
                    overflow.getItem(i).setVisible(false);
                }
                return true;

            case R.id.report_deceased:
                String reportDeceasedMetadata = getReportDeceasedMetadata();
                startFormActivity(reportDeceasedMetadata);
                return true;
            case R.id.change_status:
                FragmentTransaction ft = this.getFragmentManager().beginTransaction();
                android.app.Fragment prev = this.getFragmentManager().findFragmentByTag(DIALOG_TAG);
                if (prev != null) {
                    ft.remove(prev);
                }
                StatusEditDialogFragment.newInstance(detailsMap).show(ft, DIALOG_TAG);
                return true;
            case R.id.report_adverse_event:
                return launchAdverseEventForm();
            case R.id.send_sms_reminder_event:{
                sendReminderSms();
            }

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendReminderSms(){
        KipOpdProfileActivity sentSms = new KipOpdProfileActivity();

        String phoneNumber = getPhoneNumber();
        String client = getMother();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
                if (checkReminderValidity() && getDateDifference()){
                String message = reminderMessage().trim();
                String phone = phoneNumber.trim();
                sentSms.sendSmsReminder(message,phone);
                    KipOpdDetailsRepository.updateKepiSmsReminder(getBaseEntityId(),calculateSmsSentDateDate());
                org.smartregister.child.util.Utils.showToast(this, "Reminder Message Successfully Sent to "+ client);}
            } else {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 0);
            }
        }
    }

    private String getBaseEntityId(){

        return Utils.getValue(childDetails.getColumnmaps(), "_id", true);
    }

    private String getPhoneNumber(){
        String phoneNumber = Utils.getValue(childDetails.getColumnmaps(), "phone_number", true);
        return phoneNumber;
    }

    private String getMother(){
        String firstName = Utils.getValue(childDetails.getColumnmaps(), "mother_first_name", true);
        String lastName = Utils.getValue(childDetails.getColumnmaps(), "mother_last_name", true);

        return firstName + " " + lastName;
    }

    private String reminderMessage(){
        String guardianName = getMother();
        String firstName = Utils.getValue(childDetails.getColumnmaps(), "first_name", true);
        String facility = LocationHelper.getInstance().getOpenMrsReadableName(KipChildUtils.getCurrentLocality());
        return "Dear "+" " + guardianName + ", "+ firstName + KEPI_SMS_REMINDER + facility + " for their " + " immunization";
    }

    private Boolean checkReminderValidity(){
        Boolean isValid = false;
        if (getPhoneNumber().length() !=0){
            isValid = true;
        }
        return isValid;
    }

    private Boolean getDateDifference(){

        Boolean comparedValue = false;

        try {
            SimpleDateFormat dates = new SimpleDateFormat("dd/MM/yyyy");
            String appointmentDate = "10/08/2021";
            Date apptDate;
            Date toDay = new Date();
            apptDate = dates.parse(appointmentDate);
            long differenceDates = Math.abs(toDay.getTime() - apptDate.getTime());
            long difference = differenceDates / (24 * 60 * 60 * 1000);

            if (difference < 365){
                comparedValue = true;
            }

        } catch (Exception e){
            Timber.d("--> getDateDifference %s", e.getMessage());
        }

        return comparedValue;
    }

    private String calculateSmsSentDateDate() {
        String date = "";
        try {
            Date adminDate = new Date();

            date = OpdUtils.convertDate(adminDate, OpdDbConstants.DATE_FORMAT);
        } catch (Exception exception) {
            Timber.e(exception);
        }
        return date;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void navigateToRegisterActivity() {
        Intent intent = new Intent(getApplicationContext(), ChildRegisterActivity.class);
        intent.putExtra(AllConstants.INTENT_KEY.IS_REMOTE_LOGIN, false);
        startActivity(intent);
        finish();
    }

    @Override
    public void startFormActivity(String formData) {
        Form formParam = new Form();
        formParam.setWizard(false);
        formParam.setHideSaveLabel(true);
        formParam.setNextLabel("");

        Intent intent = new Intent(getApplicationContext(), org.smartregister.child.util.Utils.metadata().childFormActivity);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, formParam);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.JSON, formData);

        startActivityForResult(intent, REQUEST_CODE_GET_JSON);
    }

    @Override
    protected String getReportDeceasedMetadata() {
        try {
            JSONObject form = FormUtils.getInstance(getApplicationContext()).getFormJson("report_deceased");
            if (form != null) {
                //inject zeir id into the form
                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Date_of_Death")) {
                        SimpleDateFormat simpleDateFormat =
                                new SimpleDateFormat(com.vijay.jsonwizard.utils.FormUtils.NATIIVE_FORM_DATE_FORMAT_PATTERN,
                                        Locale.ENGLISH);
                        String dobString = Utils.getValue(childDetails.getColumnmaps(), "dob", true);
                        Date dob = Utils.dobStringToDate(dobString);
                        if (dob != null) {
                            jsonObject.put("min_date", simpleDateFormat.format(dob));
                        }
                        break;
                    }
                }
            }

            return form == null ? null : form.toString();

        } catch (Exception e) {
            Timber.e(e);
        }
        return "";
    }
}
