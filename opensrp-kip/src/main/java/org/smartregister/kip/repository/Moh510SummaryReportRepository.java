package org.smartregister.kip.repository;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.kip.pojo.Moh510SummaryReport;
import org.smartregister.repository.BaseRepository;

import java.util.ArrayList;
import java.util.List;

public class Moh510SummaryReportRepository extends BaseRepository {

    public String sql(){
        String sql = "SELECT client.opensrp_id AS kip_id, child_details.first_name AS child_first_name , child_details.last_name AS child_last_name,\n" +
                "       client.gender As sex, strftime('%d/%m/%Y',child_details.dob) AS dob, strftime('%d/%m/%Y',client.date) AS date_first_seen, father_details.first_name AS father_first_name, father_details.last_name AS father_last_name,\n" +
                "       mother_details.first_name AS mother_first_name, mother_details.last_name AS mother_last_name,\n" +
                "       mother_details.phone_number AS mother_phone, client.village, mother_details.phone_number AS telephone_number,\n" +
                "       SUM(CASE WHEN vaccines.name = 'bcg' THEN vaccines.date ELSE Null END) \"bcg\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'opv_0' THEN vaccines.date ELSE Null END) \"polio_birth_dose\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'opv_1' THEN vaccines.date ELSE Null END) \"opv1\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'opv_2' THEN vaccines.date ELSE Null END) \"opv2\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'opv_3' THEN vaccines.date ELSE Null END) \"opv3\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'ipv' THEN vaccines.date ELSE Null END) \"ipv\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'penta_1' THEN vaccines.date ELSE Null END) \"penta1\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'penta_2' THEN vaccines.date ELSE Null END) \"penta2\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'penta_3' THEN vaccines.date ELSE Null END) \"penta3\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'pcv_1' THEN vaccines.date ELSE Null END) \"pcv1\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'pcv_2' THEN vaccines.date ELSE Null END) \"pcv2\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'pcv_3' THEN vaccines.date ELSE Null END) \"pcv3\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'rota_1' THEN vaccines.date ELSE Null END) \"rota1\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'rota_2' THEN vaccines.date ELSE Null END) \"rota2\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'mr_1' THEN vaccines.date ELSE Null END) \"mr1\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'vitamin_a' THEN vaccines.date ELSE Null END) \"vitamin_a\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'yellow_fever' THEN vaccines.date ELSE Null END) \"yellow_fever\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'mv_1' THEN vaccines.date ELSE Null END) \"mv1\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'mv_2' THEN vaccines.date ELSE Null END) \"mv2\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'mv_3' THEN vaccines.date ELSE Null END) \"mv3\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'mr_1' THEN vaccines.date ELSE Null END) \"fully_immunized\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'mr_2' THEN vaccines.date ELSE Null END) \"mr2\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'mv_4' THEN vaccines.date ELSE Null END) \"mv4\"\n" +
                "FROM ec_child_details child_details\n" +
                "LEFT OUTER JOIN ec_mother_details mother_details ON child_details.relational_id = mother_details.base_entity_id\n" +
                "LEFT JOIN ec_father_details father_details ON child_details.father_relational_id = father_details.base_entity_id\n" +
                "LEFT JOIN ec_client client ON child_details.base_entity_id = client.base_entity_id\n" +
                "LEFT JOIN vaccines ON vaccines.base_entity_id = child_details.base_entity_id\n" +
                "GROUP BY kip_id;";

        return sql;
    }

