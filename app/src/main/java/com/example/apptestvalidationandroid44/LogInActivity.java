package com.example.apptestvalidationandroid44;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.apptestvalidationandroid44.config.Constants;

import java.util.HashMap;
import java.util.Map;

public class LogInActivity
        extends AppCompatActivity {

    public static String TAG = "LogInActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.log_in_activity);

        final EditText username = (EditText)findViewById(R.id.editTextUserName);
        final EditText password = (EditText)findViewById(R.id.editTextPassword);

        Button login =(Button)findViewById(R.id.buttonLogin);
        Button cancel =(Button)findViewById(R.id.buttonCancel);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean formValid = false;

                if(username.getText().toString().equals("")){
                    Toast.makeText(InvoiceApp.getContext(),
                            "Username must not be null or void",Toast.LENGTH_LONG).show();
                }else if(password.getText().toString().equals("")) {
                    Toast.makeText(InvoiceApp.getContext(),
                            "Password must not be null or void", Toast.LENGTH_LONG).show();
                }else{
                    formValid = true;
                }

                if(formValid){
                    Map<String, String> params = new HashMap<>();
                    params.put("op", "login");
                    params.put("username", username.getText().toString());
                    params.put("pass", password.getText().toString());

                    try {
                        PostDataToUrlTask getData = new PostDataToUrlTask(params);

                        String res = getData.execute(Constants.URL_LOGIN, username.getText().toString(), password.getText().toString()).get();
                        Log.i(TAG, "res : " + res);
                    } catch (Exception e) {
                        Toast.makeText(InvoiceApp.getContext(),
                                "ERROR : " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "ERROR : " + e.getClass().getCanonicalName() + " : "+ e.getLocalizedMessage() + " : " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
