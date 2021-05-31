package org.smartregister.kip.model;

import android.support.annotation.NonNull;

import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.contract.KipOpdProfileOverviewFragmentContract;
import org.smartregister.kip.pojo.OpdCovid19CalculateRiskFactorForm;
import org.smartregister.kip.pojo.OpdCovid19VaccinationEligibilityCheckForm;
import org.smartregister.kip.pojo.OpdCovid19VaccinationForm;
import org.smartregister.kip.pojo.OpdInfluenzaVaccineAdministrationForm;
import org.smartregister.kip.pojo.OpdMedicalCheckForm;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.model.OpdProfileOverviewFragmentModel;
import org.smartregister.opd.pojo.OpdDetails;
import org.smartregister.opd.pojo.OpdVisit;
import org.smartregister.opd.utils.AppExecutors;

import java.util.Map;

public class KipOpdProfileOverviewFragmentModel extends OpdProfileOverviewFragmentModel implements KipOpdProfileOverviewFragmentContract.Model {
    private AppExecutors appExecutors;
    private OpdDetails opdDetails = null;
    private OpdCovid19CalculateRiskFactorForm opdCovid19CalculateRiskFactorForm = null;
    private OpdCovid19VaccinationEligibilityCheckForm opdCovid19VaccinationEligibilityCheckForm = null;
    private OpdCovid19VaccinationForm opdCovid19VaccinationForm = null;
    private OpdMedicalCheckForm opdMedicalCheckForm = null;
    private OpdInfluenzaVaccineAdministrationForm opdInfluenzaVaccineAdministrationForm = null;

    public KipOpdProfileOverviewFragmentModel() {
        this.appExecutors = new AppExecutors();
    }

    @Override
    public void fetchLastCheckAndVisit(final @NonNull String baseEntityId, @NonNull final KipOpdProfileOverviewFragmentContract.Model.OnFetchedCallback onFetchedCallback) {
        appExecutors.diskIO().execute(() -> {
            final OpdVisit visit = OpdLibrary.getInstance().getVisitRepository().getLatestVisit(baseEntityId);
            final Map<String, String> checkInMap = visit != null ? OpdLibrary.getInstance().getCheckInRepository().getCheckInByVisit(visit.getId()) : null;

            getOpdDetails(baseEntityId, visit);
            if (opdDetails != null && opdDetails.getCurrentVisitEndDate() == null) {
                getOpdCovid19CalculateRiskFactor(baseEntityId, visit);
                getOpdCovid19VaccinationEligibility(baseEntityId, visit);
                getOpdCovid19VaccinationRecord(baseEntityId, visit);

                getOpdInfluenzaMedicalCheck(baseEntityId,visit);
                getOpdInfluenzaVaccinationRecord(baseEntityId,visit);
            }

            appExecutors.mainThread().execute(() -> onFetchedCallback.onFetched(checkInMap, visit, opdDetails, opdCovid19CalculateRiskFactorForm, opdCovid19VaccinationForm, opdCovid19VaccinationEligibilityCheckForm,opdMedicalCheckForm,opdInfluenzaVaccineAdministrationForm));
        });
    }

    private void getOpdDetails(@NonNull String baseEntityId, OpdVisit visit) {
        opdDetails = null;

        if (visit != null) {
            opdDetails = new OpdDetails(baseEntityId, visit.getId());
            opdDetails = OpdLibrary.getInstance().getOpdDetailsRepository().findOne(opdDetails);
        }
    }

    private void getOpdCovid19CalculateRiskFactor(@NonNull String baseEntityId, OpdVisit visit) {
        opdCovid19CalculateRiskFactorForm = null;

        if (visit != null) {
            opdCovid19CalculateRiskFactorForm = new OpdCovid19CalculateRiskFactorForm();
            opdCovid19CalculateRiskFactorForm.setBaseEntityId(baseEntityId);
            opdCovid19CalculateRiskFactorForm.setVisitId(visit.getId());
            opdCovid19CalculateRiskFactorForm = KipApplication.getInstance().opdCovid19CalculateRiskRepository().findOneByVisit(opdCovid19CalculateRiskFactorForm);
        }
    }

    private void getOpdCovid19VaccinationEligibility(@NonNull String baseEntityId, OpdVisit visit) {
        opdCovid19VaccinationEligibilityCheckForm = null;

        if (visit != null) {
            opdCovid19VaccinationEligibilityCheckForm = new OpdCovid19VaccinationEligibilityCheckForm();
            opdCovid19VaccinationEligibilityCheckForm.setBaseEntityId(baseEntityId);
            opdCovid19VaccinationEligibilityCheckForm.setVisitId(visit.getId());
            opdCovid19VaccinationEligibilityCheckForm = KipApplication.getInstance().opdCovid19VaccinationEligibilityRepository().findOneByVisit(opdCovid19VaccinationEligibilityCheckForm);
        }
    }

    private void getOpdCovid19VaccinationRecord(@NonNull String baseEntityId, OpdVisit visit) {
        opdCovid19VaccinationForm = null;

        if (visit != null) {
            opdCovid19VaccinationForm = new OpdCovid19VaccinationForm();
            opdCovid19VaccinationForm.setBaseEntityId(baseEntityId);
            opdCovid19VaccinationForm.setVisitId(visit.getId());
            opdCovid19VaccinationForm = KipApplication.getInstance().opdCovid19VaccinationRepository().findOneByVisit(opdCovid19VaccinationForm);
        }
    }

    private void getOpdInfluenzaMedicalCheck(@NonNull String baseEntityId, OpdVisit visit) {
        opdMedicalCheckForm = null;

        if (visit != null) {
            opdMedicalCheckForm = new OpdMedicalCheckForm();
            opdMedicalCheckForm.setBaseEntityId(baseEntityId);
            opdMedicalCheckForm.setVisitId(visit.getId());
            opdMedicalCheckForm = KipApplication.getInstance().opdMedicalCheckFormRepository().findOneByVisit(opdMedicalCheckForm);
        }
    }

    private void getOpdInfluenzaVaccinationRecord(@NonNull String baseEntityId, OpdVisit visit) {
        opdInfluenzaVaccineAdministrationForm = null;

        if (visit != null) {
            opdInfluenzaVaccineAdministrationForm = new OpdInfluenzaVaccineAdministrationForm();
            opdInfluenzaVaccineAdministrationForm.setBaseEntityId(baseEntityId);
            opdInfluenzaVaccineAdministrationForm.setVisitId(visit.getId());
            opdInfluenzaVaccineAdministrationForm = KipApplication.getInstance().opdInfluenzaVaccineAdministrationFormRepository().findOneByVisit(opdInfluenzaVaccineAdministrationForm);
        }
    }
}
