package org.smartregister.kip.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.smartregister.child.util.Utils;
import org.smartregister.kip.R;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.pojo.Moh510SummaryReport;
import org.smartregister.kip.repository.Moh510SummaryReportRepository;
import org.smartregister.kip.util.KipChildUtils;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.opd.utils.OpdUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import timber.log.Timber;


public class Moh510ReportActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, View.OnClickListener {

    Moh510SummaryReportRepository summaryReportRepository = KipApplication.getInstance().moh510SummaryReportRepository();
    private File filePath = new File(Environment.getExternalStorageDirectory()+"/moh510Report.xls");
    private int mYear, mMonth, mDay, mHour, mMinute;
    ImageButton closeReport;
    EditText endTextDate, startEditTextDate;
    Button cancel, endDateBtn,startDateBtn, customDateRangeBtn;
    FrameLayout frameLayout, download;

    private String[] columnsVariable = {"KIP ID", "CHILD'S NAME", "SEX", "DATE OF BIRTH (DD/MM/YYYY)",
            "DATE FIRST SEEN", "FATHER'S FULL NAME", "MOTHER'S FULL NAME", "MOTHER'S PHONE NUMBER","VILLAGE/ESTATE/LANDMARK",
            "TELEPHONE NUMBER", "BCG", "POLIO BIRTH DOSE", "OPV1", "OPV2", "OPV3","IPV", "DPT/HEP.B/HIB.1","DPT/HEP.B/HIB.2",
            "DPT/HEP.B/HIB.3", "PCV 10 (PNEUMOCOCCAL) 1","PCV 10 (PNEUMOCOCCAL) 2","PCV 10 (PNEUMOCOCCAL) 3", "ROTA 1",
            "ROTA 2", "VITAMIN A","MEASLES 1","YELLOW FEVER","Malaria 1","Malaria 2", "Malaria 3", "FULLY IMMUNIZED", "MEASLES 2 (MR 2)", "Malaria 4", "REMARKS"};

