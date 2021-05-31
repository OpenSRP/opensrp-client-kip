package org.smartregister.kip.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.jeasy.rules.api.Facts;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.kip.R;
import org.smartregister.kip.contract.KipOpdProfileVisitsFragmentContract;
import org.smartregister.kip.pojo.KipOpdVisitSummary;
import org.smartregister.kip.presenter.KipOpdProfileVisitsFragmentPresenter;
import org.smartregister.opd.adapter.OpdProfileVisitsAdapter;
import org.smartregister.opd.domain.YamlConfigWrapper;
import org.smartregister.opd.fragment.OpdProfileVisitsFragment;
import org.smartregister.opd.listener.OnSendActionToFragment;
import org.smartregister.opd.utils.OpdConstants;

import java.util.ArrayList;
import java.util.List;

public class KipOpdProfileVisitsFragment extends OpdProfileVisitsFragment implements OnSendActionToFragment, KipOpdProfileVisitsFragmentContract.View {
    private KipOpdProfileVisitsFragmentContract.Presenter presenter;
    private String baseEntityId;
    private Button nextPageBtn;
    private Button previousPageBtn;
    private TextView pageCounter;
    private RecyclerView recyclerView;

    public static KipOpdProfileVisitsFragment newInstance(@Nullable Bundle bundle) {
        Bundle args = bundle;
        KipOpdProfileVisitsFragment fragment = new KipOpdProfileVisitsFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initializePresenter() {
        super.initializePresenter();
        if (getActivity() == null) {
            return;
        }
        presenter = new KipOpdProfileVisitsFragmentPresenter(this);
    }

    @Override
    protected void onCreation() {
        initializePresenter();
        if (getArguments() != null) {
            CommonPersonObjectClient commonPersonObjectClient = (CommonPersonObjectClient) getArguments()
                    .getSerializable(OpdConstants.IntentKey.CLIENT_OBJECT);

            if (commonPersonObjectClient != null) {
                baseEntityId = commonPersonObjectClient.getCaseId();
            }
        }
    }

    @Override
    protected void onResumption() {
        presenter.loadPageCounter(baseEntityId);
        presenter.loadKipVisits(baseEntityId, new KipOpdProfileVisitsFragmentContract.Presenter.OnKipFinishedCallback() {
            @Override
            public void onKipFinished(@NonNull List<KipOpdVisitSummary> opdVisitSummaries, @NonNull ArrayList<Pair<YamlConfigWrapper, Facts>> items) {
                displayKipVisits(opdVisitSummaries, items);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.opd_fragment_profile_visits, container, false);

        recyclerView = fragmentView.findViewById(R.id.rv_opdFragmentProfileVisit_recyclerView);
        nextPageBtn = fragmentView.findViewById(R.id.btn_opdFragmentProfileVisit_nextPageBtn);
        previousPageBtn = fragmentView.findViewById(R.id.btn_opdFragmentProfileVisit_previousPageBtn);
        pageCounter = fragmentView.findViewById(R.id.tv_opdFragmentProfileVisit_pageCounter);

        nextPageBtn.setOnClickListener(v -> presenter.onNextPageClicked());
        previousPageBtn.setOnClickListener(v -> presenter.onPreviousPageClicked());

        return fragmentView;
    }

    @Override
    public void onActionReceive() {
        onCreation();
        onResumption();
    }

    @Override
    public void showPageCountText(@NonNull String pageCounterText) {
        this.pageCounter.setText(pageCounterText);
    }

    @Override
    public void showNextPageBtn(boolean show) {
        nextPageBtn.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        nextPageBtn.setClickable(show);
    }

    @Override
    public void showPreviousPageBtn(boolean show) {
        previousPageBtn.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        previousPageBtn.setClickable(show);
    }

    @Nullable
    @Override
    public String getClientBaseEntityId() {
        return baseEntityId;
    }

    @Override
    public void displayKipVisits(@NonNull List<KipOpdVisitSummary> kipOpdVisitSummaries, @NonNull ArrayList<Pair<YamlConfigWrapper, Facts>> items) {
        if (getActivity() != null) {
            OpdProfileVisitsAdapter adapter = new OpdProfileVisitsAdapter(getActivity(), items);
            adapter.notifyDataSetChanged();

            // set up the RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(adapter);
        }
    }
}
