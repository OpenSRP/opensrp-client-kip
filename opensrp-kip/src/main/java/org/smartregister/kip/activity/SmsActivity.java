package org.smartregister.kip.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.smartregister.child.util.Utils;
import org.smartregister.kip.R;
import org.smartregister.kip.adapter.SmsEnrollmentAdapter;
import org.smartregister.kip.application.KipApplication;
import org.smartregister.kip.model.SmsEnrollementModel;
import org.smartregister.kip.pojo.SmsErolledClient;
import org.smartregister.kip.repository.SmsEnrolledClientRepository;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class SmsActivity extends Activity {

    Button sendBtn, addClient;
    EditText txtMessage;
    String message;
    ImageButton closeSms;
    TextView selectClient;
    View clientEnrolledListView, sendSms;

    private ListView listView;
    private ArrayList<SmsEnrollementModel> modelArrayList;
    private SmsEnrollmentAdapter customAdapter;
    Button btnSelect, btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        listView = findViewById(R.id.listView);
        btnSelect = findViewById(R.id.viewCheckedItem);
        btnDelete = findViewById(R.id.deSelect);
        addClient = findViewById(R.id.add);
        modelArrayList = getModel(false);
        customAdapter = new SmsEnrollmentAdapter(SmsActivity.this, modelArrayList);
        listView.setAdapter(customAdapter);
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modelArrayList = getModel(true);
                customAdapter = new SmsEnrollmentAdapter(SmsActivity.this, modelArrayList);
                listView.setAdapter(customAdapter);
                Toast.makeText(getApplicationContext(), "Checked all items",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modelArrayList = getModel(false);
                customAdapter = new SmsEnrollmentAdapter(SmsActivity.this, modelArrayList);
                listView.setAdapter(customAdapter);
                Toast.makeText(getApplicationContext(), "Unchecked all items",
                        Toast.LENGTH_SHORT).show();
            }
        });

        sendBtn = (Button) findViewById(R.id.btnSendSMS);
        txtMessage = (EditText) findViewById(R.id.editText2);
        closeSms = (ImageButton) findViewById(R.id.close_advocacy_sms);
        selectClient = (TextView) findViewById(R.id.select_clients);
        clientEnrolledListView = (View) findViewById(R.id.enrolled_list);
        sendSms = (View) findViewById(R.id.send_sms);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                        message = txtMessage.getText().toString();
                        sendSMSMessage(message);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 1);
                    }
                }
                Intent intent = new Intent(view.getContext(), KipOpdRegisterActivity.class);
                startActivityForResult(intent, 0);
                finish();
            }
        });

        addClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientEnrolledListView.setVisibility(View.GONE);
                sendSms.setVisibility(View.VISIBLE);
            }
        });

        closeSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), KipOpdRegisterActivity.class);
                startActivityForResult(intent, 0);
                finish();
            }
        });

        selectClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientEnrolledListView.setVisibility(View.VISIBLE);
            }
        });

    }

    public void sendSMSMessage(String message){

        try {
            SmsManager smsManager = SmsManager.getDefault();
            for (int i = 0; i < getPhoneNumber().size(); i++){
            smsManager.sendTextMessage(getPhoneNumber().get(i),null, message, null, null);
            Utils.showToast(this, "Message Advocacy Message Sent Successfully");}
        } catch (Exception e){
            Timber.e(e, "Message could not be sent");
        }
        Timber.i("-->Send AdvocacySMS");
    }

    private ArrayList<SmsEnrollementModel> getModel(boolean isSelected){

        ArrayList<SmsEnrollementModel> list = new ArrayList<>();
        try {
            for (int i = 0; i < getFullName().size(); i++){
                SmsEnrollementModel model = new SmsEnrollementModel();
                model.setClient(getFullName().get(i));
                model.setPhoneNumber(getPhoneNumber().get(i));
                model.setSelected(isSelected);
                list.add(model);
            }
        } catch (Exception e){
            Timber.d("-->getModel %s", e.getMessage());
        }

        return list;
    }

    private List<String> getFullName(){
        List<String> list = new ArrayList<>();
        SmsEnrolledClientRepository smsErolledClient = KipApplication.getInstance().smsEnrolledClientRepository();
        List<SmsErolledClient> enrolledClients = smsErolledClient.getEnrolledClients();
        if (enrolledClients !=null){
            for (SmsErolledClient erolledClient : enrolledClients){
                list.add(erolledClient.getFirstName() + " " + erolledClient.getLastName() );
            }
        }
        return list;
    }

    public List<String> getPhoneNumber(){
        List<String> list = new ArrayList<>();
        SmsEnrolledClientRepository smsErolledClient = KipApplication.getInstance().smsEnrolledClientRepository();
        List<SmsErolledClient> enrolledClients = smsErolledClient.getEnrolledClients();
        if (enrolledClients !=null){
            for (SmsErolledClient erolledClient : enrolledClients){
                list.add(erolledClient.getPhoneNumber());
            }
        }
        return list;
    }

}
