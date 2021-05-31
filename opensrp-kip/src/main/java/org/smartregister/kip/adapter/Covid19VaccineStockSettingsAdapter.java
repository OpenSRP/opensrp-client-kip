package org.smartregister.kip.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.smartregister.kip.R;
import org.smartregister.kip.domain.KipServerSetting;
import org.smartregister.kip.util.KipJsonFormUtils;

import java.util.List;

public class Covid19VaccineStockSettingsAdapter extends RecyclerView.Adapter<Covid19VaccineStockSettingsAdapter.ViewHolder> {

    private final List<KipServerSetting> mData;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    public Covid19VaccineStockSettingsAdapter(Context context, List<KipServerSetting> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.activity_covid19_vaccine_stock_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        KipServerSetting serverSetting = mData.get(position);
        String label = serverSetting.getLabel() != null ? serverSetting.getLabel() : "";
        holder.labelTextView.setText(label);
        holder.valueTextView.setText(serverSetting.getValue());
        holder.info.setTag(serverSetting.getKey());
        holder.info.setTag(R.id.COVID19_STOCK, "Lot Number: " + KipJsonFormUtils.splitValue(serverSetting.getDescription())[0] + "\n" + "Expiry Date: " + KipJsonFormUtils.splitValue(serverSetting.getDescription())[1]);
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView labelTextView;
        private final TextView valueTextView;
        private final View info;

        ViewHolder(View itemView) {
            super(itemView);
            labelTextView = itemView.findViewById(R.id.label);
            valueTextView = itemView.findViewById(R.id.value);
            info = itemView.findViewById(R.id.info);
            info.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
}
