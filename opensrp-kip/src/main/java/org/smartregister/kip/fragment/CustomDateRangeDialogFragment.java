package org.smartregister.kip.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.clientandeventmodel.DateUtil;
import org.smartregister.kip.R;
import org.smartregister.kip.listener.DatePickerListener;
import org.smartregister.kip.listener.DateRangeActionListener;
import org.smartregister.util.Log;

import java.text.ParseException;
import java.util.Date;

@SuppressLint("ValidFragment")
public class CustomDateRangeDialogFragment extends DialogFragment {
    private DateRangeActionListener listener;
    private Date startDate;
    private Date endDate;

    private CustomDateRangeDialogFragment(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;

    }

    public static CustomDateRangeDialogFragment newInstance(
            Date startDate, Date endDate) {
        return new CustomDateRangeDialogFragment(startDate, endDate);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.custom_date_range_dialog_view, container, false);

        final EditText startDateView = (EditText) dialogView.findViewById(R.id.start_date);
        final EditText endDateView = (EditText) dialogView.findViewById(R.id.end_date);
        if (startDate != null) {
            startDateView.setText(formatDate(startDate));
        }

        if (endDate != null) {
            endDateView.setText(formatDate(endDate));
        }

        final TextInputLayout startDateLayout = (TextInputLayout) dialogView.findViewById(R.id.start_date_layout);
        final TextInputLayout endDateLayout = (TextInputLayout) dialogView.findViewById(R.id.end_date_layout);

        setDatePicker(startDateView);
        setDatePicker(endDateView);

        Button cancel = (Button) dialogView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        Button ok = (Button) dialogView.findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String startDateText = startDateView.getText().toString();
                String endDateText = endDateView.getText().toString();
                if (StringUtils.isBlank(startDateText)) {
                    startDateLayout.setError(getString(R.string.start_date_blank));
                    return;
                }

                if (StringUtils.isBlank(endDateText)) {
                    endDateLayout.setError(getString(R.string.end_date_blank));
                    return;
                }

                Date startDate = parseDate(startDateText, startDateLayout);
                if (startDate == null) {
                    return;
                }

                Date endDate = parseDate(endDateText, endDateLayout);
                if (endDate == null) {
                    return;
                }

                if (startDate.after(endDate)) {
                    startDateLayout.setError(getString(R.string.start_after_end));
                    endDateLayout.setError(getString(R.string.end_before_start));
                    return;
                }

                getDialog().dismiss();
                listener.onDateRangeSelected(startDate, endDate);

            }
        });

        return dialogView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            listener = (DateRangeActionListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement DateRangeActionListener");
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // without a handler, the window sizes itself correctly
        // but the keyboard does not show up
        new Handler().post(new Runnable() {
            @Override
            public void run() {

                Window window = getDialog().getWindow();
                Point size = new Point();

                Display display = window.getWindowManager().getDefaultDisplay();
                display.getSize(size);

                int width = size.x;

                window.setLayout((int) (width * 0.9), FrameLayout.LayoutParams.WRAP_CONTENT);
                window.setGravity(Gravity.CENTER);
            }
        });

    }

    private void setDatePicker(final EditText editText) {
        editText.setOnClickListener(new DatePickerListener(getActivity(), editText));
    }

    private Date parseDate(String text, TextInputLayout textInputLayout) {
        try {
            return DateUtil.yyyyMMdd.parse(text);
        } catch (ParseException e) {
            textInputLayout.setError(getString(R.string.wrong_date_format));
        }
        return null;
    }


    private String formatDate(Date date) {
        try {
            return DateUtil.yyyyMMdd.format(date);
        } catch (Exception e) {
            Log.logError(e.getMessage());
        }
        return null;
    }
}
