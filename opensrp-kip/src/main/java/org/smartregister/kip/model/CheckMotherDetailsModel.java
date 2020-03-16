package org.smartregister.kip.model;

import org.json.JSONObject;
import org.smartregister.kip.util.KipChildJsonFormUtils;
import org.smartregister.kip.util.KipConstants;

public class CheckMotherDetailsModel {
    private JSONObject client;
    private String motherBaseEntityId;
    private String motherFirstName;
    private String motherLastName;

    public CheckMotherDetailsModel(JSONObject client) {
        this.client = client;
    }

    public String getMotherBaseEntityId() {
        return motherBaseEntityId;
    }

    public String getMotherFirstName() {
        return motherFirstName;
    }

    public String getMotherLastName() {
        return motherLastName;
    }

    public CheckMotherDetailsModel invoke() {
        motherBaseEntityId = "";
        motherFirstName = "";
        motherLastName = "";

        if (client.has(KipConstants.KEY.MOTHER)) {
            JSONObject mother = KipChildJsonFormUtils.getJsonObject(client, KipConstants.KEY.MOTHER);
            motherFirstName = KipChildJsonFormUtils.getJsonString(mother, KipConstants.KEY.FIRSTNAME);
            motherLastName = KipChildJsonFormUtils.getJsonString(mother, KipConstants.KEY.LASTNAME);
            motherBaseEntityId = KipChildJsonFormUtils.getJsonString(mother, KipConstants.EC_CHILD_TABLE.BASE_ENTITY_ID);
        }
        return this;
    }
}
