package org.smartregister.kip.repository;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.kip.pojo.Moh510SummaryReport;
import org.smartregister.repository.BaseRepository;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class Moh510SummaryReportRepository extends BaseRepository {

    private String sql(){

        String sql = "SELECT client.opensrp_id AS kip_id, COALESCE(child_details.first_name , ' ', child_details.last_name) As child_full_name,\n" +
                "       client.gender As sex, child_details.dob, client.date AS date_first_seen, COALESCE(father_details.first_name , ' ', father_details.last_name) As father_full_name,\n" +
                "       COALESCE(mother_details.first_name , ' ', mother_details.last_name) As mother_full_name,\n" +
                "       mother_details.phone_number AS mother_phone, client.village, mother_details.phone_number AS telephone_number,\n" +
                "       CASE WHEN vaccines.name = 'bcg' THEN vaccines.date ELSE Null END \"bcg\",\n" +
                "       CASE WHEN vaccines.name = 'opv_0' THEN vaccines.date ELSE Null END \"polio_birth_dose\",\n" +
                "       CASE WHEN vaccines.name = 'opv_1' THEN vaccines.date ELSE Null END \"opv1\",\n" +
                "       CASE WHEN vaccines.name = 'opv_2' THEN vaccines.date ELSE Null END \"opv2\",\n" +
                "       CASE WHEN vaccines.name = 'opv_3' THEN vaccines.date ELSE Null END \"opv3\",\n" +
                "       CASE WHEN vaccines.name = 'ipv' THEN vaccines.date ELSE Null END \"ipv\",\n" +
                "       CASE WHEN vaccines.name = 'penta_1' THEN vaccines.date ELSE Null END \"penta1\",\n" +
                "       CASE WHEN vaccines.name = 'penta_2' THEN vaccines.date ELSE Null END \"penta2\",\n" +
                "       CASE WHEN vaccines.name = 'penta_3' THEN vaccines.date ELSE Null END \"penta3\",\n" +
                "       CASE WHEN vaccines.name = 'pcv_1' THEN vaccines.date ELSE Null END \"pcv1\",\n" +
                "       CASE WHEN vaccines.name = 'pcv_2' THEN vaccines.date ELSE Null END \"pcv2\",\n" +
                "       CASE WHEN vaccines.name = 'pcv_3' THEN vaccines.date ELSE Null END \"pcv3\",\n" +
                "       CASE WHEN vaccines.name = 'rota_1' THEN vaccines.date ELSE Null END \"rota1\",\n" +
                "       CASE WHEN vaccines.name = 'rota_2' THEN vaccines.date ELSE Null END \"rota2\",\n" +
                "       CASE WHEN vaccines.name = 'mr_1' THEN vaccines.date ELSE Null END \"mr1\",\n" +
                "       CASE WHEN vaccines.name = 'vitamin_a' THEN vaccines.date ELSE Null END \"vitamin_a\",\n" +
                "       CASE WHEN vaccines.name = 'yellow_fever' THEN vaccines.date ELSE Null END \"yellow_fever\",\n" +
                "       CASE WHEN vaccines.name = 'mv_1' THEN vaccines.date ELSE Null END \"mv1\",\n" +
                "       CASE WHEN vaccines.name = 'mv_2' THEN vaccines.date ELSE Null END \"mv2\",\n" +
                "       CASE WHEN vaccines.name = 'mv_3' THEN vaccines.date ELSE Null END \"mv3\",\n" +
                "       CASE WHEN vaccines.name = 'mr_1' THEN vaccines.date ELSE Null END \"fully_immunized\",\n" +
                "       CASE WHEN vaccines.name = 'mr_2' THEN vaccines.date ELSE Null END \"mr2\",\n" +
                "       CASE WHEN vaccines.name = 'mv_4' THEN vaccines.date ELSE Null END \"mv4\"\n" +
                "FROM ec_child_details child_details\n" +
                "LEFT JOIN ec_mother_details mother_details ON child_details.base_entity_id = mother_details.base_entity_id\n" +
                "LEFT JOIN ec_father_details father_details ON child_details.base_entity_id = father_details.base_entity_id\n" +
                "LEFT JOIN ec_client client ON child_details.base_entity_id = client.base_entity_id\n" +
                "LEFT JOIN vaccines ON vaccines.base_entity_id = child_details.base_entity_id\n" +
                "GROUP BY child_details.base_entity_id;";

        return sql;
    }

    public List<Moh510SummaryReport> getMoh510SummaryReport(){
        List<Moh510SummaryReport> moh510SummaryReports = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor sCursor = null;

        try {
            sCursor = db.rawQuery(sql(),null);
            moh510SummaryReports = readAll(sCursor);

        } catch (Exception e){
            Timber.d("-->getMoh510SummaryReport" + e.getMessage());
        } finally {
            if (sCursor !=null){
                sCursor.close();
            }
        }
        return moh510SummaryReports;
    }

    public Moh510SummaryReport getMoh510SummaryReport(Cursor cursor){
        Moh510SummaryReport moh510SummaryReport = new Moh510SummaryReport();
        moh510SummaryReport.setKipId(cursor.getString(cursor.getColumnIndex("kip_id")));
        moh510SummaryReport.setChildFullName(cursor.getString(cursor.getColumnIndex("child_full_name")));
        moh510SummaryReport.setSex(cursor.getString(cursor.getColumnIndex("sex")));
        moh510SummaryReport.setDob(cursor.getString(cursor.getColumnIndex("dob")));
        moh510SummaryReport.setDateFirstSeen(cursor.getString(cursor.getColumnIndex("date_first_seen")));
        moh510SummaryReport.setFatherName(cursor.getString(cursor.getColumnIndex("father_full_name")));
        moh510SummaryReport.setMotherName(cursor.getString(cursor.getColumnIndex("mother_full_name")));
        moh510SummaryReport.setMotherPhoneNumber(cursor.getString(cursor.getColumnIndex("mother_phone")));
        moh510SummaryReport.setVillage(cursor.getString(cursor.getColumnIndex("village")));
        moh510SummaryReport.setTelephone(cursor.getString(cursor.getColumnIndex("telephone_number")));
        moh510SummaryReport.setBcg(cursor.getString(cursor.getColumnIndex("bcg")));
        moh510SummaryReport.setPolioBirthDose(cursor.getString(cursor.getColumnIndex("polio_birth_dose")));
        moh510SummaryReport.setOpv1(cursor.getString(cursor.getColumnIndex("opv1")));
        moh510SummaryReport.setOpv2(cursor.getString(cursor.getColumnIndex("opv2")));
        moh510SummaryReport.setOpv3(cursor.getString(cursor.getColumnIndex("opv3")));
        moh510SummaryReport.setIpv(cursor.getString(cursor.getColumnIndex("ipv")));
        moh510SummaryReport.setDpt1(cursor.getString(cursor.getColumnIndex("penta1")));
        moh510SummaryReport.setDpt2(cursor.getString(cursor.getColumnIndex("penta2")));
        moh510SummaryReport.setDpt3(cursor.getString(cursor.getColumnIndex("penta3")));
        moh510SummaryReport.setPcv1(cursor.getString(cursor.getColumnIndex("pcv1")));
        moh510SummaryReport.setPcv2(cursor.getString(cursor.getColumnIndex("pcv2")));
        moh510SummaryReport.setPcv3(cursor.getString(cursor.getColumnIndex("pcv3")));
        moh510SummaryReport.setRota1(cursor.getString(cursor.getColumnIndex("rota1")));
        moh510SummaryReport.setRota2(cursor.getString(cursor.getColumnIndex("rota2")));
        moh510SummaryReport.setVitaminA(cursor.getString(cursor.getColumnIndex("vitamin_a")));
        moh510SummaryReport.setMeasles1(cursor.getString(cursor.getColumnIndex("mr1")));
        moh510SummaryReport.setYellowFever(cursor.getString(cursor.getColumnIndex("yellow_fever")));
        moh510SummaryReport.setMalaria1(cursor.getString(cursor.getColumnIndex("mv1")));
        moh510SummaryReport.setMalaria2(cursor.getString(cursor.getColumnIndex("mv2")));
        moh510SummaryReport.setMalaria3(cursor.getString(cursor.getColumnIndex("mv3")));
        moh510SummaryReport.setFullyImmunized(cursor.getString(cursor.getColumnIndex("fully_immunized")));
        moh510SummaryReport.setMeasles2(cursor.getString(cursor.getColumnIndex("mr2")));
        moh510SummaryReport.setMalaria4(cursor.getString(cursor.getColumnIndex("mv4")));
        moh510SummaryReport.setRemarks(cursor.getString(cursor.getColumnIndex("mv1")));
        return moh510SummaryReport;
    }

    private List<Moh510SummaryReport> readAll(Cursor cursor){
        List<Moh510SummaryReport> moh510SummaryReports = new ArrayList<>();
        if (cursor !=null && cursor.getCount() > 0 && cursor.moveToNext()){
            cursor.moveToFirst();
            while (cursor.getCount() > 0 && !cursor.isAfterLast()){
                moh510SummaryReports.add(getMoh510SummaryReport(cursor));
                cursor.moveToNext();
            }
        }
        return moh510SummaryReports;
    }
}
