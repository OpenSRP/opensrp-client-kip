package org.smartregister.kip.service;//package org.smartregister.kip.service;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class Moh710CustomeService {
//
//    public static final String MOH_BCG = "BCG";
//    public static final String MOH_IPV = "IPV";
//    public static final String MOH_MR_1 = "MR 1";
//    public static final String MOH_RTSS_1 = "Malaria Vaccine 1";
//    public static final String MOH_RTSS_2 = "Malaria Vaccine 2";
//    public static final String MOH_RTSS_3 = "Malaria Vaccine 3";
//    public static final String MOH_RTSS_4 = "Malaria Vaccine 4";
//    public static final String MOH_OPV_0 = "OPV 0";
//    public static final String MOH_OPV_1 = "OPV 1";
//    public static final String MOH_OPV_2 = "OPV 2";
//    public static final String MOH_OPV_3 = "OPV 3";
//    public static final String MOH_PCV_1 = "PCV 1";
//    public static final String MOH_PCV_2 = "PCV 2";
//    public static final String MOH_Penta_1 = "Penta 1";
//    public static final String MOH_Penta_2 = "Penta 2";
//    public static final String MOH_Penta_3 = "Penta 3";
//    public static final String MOH_Rota_1 = "Rota 1";
//    public static final String MOH_Rota_2 = "Rota 2";
//    public static final String MOH_WAITING_LIST = "Total Clients in Waiting List";
//
//    public static final String MOH_AZ_OXFORD = "AZ/Oxford";
//    public static final String MOH_SINOPHARM = "Sinopharm";
//    public static final String MOH_SINOVAC = "Sinovac";
//
//    public Map<String, Object> mohDailyTally = new HashMap<>();
//
//    public static final String MOH_BCG_UN = "ME_Vaccines_Age_bcg_Over_1";
//    public static final String MOH_BCG_OV = "ME_Vaccines_Age_bcg_Under_1";
//    public static final String MOH_OPV_OV = "ME_Vaccines_Age_opv_0_Over_1";
//    public static final String MOH_OPV_UN = "ME_Vaccines_Age_opv_0_Under_1";
//    public static final String MOH_OPV_1_OV = "ME_Vaccines_Age_opv_1_Over_1";
//    public static final String MOH_OPV_1_UN = "ME_Vaccines_Age_opv_1_Under_1";
//    public static final String MOH_OPV_2_OV = "ME_Vaccines_Age_opv_2_Over_1";
//    public static final String MOH_OPV_2_UN = "ME_Vaccines_Age_opv_2_Under_1";
//    public static final String MOH_OPV_3_OV = "ME_Vaccines_Age_opv_3_Over_1";
//    public static final String MOH_OPV_3_UN = "ME_Vaccines_Age_opv_3_Under_1";
//    public static final String MOH_PENTA_1_OV = "ME_Vaccines_Age_penta_1_Over_1";
//    public static final String MOH_PENTA_1_UN = "ME_Vaccines_Age_penta_1_Under_1";
//    public static final String MOH_PENTA_2_OV = "ME_Vaccines_Age_penta_2_Over_1";
//    public static final String MOH_PENTA_2_UN = "ME_Vaccines_Age_penta_2_Under_1";
//    public static final String MOH_PENTA_3_OV = "ME_Vaccines_Age_penta_3_Over_1";
//    public static final String MOH_PENTA_3_UN = "ME_Vaccines_Age_penta_3_Under_1";
//    public static final String MOH_PCV_1_OV = "ME_Vaccines_Age_pcv_1_Over_1";
//    public static final String MOH_PCV_1_UN = "ME_Vaccines_Age_pcv_1_Under_1";
//    public static final String MOH_PCV_2_OV = "ME_Vaccines_Age_pcv_2_Over_1";
//    public static final String MOH_PCV_2_UN = "ME_Vaccines_Age_pcv_2_Under_1";
//    public static final String MOH_ROTA_1_OV = "ME_Vaccines_Age_rota_1_Over_1";
//    public static final String MOH_ROTA_1_UN = "ME_Vaccines_Age_rota_1_Under_1";
//    public static final String MOH_ROTA_2_OV = "ME_Vaccines_Age_rota_2_Over_1";
//    public static final String MOH_ROTA_2_UN = "ME_Vaccines_Age_rota_2_Under_1";
//    public static final String MOH_RTSS_1_OV = "ME_Vaccines_Age_mv_1_Over_1";
//    public static final String MOH_RTSS_1_UN = "ME_Vaccines_Age_mv_1_Under_1";
//    public static final String MOH_RTSS_2_OV = "ME_Vaccines_Age_mv_2_Over_1";
//    public static final String MOH_RTSS_2_UN = "ME_Vaccines_Age_mv_2_Under_1";
//    public static final String MOH_RTSS_3_OV = "ME_Vaccines_Age_mv_3_Over_1";
//    public static final String MOH_RTSS_3_UN = "ME_Vaccines_Age_mv_3_Under_1";
//    public static final String MOH_MR_1_OV = "ME_Vaccines_Age_measles_1_/_mr_1_Over_1";
//    public static final String MOH_MR_1_UN = "ME_Vaccines_Age_measles_1_/_mr_1_Under_1";
//    public static final String MOH_IPV_OV = "ME_Vaccines_Age_ipv_Over_1";
//    public static final String MOH_IPV_UN = "ME_Vaccines_Age_ipv_Under_1";
//
//    public static final String MOH_AZ_OXFORD_OV = "ME_Vaccines_Age_az_oxford_Over_50";
//    public static final String MOH_AZ_OXFORD_UN = "ME_Vaccines_Age_az_oxford_Under_50";
//    public static final String MOH_SINOPHARM_OV = "ME_Vaccines_Age_sinopharm_Over_50";
//    public static final String MOH_SINOPHARM_UN = "ME_Vaccines_Age_sinopharm_Under_50";
//    public static final String MOH_SINOVAC_OV = "ME_Vaccines_Age_sinovac_Over_50";
//    public static final String MOH_SINOVAC_UN = "ME_Vaccines_Age_sinovac_Under_50";
//
//    public static final String MOH_WAITING_LIST_TXT = "ME_Total_Client_On_Waiting_List";
//
//
//    public static String indicatorAge(String age) {
//        String name = null;
//
//        if (age.contains("Under_1")) {
//            name = "Under 1 Year";
//        }
//        if (age.contains("Over_1")) {
//            name = "Over 1 Year";
//        }
//        if (age.contains("Over_50")){
//            name = "Over 50";
//        }
//        if (age.contains("Under_50")){
//            name = "Under 50";
//        }
//
//        return name;
//    }
//
//    public static String indicatorName(String indicators){
//
//        String vaccineName = null;
//        if (indicators.equals(MOH_BCG_UN) || indicators.equals(MOH_BCG_OV)){
//            vaccineName = MOH_BCG;
//        }
//
//        if (indicators.equals(MOH_OPV_UN) || indicators.equals(MOH_OPV_OV)){
//            vaccineName = MOH_OPV_0;
//        }
//
//        if (indicators.equals(MOH_OPV_1_OV) || indicators.equals(MOH_OPV_1_UN)){
//            vaccineName = MOH_OPV_1;
//        }
//
//        if (indicators.equals(MOH_OPV_2_OV) || indicators.equals(MOH_OPV_2_UN)){
//            vaccineName = MOH_OPV_2;
//        }
//
//        if (indicators.equals(MOH_OPV_3_OV) || indicators.equals(MOH_OPV_3_UN)){
//            vaccineName = MOH_OPV_3;
//        }
//
//        if (indicators.equals(MOH_PENTA_1_OV) || indicators.equals(MOH_PENTA_1_UN)){
//            vaccineName = MOH_Penta_1;
//        }
//
//        if (indicators.equals(MOH_PENTA_2_OV) || indicators.equals(MOH_PENTA_2_UN)){
//            vaccineName = MOH_Penta_2;
//        }
//
//        if (indicators.equals(MOH_PENTA_1_OV) || indicators.equals(MOH_PENTA_1_UN)){
//            vaccineName = MOH_Penta_1;
//        }
//
//        if (indicators.equals(MOH_PENTA_3_OV) || indicators.equals(MOH_PENTA_3_UN)){
//            vaccineName = MOH_Penta_3;
//        }
//
//        if (indicators.equals(MOH_PCV_1_OV) || indicators.equals(MOH_PCV_1_UN)){
//            vaccineName = MOH_PCV_1;
//        }
//        if (indicators.equals(MOH_PCV_2_OV) || indicators.equals(MOH_PCV_2_UN)){
//            vaccineName = MOH_PCV_2;
//        }
//
//        if (indicators.equals(MOH_ROTA_1_OV) || indicators.equals(MOH_ROTA_1_UN)){
//            vaccineName = MOH_Rota_1;
//        }
//
//        if (indicators.equals(MOH_ROTA_2_OV)  || indicators.equals(MOH_ROTA_2_UN)){
//            vaccineName = MOH_Rota_2;
//        }
//
//        if (indicators.equals(MOH_RTSS_1_OV)  || indicators.equals(MOH_RTSS_1_UN)){
//            vaccineName = MOH_RTSS_1;
//        }
//
//        if (indicators.equals(MOH_RTSS_2_OV)  || indicators.equals(MOH_RTSS_2_UN)){
//            vaccineName = MOH_RTSS_2;
//        }
//
//        if (indicators.equals(MOH_RTSS_3_OV)  || indicators.equals(MOH_RTSS_3_UN)){
//            vaccineName = MOH_RTSS_3;
//        }
//
//        if (indicators.equals(MOH_MR_1_OV)  || indicators.equals(MOH_MR_1_UN)){
//            vaccineName = MOH_MR_1;
//        }
//
//        if (indicators.equals(MOH_IPV_OV)  || indicators.equals(MOH_IPV_UN)){
//            vaccineName = MOH_IPV;
//        }
//
//        if (indicators.equals(MOH_AZ_OXFORD_OV)  || indicators.equals(MOH_AZ_OXFORD_UN)){
//            vaccineName = MOH_AZ_OXFORD;
//        }
//
//        if (indicators.equals(MOH_SINOPHARM_OV)  || indicators.equals(MOH_SINOPHARM_UN)){
//            vaccineName = MOH_SINOPHARM;
//        }
//
//        if (indicators.equals(MOH_SINOVAC_OV)  || indicators.equals(MOH_SINOVAC_UN)){
//            vaccineName = MOH_SINOVAC;
//        }
//
//        if (indicators.equals(MOH_WAITING_LIST_TXT)){
//            vaccineName = MOH_WAITING_LIST;
//        }
//
//
//
//        return vaccineName;
//    }
//
//
//}
