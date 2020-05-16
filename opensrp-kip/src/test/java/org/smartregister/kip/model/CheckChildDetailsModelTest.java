package org.smartregister.kip.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.smartregister.child.util.JsonFormUtils;
import org.smartregister.clientandeventmodel.DateUtil;
import org.smartregister.kip.util.KipConstants;

import java.util.Date;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest(DateUtil.class)
public class CheckChildDetailsModelTest {

    @Test
    public void invoke() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonChildObject = new JSONObject();
        JSONObject jsonIdentifiersObject = new JSONObject();
        JSONObject jsonAttributesObject = new JSONObject();
        jsonAttributesObject.put(KipConstants.KEY.INACTIVE, "inactive");
        jsonAttributesObject.put(KipConstants.KEY.LOST_TO_FOLLOW_UP, "lost to follow up");

        jsonIdentifiersObject.put(JsonFormUtils.ZEIR_ID, "zeir-id");
        jsonChildObject.put(KipConstants.KEY.IDENTIFIERS, jsonIdentifiersObject);
        jsonChildObject.put(KipConstants.KEY.ATTRIBUTES, jsonAttributesObject);

        jsonChildObject.put(KipConstants.KEY.BASE_ENTITY_ID, "entityId");
        jsonChildObject.put(KipConstants.KEY.FIRSTNAME, "first");
        jsonChildObject.put(KipConstants.KEY.LASTNAME, "last");
        jsonChildObject.put(KipConstants.KEY.MIDDLENAME, "middle");
        jsonChildObject.put(KipConstants.KEY.BIRTHDATE, String.valueOf(new Date().getTime()));
        jsonChildObject.put(KipConstants.KEY.GENDER, "male");

        jsonObject.put(KipConstants.KEY.CHILD,jsonChildObject);
        CheckChildDetailsModel checkChildDetailsModel = new CheckChildDetailsModel(jsonObject);
        CheckChildDetailsModel resultChildDetailsModel = checkChildDetailsModel.invoke();
        Assert.assertEquals("entityId", resultChildDetailsModel.getEntityId());
        Assert.assertEquals("zeirid", resultChildDetailsModel.getZeirId());
        Assert.assertEquals("first", resultChildDetailsModel.getFirstName());
        Assert.assertEquals("last", resultChildDetailsModel.getLastName());
        Assert.assertEquals("male", resultChildDetailsModel.getGender());
        Assert.assertEquals("inactive", resultChildDetailsModel.getInactive());
        Assert.assertEquals("lost to follow up", resultChildDetailsModel.getLostToFollowUp());
    }
}