package org.smartregister.kip.provider;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.SmartRegisterCLientsProviderForCursorAdapter;
import org.smartregister.domain.Alert;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.VaccinateActionUtils;
import org.smartregister.kip.R;
import org.smartregister.service.AlertService;
import org.smartregister.util.OpenSRPImageLoader;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.contract.SmartRegisterClients;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;
import org.smartregister.view.viewholder.OnClickFormLauncher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import util.ImageUtils;
import util.KipConstants;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static org.smartregister.immunization.util.VaccinatorUtils.generateScheduleList;
import static org.smartregister.immunization.util.VaccinatorUtils.nextVaccineDue;
import static org.smartregister.immunization.util.VaccinatorUtils.receivedVaccines;
import static org.smartregister.util.Utils.fillValue;
import static org.smartregister.util.Utils.getName;
import static org.smartregister.util.Utils.getValue;

/**
 * Created by Keyman on 14-Sep-17.
 */
public class DefaulterListSmartClientsProvider implements SmartRegisterCLientsProviderForCursorAdapter {
    private final LayoutInflater inflater;
    private final Context context;
    private final View.OnClickListener onClickListener;
    private final AlertService alertService;
    private final VaccineRepository vaccineRepository;
    private final CommonRepository commonRepository;
    private final AbsListView.LayoutParams clientViewLayoutParams;

    public DefaulterListSmartClientsProvider(Context context, View.OnClickListener onClickListener,
                                             AlertService alertService, VaccineRepository vaccineRepository,
                                             CommonRepository commonRepository) {
        this.onClickListener = onClickListener;
        this.context = context;
        this.alertService = alertService;
        this.vaccineRepository = vaccineRepository;
        this.commonRepository = commonRepository;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        clientViewLayoutParams = new AbsListView.LayoutParams(MATCH_PARENT, (int) context.getResources().getDimension(org.smartregister.R.dimen.list_item_height));
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, final View convertView) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;

        fillValue((TextView) convertView.findViewById(R.id.child_unique_id), getValue(pc.getColumnmaps(), "zeir_id", false));

        String firstName = getValue(pc.getColumnmaps(), "first_name", true);
        String lastName = getValue(pc.getColumnmaps(), "last_name", true);
        String childName = getName(firstName, lastName);

        String motherFirstName = getValue(pc.getColumnmaps(), "mother_first_name", true);
        if (StringUtils.isBlank(childName) && StringUtils.isNotBlank(motherFirstName)) {
            childName = "B/o " + motherFirstName.trim();
        }
        fillValue((TextView) convertView.findViewById(R.id.child_name), childName);

        String gender = getValue(pc.getColumnmaps(), "gender", true);
        fillValue((TextView) convertView.findViewById(R.id.child_gender), gender);

        String village = getValue(pc.getColumnmaps(), "village", true);
        // String estate = getValue(pc.getColumnmaps(), "estate", true);
        String landmark = getValue(pc.getColumnmaps(), "landmark", true);

        List<String> velList = new ArrayList<>();
        if (StringUtils.isNotBlank(village)) {
            velList.add(village);
        }
        /*  if (StringUtils.isNotBlank(estate)) {
            velList.add(estate);
        }*/
        if (StringUtils.isNotBlank(landmark)) {
            velList.add(landmark);
        }

        fillValue((TextView) convertView.findViewById(R.id.child_ce_village), velList.isEmpty() ? "" : TextUtils.join(", ", velList));

        fillValue((TextView) convertView.findViewById(R.id.child_cwc_number), pc.getColumnmaps(), "cwc_number", false);

        fillValue((TextView) convertView.findViewById(R.id.mother_phone_number), pc.getColumnmaps(), "mother_phone_number", false);

        fillValue((TextView) convertView.findViewById(R.id.chw_phone_number), pc.getColumnmaps(), "chw_phone_number", false);

        String dobString = getValue(pc.getColumnmaps(), "dob", false);

        final ImageView profilePic = (ImageView) convertView.findViewById(R.id.child_profilepic);
        int defaultImageResId = ImageUtils.profileImageResourceByGender(gender);
        profilePic.setImageResource(defaultImageResId);
        if (pc.entityId() != null) { //image already in local storage most likey ):
            //set profile image by passing the client id.If the image doesn't exist in the image repository then download and save locally
            profilePic.setTag(org.smartregister.R.id.entity_id, pc.entityId());
            DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(pc.entityId(), OpenSRPImageLoader.getStaticImageListener(profilePic, 0, 0));
        }

        convertView.findViewById(R.id.child_profile_info_layout).setTag(client);
        convertView.findViewById(R.id.child_profile_info_layout).setOnClickListener(onClickListener);

        View recordVaccination = convertView.findViewById(R.id.record_vaccination);
        recordVaccination.setTag(client);
        recordVaccination.setOnClickListener(onClickListener);
        recordVaccination.setVisibility(View.INVISIBLE);

