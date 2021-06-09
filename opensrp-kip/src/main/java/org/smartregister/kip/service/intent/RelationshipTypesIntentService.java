package org.smartregister.kip.service.intent;

import android.app.IntentService;
import android.content.Intent;
import androidx.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;
import org.smartregister.kip.context.AllSettings;
import org.smartregister.kip.context.KipContext;

/**
 * Created by amos.laboso on 07/11/2017.
 */

public class RelationshipTypesIntentService extends IntentService {

    private static final String TAG = RelationshipTypesIntentService.class.getCanonicalName();

    private AllSettings allSettings;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public RelationshipTypesIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "onHandleIntent.................");
        String userInfo = intent.getExtras().getString("userInfo");
        try {
            JSONObject userInfoObject = new JSONObject(userInfo);
            if (userInfoObject.has("relationshipTypes")) {
                String relationshipTypes = userInfoObject.getString("relationshipTypes");
                allSettings.saveRelationshipTypes(relationshipTypes);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.i(TAG, "onHandleIntent.................#");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        allSettings = (AllSettings) KipContext.getInstance().allSettings();
        return super.onStartCommand(intent, flags, startId);
    }
}
