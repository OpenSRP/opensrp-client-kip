package org.smartregister.kip.service;

import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by coder on 5/19/17.
 */
public class Moh710Service {
    private final String TAG = Moh710Service.class.getCanonicalName();
    public static final DateFormat dfyymm = new SimpleDateFormat("yyyy-MM");
    public static final String REPORT_NAME = "MOH 710";

    private static final String MOH_001 = "MOH-001";
    private static final String MOH_002 = "MOH-002";
    private static final String MOH_003 = "MOH-003";
    private static final String MOH_004 = "MOH-004";
    private static final String MOH_005 = "MOH-005";
    private static final String MOH_006 = "MOH-006";
    private static final String MOH_007 = "MOH-007";
    private static final String MOH_008 = "MOH-008";
    private static final String MOH_009 = "MOH-009";
    private static final String MOH_010 = "MOH-010";
    private static final String MOH_011 = "MOH-011";
    private static final String MOH_012 = "MOH-012";
    private static final String MOH_013 = "MOH-013";
    private static final String MOH_014 = "MOH-014";
    private static final String MOH_015 = "MOH-015";
    private static final String MOH_016 = "MOH-016";
    private static final String MOH_017 = "MOH-017";
    private static final String MOH_018 = "MOH-018";
    private static final String MOH_019 = "MOH-019";
    private static final String MOH_020 = "MOH-020";
    private static final String MOH_021 = "MOH-021";
    private static final String MOH_022 = "MOH-022";
    private static final String MOH_023 = "MOH-023";
    private static final String MOH_024 = "MOH-024";
    private static final String MOH_025 = "MOH-025";
    public static final String MOH_026 = "MOH-026";
    private static final String MOH_027 = "MOH-027";
    private static final String MOH_028 = "MOH-028";
    private static final String MOH_029 = "MOH-029";
    private static final String MOH_030 = "MOH-030";
    private static final String MOH_031 = "MOH-031";
    public static final String MOH_032 = "MOH-032";
    public static final String MOH_033 = "MOH-033";
    private static final String MOH_034 = "MOH-034";
    private static final String MOH_035 = "MOH-035";
    public static final String MOH_036 = "MOH-036";
    public static final String MOH_037 = "MOH-037";
    public static final String MOH_038 = "MOH-038";
    public static final String MOH_039 = "MOH-039";
    public static final String MOH_040 = "MOH-040";
    private static final String MOH_041 = "MOH-041";
    public static final String MOH_042 = "MOH-042";
    public static final String MOH_043 = "MOH-043";
    public static final String MOH_044 = "MOH-044";

    private Map<String, Object> mohReport = new HashMap<>();
    private SQLiteDatabase database;
    private String reportDate;

    //FIXME add month as a variable to allow generation of previous months reports
    //FIXME add last generated date to make this process incremental, should this date be per indicator? just in case an indicator was skipped due to exceptions

    /**
     * Generate  vaccine indicators populating them to mohReport map by executing various db queries.
     *
     * @param _database
     */
    public Map<String, Object> generateVaccineIndicators(final SQLiteDatabase _database, String day) {
        database = _database;
        reportDate = day;
        mohReport = new HashMap<>();

        getMOH001();
        getMOH002();
        getMOH003();
        getMOH004();
        getMOH005();
        getMOH006();
        getMOH007();
        getMOH008();
        getMOH009();
        getMOH010();
        getMOH011();
        getMOH012();
        getMOH013();
        getMOH014();
        getMOH015();
        getMOH016();
        getMOH017();
        getMOH018();
        getMOH019();
        getMOH020();
        getMOH021();
        getMOH022();
        getMOH023();
        getMOH024();
        getMOH025();

        getMOH027();
        getMOH028();
        getMOH029();
        getMOH030();
        getMOH031();

        getMOH034();
        getMOH035();

        return mohReport;
    }