        String lostToFollowUp = getValue(pc.getColumnmaps(), "lost_to_follow_up", false);
        String inactive = getValue(pc.getColumnmaps(), "inactive", false);

        try {
            Utils.startAsyncTask(new VaccinationAsyncTask(convertView, pc.entityId(), dobString, lostToFollowUp, inactive), null);
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage(), e);
        }

    }

    private void updateRecordVaccination(View convertView, List<Vaccine> vaccines, Map<String, Object> nv, String lostToFollowUp, String inactive) {
        View recordVaccination = convertView.findViewById(R.id.record_vaccination);
        recordVaccination.setVisibility(View.VISIBLE);

        TextView recordVaccinationText = (TextView) convertView.findViewById(R.id.record_vaccination_text);
        ImageView recordVaccinationCheck = (ImageView) convertView.findViewById(R.id.record_vaccination_check);
        recordVaccinationCheck.setVisibility(View.GONE);

        convertView.setLayoutParams(clientViewLayoutParams);

        State state = State.FULLY_IMMUNIZED;
        String stateKey = null;

        if (nv != null) {
            DateTime dueDate = (DateTime) nv.get("date");
            VaccineRepo.Vaccine vaccine = (VaccineRepo.Vaccine) nv.get("vaccine");
            stateKey = VaccinateActionUtils.stateKey(vaccine);
            if (nv.get("alert") == null) {
                state = State.NO_ALERT;
            } else if (((Alert) nv.get("alert")).status().value().equalsIgnoreCase("normal")) {
                state = State.DUE;
            } else if (((Alert) nv.get("alert")).status().value().equalsIgnoreCase("upcoming")) {
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                if (dueDate.getMillis() >= (today.getTimeInMillis() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)) && dueDate.getMillis() < (today.getTimeInMillis() + TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS))) {
                    state = State.UPCOMING_NEXT_7_DAYS;
                } else {
                    state = State.UPCOMING;
                }
            } else if (((Alert) nv.get("alert")).status().value().equalsIgnoreCase("urgent")) {
                state = State.OVERDUE;
            } else if (((Alert) nv.get("alert")).status().value().equalsIgnoreCase("expired")) {
                state = State.EXPIRED;
            }
        } else {
            state = State.WAITING;
               }


        // Update active/inactive/lostToFollowup status
        if (lostToFollowUp.equals(Boolean.TRUE.toString())) {
            state = State.LOST_TO_FOLLOW_UP;
        }

        if (inactive.equals(Boolean.TRUE.toString())) {
            state = State.INACTIVE;
        }

        if (state.equals(State.FULLY_IMMUNIZED)) {
            recordVaccinationText.setText("Fully\nimmunized");
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.client_list_grey));

            recordVaccinationCheck.setImageResource(R.drawable.ic_action_check);
            recordVaccinationCheck.setVisibility(View.VISIBLE);

            recordVaccination.setBackgroundColor(context.getResources().getColor(R.color.white));
            recordVaccination.setEnabled(false);

        } else if (state.equals(State.INACTIVE)) {
            recordVaccinationText.setText("Inactive");
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.client_list_grey));

            recordVaccinationCheck.setImageResource(R.drawable.ic_icon_status_inactive);
            recordVaccinationCheck.setVisibility(View.VISIBLE);

            recordVaccination.setBackgroundColor(context.getResources().getColor(R.color.white));
            recordVaccination.setEnabled(false);


        } else if (state.equals(State.LOST_TO_FOLLOW_UP)) {
            recordVaccinationText.setText("Lost to\nFollow-Up");
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.client_list_grey));

            recordVaccinationCheck.setImageResource(R.drawable.ic_icon_status_losttofollowup);
            recordVaccinationCheck.setVisibility(View.VISIBLE);

            recordVaccination.setBackgroundColor(context.getResources().getColor(R.color.white));
            recordVaccination.setEnabled(false);

        } else if (state.equals(State.WAITING)) {
            recordVaccinationText.setText("Waiting");
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.client_list_grey));

            recordVaccination.setBackgroundColor(context.getResources().getColor(R.color.white));
            recordVaccination.setEnabled(false);
        } else if (state.equals(State.EXPIRED)) {
            recordVaccinationText.setText("Expired");
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.client_list_grey));

            recordVaccination.setBackgroundColor(context.getResources().getColor(R.color.white));
            recordVaccination.setEnabled(false);
        } else if (state.equals(State.UPCOMING)) {
            recordVaccinationText.setText("Due\n" + stateKey);
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.client_list_grey));

            recordVaccination.setBackgroundColor(context.getResources().getColor(R.color.white));
            recordVaccination.setEnabled(false);
        } else if (state.equals(State.UPCOMING_NEXT_7_DAYS)) {
            recordVaccinationText.setText("Record\n" + stateKey);
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.client_list_grey));

            recordVaccination.setBackground(context.getResources().getDrawable(R.drawable.due_vaccine_light_blue_bg));
            recordVaccination.setEnabled(true);
        } else if (state.equals(State.DUE)) {
            recordVaccinationText.setText("Record\n" + stateKey);
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.status_bar_text_almost_white));

            recordVaccination.setBackground(context.getResources().getDrawable(R.drawable.due_vaccine_blue_bg));
            recordVaccination.setEnabled(true);
        } else if (state.equals(State.OVERDUE)) {
            recordVaccinationText.setText("Record\n" + stateKey);
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.status_bar_text_almost_white));

            recordVaccination.setBackground(context.getResources().getDrawable(R.drawable.due_vaccine_red_bg));
            recordVaccination.setEnabled(true);
        } else if (state.equals(State.NO_ALERT)) {
            if (StringUtils.isNotBlank(stateKey) && (StringUtils.containsIgnoreCase(stateKey, "week") || StringUtils.containsIgnoreCase(stateKey, "month")) && !vaccines.isEmpty()) {
                Vaccine vaccine = vaccines.isEmpty() ? null : vaccines.get(vaccines.size() - 1);
                String previousStateKey = VaccinateActionUtils.previousStateKey("child", vaccine);
                if (previousStateKey != null) {
                    recordVaccinationText.setText(previousStateKey);
                } else {
                    recordVaccinationText.setText(stateKey);
                }
                recordVaccinationCheck.setImageResource(R.drawable.ic_action_check);
                recordVaccinationCheck.setVisibility(View.VISIBLE);
            } else {
                recordVaccinationText.setText("Due\n" + stateKey);
            }
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.client_list_grey));

            recordVaccination.setBackgroundColor(context.getResources().getColor(R.color.white));
            recordVaccination.setEnabled(false);
        } else {
            recordVaccinationText.setText("");
            recordVaccinationText.setTextColor(context.getResources().getColor(R.color.client_list_grey));

            recordVaccination.setBackgroundColor(context.getResources().getColor(R.color.white));
            recordVaccination.setEnabled(false);
        }
    }

    @Override
    public SmartRegisterClients updateClients(FilterOption villageFilter, ServiceModeOption
            serviceModeOption, FilterOption searchFilter, SortOption sortOption) {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {

    }

    @Override
    public OnClickFormLauncher newFormLauncher(String formName, String entityId, String
            metaData) {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public View inflatelayoutForCursorAdapter() {
        return inflater().inflate(R.layout.smart_register_child_defaulter_list, null);
    }

    public LayoutInflater inflater() {
        return inflater;
    }

    public enum State {
        DUE,
        OVERDUE,
        UPCOMING_NEXT_7_DAYS,
        UPCOMING,
        INACTIVE,
        LOST_TO_FOLLOW_UP,
        EXPIRED,
        WAITING,
        NO_ALERT,
        FULLY_IMMUNIZED
    }

    private class VaccinationAsyncTask extends AsyncTask<Void, Void, Void> {
        private final View convertView;
        private final String entityId;
        private final String dobString;
        private final String lostToFollowUp;
        private final String inactive;
        private List<Vaccine> vaccines = new ArrayList<>();
        Map<String, Object> nv = null;

        private VaccinationAsyncTask(View convertView,
                                     String entityId,
                                     String dobString,
                                     String lostToFollowUp,
                                     String inactive) {
            this.convertView = convertView;
            this.entityId = entityId;
            this.dobString = dobString;
            this.lostToFollowUp = lostToFollowUp;
            this.inactive = inactive;
        }


        @Override
        protected Void doInBackground(Void... params) {
            vaccines = vaccineRepository.findByEntityId(entityId);
            List<Alert> alerts = alertService.findByEntityIdAndAlertNames(entityId, VaccinateActionUtils.allAlertNames("child"));

            Map<String, Date> recievedVaccines = receivedVaccines(vaccines);

            List<Map<String, Object>> sch = generateScheduleList("child", new DateTime(dobString), recievedVaccines, alerts);

            if (vaccines.isEmpty()) {
                List<VaccineRepo.Vaccine> vList = Arrays.asList(VaccineRepo.Vaccine.values());
                nv = nextVaccineDue(sch, vList);
            }

            if (nv == null) {
                Date lastVaccine = null;
                if (!vaccines.isEmpty()) {
                    Vaccine vaccine = vaccines.get(vaccines.size() - 1);
                    lastVaccine = vaccine.getDate();
                }
                nv = nextVaccineDue(sch, lastVaccine);
            }

            final String date = "date";
            if (nv != null && nv.containsKey(date) && nv.get(date) instanceof DateTime) {
                DateTime dueDate = (DateTime) nv.get(date);
                boolean updated = commonRepository.populateSearchValues(entityId, KipConstants.EC_CHILD_TABLE.DUE_DATE, String.valueOf(dueDate.getMillis()), null);
                if (!updated) {
                    Log.e(getClass().getName(), "Unable to update FTS due date for " + entityId);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            updateRecordVaccination(convertView, vaccines, nv, lostToFollowUp, inactive);

        }
    }
}
