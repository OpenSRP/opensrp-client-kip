package org.smartregister.kip.activity;

import android.os.Build;
import android.os.Bundle;

import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.kip.R;
import org.smartregister.kip.adapter.Covid19VaccineStockSettingsAdapter;
import org.smartregister.kip.contract.BaseSettingsContract;
import org.smartregister.kip.contract.SettingsContract;
import org.smartregister.kip.domain.KipServerSetting;

import java.util.List;

public abstract class BaseSettingsActivity extends BaseActivity
        implements Covid19VaccineStockSettingsAdapter.ItemClickListener, SettingsContract.View {

    protected Toolbar mToolbar;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covid19_vaccine_stock);

        recyclerView = findViewById(R.id.settings);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        BaseSettingsContract.BasePresenter basePresenter = getPresenter();
        basePresenter.getSettings();

        mToolbar = findViewById(R.id.register_toolbar);
        mToolbar.findViewById(R.id.close_settings).setOnClickListener(view -> onBackPressed());

        TextView titleTextView = mToolbar.findViewById(R.id.settings_toolbar_title);
        String title = getToolbarTitle();
        titleTextView.setText(title);

        mToolbar.findViewById(R.id.covid19_vaccine_stock_settings_toolbar_edit)
                .setVisibility(title.equals(getString(R.string.population_characteristics)) ? View.GONE : View.VISIBLE);
    }

    protected abstract BaseSettingsContract.BasePresenter getPresenter();

    protected abstract String getToolbarTitle();

    @Override
    public void onItemClick(View view, int position) {
        renderSubInfoAlertDialog(view.findViewById(R.id.info).getTag(R.id.COVID19_STOCK).toString());
    }

    protected void renderSubInfoAlertDialog(String info) {

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, R.style.KipAlertDialog);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Info").setMessage(info)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> dialog.dismiss()).setIcon(R.drawable.ic_info);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.0f);
    }

    @Override
    public void renderSettings(List<KipServerSetting> characteristics) {
        Covid19VaccineStockSettingsAdapter adapter = new Covid19VaccineStockSettingsAdapter(this, characteristics);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }
}
