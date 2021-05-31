package org.smartregister.kip.interactor;

import android.database.Cursor;

import org.smartregister.child.util.Constants;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.contract.NavigationContract;
import org.smartregister.kip.util.AppExecutors;
import org.smartregister.kip.util.KipConstants;

import java.text.MessageFormat;
import java.util.Date;

import timber.log.Timber;

public class NavigationInteractor implements NavigationContract.Interactor {

    private static NavigationInteractor instance;
    private AppExecutors appExecutors = new AppExecutors();

    public static NavigationInteractor getInstance() {
        if (instance == null)
            instance = new NavigationInteractor();

        return instance;
    }

    @Override
    public void getRegisterCount(final String registerType, final NavigationContract.InteractorCallback<Integer> callback) {
        if (callback != null) {
            appExecutors.diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Integer finalCount = getCount(registerType);
                        appExecutors.mainThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(finalCount);
                            }
                        });
                    } catch (final Exception e) {
                        appExecutors.mainThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(e);
                            }
                        });
                    }
                }
            });

        }
    }

    private int getCount(String tempRegisterType) {
        String registerType = tempRegisterType;
        int count = 0;
        Cursor cursor = null;
        if (KipConstants.RegisterType.ALL_CLIENTS.equals(registerType)) {
            registerType = "'" + KipConstants.RegisterType.OPD + "'," + "'" + KipConstants.RegisterType.CHILD + "'";
        } else {
            registerType = "'" + registerType + "'";
        }

        String mainCondition = String.format(" where %s is null AND %s is null AND register_type IN (%s) ", KipConstants.TABLE_NAME.ALL_CLIENTS + "." + KipConstants.KEY.DATE_REMOVED, KipConstants.TABLE_NAME.REGISTER_TYPE + "." + KipConstants.KEY.DATE_REMOVED, registerType);

        if (registerType.contains(KipConstants.RegisterType.CHILD)) {
            mainCondition += " AND ( " + Constants.KEY.DOD + " is NULL OR " + Constants.KEY.DOD + " = '' ) ";
        }

        try {
            SmartRegisterQueryBuilder smartRegisterQueryBuilder = new SmartRegisterQueryBuilder();
            String query = MessageFormat.format("select count(*) from {0} inner join client_register_type on ec_client.id = client_register_type.base_entity_id {1}", KipConstants.TABLE_NAME.ALL_CLIENTS, mainCondition);
            query = smartRegisterQueryBuilder.Endquery(query);
            Timber.i("2%s", query);
            cursor = commonRepository(KipConstants.TABLE_NAME.ALL_CLIENTS).rawCustomQueryForAdapter(query);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }


    private CommonRepository commonRepository(String tableName) {
        return KipApplication.getInstance().getContext().commonrepository(tableName);
    }

    @Override
    public Date sync() {
        Date syncDate = null;
        try {
            syncDate = new Date(getLastCheckTimeStamp());
        } catch (Exception e) {
            Timber.e(e);
        }

        return syncDate;
    }

    private Long getLastCheckTimeStamp() {
        return KipApplication.getInstance().getEcSyncHelper().getLastCheckTimeStamp();
    }
}
