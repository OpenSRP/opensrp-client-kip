package org.smartregister.kip.presenter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.api.Facts;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.kip.contract.KipOpdProfileOverviewFragmentContract;
import org.smartregister.kip.model.KipOpdProfileOverviewFragmentModel;
import org.smartregister.kip.pojo.OpdCovid19CalculateRiskFactorForm;
import org.smartregister.kip.pojo.OpdCovid19VaccinationEligibilityCheckForm;
import org.smartregister.kip.pojo.OpdCovid19VaccinationForm;
import org.smartregister.kip.pojo.OpdInfluenzaVaccineAdministrationForm;
import org.smartregister.kip.pojo.OpdMedicalCheckForm;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.domain.YamlConfig;
import org.smartregister.opd.domain.YamlConfigItem;
import org.smartregister.opd.domain.YamlConfigWrapper;
import org.smartregister.opd.pojo.OpdDetails;
import org.smartregister.opd.pojo.OpdVisit;
import org.smartregister.opd.presenter.OpdProfileOverviewFragmentPresenter;
import org.smartregister.opd.utils.FilePath;
import org.smartregister.opd.utils.OpdFactsUtil;
import org.smartregister.opd.utils.OpdUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class KipOpdProfileOverviewFragmentPresenter extends OpdProfileOverviewFragmentPresenter implements KipOpdProfileOverviewFragmentContract.Presenter {
    private KipOpdProfileOverviewFragmentModel model;
    private static CommonPersonObjectClient client;
    private WeakReference<KipOpdProfileOverviewFragmentContract.View> view;

    public KipOpdProfileOverviewFragmentPresenter(@NonNull KipOpdProfileOverviewFragmentContract.View view) {
        super(view);
        this.view = new WeakReference<>(view);
        model = new KipOpdProfileOverviewFragmentModel();
    }

    @Override
    public void loadOverviewFacts(@NonNull String baseEntityId, @NonNull final OnFinishedCallback onFinishedCallback) {
        model.fetchLastCheckAndVisit(baseEntityId, (opdCheckIn, opdVisit, opdDetails, covid19CalculateRiskFactorForm, opdCovid19VaccinationForm, opdCovid19VaccinationEligibilityCheckForm,opdMedicalCheckForm,opdInfluenzaVaccineAdministrationForm) -> loadOverviewDataAndDisplay(opdCheckIn, opdVisit, opdDetails, covid19CalculateRiskFactorForm, opdCovid19VaccinationForm, opdCovid19VaccinationEligibilityCheckForm,opdMedicalCheckForm,opdInfluenzaVaccineAdministrationForm, onFinishedCallback));
    }

    @Override
    public void loadOverviewDataAndDisplay(@Nullable Map<String, String> opdCheckIn, @Nullable OpdVisit opdVisit, @Nullable OpdDetails opdDetails, @Nullable OpdCovid19CalculateRiskFactorForm covid19CalculateRiskFactorForm, @Nullable OpdCovid19VaccinationForm opdCovid19VaccinationForm, @Nullable OpdCovid19VaccinationEligibilityCheckForm opdCovid19VaccinationEligibilityCheckForm, @NonNull OpdMedicalCheckForm opdMedicalCheckForm, @NonNull OpdInfluenzaVaccineAdministrationForm opdInfluenzaVaccineAdministrationForm, @NonNull final OnFinishedCallback onFinishedCallback) {
        List<YamlConfigWrapper> yamlConfigListGlobal = new ArrayList<>(); //This makes sure no data duplication happens
        Facts facts = new Facts();
        setDataFromCheckIn(opdCheckIn, opdVisit, opdDetails, facts);
        if (covid19CalculateRiskFactorForm != null) {
            generateCovid19CalculateRiskFactorFacts(covid19CalculateRiskFactorForm, facts);
        }

        if (opdCovid19VaccinationEligibilityCheckForm != null) {
            generateCovid19EligibilityFacts(opdCovid19VaccinationEligibilityCheckForm, facts);
        }

        if (opdCovid19VaccinationForm != null) {
            generateCovid19VaccinationRecordFacts(opdCovid19VaccinationForm, facts);
        }

        if(opdMedicalCheckForm != null){
            generateInfluenzaMedicalCheckFacts(opdMedicalCheckForm, facts);
        }

        if (opdInfluenzaVaccineAdministrationForm != null){
            generateInfluenzaVaccinationRecordFacts(opdInfluenzaVaccineAdministrationForm,facts);
        }

        try {
            generateYamlConfigList(facts, yamlConfigListGlobal);
        } catch (IOException ioException) {
            Timber.e(ioException);
        }

        onFinishedCallback.onFinished(facts, yamlConfigListGlobal);
    }

    public void generateCovid19CalculateRiskFactorFacts(@Nullable OpdCovid19CalculateRiskFactorForm covid19CalculateRiskFactorForm, @NonNull Facts facts) {
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.CalculateRiskFactor.PRE_EXISTING_CONDITIONS, covid19CalculateRiskFactorForm.getPreExistingConditions());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.CalculateRiskFactor.OTHER_PRE_EXISTING_CONDITIONS, covid19CalculateRiskFactorForm.getOtherPreExistingConditions());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.CalculateRiskFactor.OCCUPATION, covid19CalculateRiskFactorForm.getOccupation());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.CalculateRiskFactor.OTHER_OCCUPATION, covid19CalculateRiskFactorForm.getOtherOccupation());
    }

    public void generateCovid19EligibilityFacts(@Nullable OpdCovid19VaccinationEligibilityCheckForm opdCovid19VaccinationEligibilityCheckForm, @NonNull Facts facts) {
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.VaccinationEligibility.TEMPERATURE, opdCovid19VaccinationEligibilityCheckForm.getTemperature());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.VaccinationEligibility.COVID_19_HISTORY, opdCovid19VaccinationEligibilityCheckForm.getCovid19History());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.VaccinationEligibility.RESPIRATORY_SYMPTOMS, opdCovid19VaccinationEligibilityCheckForm.getRespiratorySymptoms());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.VaccinationEligibility.OTHER_RESPIRATORY_SYMPTOMS, opdCovid19VaccinationEligibilityCheckForm.getOtherRespiratorySymptoms());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.VaccinationEligibility.ALLERGIES, opdCovid19VaccinationEligibilityCheckForm.getAllergies());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.VaccinationEligibility.ORAL_CONFIRMATION, opdCovid19VaccinationEligibilityCheckForm.getOralConfirmation());
    }

    public void generateCovid19VaccinationRecordFacts(@Nullable OpdCovid19VaccinationForm opdCovid19VaccinationForm, @NonNull Facts facts) {
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.VaccineRecord.COVID_19_ANTIGENS, opdCovid19VaccinationForm.getCovid19Antigens());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.VaccineRecord.SITE_OF_ADMINISTRATION, opdCovid19VaccinationForm.getSiteOfAdministration());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.VaccineRecord.ADMINISTRATION_DATE, opdCovid19VaccinationForm.getAdministrationDate());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.VaccineRecord.ADMINISTRATION_ROUTE, opdCovid19VaccinationForm.getAdministrationRoute());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.VaccineRecord.LOT_NUMBER, opdCovid19VaccinationForm.getLotNumber());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.VaccineRecord.VACCINE_EXPIRY, opdCovid19VaccinationForm.getVaccineExpiry());
    }

    public void generateInfluenzaMedicalCheckFacts(@Nullable OpdMedicalCheckForm opdMedicalCheckForm, @NonNull Facts facts){
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.OpdMedicalCheck.PRE_EXISTING_CONDITIONS, opdMedicalCheckForm.getPreExistingConditions());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.OpdMedicalCheck.OTHER_PRE_EXISTING_CONDITIONS, opdMedicalCheckForm.getOtherPreExistingConditions());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.OpdMedicalCheck.ALLERGIES, opdMedicalCheckForm.getAllergies());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.OpdMedicalCheck.OTHER_ALLERGIES, opdMedicalCheckForm.getOtherAllergies());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.OpdMedicalCheck.TEMPERATURE, opdMedicalCheckForm.getTemperature());
    }

    public void generateInfluenzaVaccinationRecordFacts(@Nullable OpdInfluenzaVaccineAdministrationForm opdInfluenzaVaccineAdministrationForm, @NonNull Facts facts) {
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.INFLUENZA_VACCINE, opdInfluenzaVaccineAdministrationForm.getInfluenzaVaccines());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.SITE_OF_ADMINISTRATION, opdInfluenzaVaccineAdministrationForm.getInfluenzaSiteOfAdministration());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.ADMINISTRATION_DATE, opdInfluenzaVaccineAdministrationForm.getInfluenzaAdministrationDate());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.ADMINISTRATION_ROUTE, opdInfluenzaVaccineAdministrationForm.getInfluenzaAdministrationRoute());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.LOT_NUMBER, opdInfluenzaVaccineAdministrationForm.getInfluenzaLotNumber());
        OpdFactsUtil.putNonNullFact(facts, KipConstants.DbConstants.Columns.InfluenzaVaccineRecord.VACCINE_EXPIRY, opdInfluenzaVaccineAdministrationForm.getInfluenzaVaccineExpiry());
    }

    private void generateYamlConfigList(@NonNull Facts facts, @NonNull List<YamlConfigWrapper> yamlConfigListGlobal) throws IOException {
        Iterable<Object> ruleObjects = loadFile(FilePath.FILE.OPD_PROFILE_OVERVIEW);
        addCovidFlags(facts);
        for (Object ruleObject : ruleObjects) {
            List<YamlConfigWrapper> yamlConfigList = new ArrayList<>();
            int valueCount = 0;

            YamlConfig yamlConfig = (YamlConfig) ruleObject;
            if (yamlConfig.getGroup() != null) {
                yamlConfigList.add(new YamlConfigWrapper(yamlConfig.getGroup(), null, null));
            }

            if (yamlConfig.getSubGroup() != null) {
                yamlConfigList.add(new YamlConfigWrapper(null, yamlConfig.getSubGroup(), null));
            }

            List<YamlConfigItem> configItems = yamlConfig.getFields();

            if (configItems != null) {

                for (YamlConfigItem configItem : configItems) {
                    String relevance = configItem.getRelevance();
                    if (relevance != null && OpdLibrary.getInstance().getOpdRulesEngineHelper()
                            .getRelevance(facts, relevance)) {
                        yamlConfigList.add(new YamlConfigWrapper(null, null, configItem));
                        valueCount += 1;
                    }
                }
            }

            if (valueCount > 0) {
                yamlConfigListGlobal.addAll(yamlConfigList);
            }
        }
    }

    private Iterable<Object> loadFile(@NonNull String filename) throws IOException {
        return OpdLibrary.getInstance().readYaml(filename);
    }

    @Override
    public void setClient(@NonNull CommonPersonObjectClient client) {
        this.client = client;
    }

    private void addCovidFlags(@NonNull Facts facts) {
        CommonPersonObjectClient commonPersonObjectClient = client;
        Map<String, String> clientDetails = OpdUtils.getClientDemographicDetails(commonPersonObjectClient.getDetails().get(KipConstants.KEY.ID_LOWER_CASE));
        if (clientDetails != null) {
            if (StringUtils.isNotEmpty(clientDetails.get(KipConstants.COVID_19_RISK_FACTOR))) {
                OpdFactsUtil.putNonNullFact(facts, KipConstants.COVID_19_RISK_FACTOR, returnRiskFactor(clientDetails.get(KipConstants.COVID_19_RISK_FACTOR)));
            }
            if (StringUtils.isNotEmpty(clientDetails.get(KipConstants.COVID_19_VACCINE_ELIGIBILITY))) {
                OpdFactsUtil.putNonNullFact(facts, KipConstants.COVID_19_VACCINE_ELIGIBILITY, returnEligibility(clientDetails.get(KipConstants.COVID_19_VACCINE_ELIGIBILITY)));
            }
            if (StringUtils.isNotEmpty(clientDetails.get(KipConstants.COVID_19_VACCINE_GIVEN))) {
                OpdFactsUtil.putNonNullFact(facts, KipConstants.COVID_19_VACCINE_GIVEN, returnVaccinationStatus(clientDetails.get(KipConstants.COVID_19_VACCINE_GIVEN)));
            }

            if (StringUtils.isNotEmpty(clientDetails.get(KipConstants.INFLUENZA_VACCINE_GIVEN))) {
                OpdFactsUtil.putNonNullFact(facts, KipConstants.INFLUENZA_VACCINE_GIVEN, returnVaccinationStatus(clientDetails.get(KipConstants.INFLUENZA_VACCINE_GIVEN)));
            }
            OpdFactsUtil.putNonNullFact(facts, KipConstants.COVID_19_VACCINE_NEXT_DATE, clientDetails.get(KipConstants.COVID_19_VACCINE_NEXT_DATE));
        }
    }

    private String returnRiskFactor(String calculatedRiskFactor) {
        String riskFactor = "Not Checked yet";
        if (StringUtils.isNotEmpty(calculatedRiskFactor)) {
            if (calculatedRiskFactor.equalsIgnoreCase("2")) {
                riskFactor = "High Risk";
            } else if (calculatedRiskFactor.equalsIgnoreCase("1")) {
                riskFactor = "Medium Risk";
            } else {
                riskFactor = "Low Risk";
            }
        }
        return riskFactor;
    }

    private String returnEligibility(String eligibility) {
        String riskFactor = "Not Eligible";
        if (StringUtils.isNotEmpty(eligibility)) {
            if (eligibility.equalsIgnoreCase("1")) {
                riskFactor = "Eligible";
            }
        }
        return riskFactor;
    }

    private static String returnVaccinationStatus(String VaccineStatus) {
        String riskFactor = "Not Vaccinated";
        if (StringUtils.isNotEmpty(VaccineStatus)) {
            if (VaccineStatus.equalsIgnoreCase("1")) {
                riskFactor = "Vaccinated";
            }
        }
        return riskFactor;
    }

    private static CommonPersonObjectClient commonPersonObjectClient = client;

    public static String getVaccinationStatus(){

        return returnVaccinationStatus(commonPersonObjectClient.getDetails().get(KipConstants.COVID_19_VACCINE_GIVEN));
    }

    public static String getnextVaccineDate(){
        return commonPersonObjectClient.getDetails().get(KipConstants.COVID_19_VACCINE_NEXT_DATE);
    }
}
