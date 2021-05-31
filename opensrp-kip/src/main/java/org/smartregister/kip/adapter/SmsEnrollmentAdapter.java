package org.smartregister.kip.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.smartregister.kip.R;
import org.smartregister.kip.model.SmsEnrollementModel;

import java.util.ArrayList;
import java.util.Objects;

public class SmsEnrollmentAdapter extends BaseAdapter {

    private Context context;
    private static ArrayList<SmsEnrollementModel> modelArrayList;

    public SmsEnrollmentAdapter() {
    }

    public SmsEnrollmentAdapter(Context context, ArrayList<SmsEnrollementModel> modelArrayList){
        this.context = context;
        SmsEnrollmentAdapter.modelArrayList = modelArrayList;
    }
    @Override
    public int getViewTypeCount() {
        return getCount();
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    @Override
    public int getCount() {
        return modelArrayList.size();
    }
    @Override
    public Object getItem(int position) {
        return modelArrayList.get(position);
    }
    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = Objects.requireNonNull(inflater).inflate(R.layout.content_sms_enrollment, null, true);
            holder.checkBox = convertView.findViewById(R.id.checkBox);
            holder.tvClient = convertView.findViewById(R.id.clientNameList);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.checkBox.setText(modelArrayList.get(position).getPhoneNumber());
        holder.tvClient.setText(modelArrayList.get(position).getClient());
        holder.checkBox.setChecked(modelArrayList.get(position).getSelected());
        holder.checkBox.setTag(R.integer.btnPlusView, convertView);
        holder.checkBox.setTag(position);
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer pos = (Integer) holder.checkBox.getTag();
                Toast.makeText(context, "Checkbox " +pos+ "Clicked!",
                        Toast.LENGTH_SHORT).show();
                if (modelArrayList.get(pos).getSelected()) {
                    modelArrayList.get(pos).setSelected(false);

                } else {
                    modelArrayList.get(pos).setSelected(true);

                }

                if (modelArrayList.get(pos).getSelected()==true){
                    phoneNumber.add(modelArrayList.get(pos).getPhoneNumber());

                } else{
                    phoneNumber.remove(modelArrayList.get(pos).getPhoneNumber());
                }
                System.out.println("selected Items --->.>>.>"+phoneNumber);
            }

        });

        return convertView;
    }
    private class ViewHolder {
        CheckBox checkBox;
        private TextView tvClient;
    }

    public ArrayList<String> phoneNumber = new ArrayList<>();
}
