package org.smartregister.kip.fragment;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.CursorCommonObjectFilterOption;
import org.smartregister.cursoradapter.CursorCommonObjectSort;
import org.smartregister.cursoradapter.CursorSortOption;
import org.smartregister.cursoradapter.SmartRegisterPaginatedCursorAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.util.VaccinateActionUtils;
import org.smartregister.kip.R;
import org.smartregister.kip.activity.ChildImmunizationActivity;
import org.smartregister.kip.activity.ChildSmartRegisterActivity;
import org.smartregister.kip.activity.LoginActivity;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.domain.RegisterClickables;
import org.smartregister.kip.helper.SpinnerHelper;
import org.smartregister.kip.option.BasicSearchOption;
import org.smartregister.kip.option.DateSort;
import org.smartregister.kip.option.StatusSort;
import org.smartregister.kip.provider.DefaulterListSmartClientsProvider;
import org.smartregister.kip.servicemode.VaccinationServiceModeOption;
import org.smartregister.provider.SmartRegisterClientsProvider;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.SecuredNativeSmartRegisterActivity;
import org.smartregister.view.dialog.DialogOption;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import util.KipConstants;

import static android.view.View.INVISIBLE;

public class DefaulterListRegisterFragment extends BaseSmartRegisterFragment {
    private final ClientActionHandler clientActionHandler = new ClientActionHandler();
    private static final String defaultCondition = " (" + KipConstants.EC_CHILD_TABLE.DOD + " is NULL OR " + KipConstants.EC_CHILD_TABLE.DOD + " = '') AND ";
    private Holder holder = new Holder();
    com.vijay.jsonwizard.customviews.CheckBox selectOnlyOverdue;

    @Override

