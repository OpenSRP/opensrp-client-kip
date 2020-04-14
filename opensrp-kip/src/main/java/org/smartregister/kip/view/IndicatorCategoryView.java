package org.smartregister.kip.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.smartregister.kip.R;
import org.smartregister.kip.domain.Tally;
import org.smartregister.kip.util.KipReportUtils;

import java.util.ArrayList;

import timber.log.Timber;

import static org.smartregister.kip.service.Moh710CustomeService.MOH_BCG;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_BCG_OV;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_BCG_UN;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_IPV;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_IPV_OV;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_IPV_UN;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_MR_1;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_MR_1_OV;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_MR_1_UN;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_OPV_0;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_OPV_1;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_OPV_1_OV;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_OPV_1_UN;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_OPV_2;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_OPV_2_OV;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_OPV_2_UN;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_OPV_3;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_OPV_3_OV;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_OPV_3_UN;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_OPV_OV;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_OPV_UN;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_PCV_1;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_PCV_1_OV;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_PCV_1_UN;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_PCV_2;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_PCV_2_OV;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_PCV_2_UN;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_PENTA_1_OV;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_PENTA_1_UN;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_PENTA_2_OV;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_PENTA_2_UN;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_PENTA_3_OV;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_PENTA_3_UN;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_Penta_1;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_Penta_2;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_Penta_3;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_ROTA_1_OV;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_ROTA_1_UN;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_ROTA_2_OV;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_ROTA_2_UN;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_RTSS_1;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_RTSS_1_OV;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_RTSS_1_UN;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_RTSS_2;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_RTSS_2_OV;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_RTSS_2_UN;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_RTSS_3;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_RTSS_3_OV;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_RTSS_3_UN;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_Rota_1;
import static org.smartregister.kip.service.Moh710CustomeService.MOH_Rota_2;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-07-11
 */

public class IndicatorCategoryView extends LinearLayout {
    private Context context;
    private TableLayout indicatorTable;
    private ArrayList<Tally> tallies;

    public IndicatorCategoryView(Context context) {
        super(context);
        init(context);
    }

    public IndicatorCategoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IndicatorCategoryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public IndicatorCategoryView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_indicator_category, this, true);
        indicatorTable = findViewById(R.id.indicator_table);
    }

    public void setTallies(ArrayList<Tally> tallies) {
        this.tallies = tallies;
        refreshIndicatorTable();
    }

    private void refreshIndicatorTable() {
        if (tallies != null) {
            for (Tally curTally : tallies) {
                TableRow dividerRow = new TableRow(context);
                View divider = new View(context);
                TableRow.LayoutParams params = (TableRow.LayoutParams) divider.getLayoutParams();
                if (params == null) params = new TableRow.LayoutParams();
                params.width = TableRow.LayoutParams.MATCH_PARENT;
                params.height = getResources().getDimensionPixelSize(R.dimen.indicator_table_divider_height);
                params.span = 3;
                divider.setLayoutParams(params);
                divider.setBackgroundColor(getResources().getColor(R.color.client_list_header_dark_grey));
                dividerRow.addView(divider);
                indicatorTable.addView(dividerRow);

                TableRow curRow = new TableRow(context);

                TextView nameTextView = new TextView(context);
                nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        getResources().getDimension(R.dimen.indicator_table_contents_text_size));
                nameTextView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                nameTextView.setPadding(
                        getResources().getDimensionPixelOffset(R.dimen.table_row_side_margin),
                        getResources().getDimensionPixelSize(R.dimen.table_contents_text_v_margin),
                        getResources().getDimensionPixelSize(R.dimen.table_row_middle_margin),
                        getResources().getDimensionPixelSize(R.dimen.table_contents_text_v_margin));
                int resourceId = this.getResources().getIdentifier(KipReportUtils.getStringIdentifier(
                        curTally.getIndicator()), "string", getContext().getPackageName());
                String name = indicatorName(curTally.getIndicator());
                nameTextView.setText(name);
                nameTextView.setTextColor(getResources().getColor(R.color.client_list_grey));
                curRow.addView(nameTextView);

//                center

                TextView ageTextView = new TextView(context);
                ageTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        getResources().getDimension(R.dimen.indicator_table_contents_text_size));
                ageTextView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
                ageTextView.setPadding(
                        getResources().getDimensionPixelOffset(R.dimen.table_row_side_margin),
                        getResources().getDimensionPixelSize(R.dimen.table_contents_text_v_margin),
                        getResources().getDimensionPixelSize(R.dimen.table_row_middle_margin),
                        getResources().getDimensionPixelSize(R.dimen.table_contents_text_v_margin));
//                int resourceId = this.getResources().getIdentifier(KipReportUtils.getStringIdentifier(
//                        curTally.getIndicator()), "string", getContext().getPackageName());
                String age = indicatorAge(curTally.getIndicator());
                ageTextView.setText(age);
                ageTextView.setTextColor(getResources().getColor(R.color.client_list_grey));
                curRow.addView(ageTextView);

                TextView valueTextView = new TextView(context);
                valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        getResources().getDimension(R.dimen.indicator_table_contents_text_size));
                valueTextView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                valueTextView.setPadding(
                        getResources().getDimensionPixelSize(R.dimen.table_row_middle_margin),
                        getResources().getDimensionPixelSize(R.dimen.table_contents_text_v_margin),
                        getResources().getDimensionPixelSize(R.dimen.table_row_side_margin),
                        getResources().getDimensionPixelSize(R.dimen.table_contents_text_v_margin));
                valueTextView.setTextColor(getResources().getColor(R.color.client_list_grey));
