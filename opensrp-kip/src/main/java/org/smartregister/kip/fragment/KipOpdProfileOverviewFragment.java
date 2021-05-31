package org.smartregister.kip.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.kip.R;
import org.smartregister.kip.activity.KipOpdProfileActivity;
import org.smartregister.kip.contract.KipOpdProfileOverviewFragmentContract;
import org.smartregister.kip.presenter.KipOpdProfileOverviewFragmentPresenter;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.opd.activity.BaseOpdProfileActivity;
import org.smartregister.opd.adapter.OpdProfileOverviewAdapter;
import org.smartregister.opd.contract.OpdProfileOverviewFragmentContract;
import org.smartregister.opd.fragment.OpdProfileOverviewFragment;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.opd.utils.OpdUtils;

import java.util.Date;
import java.util.Map;

public class KipOpdProfileOverviewFragment extends OpdProfileOverviewFragment implements KipOpdProfileOverviewFragmentContract.View {
    public static final String COVID_19_VACCINE_NEXT_NUMBER = "covid19_vaccine_next_number";
    public static final String COVID_19_VACCINE_NEXT_DATE = "covid19_vaccine_next_date";
    private ConstraintLayout mainView;
    private LinearLayout opdCheckinSectionLayout;
    private Button checkInDiagnoseAndTreatBtn;
    private TextView opdCheckedInTv;
    private Button checkInVaccinationEligibilityBtn;
    private Button checkInRecordVaccineBtn;
    private Button checkInInfluenzaMedicalConditionBtn;
    private Button checkInInfluenzaRecordVaccineBtn;
    CommonPersonObjectClient commonPersonObjectClient;
    private String baseEntityId;
    private OpdProfileOverviewFragmentContract.Presenter presenter;


    public static KipOpdProfileOverviewFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        KipOpdProfileOverviewFragment fragment = new KipOpdProfileOverviewFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onCreation() {
        presenter = new KipOpdProfileOverviewFragmentPresenter(this);

        if (getArguments() != null) {
            CommonPersonObjectClient commonPersonObjectClient = (CommonPersonObjectClient) getArguments()
                    .getSerializable(OpdConstants.IntentKey.CLIENT_OBJECT);

            if (commonPersonObjectClient != null) {
                presenter.setClient(commonPersonObjectClient);
                baseEntityId = commonPersonObjectClient.getCaseId();
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.opd_fragment_profile_overview, container, false);
        mainView = view.findViewById(R.id.main_view);
        opdCheckinSectionLayout = mainView.findViewById(R.id.ll_opdFragmentProfileOverview_checkinLayout);
        opdCheckedInTv = mainView.findViewById(R.id.tv_opdFragmentProfileOverview_checkedInTitle);
        checkInDiagnoseAndTreatBtn = mainView.findViewById(R.id.btn_opdFragmentProfileOverview_diagnoseAndTreat);
        checkInVaccinationEligibilityBtn = view.findViewById(R.id.btn_opdFragmentProfileOverview_vaccine_eligibility);
        checkInRecordVaccineBtn = view.findViewById(R.id.btn_opdFragmentProfileOverview_record_vaccine);
        checkInInfluenzaMedicalConditionBtn = view.findViewById(R.id.btn_opdFragmentProfileOverview_influenza_medical_condition_vaccine);
        checkInInfluenzaRecordVaccineBtn = view.findViewById(R.id.btn_opdFragmentProfileOverview_influenza_vaccine_administration);

        commonPersonObjectClient = (CommonPersonObjectClient) getArguments().getSerializable(OpdConstants.IntentKey.CLIENT_OBJECT);

        return view;
    }

    private void checkInActionDialog(String form) {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Select an action to proceed");
            builder.setItems(formsToOpen(form), (dialog, position) -> {
                FragmentActivity activity = getActivity();
                if (position == 0) {
                    if (activity instanceof KipOpdProfileActivity) {
                        ((BaseOpdProfileActivity) activity).openDiagnoseAndTreatForm();
                    }
                } else if (position == 1 && ((KipOpdProfileActivity) activity).getAge() > 0.5 ) {
                    if (activity instanceof KipOpdProfileActivity) {
                        ((KipOpdProfileActivity) activity).openCovid19Forms(form);
                    }
                } else if (position == 2 || (position == 1 && ((KipOpdProfileActivity) activity).getAge() < 0.5 )){
                    if (activity instanceof KipOpdProfileActivity){
                        ((KipOpdProfileActivity) activity).openInfluenzaMedicalConditionForm();
                    }
                }

            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private String[] formsToOpen(String form) {
        FragmentActivity activity = getActivity();
        String[] forms = new String[]{"OPD Work Flow", "", ""};

        updateFormToDisplay(form, forms);


        if (activity instanceof KipOpdProfileActivity) {
            if (((KipOpdProfileActivity) activity).getAge() < 0.5) {
                forms = ArrayUtils.remove(forms, 1);
            }
            if (((KipOpdProfileActivity) activity).getAge() > 0.5 && !showCovid19Forms()) {
                forms = ArrayUtils.remove(forms, 1);
            }
        }
        return forms;
    }

    private boolean showCovid19Forms() {
        boolean showForms = true;
        String covidNextVaccine = commonPersonObjectClient.getDetails().get(COVID_19_VACCINE_NEXT_NUMBER);
        String covid19VaccineNextDate = commonPersonObjectClient.getDetails().get(COVID_19_VACCINE_NEXT_DATE);
        if (StringUtils.isNoneBlank(covidNextVaccine) && StringUtils.isNoneBlank(covid19VaccineNextDate)) {
            Date date = OpdUtils.convertStringToDate(OpdConstants.DateFormat.YYYY_MM_DD_HH_MM_SS, covid19VaccineNextDate);
            if (covidNextVaccine.equalsIgnoreCase("2") && (date.getTime() > new Date().getTime())) {
                showForms = false;
            }
        }
        return showForms;
    }

    private void updateFormToDisplay(String form, String[] forms) {
        if (form.equalsIgnoreCase(KipConstants.JSON_FORM.OPD_VACCINATION_ELIGIBILITY_CHECK_FORM)) {
            forms[1] = getString(R.string.covid19_vaccination_eligibility_check);
        } else if (form.equalsIgnoreCase(KipConstants.JSON_FORM.OPD_COVID19_VACCINE_ADMINISTRATION_FORM)) {
            forms[1] = getString(R.string.covid19_vaccination_administration);
        } else if (form.equalsIgnoreCase(KipConstants.JSON_FORM.OPD_INFLUENZA_VACCINE_ADMINISTRATION)){
            forms[1] = getString(R.string.influenza_vaccine_administration);
        }else {
            forms[1] = getString(R.string.covid_risk_factor_calculate_text);
            forms[2] = getString(R.string.influenza_medical_condition);

        }
    }



    @Override
    protected void onResumption() {
        if (baseEntityId != null) {
            presenter.loadOverviewFacts(baseEntityId, (facts, yamlConfigListGlobal) -> {
                if (getActivity() != null && facts != null && yamlConfigListGlobal != null) {
                    Boolean isPendingDiagnoseAndTreat = facts.get(OpdDbConstants.Column.OpdDetails.PENDING_DIAGNOSE_AND_TREAT);
                    isPendingDiagnoseAndTreat = isPendingDiagnoseAndTreat == null ? Boolean.FALSE : isPendingDiagnoseAndTreat;

                    FragmentActivity activity = getActivity();

                    if (isPendingDiagnoseAndTreat) {
                        opdCheckedInTv.setText(R.string.opd_checked_in);
                        showDiagnoseAndTreatBtn();

                            showInfluenzaMedicalConditionBtn();


                        if (StringUtils.isNotEmpty(((KipOpdProfileActivity) activity).getOpdMedicalCheck())) {
                            checkInVaccinationEligibilityBtn.setVisibility(View.GONE);
                            checkInDiagnoseAndTreatBtn.setVisibility(View.GONE);
                            checkInInfluenzaMedicalConditionBtn.setVisibility(View.GONE);
                            checkInInfluenzaRecordVaccineBtn.setVisibility(View.VISIBLE);
                            showInfluenzaVaccinationBtn();
                        }

                        if (StringUtils.isNotEmpty(((KipOpdProfileActivity) activity).getRiskFactor())) {
                            checkInVaccinationEligibilityBtn.setVisibility(View.VISIBLE);
                            checkInDiagnoseAndTreatBtn.setVisibility(View.GONE);
                            showVaccinationConditionBtn();
                        }
                        if (((KipOpdProfileActivity) activity).checkEligibility()) {
                            checkInVaccinationEligibilityBtn.setVisibility(View.GONE);
                            checkInDiagnoseAndTreatBtn.setVisibility(View.GONE);
                            checkInRecordVaccineBtn.setVisibility(View.VISIBLE);
                            showRecordVaccinationBtn();
                        }
                    } else {
                        opdCheckedInTv.setText(R.string.opd);
                        showCheckInBtn();
                    }

//                    if (((KipOpdProfileActivity) activity).getDifferenceInDate()){
//                        showSendReminderBtn();
//                    }

                    OpdProfileOverviewAdapter adapter = new OpdProfileOverviewAdapter(getActivity(), yamlConfigListGlobal, facts);
                    adapter.notifyDataSetChanged();
                    // set up the RecyclerView
                    RecyclerView recyclerView = getActivity().findViewById(R.id.profile_overview_recycler);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    recyclerView.setAdapter(adapter);
                }
            });
        }
    }

    private void showCheckInBtn() {
        if (getActivity() != null) {
            opdCheckinSectionLayout.setVisibility(View.VISIBLE);
            checkInDiagnoseAndTreatBtn.setText(R.string.check_in);
            checkInDiagnoseAndTreatBtn.setBackgroundResource(R.drawable.check_in_btn_overview_bg);
            checkInDiagnoseAndTreatBtn.setTextColor(getActivity().getResources().getColorStateList(R.color.check_in_btn_overview_text_color));
            checkInDiagnoseAndTreatBtn.setOnClickListener(v -> {
                FragmentActivity activity = getActivity();

                if (activity instanceof BaseOpdProfileActivity) {
                    ((BaseOpdProfileActivity) activity).openCheckInForm();
                }
            });
        }
    }

    private void showDiagnoseAndTreatBtn() {
        if (getActivity() != null) {
            opdCheckinSectionLayout.setVisibility(View.VISIBLE);
            checkInDiagnoseAndTreatBtn.setText(R.string.diagnose_and_treat);
            checkInDiagnoseAndTreatBtn.setBackgroundResource(R.drawable.diagnose_treat_bg);
            checkInDiagnoseAndTreatBtn.setTextColor(getActivity().getResources().getColor(R.color.diagnose_treat_txt_color));
            checkInDiagnoseAndTreatBtn.setOnClickListener(v -> {
                checkInActionDialog(KipConstants.JSON_FORM.OPD_CALCULATE_RISK_FACTOR_FORM);
            });
        }
    }

    private void showVaccinationConditionBtn() {
        if (getActivity() != null) {
            opdCheckinSectionLayout.setVisibility(View.VISIBLE);
            checkInVaccinationEligibilityBtn.setText(R.string.diagnose_and_treat);
            checkInVaccinationEligibilityBtn.setBackgroundResource(R.drawable.diagnose_treat_bg);
            checkInVaccinationEligibilityBtn.setTextColor(getActivity().getResources().getColor(R.color.diagnose_treat_txt_color));
            checkInVaccinationEligibilityBtn.setOnClickListener(v -> {
                checkInActionDialog(KipConstants.JSON_FORM.OPD_VACCINATION_ELIGIBILITY_CHECK_FORM);
            });
        }
    }

    private void showRecordVaccinationBtn() {
        if (getActivity() != null) {
            opdCheckinSectionLayout.setVisibility(View.VISIBLE);
            checkInRecordVaccineBtn.setText(R.string.diagnose_and_treat);
            checkInRecordVaccineBtn.setBackgroundResource(R.drawable.diagnose_treat_bg);
            checkInRecordVaccineBtn.setTextColor(getActivity().getResources().getColor(R.color.diagnose_treat_txt_color));
            checkInRecordVaccineBtn.setOnClickListener(v -> {
                checkInActionDialog(KipConstants.JSON_FORM.OPD_COVID19_VACCINE_ADMINISTRATION_FORM);
            });
        }
    }

    private void showInfluenzaMedicalConditionBtn() {
        if (getActivity() != null) {
            opdCheckinSectionLayout.setVisibility(View.VISIBLE);
            checkInInfluenzaMedicalConditionBtn.setText(R.string.diagnose_and_treat);
            checkInInfluenzaMedicalConditionBtn.setBackgroundResource(R.drawable.diagnose_treat_bg);
            checkInInfluenzaMedicalConditionBtn.setTextColor(getActivity().getResources().getColor(R.color.diagnose_treat_txt_color));
            checkInInfluenzaMedicalConditionBtn.setOnClickListener(v -> {
                checkInActionDialog(KipConstants.JSON_FORM.OPD_INFLUENZA_MEDICAL_CONDITION);
            });
        }
    }

    private void showInfluenzaVaccinationBtn() {
        if (getActivity() != null) {
            opdCheckinSectionLayout.setVisibility(View.VISIBLE);
            checkInInfluenzaRecordVaccineBtn.setText(R.string.diagnose_and_treat);
            checkInInfluenzaRecordVaccineBtn.setBackgroundResource(R.drawable.diagnose_treat_bg);
            checkInInfluenzaRecordVaccineBtn.setTextColor(getActivity().getResources().getColor(R.color.diagnose_treat_txt_color));
            checkInInfluenzaRecordVaccineBtn.setOnClickListener(v -> {
                checkInActionDialog(KipConstants.JSON_FORM.OPD_INFLUENZA_VACCINE_ADMINISTRATION);
            });
        }
    }

    private void showSendReminderBtn() {
        if (getActivity() != null) {
            opdCheckinSectionLayout.setVisibility(View.VISIBLE);
            checkInRecordVaccineBtn.setText(R.string.diagnose_and_treat);
            checkInRecordVaccineBtn.setBackgroundResource(R.drawable.diagnose_treat_bg);
            checkInRecordVaccineBtn.setTextColor(getActivity().getResources().getColor(R.color.diagnose_treat_txt_color));
            checkInRecordVaccineBtn.setOnClickListener(v -> {
                checkInActionDialog(KipConstants.JSON_FORM.OPD_COVID19_VACCINE_ADMINISTRATION_FORM);
            });
        }
    }

    public String getRiskFactor() {
        Map<String, String> details = commonPersonObjectClient.getDetails();
        return details.get("covid19_risk_factor");
    }

    @Override
    public void onActionReceive() {
        onCreation();
        onResumption();
    }
}