    /**
     * Generate recurring services indicators populating them to mohReport map by executing various db queries.
     *
     * @param _database
     */
    public Map<String, Object> generateRecurringServiceIndicators(final SQLiteDatabase _database, String day) {
        database = _database;
        reportDate = day;
        mohReport = new HashMap<>();

        getMOH026();

        getMOH032();
        getMOH033();

        getMOH042();
        getMOH043();

        return mohReport;
    }

    /**
     * Generate tetanus indicators populating them to mohReport map by executing various db queries.
     *
     * @param _database
     */
    public Map<String, Object> generateTetanusIndicators(final SQLiteDatabase _database, String day) {
        database = _database;
        reportDate = day;
        mohReport = new HashMap<>();

        getMOH036();
        getMOH037();
        getMOH038();
        getMOH039();
        getMOH040();

        return mohReport;
    }

    /**
     * Generate adverse effect indicators populating them to mohReport map by executing various db queries.
     *
     * @param _database
     */
    public Map<String, Object> generateAdverseEffectIndicators(final SQLiteDatabase _database, String day) {
        database = _database;
        reportDate = day;
        mohReport = new HashMap<>();

        getMOH041();

        return mohReport;
    }

    /**
     * Generate white eye indicators populating them to mohReport map by executing various db queries.
     *
     * @param _database
     */
    public Map<String, Object> generateWhiteEyeIndicators(final SQLiteDatabase _database, String day) {
        database = _database;
        reportDate = day;
        mohReport = new HashMap<>();

        getMOH044();

        return mohReport;
    }

    /**
     * Number of children < one year who received BCG dose at this facility in this month
     */
    private void getMOH001() {
        try {
            int count = getVaccineCount("bcg", "<12");
            mohReport.put(MOH_001, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_001 + e.getMessage());
        }
    }

    /**
     * Number of children > one year who received BCG dose at this facility in this month
     */
    private void getMOH002() {
        try {
            int count = getVaccineCount("bcg", ">=12");
            mohReport.put(MOH_002, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_002 + e.getMessage());
        }
    }

    /**
     * Number of children who received OPV (Birth dose) within two weeks at this facility in this month
     */
    private void getMOH003() {
        try {
            int count = getVaccineCountAgeWithinDays("opv_0", "<=14");
            mohReport.put(MOH_003, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_003 + e.getMessage());
        }
    }


    /**
     * Number of children < one year who received OPV 1 dose at this facility in this month
     */
    private void getMOH004() {
        try {
            int count = getVaccineCount("opv_1", "<12");
            mohReport.put(MOH_004, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_004 + e.getMessage());
        }
    }

    /**
     * Number of children > one year who received OPV 1 dose at this facility in this month
     */
    private void getMOH005() {
        try {
            int count = getVaccineCount("opv_1", ">=12");
            mohReport.put(MOH_005, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_005 + e.getMessage());
        }
    }

    /**
     * Number of children < one year who received OPV 2 dose at this facility in this month
     */
    private void getMOH006() {
        try {
            int count = getVaccineCount("opv_2", "<12");
            mohReport.put(MOH_006, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_006 + e.getMessage());
        }
    }

    /**
     * Number of children > one year who received OPV 2 dose at this facility in this month
     */
    private void getMOH007() {
        try {
            int count = getVaccineCount("opv_2", ">=12");
            mohReport.put(MOH_007, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_007 + e.getMessage());
        }
    }

    /**
     * Number of children < one year who received OPV 3 dose at this facility in this month
     */
    private void getMOH008() {
        try {
            int count = getVaccineCount("opv_3", "<12");
            mohReport.put(MOH_008, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_008 + e.getMessage());
        }
    }

    /**
     * Number of children > one year who received OPV 3 dose at this facility in this month
     */
    private void getMOH009() {
        try {
            int count = getVaccineCount("opv_3", ">=12");
            mohReport.put(MOH_009, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_009 + e.getMessage());
        }
    }