//                String indi = resourceId != 0 ? getResources().getString(resourceId) : curTally.getIndicator();
//                valueTextView.setText(indi);
                valueTextView.setText(curTally.getValue());

                curRow.addView(valueTextView);
                indicatorTable.addView(curRow);

                }
            }
        }

    private String indicatorAge(String age) {
        String name = null;

        if (age.contains("Under_1")) {
            name = "Under 1 Year";
        }
        if (age.contains("Over_1")) {
            name = "Over 1 Year";
        }

        return name;
    }

    private String indicatorName(String indicators){

        String bcgName = null;
        if (indicators.equals(MOH_BCG_UN) || indicators.equals(MOH_BCG_OV)){
            bcgName = MOH_BCG;
        }

        if (indicators.equals(MOH_OPV_UN) || indicators.equals(MOH_OPV_OV)){
            bcgName = MOH_OPV_0;
        }

        if (indicators.equals(MOH_OPV_1_OV) || indicators.equals(MOH_OPV_1_UN)){
            bcgName = MOH_OPV_1;
        }

        if (indicators.equals(MOH_OPV_2_OV) || indicators.equals(MOH_OPV_2_UN)){
            bcgName = MOH_OPV_2;
        }

        if (indicators.equals(MOH_OPV_3_OV) || indicators.equals(MOH_OPV_3_UN)){
            bcgName = MOH_OPV_3;
        }

        if (indicators.equals(MOH_PENTA_1_OV) || indicators.equals(MOH_PENTA_1_UN)){
            bcgName = MOH_Penta_1;
        }

        if (indicators.equals(MOH_PENTA_2_OV) || indicators.equals(MOH_PENTA_2_UN)){
            bcgName = MOH_Penta_2;
        }

        if (indicators.equals(MOH_PENTA_1_OV) || indicators.equals(MOH_PENTA_1_UN)){
            bcgName = MOH_Penta_1;
        }

        if (indicators.equals(MOH_PENTA_3_OV) || indicators.equals(MOH_PENTA_3_UN)){
            bcgName = MOH_Penta_3;
        }

        if (indicators.equals(MOH_PCV_1_OV) || indicators.equals(MOH_PCV_1_UN)){
            bcgName = MOH_PCV_1;
        }
        if (indicators.equals(MOH_PCV_2_OV) || indicators.equals(MOH_PCV_2_UN)){
            bcgName = MOH_PCV_2;
        }

        if (indicators.equals(MOH_ROTA_1_OV) || indicators.equals(MOH_ROTA_1_UN)){
            bcgName = MOH_Rota_1;
        }

        if (indicators.equals(MOH_ROTA_2_OV)  || indicators.equals(MOH_ROTA_2_UN)){
            bcgName = MOH_Rota_2;
        }

        if (indicators.equals(MOH_RTSS_1_OV)  || indicators.equals(MOH_RTSS_1_UN)){
            bcgName = MOH_RTSS_1;
        }

        if (indicators.equals(MOH_RTSS_2_OV)  || indicators.equals(MOH_RTSS_2_UN)){
            bcgName = MOH_RTSS_2;
        }

        if (indicators.equals(MOH_RTSS_3_OV)  || indicators.equals(MOH_RTSS_3_UN)){
            bcgName = MOH_RTSS_3;
        }

        if (indicators.equals(MOH_MR_1_OV)  || indicators.equals(MOH_MR_1_UN)){
            bcgName = MOH_MR_1;
        }

        if (indicators.equals(MOH_IPV_OV)  || indicators.equals(MOH_IPV_UN)){
            bcgName = MOH_IPV;
        }



        return bcgName;
    }
}

