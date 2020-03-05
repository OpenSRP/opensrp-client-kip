package org.smartregister.kip.cursor;

import android.database.Cursor;

import org.smartregister.kip.util.KipConstants;


public class CreateRemoteLocalCursor {
    private String id;
    private String relationalId;
    private String firstName;
    private String lastName;
    private String dob;
    private String openSrpId;

    private String phoneNumber;
    private String altName;

    public CreateRemoteLocalCursor(Cursor cursor, boolean isRemote) {
        if (isRemote) {
            id = cursor.getString(cursor.getColumnIndex(KipConstants.KEY.ID_LOWER_CASE));
        } else {
            id = cursor.getString(cursor.getColumnIndex(KipConstants.KEY.BASE_ENTITY_ID));
        }
        relationalId = cursor.getString(cursor.getColumnIndex(KipConstants.KEY.RELATIONALID));
        firstName = cursor.getString(cursor.getColumnIndex(KipConstants.KEY.FIRST_NAME));
        lastName = cursor.getString(cursor.getColumnIndex(KipConstants.KEY.LAST_NAME));
        dob = cursor.getString(cursor.getColumnIndex(KipConstants.KEY.DOB));
        openSrpId = cursor.getString(cursor.getColumnIndex(KipConstants.KEY.ZEIR_ID));
    }

    public String getId() {
        return id;
    }

    public String getRelationalId() {
        return relationalId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDob() {
        return dob;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAltName() {
        return altName;
    }

    public String getOpenSrpId() {
        return openSrpId;
    }

    public void setOpenSrpId(String openSrpId) {
        this.openSrpId = openSrpId;
    }
}