    protected SecuredNativeSmartRegisterActivity.DefaultOptionsProvider getDefaultOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.DefaultOptionsProvider() {
            // FIXME path_conflict
            //@Override
            public FilterOption searchFilterOption() {
                return new BasicSearchOption("");
            }

            @Override
            public ServiceModeOption serviceMode() {
                return new VaccinationServiceModeOption(null, "Linda Clinic", new int[]{
                        R.string.child_profile, R.string.epi_number, R.string.contact,
                        R.string.child_next_vaccine
                }, new int[]{4, 2, 2, 2});
            }

            @Override
            public FilterOption villageFilter() {
                return new CursorCommonObjectFilterOption("no village filter", "");
            }

            @Override
            public SortOption sortOption() {
                return new CursorCommonObjectSort(getResources().getString(R.string.woman_alphabetical_sort), "last_interacted_with desc");
            }

            @Override
            public String nameInShortFormForTitle() {
                return context().getStringResource(R.string.kip);
            }
        };
    }

    @Override
    protected SecuredNativeSmartRegisterActivity.NavBarOptionsProvider getNavBarOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.NavBarOptionsProvider() {

            @Override
            public DialogOption[] filterOptions() {
                return new DialogOption[]{};
            }

            @Override
            public DialogOption[] serviceModeOptions() {
                return new DialogOption[]{
                };
            }

            @Override
            public DialogOption[] sortingOptions() {
                return new DialogOption[]{
                        new CursorCommonObjectSort(getResources().getString(R.string.woman_alphabetical_sort), "first_name"),
                        new DateSort("Age", "dob"),
                        new StatusSort("Due Status"),
                        new CursorCommonObjectSort(getResources().getString(R.string.id_sort), "zeir_id")
                };
            }

            @Override
            public String searchHint() {
                return context().getStringResource(R.string.str_search_hint);
            }
        };
    }


    @Override
    protected SmartRegisterClientsProvider clientsProvider() {
        return null;
    }

    @Override
    protected void onInitialization() {
    }

    @Override
    protected void startRegistration() {
        ((ChildSmartRegisterActivity) getActivity()).startFormActivity("kip_child_enrollment", null, null);
    }

    @Override
    protected void onCreation() {
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        getDefaultOptionsProvider();
        try {
            LoginActivity.setLanguage();
        } catch (Exception e) {
            Log.e(getClass().getCanonicalName(), e.getMessage());
        }

        updateLocationText();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            holder = new Holder();
            initializeQueries();
            updateOnlyOverdueCheckbox();
            updateDatePeriodSpinner();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.smart_register_activity_defaulter_list, container, false);
        mView = view;
        onInitialization();
        setupViews(view);
        onResumption();
        return view;
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);

        // Status bar views
        View filterSection = view.findViewById(R.id.filter_selection);
        filterSection.setBackgroundResource(R.drawable.transparent_clicked_background);
        filterSection.setOnClickListener(clientActionHandler);

        if (titleLabelView != null) {
            titleLabelView.setText(getString(R.string.defaulter_list));
        }

        // Client List
        clientsView.setVisibility(View.VISIBLE);
        clientsProgressView.setVisibility(View.INVISIBLE);
        setServiceModeViewDrawableRight(null);
        populateClientListHeaderView(view);

        View globalSearchButton = mView.findViewById(R.id.global_search);
        globalSearchButton.setOnClickListener(clientActionHandler);

        // Disabled Views
        view.findViewById(R.id.btn_report_month).setVisibility(INVISIBLE);
        view.findViewById(R.id.service_mode_selection).setVisibility(INVISIBLE);

        TextView filterCount = (TextView) view.findViewById(R.id.filter_count);
        filterCount.setVisibility(View.GONE);

        ImageView backButton = (ImageView) view.findViewById(R.id.back_button);
        backButton.setVisibility(View.VISIBLE);

        TextView nameInitials = (TextView) view.findViewById(R.id.name_inits);
        nameInitials.setVisibility(View.GONE);
    }

    @Override
    public void setupSearchView(View view) {
    }

    @Override
    protected void goBack() {
        ((ChildSmartRegisterActivity) getActivity()).switchToBaseFragment(null);
    }

    private void initializeQueries() {
        String tableName = KipConstants.CHILD_TABLE_NAME;
        String parentTableName = KipConstants.MOTHER_TABLE_NAME;

        DefaulterListSmartClientsProvider defaulterListSmartClientsProvider = new DefaulterListSmartClientsProvider(getActivity(),
                clientActionHandler, context().alertService(), KipApplication.getInstance().vaccineRepository(), context().commonrepository(tableName));
        clientAdapter = new SmartRegisterPaginatedCursorAdapter(getActivity(), null, defaulterListSmartClientsProvider, context().commonrepository(tableName));
        clientsView.setAdapter(clientAdapter);

        setTablename(tableName);
        SmartRegisterQueryBuilder countqueryBUilder = new SmartRegisterQueryBuilder();
        countqueryBUilder.SelectInitiateMainTableCounts(tableName);

        filters = "";
        joinTable = "";
        mainCondition = filterSelectionCondition(false);
        countSelect = countqueryBUilder.mainCondition(mainCondition);

        super.CountExecute();
        updateCustomCount();

        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.SelectInitiateMainTable(tableName, new String[]{
                tableName + ".relationalid",
                tableName + ".details",
                tableName + ".zeir_id",
                tableName + ".relational_id",
                tableName + ".first_name",
                tableName + ".last_name",
                tableName + ".gender",
                parentTableName + ".first_name as mother_first_name",
                parentTableName + ".last_name as mother_last_name",
                parentTableName + ".dob as mother_dob",
                parentTableName + ".contact_phone_number as mother_phone_number",
                tableName + ".father_name",
                tableName + ".dob",
                tableName + ".epi_card_number",
                tableName + ".contact_phone_number",
                tableName + ".pmtct_status",
                tableName + ".provider_uc",
                tableName + ".provider_town",
                tableName + ".provider_id",
                tableName + ".provider_location_id",
                tableName + ".client_reg_date",
                tableName + ".last_interacted_with",
                tableName + ".inactive",
                tableName + ".lost_to_follow_up",
                tableName + ".cwc_number"
        });
        queryBUilder.customJoin("LEFT JOIN " + parentTableName + " ON  " + tableName + ".relational_id =  " + parentTableName + ".id");
        mainSelect = queryBUilder.mainCondition(mainCondition);
        Sortqueries = ((CursorSortOption) getDefaultOptionsProvider().sortOption()).sort();

        currentlimit = 20;
        currentoffset = 0;

        super.filterandSortInInitializeQueries();
        refresh();
    }

    protected void filter() {
        filters = "";
        joinTable = "";
        mainCondition = filterSelectionCondition(holder.duePeriod, holder.onlyShowOverdue);
        CountExecute();
        filterandSortExecute();
    }

    private void populateClientListHeaderView(View view) {
        LinearLayout clientsHeaderLayout = (LinearLayout) view.findViewById(org.smartregister.R.id.clients_header_layout);
        clientsHeaderLayout.setVisibility(View.GONE);

        LinearLayout headerLayout = (LinearLayout) getLayoutInflater(null).inflate(R.layout.smart_register_child_defaulter_list_header, null);
        clientsView.addHeaderView(headerLayout);
        clientsView.setEmptyView(getActivity().findViewById(R.id.empty_view));
    }

    private void updateOnlyOverdueCheckbox() {
        selectOnlyOverdue = (com.vijay.jsonwizard.customviews.CheckBox) mView.findViewById(R.id.select_only_overdue);
        View selectOnlyOverDueLayout = mView.findViewById(R.id.only_overdue_layout);
        selectOnlyOverDueLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectOnlyOverdue.toggle();
            }
        });

        selectOnlyOverdue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                holder.setOnlyShowOverdue(isChecked);
                filter();
            }
        });
        selectOnlyOverdue.setChecked(false);
    }

    private void updateCustomCount() {
        TextView selectOnlyOverDueText = (TextView) mView.findViewById(R.id.only_overdue_text);
        TextView totalStats = (TextView) mView.findViewById(R.id.total_stats);
        TextView maleStats = (TextView) mView.findViewById(R.id.male_stats);
        TextView femaleStats = (TextView) mView.findViewById(R.id.female_stats);

        Utils.startAsyncTask(new UpdateCustomCountTask(maleStats, femaleStats, totalStats, selectOnlyOverDueText), null);
    }

    private void updateDatePeriodSpinner() {
        View duePeriodSpinnerView = mView.findViewById(R.id.due_period_spinner);
        final SpinnerHelper duePeriodSpinner = new SpinnerHelper(duePeriodSpinnerView);

        View spinnerLayout = mView.findViewById(R.id.due_period_layout);
        spinnerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                duePeriodSpinner.getSpinner().performClick();
            }
        });

        CharSequence[] strings = getActivity().getResources().getTextArray(R.array.due_period_array);
        HintAdapter<CharSequence> adapter = new HintAdapter<CharSequence>(getActivity(), R.layout.item_spinner_default_list, strings);
        adapter.setDropDownViewResource(R.layout.item_spinner_drop_down_default_list);
        duePeriodSpinner.setAdapter(adapter);

        duePeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = ((TextView) view).getText().toString();
                holder.setDuePeriod(text);
                filter();
                updateCustomCount();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        duePeriodSpinner.setSelection(adapter.getCount());
    }

    private String filterSelectionCondition(String gender, Date dueDate, boolean urgentOnly) {
        String genderCondtion = "";
        if (StringUtils.isNotBlank(gender)) {
            genderCondtion = KipConstants.EC_CHILD_TABLE.GENDER + " = '" + gender + "' AND ";
        }
        String dueDateCondition = "";
        if (dueDate != null) {
            dueDateCondition = KipConstants.EC_CHILD_TABLE.DUE_DATE + " >= '" + dueDate.getTime() + "' AND ";
        }

        String alertCondition = alertCondition(urgentOnly);
        return defaultCondition + genderCondtion + dueDateCondition + alertCondition;
    }

    private String filterSelectionCondition(boolean urgentOnly) {
        return filterSelectionCondition(null, null, urgentOnly);
    }

    private String filterSelectionCondition(Date dueDate, boolean urgentOnly) {
        return filterSelectionCondition(null, dueDate, urgentOnly);
    }

    private String alertCondition(boolean urgentOnly) {
        String alertCondition = " (inactive != 'true' and lost_to_follow_up != 'true') AND ( ";
        ArrayList<VaccineRepo.Vaccine> vaccines = VaccineRepo.getVaccines("child");

        if (vaccines.contains(VaccineRepo.Vaccine.bcg2)) {
            vaccines.remove(VaccineRepo.Vaccine.bcg2);
        }

        if (vaccines.contains(VaccineRepo.Vaccine.opv4)) {
            vaccines.remove(VaccineRepo.Vaccine.opv4);
        }

        for (int i = 0; i < vaccines.size(); i++) {
            VaccineRepo.Vaccine vaccine = vaccines.get(i);
            if (i == vaccines.size() - 1) {
                alertCondition += " " + VaccinateActionUtils.addHyphen(vaccine.display()) + " = 'urgent' ";
            } else {
                alertCondition += " " + VaccinateActionUtils.addHyphen(vaccine.display()) + " = 'urgent' or ";
            }
        }

        if (urgentOnly) {
            return alertCondition + ") ";
        }

        alertCondition += " or ";
        for (int i = 0; i < vaccines.size(); i++) {
            VaccineRepo.Vaccine vaccine = vaccines.get(i);
            if (i == vaccines.size() - 1) {
                alertCondition += " " + VaccinateActionUtils.addHyphen(vaccine.display()) + " = 'normal' ";
            } else {
                alertCondition += " " + VaccinateActionUtils.addHyphen(vaccine.display()) + " = 'normal' or ";
            }
        }

        return alertCondition + ") ";
    }

    private int customCount(String gender, boolean urgentOnly) {
        String mainCondition = filterSelectionCondition(gender, holder.duePeriod, urgentOnly);
        return count(mainCondition);
    }

    private int customCount(boolean urgentOnly) {
        return customCount(null, urgentOnly);
    }

    private int count(String mainConditionString) {

        int count = 0;
        Cursor c = null;
        try {
            SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(countSelect);
            String query = "";
            if (isValidFilterForFts(commonRepository())) {
                String sql = sqb.countQueryFts(tablename, "", mainConditionString, "");
                List<String> ids = commonRepository().findSearchIds(sql);
                query = sqb.toStringFts(ids, tablename + "." + CommonRepository.ID_COLUMN);
                query = sqb.Endquery(query);
            } else {
                sqb.addCondition(filters);
                query = sqb.orderbyCondition(Sortqueries);
                query = sqb.Endquery(query);
            }

            Log.i(getClass().getName(), query);
            c = commonRepository().rawCustomQueryForAdapter(query);
            c.moveToFirst();
            count = c.getInt(0);

        } catch (Exception e) {
            Log.e(getClass().getName(), e.toString(), e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return count;
    }

    ////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////

    private class ClientActionHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            CommonPersonObjectClient client = null;
            if (view.getTag() != null && view.getTag() instanceof CommonPersonObjectClient) {
                client = (CommonPersonObjectClient) view.getTag();
            }
            RegisterClickables registerClickables = new RegisterClickables();

            switch (view.getId()) {
                case R.id.child_profile_info_layout:

                    ChildImmunizationActivity.launchActivity(getActivity(), client, null);
                    break;
                case R.id.record_weight:
                    registerClickables.setRecordWeight(true);
                    ChildImmunizationActivity.launchActivity(getActivity(), client, registerClickables);
                    break;

                case R.id.record_vaccination:
                    registerClickables.setRecordAll(true);
                    ChildImmunizationActivity.launchActivity(getActivity(), client, registerClickables);
                    break;
                case R.id.filter_selection:
                    goBack();
                    break;

                case R.id.global_search:
                    ((ChildSmartRegisterActivity) getActivity()).startAdvancedSearch();
                    break;

                case R.id.scan_qr_code:
                    ((ChildSmartRegisterActivity) getActivity()).startQrCodeScanner();
                    break;
                default:
                    break;
            }
        }
    }

    private class UpdateCustomCountTask extends AsyncTask<Void, Void, Integer[]> {
        TextView selectOnlyOverDue;
        TextView totalStats;
        TextView maleStats;
        TextView femaleStats;

        public UpdateCustomCountTask(TextView maleStats, TextView femaleStats, TextView totalStats, TextView selectOnlyOverDue) {
            this.maleStats = maleStats;
            this.femaleStats = femaleStats;
            this.totalStats = totalStats;
            this.selectOnlyOverDue = selectOnlyOverDue;
        }

        @Override
        protected Integer[] doInBackground(Void... params) {
            final String male = "Male";
            final String female = "Female";

            int overDueCount = customCount(true);
            int maleOverDue = customCount(male, true);
            int femaleOverDue = customCount(female, true);

            int dueOverDueCount = customCount(false);
            int maleDueOverDueCount = customCount(male, false);
            int femaleDueOverDueCount = customCount(female, false);


            return new Integer[]{overDueCount, maleOverDue, femaleOverDue, dueOverDueCount, maleDueOverDueCount, femaleDueOverDueCount};
        }

        @Override
        protected void onPostExecute(Integer[] integers) {
            if (integers == null || integers.length != 6) {
                return;
            }

            int overDueCount = integers[0];
            int maleOverDue = integers[1];
            int femaleOverDue = integers[2];

            int dueOverDueCount = integers[3];
            int maleDueOverDueCount = integers[4];
            int femaleDueOverDueCount = integers[5];

            maleStats.setText(String.format(getString(R.string.male_stats), maleOverDue, maleDueOverDueCount));
            femaleStats.setText(String.format(getString(R.string.female_stats), femaleOverDue, femaleDueOverDueCount));
            totalStats.setText(String.format(getString(R.string.total_stats), overDueCount, dueOverDueCount));
            selectOnlyOverDue.setText(String.format(getString(R.string.only_show_overdue_with_count), overDueCount));
        }

    }

    private class HintAdapter<T> extends ArrayAdapter {

        public HintAdapter(Context context, int resource, T[] objects) {
            super(context, resource, objects);
        }

        @Override
        public int getCount() {
            // don't display last item. It is used as hint.
            int count = super.getCount();
            return count > 0 ? count - 1 : count;
        }
    }

    private class Holder {
        private boolean onlyShowOverdue;
        private Date duePeriod;

        public Holder() {
            this.onlyShowOverdue = false;
            duePeriod = null;
        }

        public void setOnlyShowOverdue(boolean onlyShowOverdue) {
            this.onlyShowOverdue = onlyShowOverdue;
        }

        public void setDuePeriod(String duePeriodText) {
            if (StringUtils.isNotBlank(duePeriodText)) {
                Calendar cal = Calendar.getInstance();
                standardiseCalendarDate(cal);
                String[] array = getResources().getStringArray(R.array.due_period_array);
                if (duePeriodText.equals(array[0]) && duePeriodText.contains("1 week")) { // Last 1 week
                    cal.add(Calendar.DAY_OF_MONTH, -7);
                    this.duePeriod = cal.getTime();
                } else if (duePeriodText.equals(array[1]) && duePeriodText.contains("2 weeks")) { // Last 2 weeks
                    cal.add(Calendar.DAY_OF_MONTH, -14);
                    this.duePeriod = cal.getTime();
                } else if (duePeriodText.equals(array[2]) && duePeriodText.contains("1 month")) { // Last 1 month
                    cal.add(Calendar.MONTH, -1);
                    this.duePeriod = cal.getTime();
                } else if (duePeriodText.equals(array[3]) && duePeriodText.contains("3 months")) { // Last 3 months
                    cal.add(Calendar.MONTH, -3);
                    this.duePeriod = cal.getTime();
                } else if (duePeriodText.equals(array[4]) && duePeriodText.contains("all")) { // Show all
                    this.duePeriod = null;
                }
            }
        }

        private void standardiseCalendarDate(Calendar calendarDate) {
            calendarDate.set(Calendar.HOUR_OF_DAY, 0);
            calendarDate.set(Calendar.MINUTE, 0);
            calendarDate.set(Calendar.SECOND, 0);
            calendarDate.set(Calendar.MILLISECOND, 0);
        }
    }

}
