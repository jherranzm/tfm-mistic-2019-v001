package com.example.apptestvalidationandroid44;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.apptestvalidationandroid44.config.Constants;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    public static String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.sign_up_activity);

        final EditText username = (EditText)findViewById(R.id.editTextUserName);
        final EditText password = (EditText)findViewById(R.id.editTextPassword);
        final EditText passwordAgain = (EditText)findViewById(R.id.editTextPasswordAgain);

        Button login =(Button)findViewById(R.id.buttonLogin);
        Button cancel =(Button)findViewById(R.id.buttonCancel);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String passwordValidRegEx = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[$!_-]).*$";

                Pattern pattern = Pattern.compile(passwordValidRegEx);

                boolean formValid = false;

                if(username.getText().toString().equals("")){
                    Toast.makeText(InvoiceApp.getContext(),
                            "Username must not be null or void",Toast.LENGTH_LONG).show();
                }else if( !android.util.Patterns.EMAIL_ADDRESS.matcher(username.getText().toString()).matches() ) {
                    Toast.makeText(InvoiceApp.getContext(),
                            "Username must be a valid email", Toast.LENGTH_LONG).show();

                }else if(password.getText().toString().equals("")) {
                    Toast.makeText(InvoiceApp.getContext(),
                            "Password must not be null or void", Toast.LENGTH_LONG).show();
                }else if(password.getText().toString().length() < 12) {
                    Toast.makeText(InvoiceApp.getContext(),
                            "Password must be at least 12 chars long", Toast.LENGTH_LONG).show();

                }else if( !pattern.matcher(password.getText().toString()).matches() ) {
                    Toast.makeText(InvoiceApp.getContext(),
                            "Password must contain at least one digit, one uppercase letter, one lowercase letter and one special char.", Toast.LENGTH_LONG).show();

                }else if(passwordAgain.getText().toString().equals("")) {
                    Toast.makeText(InvoiceApp.getContext(),
                            "Repeat password must not be null or void", Toast.LENGTH_LONG).show();
                }else if(!passwordAgain.getText().toString().equals(password.getText().toString())) {
                    Toast.makeText(InvoiceApp.getContext(),
                            "Passwords are NOT equal", Toast.LENGTH_LONG).show();
                }else{
                    formValid = true;
                }

                if(formValid){
                    Map<String, String> params = new HashMap<>();
                    params.put("op", "signUp");
                    params.put("username", username.getText().toString());
                    params.put("pass", password.getText().toString());


                    try {
                        PostDataToUrlTask getData = new PostDataToUrlTask(params);

                        String res = getData.execute(Constants.URL_SIGNUP).get();
                        Log.i(TAG, "res : " + res);

                        JSONObject receivedAnswer = new JSONObject(res);

                        if (receivedAnswer.has("responseCode")) {
                            int responseCode = (Integer)receivedAnswer.get("responseCode");

                            switch (responseCode){
                                case HttpURLConnection.HTTP_OK:
                                    Toast.makeText(InvoiceApp.getContext(),
                                            "OK : Username " + username.getText().toString()+ " registered correctly!.", Toast.LENGTH_LONG).show();
                                    Log.i(TAG, "OK : Username " + username.getText().toString()+ " registered correctly!.");
                                    infoDialog(
                                            "User correctly registered in system",
                                            "OK : Username " + username.getText().toString()+ " registered correctly!. A message has been sent to the users email to confirm the registration. Please review your inbox. The email is valid for 60 minutes.",
                                    "OK");

                                    break;
                                case HttpURLConnection.HTTP_CONFLICT:
                                    Toast.makeText(InvoiceApp.getContext(),
                                            "ERROR : Username " + username.getText().toString()+ " already registered.", Toast.LENGTH_LONG).show();
                                    Log.e(TAG, "ERROR : " + "ERROR : Username " + username.getText().toString()+ " already registered.");
                                    break;
                                default:
                                    Toast.makeText(InvoiceApp.getContext(),
                                            "ERROR : Unexpected behaviour. Please see logs.", Toast.LENGTH_LONG).show();
                                    Log.e(TAG, "ERROR : Unexpected behaviour. Please see logs." + receivedAnswer.toString());
                                    break;
                            }

                        }else{
                            throw new Exception("ERROR: Answer type not expected!");
                        }


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

    private void infoDialog(
            String title,
            String message,
            final String okMethod){
        final android.support.v7.app.AlertDialog.Builder builderSingle = new android.support.v7.app.AlertDialog.Builder(this);
        builderSingle.setIcon(R.mipmap.ic_launcher_round);
        builderSingle.setTitle("Info");
        builderSingle.setMessage(message);


        builderSingle.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "onClick: OK Called.");
                        if(okMethod.equals("ok")){
                            Log.d(TAG, "onClick: OK Called.");

                        }
                    }
                });


        builderSingle.show();
    }
}