    /**
     * Number of children < one year who received IPV dose at this facility in this month
     */
    private void getMOH010() {
        try {
            int count = getVaccineCount("ipv", "<12");
            mohReport.put(MOH_010, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_010 + e.getMessage());
        }
    }

    /**
     * Number of children > one year who received IPV dose at this facility in this month
     */
    private void getMOH011() {
        try {
            int count = getVaccineCount("ipv", ">=12");
            mohReport.put(MOH_011, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_011 + e.getMessage());
        }
    }

    /**
     * Number of children < one year who received DPT+HIB+HEPB 1 dose at this facility in this month
     */
    private void getMOH012() {
        try {
            int count = getVaccineCount("penta_1", "<12");
            mohReport.put(MOH_012, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_012 + e.getMessage());
        }
    }

    /**
     * Number of children > one year who received DPT+HIB+HEPB 1 dose at this facility in this month
     */
    private void getMOH013() {
        try {
            int count = getVaccineCount("penta_1", ">=12");
            mohReport.put(MOH_013, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_013 + e.getMessage());
        }
    }

    /**
     * Number of children < one year who received DPT+HIB+HEPB 2 dose at this facility in this month
     */
    private void getMOH014() {
        try {
            int count = getVaccineCount("penta_2", "<12");
            mohReport.put(MOH_014, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_014 + e.getMessage());
        }
    }

    /**
     * Number of children > one year who received DPT+HIB+HEPB 2 dose at this facility in this month
     */
    private void getMOH015() {
        try {
            int count = getVaccineCount("penta_2", ">=12");
            mohReport.put(MOH_015, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_015 + e.getMessage());
        }
    }

    /**
     * Number of children < one year who received DPT+HIB+HEPB 3 dose at this facility in this month
     */
    private void getMOH016() {
        try {
            int count = getVaccineCount("penta_3", "<12");
            mohReport.put(MOH_016, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_016 + e.getMessage());
        }
    }

    /**
     * Number of children > one year who received DPT+HIB+HEPB 3 dose at this facility in this month
     */
    private void getMOH017() {
        try {
            int count = getVaccineCount("penta_3", ">=12");
            mohReport.put(MOH_017, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_017 + e.getMessage());
        }
    }

    /**
     * Number of children < one year who received Pneumococcal 1 dose at this facility in this month
     */
    private void getMOH018() {
        try {
            int count = getVaccineCount("pcv_1", "<12");
            mohReport.put(MOH_018, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_018 + e.getMessage());
        }
    }

    /**
     * Number of children > one year who received Pneumococcal 1 dose at this facility in this month
     */
    private void getMOH019() {
        try {
            int count = getVaccineCount("pcv_1", ">=12");
            mohReport.put(MOH_019, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_019 + e.getMessage());
        }
    }

    /**
     * Number of children < one year who received Pneumococcal 2 dose at this facility in this month
     */
    private void getMOH020() {
        try {
            int count = getVaccineCount("pcv_2", "<12");
            mohReport.put(MOH_020, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_020 + e.getMessage());
        }
    }

    /**
     * Number of children > one year who received Pneumococcal 2 dose at this facility in this month
     */
    private void getMOH021() {
        try {
            int count = getVaccineCount("pcv_2", ">=12");
            mohReport.put(MOH_021, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_021 + e.getMessage());
        }
    }

    /**
     * Number of children < one year who received Pneumococcal 3 dose at this facility in this month
     */
    private void getMOH022() {
        try {
            int count = getVaccineCount("pcv_3", "<12");
            mohReport.put(MOH_022, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_022 + e.getMessage());
        }
    }

    /**
     * Number of children > one year who received Pneumococcal 3 dose at this facility in this month
     */
    private void getMOH023() {
        try {
            int count = getVaccineCount("pcv_3", ">=12");
            mohReport.put(MOH_023, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_023 + e.getMessage());
        }
    }

    /**
     * Number of children < one year who received Rota 1 dose at this facility in this month
     */
    private void getMOH024() {
        try {
            int count = getVaccineCount("rota_1", "<12");
            mohReport.put(MOH_024, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_024 + e.getMessage());
        }
    }


