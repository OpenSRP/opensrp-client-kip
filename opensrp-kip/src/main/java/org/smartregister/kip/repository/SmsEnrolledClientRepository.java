package org.smartregister.kip.repository;


import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.kip.pojo.SmsEnrolledClient;
import org.smartregister.repository.BaseRepository;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class SmsEnrolledClientRepository extends BaseRepository {

    public List<SmsEnrolledClient> getEnrolledClients(){
        List<SmsEnrolledClient> smsEnrolledClient = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor sCursor = null;

        String sql = "SELECT first_name,last_name,phone_number FROM ec_client WHERE phone_number IS NOT NULL";
        Timber.i("Fetch enrolled clients");

        try {
            sCursor = db.rawQuery(sql,null);
            smsEnrolledClient = readAll(sCursor);

        } catch (Exception e){
            Timber.d("-->getEnrolledClients" + e.getMessage());
        } finally {
            if (sCursor !=null){
                sCursor.close();
            }
        }
        return smsEnrolledClient;
    }

    public SmsEnrolledClient getEnrolledClients(Cursor cursor){
        SmsEnrolledClient smsEnrolledClient = new SmsEnrolledClient();
        smsEnrolledClient.setFirstName(cursor.getString(cursor.getColumnIndex("first_name")));
        smsEnrolledClient.setLastName(cursor.getString(cursor.getColumnIndex("last_name")));
        smsEnrolledClient.setPhoneNumber(cursor.getString(cursor.getColumnIndex("phone_number")));
        return smsEnrolledClient;
    }

    private List<SmsEnrolledClient> readAll(Cursor cursor){
        List<SmsEnrolledClient> smsEnrolledClients = new ArrayList<>();
        if (cursor !=null && cursor.getCount() > 0 && cursor.moveToNext()){
            cursor.moveToFirst();
            while (cursor.getCount() > 0 && !cursor.isAfterLast()){
                smsEnrolledClients.add(getEnrolledClients(cursor));
                cursor.moveToNext();
            }
        }
        return smsEnrolledClients;
    }
}
