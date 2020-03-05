package org.smartregister.kip.model;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.child.util.JsonFormUtils;
import org.smartregister.clientandeventmodel.DateUtil;
//import org.smartregister.kip.util.KipChildJsonFormUtils;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.kip.util.KipJsonFormUtils;

import java.util.Date;

public class CheckChildDetailsModel {
    private boolean myResult;
    private JSONObject client;
    private String entityId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private String dob;
    private String zeirId;
    private String inactive;
    private String lostToFollowUp;

    public CheckChildDetailsModel(JSONObject client) {
        this.client = client;
    }

    public boolean is() {
        return myResult;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getGender() {
        return gender;
    }

    public String getDob() {
        return dob;
    }

    public String getZeirId() {
        return zeirId;
    }

    public String getInactive() {
        return inactive;
    }

    public String getLostToFollowUp() {
        return lostToFollowUp;
    }

    public CheckChildDetailsModel invoke() {
        this.entityId = "";
        this.firstName = "";
        this.middleName = "";
        this.lastName = "";
        this.gender = "";
        this.dob = "";
        this.zeirId = "";
        this.inactive = "";
        this.lostToFollowUp = "";

        if (client.has(KipConstants.KEY.CHILD)) {
            JSONObject child = KipJsonFormUtils.getJsonObject(client, KipConstants.KEY.CHILD);

            // Skip deceased children
            if (StringUtils.isNotBlank(KipJsonFormUtils.getJsonString(child, KipConstants.KEY.DEATHDATE))) {
                myResult = true;
                return this;
            }

            entityId = KipJsonFormUtils.getJsonString(child, KipConstants.KEY.BASE_ENTITY_ID);
            firstName = KipJsonFormUtils.getJsonString(child, KipConstants.KEY.FIRSTNAME);
            middleName = KipJsonFormUtils.getJsonString(child, KipConstants.KEY.MIDDLENAME);
            lastName = KipJsonFormUtils.getJsonString(child, KipConstants.KEY.LASTNAME);

            gender = KipJsonFormUtils.getJsonString(child, KipConstants.KEY.GENDER);
            dob = KipJsonFormUtils.getJsonString(child, KipConstants.KEY.BIRTHDATE);
            if (StringUtils.isNotBlank(dob) && StringUtils.isNumeric(dob)) {
                try {
                    Long dobLong = Long.valueOf(dob);
                    Date date = new Date(dobLong);
                    dob = DateUtil.yyyyMMddTHHmmssSSSZ.format(date);
                } catch (Exception e) {
                    Log.e(getClass().getName(), e.toString(), e);
                }
            }

            zeirId = KipJsonFormUtils.getJsonString(KipJsonFormUtils.getJsonObject(child, KipConstants.KEY.IDENTIFIERS), JsonFormUtils.ZEIR_ID);
            if (StringUtils.isNotBlank(zeirId)) {
                zeirId = zeirId.replace("-", "");
            }

            inactive = KipJsonFormUtils.getJsonString(KipJsonFormUtils.getJsonObject(child, KipConstants.KEY.ATTRIBUTES), KipConstants.KEY.INACTIVE);
            lostToFollowUp = KipJsonFormUtils.getJsonString(KipJsonFormUtils.getJsonObject(child, KipConstants.KEY.ATTRIBUTES), KipConstants.KEY.LOST_TO_FOLLOW_UP);
        }
        myResult = false;
        return this;
    }
}