    /**
     * Number of children < one year who received Rota 2 dose at this facility in this month
     */
    private void getMOH025() {
        try {
            int count = getVaccineCount("rota_2", "<12");
            mohReport.put(MOH_025, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_025 + e.getMessage());
        }
    }

    /**
     * Number of children who received Vitamin A dose At 6 Months (100,000 IU) at this facility in this month
     */
    private void getMOH026() {
        try {
            int count = getRecurringServiceCount("Vit_A_1", ">=6", "<12");
            mohReport.put(MOH_026, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_026 + e.getMessage());
        }
    }


    /**
     * Number of children < one year who received Yellow Fever dose at this facility in this month
     */
    private void getMOH027() {
        try {
            int count = getVaccineCount("yf", "<12");
            mohReport.put(MOH_027, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_027 + e.getMessage());
        }
    }

    /**
     * Number of children > one year who received Yellow Fever dose at this facility in this month
     */
    private void getMOH028() {
        try {
            int count = getVaccineCount("yf", ">=12");
            mohReport.put(MOH_028, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_028 + e.getMessage());
        }
    }

    /**
     * Number of children < one year who received MR 1 dose at this facility in this month
     */
    private void getMOH029() {
        try {
            int count = getVaccineCount("measles_1", "<12");
            mohReport.put(MOH_029, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_029 + e.getMessage());
        }
    }

    /**
     * Number of children > one year who received MR 1 dose at this facility in this month
     */
    private void getMOH030() {
        try {
            int count = getVaccineCount("measles_1", ">=12");
            mohReport.put(MOH_030, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_030 + e.getMessage());
        }
    }

    /**
     * Number of children who received are Fully Immunized Child (FIC) at 1 year at this facility in this month
     */
    private void getMOH031() {
        try {
            int count = getCountFullyImmunized("<12");
            mohReport.put(MOH_031, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_031 + e.getMessage());
        }
    }

    /**
     * Number of children who received Vitamin A dose At  1 Year (200,000 IU) at this facility in this month
     */
    private void getMOH032() {
        try {
            int count = getRecurringServiceCount("Vit_A_2", ">=12", "<18");
            mohReport.put(MOH_032, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_032 + e.getMessage());
        }
    }

    /**
     * Number of children who received Vitamin A dose At 1 ½ Years (200,000 IU) at this facility in this month
     */
    private void getMOH033() {
        try {
            int count = getRecurringServiceCount("Vit_A_3", ">=18", "<24");
            mohReport.put(MOH_033, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_033 + e.getMessage());
        }
    }

    /**
     * Number of children At 1 ½ - 2 Years who received MR 2 dose at this facility in this month
     */
    private void getMOH034() {
        try {
            int count = getVaccineCount("measles_2", ">=18", "<24");
            mohReport.put(MOH_034, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_034 + e.getMessage());
        }
    }

    /**
     * Number of children > two year who received MR 2 dose at this facility in this month
     */
    private void getMOH035() {
        try {
            int count = getVaccineCount("measles_2", ">24");
            mohReport.put(MOH_035, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_035 + e.getMessage());
        }
    }

    /**
     * Number of mothers who received Tetanus Toxoid for Pregnant Women 1st Dose at this facility in this month
     */
    private void getMOH036() {
        try {
            // No Source of Date, Leave Blank
            int count = 0;
            mohReport.put(MOH_036, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_036 + e.getMessage());
        }
    }


    /**
     * Number of mothers who received Tetanus Toxoid for Pregnant Women 2nd Dose at this facility in this month
     */
    private void getMOH037() {
        try {
            // No Source of Date, Leave Blank
            int count = 0;
            mohReport.put(MOH_037, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_037 + e.getMessage());
        }
    }


    /**
     * Number of mothers who received Tetanus Toxoid for Pregnant Women 3rd Dose at this facility in this month
     */
    private void getMOH038() {
        try {
            // No Source of Date, Leave Blank
            int count = 0;
            mohReport.put(MOH_038, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_038 + e.getMessage());
        }
    }


