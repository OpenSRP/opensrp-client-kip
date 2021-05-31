package org.smartregister.kip.repository;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.child.provider.RegisterQueryProvider;
import org.smartregister.child.util.Constants;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.growthmonitoring.util.GrowthMonitoringConstants;
import org.smartregister.kip.util.KipConstants;

public class KipChildRegisterQueryProvider extends RegisterQueryProvider {

    @Override
    public String getObjectIdsQuery(String mainCondition, String filters) {
        String strMainCondition = getMainCondition(mainCondition);

        String strFilters = getFilter(filters);

        if (StringUtils.isNotBlank(strFilters) && StringUtils.isBlank(strMainCondition)) {
            strFilters = String.format(" where " + getDemographicTable() + ".phrase MATCH '*%s*'", filters);
        }

        return "select " + getDemographicTable() + ".object_id from " + CommonFtsObject.searchTableName(getDemographicTable()) + " " + getDemographicTable() + "  " +
                "join " + getChildDetailsTable() + " on " + getDemographicTable() + ".object_id =  " + getChildDetailsTable() + ".id " +
                "left join (select * from " + CommonFtsObject.searchTableName(getChildDetailsTable()) + ") " + CommonFtsObject.searchTableName(getChildDetailsTable()) + " on " + getDemographicTable() + ".object_id =  " + CommonFtsObject.searchTableName(getChildDetailsTable()) + ".object_id "
                + strMainCondition + strFilters;
    }

    @Override
    public String getCountExecuteQuery(String mainCondition, String filters) {
        String strMainCondition = getMainCondition(mainCondition);

        String strFilters = getFilter(filters);

        if (StringUtils.isNotBlank(strFilters) && StringUtils.isBlank(strMainCondition)) {
            strFilters = String.format(" where " + getDemographicTable() + ".phrase MATCH '*%s*'", filters);
        }

        return "select count(" + getDemographicTable() + ".object_id) from " + CommonFtsObject.searchTableName(getDemographicTable()) + " " + getDemographicTable() + "  " +
                "join " + getChildDetailsTable() + " on " + getDemographicTable() + ".object_id =  " + getChildDetailsTable() + ".id " +
                "left join (select * from " + CommonFtsObject.searchTableName(getChildDetailsTable()) + ") " + CommonFtsObject.searchTableName(getChildDetailsTable()) + " on " + getDemographicTable() + ".object_id =  " + CommonFtsObject.searchTableName(getChildDetailsTable()) + ".object_id "
                + strMainCondition + strFilters;
    }

    private String getFilter(String filters) {
        if (StringUtils.isNotBlank(filters)) {
            return String.format(" AND " + getDemographicTable() + ".phrase MATCH '*%s*'", filters);
        }
        return "";
    }

    private String getMainCondition(String mainCondition) {
        if (!StringUtils.isBlank(mainCondition)) {
            return " where " + mainCondition;
        }
        return "";
    }

    @Override
    public String mainRegisterQuery() {
        return "select " + StringUtils.join(mainColumns(), ",") + " from " + getChildDetailsTable() + " " +
                "join " + getMotherDetailsTable() + " on " + getChildDetailsTable() + "." + Constants.KEY.RELATIONAL_ID + " = " + getMotherDetailsTable() + "." + Constants.KEY.BASE_ENTITY_ID + " " +
                "left join " + getFatherDetailsTable() + " on " + getChildDetailsTable() + "." + Constants.KEY.FATHER_RELATIONAL_ID + " = " + getFatherDetailsTable() + "." + Constants.KEY.BASE_ENTITY_ID + " " +
                "join " + getDemographicTable() + " on " + getDemographicTable() + "." + Constants.KEY.BASE_ENTITY_ID + " = " + getChildDetailsTable() + "." + Constants.KEY.BASE_ENTITY_ID + " " +
                "join " + getDemographicTable() + " mother on mother." + Constants.KEY.BASE_ENTITY_ID + " = " + getMotherDetailsTable() + "." + Constants.KEY.BASE_ENTITY_ID + " " +
                "left join " + getDemographicTable() + " father on father." + Constants.KEY.BASE_ENTITY_ID + " = " + getFatherDetailsTable() + "." + Constants.KEY.BASE_ENTITY_ID;
    }