    private List<String> mohIndicators = Arrays.asList(columnsVariable);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moh510_report);
        closeReport = findViewById(R.id.close_moh_510_report);

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        endTextDate = findViewById(R.id.end_date);
        startEditTextDate = findViewById(R.id.start_date);
        startDateBtn = findViewById(R.id.start_date_button);
        endDateBtn = findViewById(R.id.end_date_button);
        customDateRangeBtn = findViewById(R.id.custom_date_button);
        frameLayout = findViewById(R.id.custom_date);
        download = findViewById(R.id.download);

        cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        startDateBtn.setOnClickListener(this);
        endDateBtn.setOnClickListener(this);
        customDateRangeBtn.setOnClickListener(this);
        closeReport.setOnClickListener(this);

        if (startEditTextDate.getText().toString() == null){
            startEditTextDate.setVisibility(View.GONE);
        }
        if (endTextDate.getText().toString()==null){
            endTextDate.setVisibility(View.GONE);
        }

    }


    @Override
    public void onClick(View v) {

        if (v == closeReport) {
            Intent intent = new Intent(v.getContext(), ChildRegisterActivity.class);
            startActivityForResult(intent, 0);
            finish();
        } else if (v == startDateBtn){

            // Get Current Date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                            startEditTextDate.setText(dayOfMonth + "-" + "0"+(month + 1) + "-" + year);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();


            if (v == startDateBtn){
                startEditTextDate.setVisibility(View.VISIBLE);
                startDateBtn.setVisibility(View.GONE);
            }if (v==endDateBtn){
                endTextDate.setVisibility(View.VISIBLE);
                endDateBtn.setVisibility(View.GONE);
            }
        } else if(v==cancel){
            startEditTextDate.setText("");
            endTextDate.setText("");
            endDateBtn.setVisibility(View.VISIBLE);
            startDateBtn.setVisibility(View.VISIBLE);
        } else if (v == endDateBtn){

            // Get Current Date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {

                            endTextDate.setText(dayOfMonth + "-" + "0"+(month + 1) + "-" + year);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();


            if (v == startDateBtn){
                startEditTextDate.setVisibility(View.VISIBLE);
                startDateBtn.setVisibility(View.GONE);
            }if (v==endDateBtn){
                endTextDate.setVisibility(View.VISIBLE);
                endDateBtn.setVisibility(View.GONE);
            }
        } else if (v== customDateRangeBtn){
            frameLayout.setVisibility(View.VISIBLE);
            download.setVisibility(View.GONE);
        }

    }

    private String dateConverter(String date) {
        if (date != null) {
            Date dateParser = new Date(Long.parseLong(date));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            return dateFormat.format(dateParser);
        }
        return null;
    }

    Map<Integer, Object[]> reportInfo =
            new TreeMap<Integer, Object[]>();

    public void buttonWriteToExcel(View view){
        int i = 0;
        for (Moh510SummaryReport report:getIndicatorValues()){
            reportInfo.put(i++,new Object[]{report.getKipId(), getFullName(report.getChildFirstName(),report.getChildLastName()),report.getSex(),
                    dateFormat(report.getDob()), dateFormat(report.getDateFirstSeen()), getFullName(report.getFatherFirstName(), report.getFatherLastName()),getFullName(report.getMotherFirstName(),report.getMotherLastName()),
                    report.getMotherPhoneNumber(), report.getVillage(),report.getTelephone(), dateConverter(report.getBcg()),
                    dateConverter(report.getPolioBirthDose()), dateConverter(report.getOpv1()), dateConverter(report.getOpv2()),dateConverter(report.getOpv3()), dateConverter(report.getIpv()),
            dateConverter(report.getDpt1()),dateConverter(report.getDpt2()),dateConverter(report.getDpt3()), dateConverter(report.getPcv1()), dateConverter(report.getPcv2()),dateConverter(report.getPcv3()), dateConverter(report.getRota1()),dateConverter(report.getRota2()),
            dateConverter(report.getVitaminA()),dateConverter(report.getMeasles1()),dateConverter(report.getYellowFever()), dateConverter(report.getMalaria1()),dateConverter(report.getMalaria2()),dateConverter(report.getMalaria3()),dateConverter(report.getFullyImmunized()),dateConverter(report.getMeasles2()),dateConverter(report.getMalaria4())});
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
            HSSFSheet sheets = hssfWorkbook.createSheet("ImmunizationRegister");
            HSSFRow tittle = sheets.createRow(0);
            HSSFRow facility = sheets.createRow(1);
            HSSFRow year = sheets.createRow(2);
            HSSFRow dateRange = sheets.createRow(3);
            HSSFRow columnName = sheets.createRow(4);

            HSSFCell tittleCell = tittle.createCell(0);
            HSSFCell facilityCell = facility.createCell(0);
            HSSFCell yearCell = year.createCell(0);
            HSSFCell strStart = dateRange.createCell(0);
            HSSFCell startDate = dateRange.createCell(1);
            HSSFCell strEnd = dateRange.createCell(2);
            HSSFCell endDate = dateRange.createCell(3);
            tittleCell.setCellValue("Permanent Immunization Register(MOH 510)");
            facilityCell.setCellValue("Health Facility: "+ LocationHelper.getInstance().getOpenMrsReadableName(KipChildUtils.getCurrentLocality()));
            yearCell.setCellValue("Year of Enrollment: ");

            strStart.setCellValue("Start Date");
            strEnd.setCellValue("End Date");
            endDate.setCellValue(endTextDate.getText().toString());
            startDate.setCellValue(startEditTextDate.getText().toString());


//            Column names
            for (int cell = 0; cell<mohIndicators.size(); cell ++){
                HSSFCell cell1 = columnName.createCell(cell);
                String value = mohIndicators.get(cell);
                cell1.setCellValue(value);
            }

            HSSFRow queryRow;
            int columnNumber = getIndicatorValues().size();

//            row data
            Set<Integer> keyId = reportInfo.keySet();
            for (Integer key: keyId){
                queryRow = sheets.createRow(columnNumber++);
                Object [] objectArr = reportInfo.get(key);
                int cellId = 0;
                for (Object obj:objectArr){
                    HSSFCell queryDataCell = queryRow.createCell(cellId++);
                    queryDataCell.setCellValue((String)obj);
                }

            }
            try {

                if (!filePath.exists()) {
                    filePath.createNewFile();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                hssfWorkbook.write(fileOutputStream);

                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Utils.showToast(this, "Report Downloaded Successfully");
    }

    private List<Moh510SummaryReport> getIndicatorValues(){
        List<Moh510SummaryReport> reportList = getMoh510SummaryReport();
        return reportList;
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
        Calendar mCalender = Calendar.getInstance();
        mCalender.set(Calendar.YEAR, year);
        mCalender.set(Calendar.MONTH, month);
        mCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String selectedDate = DateFormat.getDateInstance(DateFormat.FULL).format(mCalender.getTime());
//        endDate.setText(selectedDate);
//        startDate.setText(selectedDate);
    }

    private String dateFormat(String date){
        String datFt = "";
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            Date formDate = dateFormat.parse(date);

            datFt = OpdUtils.convertDate(formDate, "dd-MM-yyyy");
        } catch (Exception e){
            Timber.d("--->dateFormat %s",e.getMessage());
        }

        return datFt;
    }

    private List<Moh510SummaryReport> getMoh510SummaryReport(){
        List<Moh510SummaryReport> moh510SummaryReports = new ArrayList<>();
        SQLiteDatabase db = summaryReportRepository.getReadable();
        Cursor sCursor = null;

        String startDate = startEditTextDate.getText().toString();
        String endDate = endTextDate.getText().toString();


        try {
            if ((startDate == null || startDate.isEmpty()) && (endDate == null || endDate.isEmpty())){
            sCursor = db.rawQuery(summaryReportRepository.sql(),null);}
            else {
                sCursor = db.rawQuery(summaryReportRepository.sqlDateRange( dateFormat(startDate), dateFormat(endDate)),null);
            }
            moh510SummaryReports = summaryReportRepository.readAll(sCursor);

        } catch (Exception e){
            Timber.d("-->getMoh510SummaryReport" + e.getMessage());
        } finally {
            if (sCursor !=null){
                sCursor.close();
            }
        }
        return moh510SummaryReports;
    }

    private String getFullName(String firstName, String lastName){
        return firstName + " " + lastName;
    }
}
