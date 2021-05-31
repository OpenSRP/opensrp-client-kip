package org.smartregister.kip.util;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.api.domain.Location;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.domain.KipLocation;
import org.smartregister.kip.repository.KipLocationRepository;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.util.JsonFormUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import timber.log.Timber;

public class KipLocationUtility extends JsonFormUtils {
    public static void addChildRegLocHierarchyQuestions(JSONObject form,
                                                        org.smartregister.Context context) {
        try {
            JSONArray questions = form.getJSONObject(JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
            ArrayList<String> allLevels = getLocationLevels();
            JSONArray upToFacilities = generateLocationHierarchyTree(context, false, new ArrayList<>(allLevels.subList(4, 5)));
            JSONArray counties = generateLocationArray("County", context, true, new ArrayList<>(allLevels.subList(1, 2)));
            JSONArray subCounties = generateLocationArray("Sub County", context, true, new ArrayList<>(allLevels.subList(2, 3)));
            JSONArray wards = generateLocationArray("Ward", context, true, new ArrayList<>(allLevels.subList(3, 4)));
            updateQuestions(questions, upToFacilities, counties, subCounties, wards);
        } catch (JSONException e) {
            Timber.e(e, " --> addChildRegLocHierarchyQuestions(");
        }
    }

    private static void updateQuestions(JSONArray questions, JSONArray upToFacilities, JSONArray counties, JSONArray subCounties, JSONArray wards) throws JSONException {
        for (int i = 0; i < questions.length(); i++) {
            switch (questions.getJSONObject(i).getString("key")) {
                case "Home_Facility":
                    if (upToFacilities.length() > 0) {
                        JSONObject facility = upToFacilities.getJSONObject(0);
                        if (facility != null && facility.has("name")) {
                            questions.getJSONObject(i).put("value", facility.getString("name"));
                        }
                    }
                    break;
                case "Ce_County":
                    questions.getJSONObject(i).remove(KipJsonFormUtils.VALUES);
                    questions.getJSONObject(i).put("values", counties);
                    break;
                case "Ce_Sub_County":
                    questions.getJSONObject(i).remove(KipJsonFormUtils.VALUES);
                    questions.getJSONObject(i).put("values", subCounties);
                    break;
                case "Ce_Ward":
                    questions.getJSONObject(i).remove(KipJsonFormUtils.VALUES);
                    questions.getJSONObject(i).put("values", wards);
                    break;
                default:
                    break;
            }
        }
    }

    @NotNull
    private static ArrayList<String> getLocationLevels() {
        ArrayList<String> allLevels = new ArrayList<>();
        allLevels.add("Country");
        allLevels.add("County");
        allLevels.add("Sub County");
        allLevels.add("Ward");
        allLevels.add("Health Facility");
        return allLevels;
    }

    private static JSONArray generateLocationArray(String locationTag, org.smartregister.Context context, boolean withOtherOption, ArrayList<String> strings) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        KipLocationRepository locationRepository = KipApplication.getInstance().kipLocationRepository();
        List<KipLocation> locations = locationRepository.getLocationsByTag(locationTag);

        if (locations != null && locations.size() > 0) {
            for (Location location : locations) {
                if (location.getTags() != null && location.getTags().contains(locationTag))
                    jsonArray.put(location.getName());
            }
            jsonArray.put("Other");
        } else {
            JSONArray array = generateLocationHierarchyTree(context, withOtherOption, strings);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                if (item.has("name") && StringUtils.isNotBlank(item.getString("name"))) {
                    jsonArray.put(item.getString("name"));
                }
            }
        }

        return jsonArray;
    }

    public static JSONArray generateLocationHierarchyTree(org.smartregister.Context context, boolean withOtherOption, ArrayList<String> allowedLevels) {
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
            Timber.e(e, " --> generateLocationHierarchyTree()");
        }

        if (withOtherOption) {
            try {
                JSONObject other = new JSONObject();
                other.put("name", "Other");
                other.put("key", "Other");
                other.put("level", "");
                array.put(other);
            } catch (JSONException e) {
                Timber.e(e, "--> generateLocationHierarchyTree()");
            }
        }
        return array;
    }

    private static void getFormJsonData(JSONArray allLocationData, JSONObject openMrsLocationData, ArrayList<String> allowedLevels) throws JSONException {
        JSONObject jsonFormObject = new JSONObject();
        String name = openMrsLocationData.getJSONObject("node").getString("name");
        jsonFormObject.put("name", LocationHelper.getInstance().getOpenMrsReadableName(name));
        jsonFormObject.put("key", name);
        String level = "";
        try {
            level = openMrsLocationData.getJSONObject("node").getJSONArray("tags").getString(0);
        } catch (JSONException e) {
            Timber.e(e, " --> getFormJsonData()");
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

    private static JSONArray sortTreeViewQuestionOptions(JSONArray treeViewOptions) throws JSONException {
        JSONArray sortedTree = new JSONArray();

        HashMap<String, JSONObject> sortMap = new HashMap<>();
        for (int i = 0; i < treeViewOptions.length(); i++) {
            sortMap.put(treeViewOptions.getJSONObject(i).getString("name"), treeViewOptions.getJSONObject(i));
        }

        ArrayList<String> sortedKeys = new ArrayList<>(sortMap.keySet());
        Collections.sort(sortedKeys);

        for (String curOptionName : sortedKeys) {
            JSONObject curOption = sortMap.get(curOptionName);
            if (curOption != null && curOption.has("nodes")) {
                curOption.put("nodes", sortTreeViewQuestionOptions(curOption.getJSONArray("nodes")));
            }

            sortedTree.put(curOption);
        }

        return sortedTree;
    }
}
