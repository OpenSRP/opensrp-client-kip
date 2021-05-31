package org.smartregister.kip.repository;


import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.kip.pojo.SmsErolledClient;
import org.smartregister.repository.BaseRepository;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class SmsEnrolledClientRepository extends BaseRepository {

    public List<SmsErolledClient> getEnrolledClients(){
        List<SmsErolledClient> smsErolledClient = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor sCursor = null;

        String sql = "SELECT first_name,last_name,phone_number FROM ec_client WHERE phone_number IS NOT NULL";
        Timber.i("Fetch enrolled clients");

        try {
            sCursor = db.rawQuery(sql,null);
            smsErolledClient = readAll(sCursor);

        } catch (Exception e){
            Timber.d("-->getEnrolledClients" + e.getMessage());
        } finally {
            if (sCursor !=null){
                sCursor.close();
            }
        }
        return smsErolledClient;
    }

    public SmsErolledClient getEnrolledClients(Cursor cursor){
        SmsErolledClient smsErolledClient = new SmsErolledClient();
        smsErolledClient.setFirstName(cursor.getString(cursor.getColumnIndex("first_name")));
        smsErolledClient.setLastName(cursor.getString(cursor.getColumnIndex("last_name")));
        smsErolledClient.setPhoneNumber(cursor.getString(cursor.getColumnIndex("phone_number")));
        return smsErolledClient;
    }

    private List<SmsErolledClient> readAll(Cursor cursor){
        List<SmsErolledClient> smsErolledClients = new ArrayList<>();
        if (cursor !=null && cursor.getCount() > 0 && cursor.moveToNext()){
            cursor.moveToFirst();
            while (cursor.getCount() > 0 && !cursor.isAfterLast()){
                smsErolledClients.add(getEnrolledClients(cursor));
                cursor.moveToNext();
            }
        }
        return smsErolledClients;
    }
}
