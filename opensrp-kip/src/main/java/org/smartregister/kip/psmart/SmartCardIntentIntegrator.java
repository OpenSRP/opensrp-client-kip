/*
 * Copyright 2009 ZXing authors
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

package org.smartregister.kip.psmart;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.kip.R;
import org.smartregister.view.activity.SecuredActivity;
import org.smartregister.view.fragment.SecuredFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SmartCardIntentIntegrator {

    private static final String ACTION_READ_DATA = "org.kenyahmis.psmart.ACTION_READ_DATA";
    private static final String ACTION_WRITE_DATA = "org.kenyahmis.psmart.ACTION_WRITE_DATA";
    private static final String EXTRA_AUTH_TOKEN = "AUTH_TOKEN";
    private static final String AUTH_TOKEN_VALUE = "123";
    private static final String EXTRA_ERRORS = "errors";
    private static final String EXTRA_MESSAGE = "message";
    public static final int SMARTCARD_READ_REQUEST_CODE = 98;
    public static final int SMARTCARD_WRITE_REQUEST_CODE = 99;
    private final Activity activity;
    public SmartCardIntentIntegrator(Activity activity){
        this.activity = activity;
    }

    public static boolean isReadRequest(int requestCode){
        return requestCode == SMARTCARD_READ_REQUEST_CODE;
    }

    public static boolean isWriteRequest(int requestCode){
        return requestCode == SMARTCARD_WRITE_REQUEST_CODE;
    }

    public void initiateCardRead(){
        Intent intent = new Intent();
        intent.setAction(ACTION_READ_DATA);
        intent.setType("text/plain");
        intent.putExtra(EXTRA_AUTH_TOKEN,AUTH_TOKEN_VALUE);
        startIntentActivityForResult(intent,SMARTCARD_READ_REQUEST_CODE);

    }
    public void initiateCardWrite(String SHRModel) {
        Intent intent = new Intent();
        intent.setAction(ACTION_WRITE_DATA);
        intent.putExtra(EXTRA_AUTH_TOKEN,AUTH_TOKEN_VALUE);
        intent.setType("text/plain");
        intent.putExtra("ACTION","WRITE");
        intent.putExtra("WRITE_DATA",SHRModel);
        startIntentActivityForResult(intent,SMARTCARD_WRITE_REQUEST_CODE);
    }
    public static SmartCardIntentResult parseActivityResult(int requestCode, int resultCode, Intent intent) throws Exception{
        if (requestCode == SMARTCARD_READ_REQUEST_CODE || requestCode == SMARTCARD_WRITE_REQUEST_CODE) {
            if(intent == null){
                throw new Exception("Cannot get result intent");
            }

            SmartCardIntentResult result = new SmartCardIntentResult();
            SmartCardRecord smartCardRecord = new SmartCardRecord();
            if (requestCode == SMARTCARD_READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                String jsonSHRModel = intent.getStringExtra(EXTRA_MESSAGE);
                smartCardRecord.setPlainPayload(jsonSHRModel);
                result.setSmartCardRecord(smartCardRecord);
            }else if (requestCode == SMARTCARD_WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                String jsonSHRModel = intent.getStringExtra(EXTRA_MESSAGE);
                smartCardRecord.setEncryptedPayload(jsonSHRModel);
                result.setSmartCardRecord(smartCardRecord);
            }else if (resultCode == Activity.RESULT_CANCELED) {
                Bundle bundle = intent.getExtras();
                Object errorMessages = bundle.get(EXTRA_ERRORS);
                List<String> returnedMessages = new ArrayList<>();
                if(errorMessages instanceof String){
                    if(StringUtils.isEmpty((String)errorMessages)){
                        returnedMessages.add((String)errorMessages);
                    } else {
                        returnedMessages.add("Could not complete request");
                    }
                } else if(errorMessages instanceof ArrayList){
                    returnedMessages.addAll( (List<String>)errorMessages);
                }
                result.setErrors(returnedMessages);
            }
            return result;
        }
        return null;
    }

    private void startIntentActivityForResult(Intent intent, int requestCode){
        PackageManager packageManager = activity.getPackageManager();
        if(intent.resolveActivity(packageManager) != null) {
            activity.startActivityForResult(intent, requestCode);
        } else {
            showInstallPsmartAppDownloadDialog();
        }
    }

    private void showInstallPsmartAppDownloadDialog() {
        android.app.AlertDialog.Builder downloadDialog = new android.app.AlertDialog.Builder(activity);
        downloadDialog.setTitle("title_install_psmart");
        downloadDialog.setMessage("hint_install_psmart");
        downloadDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String packageName = "org.kenyahmis.psmart";
                Uri uri = Uri.parse("market://details?id=" + packageName);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.w(getClass().getSimpleName(), "Google Play is not installed; cannot install " + packageName, e);
                }
            }
        });
        downloadDialog.setNegativeButton("general_no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        downloadDialog.show();
    }
}
