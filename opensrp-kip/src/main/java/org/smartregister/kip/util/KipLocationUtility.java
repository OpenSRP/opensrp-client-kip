package org.smartregister.kip.util;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.api.domain.Location;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.repository.KipLocationRepository;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.util.JsonFormUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class KipLocationUtility extends JsonFormUtils {
    public static void addChildRegLocHierarchyQuestions(JSONObject form,
                                                        org.smartregister.Context context) {
        try {
            JSONArray questions = form.getJSONObject("step1").getJSONArray("fields");
            ArrayList<String> allLevels = new ArrayList<>();
            allLevels.add("Country");
            allLevels.add("County");
            allLevels.add("Sub County");
            allLevels.add("Ward");
            allLevels.add("Health Facility");

            JSONArray upToFacilities = generateLocationHierarchyTree(context, false, new ArrayList<>(allLevels.subList(4, 5)));
            JSONArray counties = generateLocationArray("County", context, true, new ArrayList<>(allLevels.subList(1, 2)));
            JSONArray subCounties = generateLocationArray("Sub County", context, true, new ArrayList<>(allLevels.subList(2, 3)));
            JSONArray wards = generateLocationArray("Ward", context, true, new ArrayList<>(allLevels.subList(3, 4)));

            for (int i = 0; i < questions.length(); i++) {
                if (questions.getJSONObject(i).getString("key").equals("Home_Facility")) {
                    if (upToFacilities.length() > 0) {
                        JSONObject facility = upToFacilities.getJSONObject(0);
                        if (facility != null && facility.has("name")) {
                            questions.getJSONObject(i).put("value", facility.getString("name"));
                        }
                    }
                } else if (questions.getJSONObject(i).getString("key").equals("County")) {
                    questions.getJSONObject(i).remove(KipChildJsonFormUtils.VALUES);
                    questions.getJSONObject(i).put("values", counties);

                } else if (questions.getJSONObject(i).getString("key").equals("Sub_County")) {
                    questions.getJSONObject(i).remove(KipChildJsonFormUtils.VALUES);
                    questions.getJSONObject(i).put("values", subCounties);

                } else if (questions.getJSONObject(i).getString("key").equals("Ward")) {
                    questions.getJSONObject(i).remove(KipChildJsonFormUtils.VALUES);
                    questions.getJSONObject(i).put("values", wards);

                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private static JSONArray generateLocationArray(String locationTag, org.smartregister.Context context, boolean b, ArrayList<String> strings) throws
            JSONException {

        JSONArray jsonArray = new JSONArray();
        KipLocationRepository locationRepository = KipApplication.getInstance().kipLocationRepository();

        List<Location> locations = locationRepository.getLocationsByTag(locationTag);

        if (locations != null && locations.size() > 0) {
            for (Location l : locations) {
                if (l.getTags() != null && l.getTags().contains(locationTag))
                    jsonArray.put(l.getName());
            }
            jsonArray.put("Other");
        } else {
            JSONArray array = generateLocationHierarchyTree(context, b, strings);
            for (int i = 0; i < array.length(); i++) {
                JSONObject jo = array.getJSONObject(i);
                if (jo.has("name") && StringUtils.isNotBlank(jo.getString("name"))) {
                    jsonArray.put(jo.getString("name"));
                }
            }
        }

        return jsonArray;
    }

    public static JSONArray generateLocationHierarchyTree(org.smartregister.Context context,
                                                          boolean withOtherOption, ArrayList<String> allowedLevels) {
        JSONArray array = new JSONArray();
        try {
            JSONObject locationData = new JSONObject(context.anmLocationController().get());
            if (locationData.has("locationsHierarchy")
                    && locationData.getJSONObject("locationsHierarchy").has("map")) {
                JSONObject map = locationData.getJSONObject("locationsHierarchy").getJSONObject("map");
                Iterator<String> keys = map.keys();
                while (keys.hasNext()) {
                    String curKey = keys.next();
                    getFormJsonData(array, map.getJSONObject(curKey), allowedLevels);
                }
            }

            array = sortTreeViewQuestionOptions(array);
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        if (withOtherOption) {
            try {
                JSONObject other = new JSONObject();
                other.put("name", "Other");
                other.put("key", "Other");
                other.put("level", "");
                array.put(other);
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        return array;
    }

    private static void getFormJsonData(JSONArray allLocationData, JSONObject
            openMrsLocationData, ArrayList<String> allowedLevels) throws JSONException {
        JSONObject jsonFormObject = new JSONObject();
        String name = openMrsLocationData.getJSONObject("node").getString("name");
        jsonFormObject.put("name", LocationHelper.getInstance().getOpenMrsReadableName(name));
        jsonFormObject.put("key", name);
        String level = "";
        try {
            level = openMrsLocationData.getJSONObject("node").getJSONArray("tags").getString(0);
        } catch (JSONException e) {
            Log.e(KipChildJsonFormUtils.class.getCanonicalName(), e.getMessage());
        }
        jsonFormObject.put("level", "");
        JSONArray children = new JSONArray();
        if (openMrsLocationData.has("children")) {
            Iterator<String> childIterator = openMrsLocationData.getJSONObject("children").keys();
            while (childIterator.hasNext()) {
                String curChildKey = childIterator.next();
                getFormJsonData(children, openMrsLocationData.getJSONObject("children").getJSONObject(curChildKey), allowedLevels);
            }
            if (allowedLevels.contains(level)) {
                jsonFormObject.put("nodes", children);
            } else {
                for (int i = 0; i < children.length(); i++) {
                    allLocationData.put(children.getJSONObject(i));
                }
            }
        }
        if (allowedLevels.contains(level)) {
            allLocationData.put(jsonFormObject);
        }
    }

    private static JSONArray sortTreeViewQuestionOptions(JSONArray treeViewOptions) throws
            JSONException {
        JSONArray sortedTree = new JSONArray();

        HashMap<String, JSONObject> sortMap = new HashMap<>();
        for (int i = 0; i < treeViewOptions.length(); i++) {
            sortMap.put(treeViewOptions.getJSONObject(i).getString("name"), treeViewOptions.getJSONObject(i));
        }

        ArrayList<String> sortedKeys = new ArrayList<>(sortMap.keySet());
        Collections.sort(sortedKeys);

        for (String curOptionName : sortedKeys) {
            JSONObject curOption = sortMap.get(curOptionName);
            if (curOption.has("nodes")) {
                curOption.put("nodes", sortTreeViewQuestionOptions(curOption.getJSONArray("nodes")));
            }

            sortedTree.put(curOption);
        }

        return sortedTree;
    }
}
