package org.smartregister.kip.presenter;

import android.database.Cursor;
import android.database.CursorJoiner;

import org.smartregister.child.contract.ChildAdvancedSearchContract;
import org.smartregister.child.cursor.AdvancedMatrixCursor;
import org.smartregister.child.presenter.BaseChildAdvancedSearchPresenter;
import org.smartregister.kip.cursor.CreateRemoteLocalCursor;
import org.smartregister.kip.model.AdvancedSearchModel;
import org.smartregister.kip.util.DBQueryHelper;
import org.smartregister.kip.util.KipConstants;

/**
 * Created by ndegwamartin on 11/04/2019.
 */
public class AdvancedSearchPresenter extends BaseChildAdvancedSearchPresenter {
    public AdvancedSearchPresenter(ChildAdvancedSearchContract.View view, String viewConfigurationIdentifier) {
        super(view, viewConfigurationIdentifier, new AdvancedSearchModel());
    }

    @Override
    protected AdvancedMatrixCursor getRemoteLocalMatrixCursor(AdvancedMatrixCursor matrixCursor) {
        String query = getView().filterAndSortQuery();
        Cursor cursor = getView().getRawCustomQueryForAdapter(query);
        if (cursor != null && cursor.getCount() > 0) {
            AdvancedMatrixCursor remoteLocalCursor = new AdvancedMatrixCursor(
                    new String[]{KipConstants.KEY.ID_LOWER_CASE, KipConstants.KEY.RELATIONALID, KipConstants.KEY.FIRST_NAME, KipConstants.KEY.LAST_NAME, KipConstants.KEY.DOB, KipConstants.KEY.ZEIR_ID});

            CursorJoiner joiner = new CursorJoiner(matrixCursor,
                    new String[]{KipConstants.KEY.ZEIR_ID, KipConstants.KEY.ID_LOWER_CASE}, cursor,
                    new String[]{KipConstants.KEY.ZEIR_ID, KipConstants.KEY.ID_LOWER_CASE});
            for (CursorJoiner.Result joinerResult : joiner) {
                switch (joinerResult) {
                    case BOTH:
                        CreateRemoteLocalCursor createRemoteLocalCursor = new CreateRemoteLocalCursor(matrixCursor, true);
                        remoteLocalCursor
                                .addRow(new Object[]{createRemoteLocalCursor.getId(), createRemoteLocalCursor.getRelationalId(),
                                        createRemoteLocalCursor.getFirstName(), createRemoteLocalCursor.getLastName(), createRemoteLocalCursor.getDob(), createRemoteLocalCursor.getOpenSrpId()});
                        break;
                    case RIGHT:
                        CreateRemoteLocalCursor localCreateRemoteLocalCursor = new CreateRemoteLocalCursor(cursor, false);
                        remoteLocalCursor
                                .addRow(new Object[]{localCreateRemoteLocalCursor.getId(), localCreateRemoteLocalCursor.getRelationalId(),
                                        localCreateRemoteLocalCursor.getFirstName(), localCreateRemoteLocalCursor.getLastName(), localCreateRemoteLocalCursor.getDob(), localCreateRemoteLocalCursor.getOpenSrpId()});

                        break;
                    case LEFT:
                        createRemoteLocalCursor = new CreateRemoteLocalCursor(matrixCursor, true);
                        remoteLocalCursor
                                .addRow(new Object[]{createRemoteLocalCursor.getId(), createRemoteLocalCursor.getRelationalId(),
                                        createRemoteLocalCursor.getFirstName(), createRemoteLocalCursor.getLastName(), createRemoteLocalCursor.getDob(), createRemoteLocalCursor.getOpenSrpId()});
                        break;
                    default:
                        break;
                }
            }

            cursor.close();
            matrixCursor.close();
            return remoteLocalCursor;
        } else {
            return matrixCursor;
        }
    }

    @Override
    public String getMainCondition() {
        return DBQueryHelper.getHomeRegisterCondition();
    }
}
