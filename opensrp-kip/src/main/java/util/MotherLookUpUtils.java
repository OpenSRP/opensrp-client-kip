package util;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.Context;
import org.smartregister.clientandeventmodel.DateUtil;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.event.Listener;
import org.smartregister.kip.domain.EntityLookUp;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.VISIBLE;
import static org.smartregister.util.Utils.getValue;

/**
 * Created by keyman on 26/01/2017.
 */
public class MotherLookUpUtils {
    private static final String TAG = MotherLookUpUtils.class.getName();

    public static final String firstName = "first_name";
    public static final String lastName = "last_name";
    public static final String birthDate = "date_birth";
    public static final String dob = "dob";
    public static final String baseEntityId = "base_entity_id";
    public static final String ce_county = "Ce_County";
    public static final String ce_subCounty = "Ce_Sub_County";
    public static final String ce_ward = "Ce_Ward";
    public static final String county = "county";
    public static final String subCounty = "sub_county";
    public static final String ward = "ward";

    public static void motherLookUp(final Context context, final EntityLookUp entityLookUp, final Listener<HashMap<CommonPersonObject, List<CommonPersonObject>>> listener, final ProgressBar progressBar) {

        org.smartregister.util.Utils.startAsyncTask(new AsyncTask<Void, Void, HashMap<CommonPersonObject, List<CommonPersonObject>>>() {
            @Override
            protected HashMap<CommonPersonObject, List<CommonPersonObject>> doInBackground(Void... params) {
                publishProgress();
                return lookUp(context, entityLookUp);
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                if (progressBar != null) {
                    progressBar.setVisibility(VISIBLE);
                }
            }

            @Override
            protected void onPostExecute(HashMap<CommonPersonObject, List<CommonPersonObject>> result) {
                listener.onEvent(result);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        }, null);
    }

    private static HashMap<CommonPersonObject, List<CommonPersonObject>> lookUp(Context context, EntityLookUp entityLookUp) {
        HashMap<CommonPersonObject, List<CommonPersonObject>> results = new HashMap<>();
        if (context == null) {
            return results;
        }


        if (entityLookUp.isEmpty()) {
            return results;
        }

        String tableName = KipConstants.MOTHER_TABLE_NAME;
        String childTableName = KipConstants.CHILD_TABLE_NAME;


        List<String> ids = new ArrayList<>();
        List<CommonPersonObject> motherList = new ArrayList<>();

        CommonRepository commonRepository = context.commonrepository(tableName);
        String query = lookUpQuery(entityLookUp.getMap(), tableName, childTableName);

        Cursor cursor = null;
        try {

            cursor = commonRepository.rawCustomQueryForAdapter(query);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    CommonPersonObject commonPersonObject = commonRepository.readAllcommonforCursorAdapter(cursor);
                    motherList.add(commonPersonObject);


                    ids.add(commonPersonObject.getCaseId());
                    cursor.moveToNext();
                }
            }


        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (motherList.isEmpty()) {
            return results;
        }

        CommonRepository childRepository = context.commonrepository(childTableName);
        List<CommonPersonObject> childList = childRepository.findByRelational_IDs(ids.toArray(new String[ids.size()]));

        for (CommonPersonObject mother : motherList) {
            results.put(mother, findChildren(childList, mother.getCaseId()));
        }


        return results;

    }

    private static List<CommonPersonObject> findChildren(List<CommonPersonObject> childList, String motherBaseEnityId) {
        List<CommonPersonObject> foundChildren = new ArrayList<>();
        for (CommonPersonObject child : childList) {
            String relationalID = getValue(child.getColumnmaps(), "relational_id", false);
            if (!foundChildren.contains(child) && relationalID.equals(motherBaseEnityId)) {
                foundChildren.add(child);
            }
        }

        return foundChildren;

    }

    private static String lookUpQuery(Map<String, String> entityMap, String tableName, String childTableName) {

        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        String joinTableAlias = "j";
        queryBUilder.SelectInitiateMainTable(tableName, new String[]{
                tableName + ".relationalid as relationalid",
                tableName + ".details as details",
                tableName + ".zeir_id as zeir_id",
                tableName + ".first_name as first_name",
                tableName + ".last_name as last_name",
                tableName + ".gender as gender",
                tableName + ".dob as dob",
                tableName + ".nrc_number as nrc_number",
                tableName + ".contact_phone_number as contact_phone_number",
                tableName + ".base_entity_id as base_entity_id",
                joinTableAlias + ".county as county",
                joinTableAlias + ".sub_county as sub_county",
                joinTableAlias + ".ward as ward"}

        );
        String joinTable = "(select relational_id, county, sub_county, ward FROM " + childTableName
                + " c where c.id = (select min(id) from " + childTableName + " ec where ec.relational_id = c.relational_id))";
        queryBUilder.customJoin("JOIN " + joinTable + " " + joinTableAlias + " ON  " + tableName + ".id = " + joinTableAlias + ".relational_id");
        String query = queryBUilder.mainCondition(getMainConditionString(tableName, entityMap));
        return queryBUilder.Endquery(query);
    }


    private static String getMainConditionString(String tableName, Map<String, String> entityMap) {

        String mainConditionString = "";
        for (Map.Entry<String, String> entry : entityMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (StringUtils.containsIgnoreCase(key, firstName)) {
                key = tableName + "." + firstName;
            }

            if (StringUtils.containsIgnoreCase(key, lastName)) {
                key = tableName + "." + lastName;
            }

            if (StringUtils.containsIgnoreCase(key, birthDate)) {
                if (!isDate(value)) {
                    continue;
                }
                key = tableName + "." + dob;
            }

            if (!key.equals(dob)) {
                if (StringUtils.isBlank(mainConditionString)) {
                    mainConditionString += " " + key + " Like '%" + value + "%'";
                } else {
                    mainConditionString += " AND " + key + " Like '%" + value + "%'";

                }
            } else {
                if (StringUtils.isBlank(mainConditionString)) {
                    mainConditionString += " cast(" + key + " as date) " + " =  cast('" + value + "'as date) ";
                } else {
                    mainConditionString += " AND cast(" + key + " as date) " + " =  cast('" + value + "'as date) ";

                }
            }
        }

        return mainConditionString;

    }

    private static boolean isDate(String dobString) {
        try {
            DateUtil.yyyyMMdd.parse(dobString);
            return true;
        } catch (ParseException e) {
            return false;
        }

    }
}
