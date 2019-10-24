package org.smartregister.kip.interactor;

import android.database.Cursor;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.contract.NavigationContract;
import org.smartregister.kip.util.AppExecutors;
import org.smartregister.kip.util.KipConstants;
import org.smartregister.kip.util.KipUtils;

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
    public void getRegisterCount(final String tableName, final NavigationContract.InteractorCallback<Integer> callback) {
        if (callback != null) {
            appExecutors.diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Integer finalCount = getCount(tableName);
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

    private int getCount(String tableName) {
        int count = 0;
        Cursor cursor = null;
        String mainCondition = "";
        if (tableName.equalsIgnoreCase(KipConstants.TABLE_NAME.CHILD)) {
            mainCondition = String.format(" where %s is null AND %s", KipConstants.KEY.DATE_REMOVED,
                    KipUtils.childAgeLimitFilter());
        }

        if (StringUtils.isNoneEmpty(mainCondition)) {
            try {
                SmartRegisterQueryBuilder smartRegisterQueryBuilder = new SmartRegisterQueryBuilder();
                String query = MessageFormat.format("select count(*) from {0} {1}", tableName, mainCondition);
                query = smartRegisterQueryBuilder.Endquery(query);
                Timber.i("2%s", query);
                cursor = commonRepository(tableName).rawCustomQueryForAdapter(query);
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(0);
                }
            } catch (Exception e) {
                Timber.e(e, "NavigationInteractor --> getCount");
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
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
            Timber.e(e, "NavigationInteractor --> sync");
        }
        return syncDate;
    }

    private Long getLastCheckTimeStamp() {
        return KipApplication.getInstance().getEcSyncHelper().getLastCheckTimeStamp();
    }
}
