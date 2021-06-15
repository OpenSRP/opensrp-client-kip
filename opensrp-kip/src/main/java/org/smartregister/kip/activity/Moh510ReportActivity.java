package org.smartregister.kip.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellReference;
import org.smartregister.kip.R;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class Moh510ReportActivity extends AppCompatActivity {

    private File filePath = new File(Environment.getExternalStorageState()+"moh510Report.xls");

    private String[] columnsVariable = {"KIP ID", "CHILD'S NAME", "SEX", "DATE OF BIRTH (DD/MM/YYYY)",
            "DATE FIRST SEEN", "FATHER'S FULL NAME", "MOTHER'S FULL NAME", "MOTHER'S PHONE NUMBER","VILLAGE/ESTATE/LANDMARK",
            "TELEPHONE NUMBER", "BCG", "POLIO BIRTH DOSE", "OPV1", "OPV2", "OPV3","IPV", "DPT/HEP.B/HIB.1","DPT/HEP.B/HIB.2",
            "DPT/HEP.B/HIB.3", "PCV 10 (PNEUMOCOCCAL) 1","PCV 10 (PNEUMOCOCCAL) 2","PCV 10 (PNEUMOCOCCAL) 3", "ROTA 1",
            "ROTA 2", "VITAMIN A","MEASLES 1","YELLOW FEVER", "FULLY IMMUNIZED", "MEASLES 2 (MR 2)", "REMARKS"};

    private List<String> mohIndicators = Arrays.asList(columnsVariable);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moh510_report);

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
    }

    public void buttonWriteToExcel(View view){

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDateTime now = LocalDateTime.now();

            HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
            HSSFSheet sheets = hssfWorkbook.createSheet("moh 510 Report");
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
            HSSFCell enddate = dateRange.createCell(3);
            tittleCell.setCellValue("Permanent Immunization Register(MOH 510)");
            facilityCell.setCellValue("Health Facility: Asayi Dispensary");
            yearCell.setCellValue("Year of Enrollment");

            strStart.setCellValue("Start Date");
            strEnd.setCellValue("End Date");
            enddate.setCellValue(dtf.format(now));
            startDate.setCellValue(dtf.format(now));

//            Column names
            for (int cell = 0; cell<mohIndicators.size(); cell ++){
                HSSFCell cell1 = columnName.createCell(cell);
                String value = mohIndicators.get(cell);
                cell1.setCellValue(value);
            }

            int columnNumber = 10;

//            row data
            for (int dataRow = 5; dataRow < columnNumber; dataRow ++){

                HSSFRow queryRow = sheets.createRow(dataRow);
                for (int dataCell = 0; dataCell<mohIndicators.size(); dataCell ++){

                    HSSFCell queryDataCell = queryRow.createCell(dataCell);

                    String str = new CellReference(queryDataCell).formatAsString();
                    queryDataCell.setCellValue(str);
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
    }
}