    /**
     * Number of mothers who received Tetanus Toxoid for Pregnant Women 4th Dose at this facility in this month
     */
    private void getMOH039() {
        try {
            // No Source of Date, Leave Blank
            int count = 0;
            mohReport.put(MOH_039, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_039 + e.getMessage());
        }
    }

    /**
     * Number of mothers who received Tetanus Toxoid for Pregnant Women 5th Dose at this facility in this month
     */
    private void getMOH040() {
        try {
            // No Source of Date, Leave Blank
            int count = 0;
            mohReport.put(MOH_040, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_040 + e.getMessage());
        }
    }

    /**
     * Number of children who received Adverse Events Following Immunization at this facility in this month
     */
    private void getMOH041() {
        try {
            int count = getAEFICount();
            mohReport.put(MOH_041, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_041 + e.getMessage());
        }
    }

    /**
     * Number of children who received Vitamin A 2 Years to 5 Years (200,000 IU) at this facility in this month
     */
    private void getMOH042() {
        try {
            int count4 = getRecurringServiceCount("Vit_A_4", ">=24", "<60");
            int count5 = getRecurringServiceCount("Vit_A_5", ">=24", "<60");
            int count6 = getRecurringServiceCount("Vit_A_6", ">=24", "<60");
            int count7 = getRecurringServiceCount("Vit_A_7", ">=24", "<60");
            int count8 = getRecurringServiceCount("Vit_A_8", ">=24", "<60");
            int count9 = getRecurringServiceCount("Vit_A_9", ">=24", "<60");
            int count10 = getRecurringServiceCount("Vit_A_10", ">=24", "<60");
            int count = count4 + count5 + count6 + count7 + count8 + count9 + count10;
            mohReport.put(MOH_042, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_042 + e.getMessage());
        }
    }

    /**
     * Number of children who received Vitamin A Lactating Mothers (200,000 IU) at this facility in this month
     */
    private void getMOH043() {
        try {
            // No Source of Date, Leave Blank
            int count = 0;
            mohReport.put(MOH_043, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_043 + e.getMessage());
        }
    }

    /**
     * Number of children who received Squint / White Eye Reflection (Under 1 Year) at this facility in this month
     */
    private void getMOH044() {
        try {
            // No Source of Date, Leave Blank
            int count = 0;
            mohReport.put(MOH_044, count);
        } catch (Exception e) {
            Log.logError(TAG, MOH_044 + e.getMessage());
        }
    }


    /**
     * @param vaccine
     * @param age     in months specified as e.g <12 or >12 or between 12 and 59
     * @return
     */
    private int getVaccineCount(String vaccine, String age) {
        int count = 0;
        try {
            String vaccineCondition = "lower(v.name)='" + vaccine.toLowerCase() + "'";
            if (vaccine.equals("measles_1")) {
                vaccineCondition = "(lower(v.name)='" + vaccine.toLowerCase() + "' or lower(v.name)='mr_1')";
            } else if (vaccine.equals("measles_2")) {
                vaccineCondition = "(lower(v.name)='" + vaccine.toLowerCase() + "' or lower(v.name)='mr_2')";
            }

            String query = "select count(*) as count, " + ageQuery() + " from vaccines v left join ec_child child on child.base_entity_id=v.base_entity_id " +
                    "where age " + age + " and  '" + reportDate + "'=strftime('%Y-%m-%d',datetime(v.date/1000, 'unixepoch')) and " + vaccineCondition;
            count = executeQueryAndReturnCount(query);
        } catch (Exception e) {
            Log.logError(TAG, vaccine.toUpperCase() + e.getMessage());
        }

        return count;

    }

