/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package util;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.domain.Response;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.domain.EditWrapper;
import org.smartregister.kip.repository.UniqueIdRepository;
import org.smartregister.service.HTTPAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;


/**
 * @author Maimoona
 *         Class containing some static utility methods.
 */
public class Utils {
    private static final String TAG = "Utils";

    private Utils() {
    }

    public static TableRow getDataRow(Context context, String label, String value, TableRow row) {
        TableRow tr = row;
        if (row == null) {
            tr = new TableRow(context);
            TableRow.LayoutParams trlp = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tr.setLayoutParams(trlp);
            tr.setPadding(10, 5, 10, 5);
        }

        TextView l = new TextView(context);
        l.setText(label + ": ");
        l.setPadding(20, 2, 20, 2);
        l.setTextColor(Color.BLACK);
        l.setTextSize(14);
        l.setBackgroundColor(Color.WHITE);
        tr.addView(l);

        TextView v = new TextView(context);
        v.setText(value);
        v.setPadding(20, 2, 20, 2);
        v.setTextColor(Color.BLACK);
        v.setTextSize(14);
        v.setBackgroundColor(Color.WHITE);
        tr.addView(v);

        return tr;
    }

    public static TableRow getDataRow(Context context, String label, String value, String field, TableRow row) {
        TableRow tr = row;
        if (row == null) {
            tr = new TableRow(context);
            TableRow.LayoutParams trlp = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tr.setLayoutParams(trlp);
            tr.setPadding(10, 5, 10, 5);
        }

        TextView l = new TextView(context);
        l.setText(label + ": ");
        l.setPadding(20, 2, 20, 2);
        l.setTextColor(Color.BLACK);
        l.setTextSize(14);
        l.setBackgroundColor(Color.WHITE);
        tr.addView(l);

        EditWrapper editWrapper = new EditWrapper();
        editWrapper.setCurrentValue(value);
        editWrapper.setField(field);

        EditText e = new EditText(context);
        e.setTag(editWrapper);
        e.setText(value);
        e.setPadding(20, 2, 20, 2);
        e.setTextColor(Color.BLACK);
        e.setTextSize(14);
        e.setBackgroundColor(Color.WHITE);
        e.setInputType(InputType.TYPE_NULL);
        tr.addView(e);

        return tr;
    }

    public static TableRow getDataRow(Context context) {
        TableRow tr = new TableRow(context);
        TableRow.LayoutParams trlp = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tr.setLayoutParams(trlp);
        tr.setPadding(0, 0, 0, 0);
        // tr.setBackgroundColor(Color.BLUE);
        return tr;
    }

    public static int addAsInts(boolean ignoreEmpty, String... vals) {
        int i = 0;
        for (String v : vals) {
            i += ignoreEmpty && StringUtils.isBlank(v) ? 0 : Integer.parseInt(v);
        }
        return i;
    }

    public static TableRow addToRow(Context context, String value, TableRow row) {
        return addToRow(context, value, row, false, 1);
    }

    public static TableRow addToRow(Context context, String value, TableRow row, int weight) {
        return addToRow(context, value, row, false, weight);
    }

    public static TableRow addToRow(Context context, String value, TableRow row, boolean compact) {
        return addToRow(context, value, row, compact, 1);
    }

    private static TableRow addToRow(Context context, String value, TableRow row, boolean compact, int weight) {
        return addToRow(context, Html.fromHtml(value), row, compact, weight);
    }

    private static TableRow addToRow(Context context, Spanned value, TableRow row, boolean compact, int weight) {
        TextView v = new TextView(context);
        v.setText(value);
        if (compact) {
            v.setPadding(15, 4, 1, 1);
        } else {
            v.setPadding(2, 15, 2, 15);
        }
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT, weight
        );
        params.setMargins(0, 0, 1, 0);
        v.setLayoutParams(params);
        v.setTextColor(Color.BLACK);
        v.setTextSize(14);
        v.setBackgroundColor(Color.WHITE);
        row.addView(v);

