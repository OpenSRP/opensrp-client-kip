package org.smartregister.kip.model;

import org.smartregister.child.cursor.AdvancedMatrixCursor;
import org.smartregister.child.model.BaseChildRegisterFragmentModel;
import org.smartregister.domain.Response;
import org.smartregister.kip.util.KipConstants;


public class ChildRegisterFragmentModel extends BaseChildRegisterFragmentModel {
    @Override
    public AdvancedMatrixCursor createMatrixCursor(Response<String> response) {
        //Just overriddenn
        return null;
    }

    @Override
    protected String[] mainColumns(String tableName, String parentTableName) {
        return new String[]{
                tableName + "." + KipConstants.KEY.RELATIONALID,
                tableName + "." + KipConstants.KEY.DETAILS,
                tableName + "." + KipConstants.KEY.ZEIR_ID,
                tableName + "." + KipConstants.KEY.RELATIONAL_ID,
                tableName + "." + KipConstants.KEY.FIRST_NAME,
                tableName + "." + KipConstants.KEY.LAST_NAME,
                tableName + "." + KipConstants.KEY.HOME_ADDRESS,
                tableName + "." + KipConstants.KEY.VILLAGE,
                tableName + "." + KipConstants.KEY.TRADITIONAL_AUTHORITY,
                tableName + "." + KipConstants.KEY.CHILD_TREATMENT,
                tableName + "." + KipConstants.KEY.CHILD_HIV_STATUS,
                tableName + "." + KipConstants.KEY.GENDER,
                tableName + "." + KipConstants.KEY.BASE_ENTITY_ID,
                parentTableName + "." + KipConstants.KEY.FIRST_NAME + " as " + KipConstants.KEY.MOTHER_FIRST_NAME,
                parentTableName + "." + KipConstants.KEY.LAST_NAME + " as " + KipConstants.KEY.MOTHER_LAST_NAME,
                parentTableName + "." + KipConstants.KEY.MOTHER_DOB + " as " + KipConstants.KEY.DB_MOTHER_DOB,
                parentTableName + "." + KipConstants.KEY.MOTHER_NRC_NUMBER,
                parentTableName + "." + KipConstants.KEY.MOTHER_GUARDIAN_NUMBER,
                parentTableName + "." + KipConstants.KEY.MOTHER_SECOND_PHONE_NUMBER,
                parentTableName + "." + KipConstants.KEY.PROTECTED_AT_BIRTH,
                parentTableName + "." + KipConstants.KEY.MOTHER_HIV_STATUS,
                tableName + "." + KipConstants.KEY.DOB,
                tableName + "." + KipConstants.KEY.CLIENT_REG_DATE,
                tableName + "." + KipConstants.KEY.LAST_INTERACTED_WITH
        };
    }
}
