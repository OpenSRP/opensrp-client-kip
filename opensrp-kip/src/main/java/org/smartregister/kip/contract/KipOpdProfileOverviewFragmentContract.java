package org.smartregister.kip.contract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.smartregister.kip.pojo.OpdCovid19CalculateRiskFactorForm;
import org.smartregister.kip.pojo.OpdCovid19VaccinationEligibilityCheckForm;
import org.smartregister.kip.pojo.OpdCovid19VaccinationForm;
import org.smartregister.kip.pojo.OpdInfluenzaVaccineAdministrationForm;
import org.smartregister.kip.pojo.OpdMedicalCheckForm;
import org.smartregister.opd.contract.OpdProfileOverviewFragmentContract;
import org.smartregister.opd.pojo.OpdDetails;
import org.smartregister.opd.pojo.OpdVisit;

import java.util.Map;

public interface KipOpdProfileOverviewFragmentContract extends OpdProfileOverviewFragmentContract {
    interface Presenter extends OpdProfileOverviewFragmentContract.Presenter {
        void loadOverviewDataAndDisplay(@Nullable Map<String, String> opdCheckIn, @Nullable OpdVisit opdVisit, @Nullable OpdDetails opdDetails, @Nullable OpdCovid19CalculateRiskFactorForm covid19CalculateRiskFactorForm, @Nullable OpdCovid19VaccinationForm OpdCovid19VaccinationForm, @Nullable OpdCovid19VaccinationEligibilityCheckForm OpdCovid19VaccinationEligibilityCheckForm, @NonNull OpdMedicalCheckForm opdMedicalCheckForm, @NonNull OpdInfluenzaVaccineAdministrationForm opdInfluenzaVaccineAdministrationForm, @NonNull final OnFinishedCallback onFinishedCallback);
    }

    interface View extends OpdProfileOverviewFragmentContract.View {
    }

    interface Model extends OpdProfileOverviewFragmentContract.Model {
        void fetchLastCheckAndVisit(@NonNull String baseEntityId, @NonNull KipOpdProfileOverviewFragmentContract.Model.OnFetchedCallback onFetchedCallback);

        interface OnFetchedCallback {
            void onFetched(@Nullable Map<String, String> opdCheckIn, @Nullable OpdVisit opdVisit, @Nullable OpdDetails opdDetails, @Nullable OpdCovid19CalculateRiskFactorForm covid19CalculateRiskFactorForm, @Nullable OpdCovid19VaccinationForm OpdCovid19VaccinationForm, @Nullable OpdCovid19VaccinationEligibilityCheckForm OpdCovid19VaccinationEligibilityCheckForm, @NonNull OpdMedicalCheckForm opdMedicalCheckForm, @NonNull OpdInfluenzaVaccineAdministrationForm opdInfluenzaVaccineAdministrationForm);
        }
    }
}