    public String sqlDateRange( String startDate, String endDate){
        String sql = "SELECT client.opensrp_id AS kip_id, child_details.first_name AS child_first_name , child_details.last_name AS child_last_name,\n" +
                "       client.gender As sex, strftime('%d/%m/%Y',child_details.dob) AS dob, strftime('%d/%m/%Y',client.date) AS date_first_seen, father_details.first_name AS father_first_name, father_details.last_name AS father_last_name,\n" +
                "       mother_details.first_name AS mother_first_name, mother_details.last_name AS mother_last_name,\n" +
                "       mother_details.phone_number AS mother_phone, client.village, mother_details.phone_number AS telephone_number,\n" +
                "       SUM(CASE WHEN vaccines.name = 'bcg' THEN vaccines.date ELSE Null END) \"bcg\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'opv_0' THEN vaccines.date ELSE Null END) \"polio_birth_dose\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'opv_1' THEN vaccines.date ELSE Null END) \"opv1\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'opv_2' THEN vaccines.date ELSE Null END) \"opv2\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'opv_3' THEN vaccines.date ELSE Null END) \"opv3\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'ipv' THEN vaccines.date ELSE Null END) \"ipv\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'penta_1' THEN vaccines.date ELSE Null END) \"penta1\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'penta_2' THEN vaccines.date ELSE Null END) \"penta2\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'penta_3' THEN vaccines.date ELSE Null END) \"penta3\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'pcv_1' THEN vaccines.date ELSE Null END) \"pcv1\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'pcv_2' THEN vaccines.date ELSE Null END) \"pcv2\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'pcv_3' THEN vaccines.date ELSE Null END) \"pcv3\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'rota_1' THEN vaccines.date ELSE Null END) \"rota1\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'rota_2' THEN vaccines.date ELSE Null END) \"rota2\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'mr_1' THEN vaccines.date ELSE Null END) \"mr1\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'vitamin_a' THEN vaccines.date ELSE Null END) \"vitamin_a\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'yellow_fever' THEN vaccines.date ELSE Null END) \"yellow_fever\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'mv_1' THEN vaccines.date ELSE Null END) \"mv1\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'mv_2' THEN vaccines.date ELSE Null END) \"mv2\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'mv_3' THEN vaccines.date ELSE Null END) \"mv3\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'mr_1' THEN vaccines.date ELSE Null END) \"fully_immunized\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'mr_2' THEN vaccines.date ELSE Null END) \"mr2\",\n" +
                "       SUM(CASE WHEN vaccines.name = 'mv_4' THEN vaccines.date ELSE Null END) \"mv4\"\n" +
                "FROM ec_child_details child_details\n" +
                "LEFT OUTER JOIN ec_mother_details mother_details ON child_details.relational_id = mother_details.base_entity_id\n" +
                "LEFT JOIN ec_father_details father_details ON child_details.father_relational_id = father_details.base_entity_id\n" +
                "LEFT JOIN ec_client client ON child_details.base_entity_id = client.base_entity_id\n" +
                "LEFT JOIN vaccines ON vaccines.base_entity_id = child_details.base_entity_id\n" +
                "LEFT JOIN vaccines ON vaccines.base_entity_id = child_details.base_entity_id\n" +
                "WHERE client.date BETWEEN '"+startDate+"' AND '"+endDate+"'\n" +
                "GROUP BY kip_id;";

        return sql;
    }

    public SQLiteDatabase getReadable(){
        return getReadableDatabase();
    }

    public Moh510SummaryReport getMoh510SummaryReport(Cursor cursor){
        Moh510SummaryReport moh510SummaryReport = new Moh510SummaryReport();
        moh510SummaryReport.setKipId(cursor.getString(cursor.getColumnIndex("kip_id")));
        moh510SummaryReport.setChildFirstName(cursor.getString(cursor.getColumnIndex("child_first_name")));
        moh510SummaryReport.setChildLastName(cursor.getString(cursor.getColumnIndex("child_last_name")));
        moh510SummaryReport.setSex(cursor.getString(cursor.getColumnIndex("sex")));
        moh510SummaryReport.setDob(cursor.getString(cursor.getColumnIndex("dob")));
        moh510SummaryReport.setDateFirstSeen(cursor.getString(cursor.getColumnIndex("date_first_seen")));
        moh510SummaryReport.setFatherFirstName(cursor.getString(cursor.getColumnIndex("father_first_name")));
        moh510SummaryReport.setFatherLastName(cursor.getString(cursor.getColumnIndex("father_last_name")));
        moh510SummaryReport.setMotherFirstName(cursor.getString(cursor.getColumnIndex("mother_first_name")));
        moh510SummaryReport.setMotherLastName(cursor.getString(cursor.getColumnIndex("mother_last_name")));
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

    public List<Moh510SummaryReport> readAll(Cursor cursor){
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