    /**
     * @param vaccine
     * @param minAge  in months specified as e.g <12 or >12
     * @param maxAge  in months specified as e.g <12 or >12
     * @return
     */
    private int getVaccineCount(String vaccine, String minAge, String maxAge) {
        int count = 0;
        try {
            String vaccineCondition = "lower(v.name)='" + vaccine.toLowerCase() + "'";
            if (vaccine.equals("measles_1")) {
                vaccineCondition = "(lower(v.name)='" + vaccine.toLowerCase() + "' or lower(v.name)='mr_1')";
            } else if (vaccine.equals("measles_2")) {
                vaccineCondition = "(lower(v.name)='" + vaccine.toLowerCase() + "' or lower(v.name)='mr_2')";
            }

            String ageCondition = " age " + minAge + " and  age " + maxAge;
            String query = "select count(*) as count, " + ageQuery() + " from vaccines v left join ec_child child on child.base_entity_id=v.base_entity_id " +
                    "where " + ageCondition + " and  '" + reportDate + "'=strftime('%Y-%m-%d',datetime(v.date/1000, 'unixepoch')) and " + vaccineCondition;
            count = executeQueryAndReturnCount(query);
        } catch (Exception e) {
            Log.logError(TAG, vaccine.toUpperCase() + e.getMessage());
        }
        return count;
    }

    /**
     * Age is specified as different between vaccine date and dob
     *
     * @param vaccine
     * @param age     within days specified as e.g <=14
     * @return
     */
    private int getVaccineCountAgeWithinDays(String vaccine, String age) {
        int count = 0;
        try {
            String vaccineCondition = "lower(v.name)='" + vaccine.toLowerCase() + "'";
            String query = "select count(*) as count, " + ageWithinDays() + " from vaccines v left join ec_child child on child.base_entity_id=v.base_entity_id " +
                    "where age " + age + " and  '" + reportDate + "'=strftime('%Y-%m-%d',datetime(v.date/1000, 'unixepoch')) and " + vaccineCondition;
            count = executeQueryAndReturnCount(query);
        } catch (Exception e) {
            Log.logError(TAG, vaccine.toUpperCase() + e.getMessage());
        }

        return count;
    }


    /**
     * @param recurringService
     * @param minAge           in months specified as e.g <12 or >12
     * @param maxAge           in months specified as e.g <12 or >12
     * @return
     */
    private int getRecurringServiceCount(String recurringService, String minAge, String maxAge) {
        int count = 0;
        try {
            String ageCondition = " age " + minAge + " and  age " + maxAge;
            String recurringServiceCondition = "t.name = '" + recurringService + "'";
            String query = "select count(*) as count, " + ageQuery() + " from recurring_service_records r left join ec_child child on child.base_entity_id=r.base_entity_id " +
                    "join recurring_service_types t on r.recurring_service_id =t._id " +
                    "where " + ageCondition + " and  '" + reportDate + "'=strftime('%Y-%m-%d',datetime(r.date/1000, 'unixepoch')) and " + recurringServiceCondition;
            count = executeQueryAndReturnCount(query);
        } catch (Exception e) {
            Log.logError(TAG, recurringService.toUpperCase() + e.getMessage());
        }
        return count;
    }

