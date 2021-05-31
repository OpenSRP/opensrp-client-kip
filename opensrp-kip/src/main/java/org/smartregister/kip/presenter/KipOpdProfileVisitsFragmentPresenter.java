package org.smartregister.kip.presenter;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.util.Pair;

import org.jeasy.rules.api.Facts;
import org.smartregister.kip.R;
import org.smartregister.kip.contract.KipOpdProfileVisitsFragmentContract;
import org.smartregister.kip.interactor.KipOpdProfileVisitsFragmentInteractor;
import org.smartregister.kip.pojo.KipOpdVisitSummary;
import org.smartregister.kip.util.KipOpdConstants;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.contract.OpdProfileVisitsFragmentContract;
import org.smartregister.opd.domain.YamlConfig;
import org.smartregister.opd.domain.YamlConfigItem;
import org.smartregister.opd.domain.YamlConfigWrapper;
import org.smartregister.opd.pojo.OpdVisitSummaryResultModel;
import org.smartregister.opd.presenter.OpdProfileVisitsFragmentPresenter;
import org.smartregister.opd.utils.FilePath;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdFactsUtil;
import org.smartregister.opd.utils.OpdUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class KipOpdProfileVisitsFragmentPresenter extends OpdProfileVisitsFragmentPresenter implements KipOpdProfileVisitsFragmentContract.Presenter {
    private WeakReference<KipOpdProfileVisitsFragmentContract.View> mProfileView;
    private KipOpdProfileVisitsFragmentContract.Interactor mProfileInteractor;

    private int currentPageNo = 0;
    private int totalPages = 0;

    public KipOpdProfileVisitsFragmentPresenter(@NonNull KipOpdProfileVisitsFragmentContract.View profileView) {
        super(profileView);
        mProfileView = new WeakReference<>(profileView);
        mProfileInteractor = new KipOpdProfileVisitsFragmentInteractor(this);
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        mProfileView = null;//set to null on destroy

        // Inform interactor
        if (mProfileInteractor != null) {
            mProfileInteractor.onDestroy(isChangingConfiguration);
        }

        // Activity destroyed set interactor to null
        if (!isChangingConfiguration) {
            mProfileInteractor = null;
        }
    }

    @Override
    public void loadKipVisits(@NonNull String baseEntityId, @NonNull final OnKipFinishedCallback onFinishedCallback) {
        if (mProfileInteractor != null) {
            mProfileInteractor.fetchKipVisits(baseEntityId, currentPageNo, new OnKipVisitsLoadedCallback() {

                @Override
                public void onKipVisitsLoaded(@NonNull List<KipOpdVisitSummary> opdVisitSummaries) {
                    updatePageCounter();

                    ArrayList<Pair<YamlConfigWrapper, Facts>> items = new ArrayList<>();
                    populateKipWrapperDataAndFacts(opdVisitSummaries, items);
                    onFinishedCallback.onKipFinished(opdVisitSummaries, items);
                }
            });

        }
    }

    private void updatePageCounter() {
        String pageCounterTemplate = getString(org.smartregister.opd.R.string.current_page_of_total_pages);

        OpdProfileVisitsFragmentContract.View profileView = getProfileView();
        if (profileView != null && pageCounterTemplate != null) {
            profileView.showPageCountText(String.format(pageCounterTemplate, (currentPageNo + 1), totalPages));

            profileView.showPreviousPageBtn(currentPageNo > 0);
            profileView.showNextPageBtn(currentPageNo < (totalPages - 1));
        }
    }

    @Override
    public void onNextPageClicked() {
        if (currentPageNo < totalPages && getProfileView() != null && getProfileView().getClientBaseEntityId() != null) {
            currentPageNo++;

            loadKipVisits(getProfileView().getClientBaseEntityId(), new OnKipFinishedCallback() {
                @Override
                public void onKipFinished(@NonNull List<KipOpdVisitSummary> opdVisitSummaries, @NonNull ArrayList<Pair<YamlConfigWrapper, Facts>> items) {
                    if (getProfileView() != null) {
                        getProfileView().displayKipVisits(opdVisitSummaries, items);
                    }
                }
            });
        }
    }

    @Override
    public void onPreviousPageClicked() {
        if (currentPageNo > 0 && getProfileView() != null && getProfileView().getClientBaseEntityId() != null) {
            currentPageNo--;

            loadKipVisits(getProfileView().getClientBaseEntityId(), new OnKipFinishedCallback() {
                @Override
                public void onKipFinished(@NonNull List<KipOpdVisitSummary> opdVisitSummaries, @NonNull ArrayList<Pair<YamlConfigWrapper, Facts>> items) {
                    if (getProfileView() != null) {
                        getProfileView().displayKipVisits(opdVisitSummaries, items);
                    }
                }
            });
        }
    }

    @Override
    public void loadPageCounter(@NonNull String baseEntityId) {
        if (mProfileInteractor != null) {
            mProfileInteractor.fetchVisitsPageCount(baseEntityId, visitsPageCount -> {
                totalPages = visitsPageCount;
                updatePageCounter();
            });
        }
    }

    @NonNull
    private Facts generateOpdVisitSummaryFact(@NonNull KipOpdVisitSummary opdVisitSummary) {
        Facts facts = new Facts();

        if (opdVisitSummary.getVisitDate() != null) {
            OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.VISIT_DATE, OpdUtils.convertDate(opdVisitSummary.getVisitDate(), OpdConstants.DateFormat.d_MMM_yyyy_hh_mm_ss));
        }

//        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.TEST_TYPE, opdVisitSummary.getTestType());
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.DIAGNOSIS, opdVisitSummary.getDiagnosis());
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.DIAGNOSIS_TYPE, opdVisitSummary.getDiagnosisType());
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.DIAGNOSIS_SAME, opdVisitSummary.getIsDiagnosisSame());
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.TREATMENT_TYPE_SPECIFY, opdVisitSummary.getTreatmentTypeSpecify());
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.TREATMENT_TYPE, OpdUtils.cleanStringArray(opdVisitSummary.getTreatmentType()));


        // Put the diseases text
        String diseasesText = generateDiseasesText(opdVisitSummary);
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.DISEASE_CODE, diseasesText);

        // Put the treatment text
        HashMap<String, OpdVisitSummaryResultModel.Treatment> treatments = opdVisitSummary.getTreatments();
        String medicationText = generateMedicationText(treatments);
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.TREATMENT, medicationText);

        // Put the test text
        HashMap<String, List<OpdVisitSummaryResultModel.Test>> test = opdVisitSummary.getTests();
        String testText = generateTestText(test);

        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.TESTS, testText);

        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.DISCHARGED_ALIVE, opdVisitSummary.getDischargedAlive());
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.DISCHARGED_HOME, opdVisitSummary.getDischargedHome());
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.REFERRAL, opdVisitSummary.getReferral());
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.REFERRAL_LOCATION, opdVisitSummary.getReferralLocation());
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.REFERRAL_LOCATION_SPECIFY, opdVisitSummary.getReferralLocationSpecify());

        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.PRE_EXISTING_CONDITIONS, opdVisitSummary.getPreExistingConditions());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.OTHER_PRE_EXISTING_CONDITIONS, opdVisitSummary.getOtherPreExistingCondition());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.OCCUPATION, opdVisitSummary.getOccupation());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.OTHER_OCCUPATION, opdVisitSummary.getOtherOccupation());


        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.INFLUENZA_PRE_EXISTING_CONDITIONS, opdVisitSummary.getInfluenzaPreExistingConditions());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.OTHER_INFLUENZA_PRE_EXISTING_CONDITIONS, opdVisitSummary.getOtherInfluenzaPreExistingCondition());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.INFLUENZA_ALLERGIES, opdVisitSummary.getInfluenzaAllergies());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.OTHER_INFLUENZA_ALLERGIES, opdVisitSummary.getOtherInfluenzaAllergies());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.INF_TEMPERATURE, opdVisitSummary.getInfTemperature());

        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.TEMPERATURE, opdVisitSummary.getTemperature());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.COVID_19_HISTORY, opdVisitSummary.getCovid19History());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.ORAL_CONFIRMATION, opdVisitSummary.getOralConfirmation());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.RESPIRATORY_SYMPTONS, opdVisitSummary.getRespiratorySymptons());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.OTHER_RESPIRATORY_SYMPTONS, opdVisitSummary.getOtherRespiratorySymptons());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.ALLERGIES, opdVisitSummary.getAllergies());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.OTHER_ALLERGIES, opdVisitSummary.getOtherAllergies());

        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.COVID19_ANTIGENS, opdVisitSummary.getCovid19Atigens());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.ADMINISTRATION_DATE, opdVisitSummary.getAdministrationDate());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.SITE_OF_ADMINISTRATION, opdVisitSummary.getSiteOfAdministration());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.ADMINISTRATION_ROUTE, opdVisitSummary.getAdministratonRoute());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.LOT_NUMBER, opdVisitSummary.getLotNumber());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.VACCINE_EXPIRY, opdVisitSummary.getVaccineExpiry());

        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.COVID_19_VACCINE_NEXT_DATE, opdVisitSummary.getCovid19VaccineNextDate());

        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.INFLUENZA_VACCINES, opdVisitSummary.getInfluenzaVaccines());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.INFLUENZA_ADMINISTRATION_DATE, opdVisitSummary.getInfluenzaAdministrationDate());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.INFLUENZA_SITE_OF_ADMINISTRATION, opdVisitSummary.getInfluenzaSiteOfAdministration());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.INFLUENZA_ADMINISTRATION_ROUTE, opdVisitSummary.getInfluenzaAdministratonRoute());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.INFLUENZA_LOT_NUMBER, opdVisitSummary.getInfluenzaLotNumber());
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.INFLUENZA_VACCINE_EXPIRY, opdVisitSummary.getInfluenzaVaccineExpiry());



        // Add translate-able labels
        setLabelsInFacts(facts);

        return facts;
    }

    private void setLabelsInFacts(@NonNull Facts facts) {
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.DIAGNOSIS_LABEL, getString(org.smartregister.opd.R.string.diagnosis));
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.DIAGNOSIS_TYPE_LABEL, getString(org.smartregister.opd.R.string.diagnosis_type));
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.DISEASE_CODE_LABEL, getString(org.smartregister.opd.R.string.disease_code));
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.TREATMENT_LABEL, getString(org.smartregister.opd.R.string.opd_treatment_label));
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.TREATMENT_TYPE_SPECIFY_LABEL, getString(org.smartregister.opd.R.string.opd_treatment_type_specify_label));
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.TREATMENT_TYPE_LABEL, getString(org.smartregister.opd.R.string.opd_treatment_type_label));
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.DIAGNOSIS_SAME_LABEL, getString(org.smartregister.opd.R.string.opd_is_diagnosis_same_label));
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.TEST_TYPE_LABEL, getString(org.smartregister.opd.R.string.opd_test_type_label));
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.TESTS_LABEL, getString(org.smartregister.opd.R.string.opd_test_result_label));
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.DISCHARGED_ALIVE_LABEL, getString(org.smartregister.opd.R.string.opd_discharged_alive_label));
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.DISCHARGED_HOME_LABEL, getString(org.smartregister.opd.R.string.opd_discharged_home_label));
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.REFERRAL_LABEL, getString(org.smartregister.opd.R.string.opd_referred_label));
        OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.OpdVisit.REFERRAL_LOCATION_LABEL, getString(org.smartregister.opd.R.string.opd_referred_to_label));

        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.PRE_EXISTING_CONDITIONS_LABEL, getString(R.string.pre_existing_conditions_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.OTHER_PRE_EXISTING_CONDITIONS_LABEL, getString(R.string.other_pre_existing_conditions_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.OCCUPATION_LABEL, getString(R.string.occupation_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.OTHER_OCCUPATION_LABEL, getString(R.string.other_occupation_label));

        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.INFLUENZA_PRE_EXISTING_CONDITIONS_LABEL, getString(R.string.influenza_pre_existing_conditions_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.OTHER_INFLUENZA_PRE_EXISTING_CONDITIONS_LABEL, getString(R.string.other_influenza_pre_existing_conditions_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.INFLUENZA_ALLERGIES_LABEL, getString(R.string.influenza_allergies_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.OTHER_INFLUENZA_ALLERGIES_LABEL, getString(R.string.other_influenza_allergies_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.INF_TEMPERATURE_LABEL, getString(R.string.influenza_tempareture_label));

        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.TEMPERATURE_LABEL, getString(R.string.temperature_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.COVID_19_HISTORY_LABEL, getString(R.string.covid_19_history_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.ORAL_CONFIRMATION_LABEL, getString(R.string.oral_confirmation_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.RESPIRATORY_SYMPTONS_LABEL, getString(R.string.respiratory_symptoms_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.OTHER_RESPIRATORY_SYMPTONS_LABEL, getString(R.string.other_respiratory_symptoms_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.ALLERGIES_LABEL, getString(R.string.allergies_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.OTHER_ALLERGIES_LABEL, getString(R.string.other_allergies_label));

        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.COVID19_ANTIGENS_LABEL, getString(R.string.covid19_antigens_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.ADMINISTRATION_DATE_LABEL, getString(R.string.administration_date_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.SITE_OF_ADMINISTRATION_LABEL, getString(R.string.site_of_administration_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.ADMINISTRATION_ROUTE_LABEL, getString(R.string.administration_route_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.LOT_NUMBER_LABEL, getString(R.string.lot_number_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.VACCINE_EXPIRY_LABEL, getString(R.string.vaccine_expiry_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.COVID_19_VACCINE_NEXT_DATE_LABEL, getString(R.string.covid19_vaccine_next_date_label));

        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.INFLUENZA_VACCINES_LABEL, getString(R.string.influenza_vaccines_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.INFLUENZA_ADMINISTRATION_DATE_LABEL, getString(R.string.influenza_administration_date_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.INFLUENZA_SITE_OF_ADMINISTRATION_LABEL, getString(R.string.influenza_site_of_vaccine_administration_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.INFLUENZA_ADMINISTRATION_ROUTE_LABEL, getString(R.string.influenza_vaccine_administration_route_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.INFLUENZA_LOT_NUMBER_LABEL, getString(R.string.influenza_vaccine_lot_number_label));
        OpdFactsUtil.putNonNullFact(facts, KipOpdConstants.FactKey.OpdVisit.INFLUENZA_VACCINE_EXPIRY_LABEL, getString(R.string.influenza_vaccine_expiry_label));
    }

    @Nullable
    @Override
    public KipOpdProfileVisitsFragmentContract.View getProfileView() {
        if (mProfileView != null) {
            return mProfileView.get();
        } else {
            return null;
        }
    }

    @Nullable
    public String getString(@StringRes int stringId) {
        KipOpdProfileVisitsFragmentContract.View profileView = getProfileView();
        if (profileView != null) {
            return profileView.getString(stringId);
        }

        return null;
    }

    @Override
    public void populateKipWrapperDataAndFacts(@NonNull List<KipOpdVisitSummary> opdVisitSummaries, @NonNull ArrayList<Pair<YamlConfigWrapper, Facts>> items) {
        for (KipOpdVisitSummary opdVisitSummary : opdVisitSummaries) {
            Facts facts = generateOpdVisitSummaryFact(opdVisitSummary);
            Iterable<Object> ruleObjects = null;

            try {
                ruleObjects = OpdLibrary.getInstance().readYaml(FilePath.FILE.OPD_VISIT_ROW);
            } catch (IOException e) {
                Timber.e(e);
            }

            if (ruleObjects != null) {
                for (Object ruleObject : ruleObjects) {
                    YamlConfig yamlConfig = (YamlConfig) ruleObject;
                    if (yamlConfig.getGroup() != null) {
                        items.add(new Pair<>(new YamlConfigWrapper(yamlConfig.getGroup(), null, null), facts));
                    }

                    if (yamlConfig.getSubGroup() != null) {
                        items.add(new Pair<>(new YamlConfigWrapper(null, yamlConfig.getSubGroup(), null), facts));
                    }

                    List<YamlConfigItem> configItems = yamlConfig.getFields();

                    if (configItems != null) {
                        for (YamlConfigItem configItem : configItems) {
                            String relevance = configItem.getRelevance();
                            if (relevance != null && OpdLibrary.getInstance().getOpdRulesEngineHelper()
                                    .getRelevance(facts, relevance)) {
                                YamlConfigWrapper yamlConfigWrapper = new YamlConfigWrapper(null, null, configItem);
                                items.add(new Pair<>(yamlConfigWrapper, facts));
                            }
                        }
                    }
                }
            }
        }
    }
}