    @Override
    public String[] mainColumns() {
        return new String[]{
                getDemographicTable() + "." + Constants.KEY.ID + " as _id",
                getDemographicTable() + "." + Constants.KEY.RELATIONALID,
                getDemographicTable() + "." + Constants.KEY.ZEIR_ID,
                getChildDetailsTable() + "." + Constants.KEY.RELATIONAL_ID,
                getDemographicTable() + "." + Constants.KEY.GENDER,
                getDemographicTable() + "." + Constants.KEY.BASE_ENTITY_ID,
                getDemographicTable() + "." + Constants.KEY.FIRST_NAME,
                getDemographicTable() + "." + Constants.KEY.LAST_NAME,
                getDemographicTable() + "." + "middle_name",
                "mother" + "." + Constants.KEY.FIRST_NAME + " as mother_first_name",
                "mother" + "." + Constants.KEY.LAST_NAME + " as mother_last_name",
                getDemographicTable() + "." + Constants.KEY.DOB,
                getDemographicTable() + "." + Constants.KEY.DOD,
                "mother" + "." + Constants.KEY.DOB + " as mother_dob",
                getMotherDetailsTable() + "." + Constants.KEY.NRC_NUMBER + " as nrc_number",
                getMotherDetailsTable() + "." + Constants.KEY.NRC_NUMBER + " as mother_nrc_number",
                getFatherDetailsTable() + "." + KipConstants.KEY.FATHER_PHONE,
                getDemographicTable() + "." + Constants.KEY.CLIENT_REG_DATE,
                getDemographicTable() + "." + Constants.KEY.LAST_INTERACTED_WITH,
                getChildDetailsTable() + "." + "inactive",
                getChildDetailsTable() + "." + Constants.KEY.LOST_TO_FOLLOW_UP,
                getDemographicTable() + "." + "village",
                getDemographicTable() + "." + "home_address",
                getChildDetailsTable() + "." + Constants.SHOW_BCG_SCAR,
                getChildDetailsTable() + "." + Constants.SHOW_BCG2_REMINDER,
                getMotherDetailsTable() + "." + KipConstants.PROTECTED_AT_BIRTH,
                getMotherDetailsTable() + "." + KipConstants.MOTHER_TDV_DOSES,
                getMotherDetailsTable() + "." + KipConstants.MOTHER_HIV_STATUS,
                getChildDetailsTable() + "." + KipConstants.BIRTH_REGISTRATION_NUMBER,
                getChildDetailsTable() + "." + GrowthMonitoringConstants.PMTCT_STATUS,
                getChildDetailsTable() + "." + "child_hiv_status",
                getChildDetailsTable() + "." + "child_treatment",
                getMotherDetailsTable() + ".second_phone_number as second_phone_number",
                getDemographicTable() + "." + "county",
                getDemographicTable() + "." + "sub_county",
                getDemographicTable() + "." + "ward",
                getMotherDetailsTable() + "."+"alt_phone_number as mother_guardian_number",
                getMotherDetailsTable() + "."+ KipConstants.PHONE_NUMBER,
                getDemographicTable() + "." + "kepi_sms_reminder_date",
                "father.first_name                     as " + KipConstants.KEY.FATHER_FIRST_NAME,
                "father.last_name                      as " + KipConstants.KEY.FATHER_LAST_NAME,
                "father.dob                            as " + KipConstants.KEY.FATHER_DOB};
    }

//    need to change table name to ec_father_details
    public String getFatherDetailsTable() {
        return "ec_father_details";
    }

}
