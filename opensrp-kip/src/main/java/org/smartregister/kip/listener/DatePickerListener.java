package org.smartregister.kip.listener;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.vijay.jsonwizard.utils.DatePickerUtils;

import org.smartregister.clientandeventmodel.DateUtil;
import org.smartregister.kip.R;

import java.util.Calendar;

/**
 * Created by keyman on 11/09/2017.
 */
public class DatePickerListener implements View.OnClickListener {
    private final Context context;
    private final EditText editText;

    public DatePickerListener(Context context, EditText editText) {
        this.context = context;
        this.editText = editText;
    }

    @Override
    public void onClick(View view) {
        //To show current date in the datepicker
        Calendar mcurrentDate = Calendar.getInstance();
        int mYear = mcurrentDate.get(Calendar.YEAR);
        int mMonth = mcurrentDate.get(Calendar.MONTH);
        int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDatePicker = new DatePickerDialog(context, android.app.AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, selectedyear);
                calendar.set(Calendar.MONTH, selectedmonth);
                calendar.set(Calendar.DAY_OF_MONTH, selectedday);

                String dateString = DateUtil.yyyyMMdd.format(calendar.getTime());
                editText.setText(dateString);

            }
        }, mYear, mMonth, mDay);

        mDatePicker.setTitle(context.getString(R.string.set_date));
        mDatePicker.getDatePicker().setCalendarViewShown(false);
        mDatePicker.show();

        DatePickerUtils.themeDatePicker(mDatePicker, new char[]{'d', 'm', 'y'});
    }
}