        return row;
    }

    /**
     * This method is only intended to be used for processing KIP_MOH_710_Report.csv
     *
     * @param csvFileName
     * @param columns     this map has the db column name as value and the csv column no as the key
     * @return each map is db row with key as the column name and value as the value from the csv file
     */
    public static List<Map<String, String>> populateMohIndicatorsTableFromCSV(Context context, String csvFileName, Map<Integer, String> columns) {
        List<Map<String, String>> result = new ArrayList<>();

        try {
            InputStream is = org.smartregister.util.Utils.getAssetFileInputStream(context, csvFileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    Map<String, String> csvValues = new HashMap<>();
                    String[] rowData = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    if (!TextUtils.isDigitsOnly(rowData[0])) {
                        continue;
                    }
                    for (Integer key : columns.keySet()) {
                        String value = rowData[key];
                        csvValues.put(columns.get(key), value);

                    }
                    result.add(csvValues);
                }
            } catch (IOException e) {
                Log.e(TAG, "populateMohIndicatorsTableFromCSV: error reading csv file " + Log.getStackTraceString(e));

            } finally {
                try {
                    is.close();
                    reader.close();
                } catch (Exception e) {
                    Log.e(TAG, "populateMohIndicatorsTableFromCSV: unable to close inputstream/bufferedreader " + Log.getStackTraceString(e));
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "populateMohIndicatorsTableFromCSV " + Log.getStackTraceString(e));
        }
        return result;
    }

    public static Date getDateFromString(String date, String dateFormatPattern) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPattern);
            return dateFormat.parse(date);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    public static JSONObject smartClient(String payload) {
        UniqueIdRepository uniqueIdRepo = KipApplication.getInstance().uniqueIdRepository();
        String openmrs_id = uniqueIdRepo.getNextUniqueId().getOpenmrsId();
        uniqueIdRepo.close(openmrs_id);
        String baseID = UUID.randomUUID().toString();
        final Response[] resp = new Response[1];
        String motherBaseID = UUID.randomUUID().toString();
        JSONObject clientObject = new JSONObject();
        JSONObject motherObject = new JSONObject();
        JSONObject addressFields = new JSONObject();
        JSONObject relationships = new JSONObject();
        JSONObject addresses = new JSONObject();
        JSONObject addressesOut = new JSONObject();
        JSONArray addressesFieldsArray = new JSONArray();
        JSONObject physicalAddresses = new JSONObject();
        JSONObject openmrs_attributes = new JSONObject();
        JSONObject openmrs_id_client = new JSONObject();
        JSONObject openmrs_id_mum = new JSONObject();
        SimpleDateFormat spf = new SimpleDateFormat("yyyyMMdd");


        JSONObject responseObj = null;
        try {
            responseObj = new JSONObject(payload);

            Log.i("WHOLE STRING ", responseObj.toString());
            JSONObject clientDetails = responseObj.getJSONObject("PATIENT_IDENTIFICATION");
            JSONObject clientDetailsNames = clientDetails.getJSONObject("PATIENT_NAME");
            JSONObject clientDetailsIds = clientDetails.getJSONObject("EXTERNAL_PATIENT_ID");
            JSONObject clientDetailsAddresses = clientDetails.getJSONObject("PATIENT_ADDRESS");

            relationships.put("relationshipType", "8d91a210-c2cc-11de-8d13-0010c6dffd0f");
            relationships.put("relativeEntityId", "dd32df6c-32c9-4d08-a116-e1386e8c93c5");
//                    relationships.put("relativeEntityId", motherBaseID);

            motherObject.put("mother", relationships);
            clientObject.put("relationships", motherObject);
            Date birthdate = spf.parse(clientDetails.getString("DATE_OF_BIRTH"));
            spf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            clientObject.put("birthdate", spf.format(birthdate));
            clientObject.put("birthdateApprox", false);
            clientObject.put("deathdateApprox", false);
            if (clientDetailsNames.has("FIRST_NAME")) {
                clientObject.put("firstName", clientDetailsNames.getString("FIRST_NAME"));
            }
            if (clientDetailsNames.has("LAST_NAME")) {
                clientObject.put("lastName", clientDetailsNames.getString("LAST_NAME"));
            }
            if (clientDetails.getString("DATE_OF_BIRTH").startsWith("f")) {
                clientObject.put("gender", "Female");
            } else {
                clientObject.put("gender", "Male");
            }

            physicalAddresses = clientDetailsAddresses.getJSONObject("PHYSICAL_ADDRESS");
//                    addresses.put("address3",physicalAddresses.getString("VILLAGE"));
//                    addresses.put("address2",physicalAddresses.getString("NEAREST_LANDMARK"));
//                    addresses.put("address1",clientDetailsAddresses.getString("POSTAL_ADDRESS"));
            addresses.put("address3", "za");
            addresses.put("address4", "Za");
            addressFields.put("addressFields", addresses);


            addressFields.put("addressType", "usual_residence");
//                    addressesOut.put("cityVillage",physicalAddresses.getString("WARD"));
            addressFields.put("cityVillage", "CENTRAL SAKWA");
//                    addressesOut.put("countyDistrict",physicalAddresses.getString("COUNTY"));
            addressFields.put("countyDistrict", "BONDO");
            addressFields.put("stateProvince", "SIAYA");
            addressesFieldsArray.put(addressFields);

            clientObject.put("addresses", addressesFieldsArray);

            clientObject.put("baseEntityId", baseID);
            openmrs_id_client.put("OPENMRS_ID", openmrs_id);
            clientObject.put("identifiers", openmrs_id_client);
            clientObject.put("attributes", openmrs_attributes);

            clientObject.put("dateCreated", spf.format(new Date()));
            clientObject.put("type", "Client");


            JSONObject clientMotherObject = new JSONObject();

            clientMotherObject.put("birthdate", "2010-01-01T00:00:00.000Z");
            clientMotherObject.put("birthdateApprox", true);
            JSONObject motherClientDetailsNames = clientDetails.getJSONObject("MOTHER_DETAILS").getJSONObject("MOTHER_NAME");
            clientMotherObject.put("firstName", motherClientDetailsNames.getString("FIRST_NAME"));
            clientMotherObject.put("lastName", motherClientDetailsNames.getString("LAST_NAME"));
            clientMotherObject.put("gender", "Female");
            clientMotherObject.put("addresses", addressesFieldsArray);
            clientMotherObject.put("baseEntityId", motherBaseID);
            openmrs_id_mum.put("M_KIP_ID", "MEKXJV_mother");
            clientMotherObject.put("identifiers", openmrs_id_mum);
            clientMotherObject.put("dateCreated", spf.format(new Date()));
            clientMotherObject.put("type", "Client");

//                    addClient(motherBaseID, clientMotherObject);
//                    Log.i("ONBLEEE ", clientMotherObject.toString());

//        addClient("dd32df6c-32c9-4d08-a116-e1386e8c93d1", clientObject);
            Log.i("ONBLEEE ", clientObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return clientObject;

    }


}