    /**
     * @param age in months specified as e.g <12 or >12 or between 12 and 59
     * @return
     */
    private int getCountFullyImmunized(String age) {
        int count = 0;
        try {
            Date dateReported = new SimpleDateFormat("yyyy-mm-dd").parse(reportDate);
            Calendar reportCalendarDate = Calendar.getInstance();
            reportCalendarDate.setTime(dateReported);
            setEndOfDay(reportCalendarDate);

            String query = "select  group_concat( v.name) v_list , group_concat( t.name) r_list, " + ageQuery() + "  from ec_child child join vaccines v  on child.base_entity_id=v.base_entity_id " +
                    "join recurring_service_records r on child.base_entity_id = r.base_entity_id join recurring_service_types t on r.recurring_service_id = t._id " +
                    "where age " + age + " and v.date <= " + reportCalendarDate.getTimeInMillis() + " and r.date <= " + reportCalendarDate.getTimeInMillis() + " group by  child.base_entity_id ";

            ArrayList<HashMap<String, String>> list = executeRawQuery(query);
            String[] services = {"Vit_A_1"};
            String[] vaccines = {"bcg", "opv_0", "opv_1", "pcv_1", "penta_1", "rota_1", "opv_2", "pcv_2", "penta_2", "rota_2", "opv_3", "pcv_3", "penta_3", "ipv", "measles_1"};

            for (HashMap<String, String> map : list) {
                Log.logError(map.toString());
                String vlist = map.get("v_list");
                String rlist = map.get("r_list");
                if (StringUtils.isNotBlank(vlist) && StringUtils.isNotBlank(rlist)) {
                    boolean allServiceExists = true;
                    for (String service : services) {
                        if (!rlist.contains(service)) {
                            allServiceExists = false;
                            break;
                        }
                    }

                    boolean allVaccinesExist = true;
                    for (String vaccine : vaccines) {
                        if (vaccine.equals("measles_1")) {
                            if (!(vlist.contains("measles_1") || vlist.contains("mr_1"))) {
                                allVaccinesExist = false;
                                break;
                            }

                        } else {
                            if (!vlist.contains(vaccine)) {
                                allVaccinesExist = false;
                                break;
                            }
                        }
                    }

                    if (allServiceExists && allVaccinesExist) {
                        count++;
                    }
                }

            }
        } catch (Exception e) {
            Log.logError(TAG, "FullyImmunized" + e.getMessage());
        }
        return count;
    }

    /**
     * @return
     */
    private int getAEFICount() {
        int count = 0;
        final String eventType = "'AEFI'";
        try {
            String query = "select count(*) as count from " + EventClientRepository.Table.event.name() +
                    " where '" + reportDate + "'= strftime('%Y-%m-%d'," + EventClientRepository.event_column.eventDate.toString() + ") " +
                    " and " + EventClientRepository.event_column.eventType.toString() + " = " + eventType;
            count = executeQueryAndReturnCount(query);
        } catch (Exception e) {
            Log.logError(TAG, eventType + e.getMessage());
        }

        return count;

    }

    private String ageQuery() {
        return " CAST ((julianday('now') - julianday(strftime('%Y-%m-%d',child.dob)))/(365/12) AS INTEGER)as age ";
    }

    private String ageWithinDays() {
        return " CAST ((julianday(strftime('%Y-%m-%d',datetime(v.date/1000, 'unixepoch'))) - julianday(strftime('%Y-%m-%d',child.dob))) AS INTEGER)as age ";
    }

    private String eventDateEqualsCurrentMonthQuery() {
        return "strftime('%Y-%m-%d',e.eventDate) = '" + reportDate + "'";
    }

    private int executeQueryAndReturnCount(String query) {
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = database.rawQuery(query, null);
            if (null != cursor) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    count = cursor.getInt(0);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.logError(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    private ArrayList<HashMap<String, String>> executeRawQuery(String query) {
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(query, null);

            ArrayList<HashMap<String, String>> maplist = new ArrayList<HashMap<String, String>>();
            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> map = new HashMap<String, String>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        map.put(cursor.getColumnName(i), cursor.getString(i));
                    }

                    maplist.add(map);
                } while (cursor.moveToNext());
            }

            return maplist;
        } catch (Exception e) {
            android.util.Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static void setEndOfDay(Calendar calendarDate) {
        calendarDate.set(Calendar.HOUR_OF_DAY, 0);
        calendarDate.set(Calendar.MINUTE, 0);
        calendarDate.set(Calendar.SECOND, 0);
        calendarDate.set(Calendar.MILLISECOND, 0);

        calendarDate.set(Calendar.HOUR_OF_DAY, 23);
        calendarDate.set(Calendar.MINUTE, 59);
        calendarDate.set(Calendar.SECOND, 59);
        calendarDate.set(Calendar.MILLISECOND, 999);
    }
}
