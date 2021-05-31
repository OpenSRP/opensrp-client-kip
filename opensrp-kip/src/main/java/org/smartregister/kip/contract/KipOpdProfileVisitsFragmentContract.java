package org.smartregister.kip.contract;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import org.jeasy.rules.api.Facts;
import org.smartregister.kip.pojo.KipOpdVisitSummary;
import org.smartregister.opd.contract.OpdProfileVisitsFragmentContract;
import org.smartregister.opd.domain.YamlConfigWrapper;

import java.util.ArrayList;
import java.util.List;

public interface KipOpdProfileVisitsFragmentContract extends OpdProfileVisitsFragmentContract {

    interface Presenter extends OpdProfileVisitsFragmentContract.Presenter {

        void populateKipWrapperDataAndFacts(@NonNull List<KipOpdVisitSummary> opdVisitSummaries, @NonNull ArrayList<Pair<YamlConfigWrapper, Facts>> items);
        void loadKipVisits(@NonNull String baseEntityId, @NonNull OnKipFinishedCallback onFinishedCallback);


        interface OnKipFinishedCallback {

            void onKipFinished(@NonNull List<KipOpdVisitSummary> opdVisitSummaries, @NonNull ArrayList<Pair<YamlConfigWrapper, Facts>> items);
        }
        interface OnKipVisitsLoadedCallback {

            void onKipVisitsLoaded(@NonNull List<KipOpdVisitSummary> opdVisitSummaries);
        }
    }

    interface View extends OpdProfileVisitsFragmentContract.View {

        void displayKipVisits(@NonNull List<KipOpdVisitSummary> kipOpdVisitSummaries, @NonNull ArrayList<Pair<YamlConfigWrapper, Facts>> items);
    }

    interface Interactor extends OpdProfileVisitsFragmentContract.Interactor {

        void fetchKipVisits(@NonNull String baseEntityId, int pageNo, @NonNull Presenter.OnKipVisitsLoadedCallback onVisitsLoadedCallback);
    }
}