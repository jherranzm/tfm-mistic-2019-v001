package edu.uoc.mistic.tfm.jherranzm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.apptestvalidationandroid44.R;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.tasks.posttasks.PostDataToUrlTask;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    // Context
    private static WeakReference<Context> sContextReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);

        sContextReference = new WeakReference<Context>(this);

        initView();

    }

    private void initView() {
        final EditText username = findViewById(R.id.editTextUserName);
        final EditText password = findViewById(R.id.editTextPassword);
        final EditText passwordAgain = findViewById(R.id.editTextPasswordAgain);

        Button login = findViewById(R.id.buttonLogin);
        Button cancel = findViewById(R.id.buttonCancel);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String passwordValidRegEx = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[$!_-]).*$";

                Pattern pattern = Pattern.compile(passwordValidRegEx);

                boolean formValid = false;

                if(username.getText().toString().equals("")){
                    Toast.makeText(sContextReference.get(),
                            "Username must not be null or void",Toast.LENGTH_LONG).show();
                }else if( !android.util.Patterns.EMAIL_ADDRESS.matcher(username.getText().toString()).matches() ) {
                    Toast.makeText(sContextReference.get(),
                            "Username must be a valid email", Toast.LENGTH_LONG).show();

                }else if(password.getText().toString().equals("")) {
                    Toast.makeText(sContextReference.get(),
                            "Password must not be null or void", Toast.LENGTH_LONG).show();
                }else if(password.getText().toString().length() < 12) {
                    Toast.makeText(sContextReference.get(),
                            "Password must be at least 12 chars long", Toast.LENGTH_LONG).show();

                }else if( !pattern.matcher(password.getText().toString()).matches() ) {
                    Toast.makeText(sContextReference.get(),
                            "Password must contain at least one digit, one uppercase letter, one lowercase letter and one special char.", Toast.LENGTH_LONG).show();

                }else if(passwordAgain.getText().toString().equals("")) {
                    Toast.makeText(sContextReference.get(),
                            "Repeat password must not be null or void", Toast.LENGTH_LONG).show();
                }else if(!passwordAgain.getText().toString().equals(password.getText().toString())) {
                    Toast.makeText(sContextReference.get(),
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
                                    Toast.makeText(sContextReference.get(),
                                            "OK : Username " + username.getText().toString()+ " registered correctly!.", Toast.LENGTH_LONG).show();
                                    Log.i(TAG, "OK : Username " + username.getText().toString()+ " registered correctly!.");
                                    infoShow(
                                            "OK : Username " + username.getText().toString()+ " registered correctly!. A message has been sent to the users email to confirm the registration. Please review your inbox. The email is valid for 60 minutes.");
                                    break;
                                case HttpURLConnection.HTTP_CONFLICT:
                                    Toast.makeText(sContextReference.get(),
                                            "ERROR : Username " + username.getText().toString()+ " already registered.", Toast.LENGTH_LONG).show();
                                    Log.e(TAG, "ERROR : " + "ERROR : Username " + username.getText().toString()+ " already registered.");
                                    infoShow("The email you gave is already used..");
                                    break;
                                default:
                                    Toast.makeText(sContextReference.get(),
                                            "ERROR : Unexpected behaviour. Please see logs.", Toast.LENGTH_LONG).show();
                                    Log.e(TAG, "ERROR : Unexpected behaviour. Please see logs." + receivedAnswer.toString());
                                    break;
                            }

                        }else{
                            throw new Exception("ERROR: Answer type not expected!");
                        }


                    } catch (Exception e) {
                        Toast.makeText(sContextReference.get(),
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

    private  void infoShow(
            String message ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "alertShow : You clicked on OK!");
                    }
                })
                .setTitle("Alert!")
                .setMessage(message)
                .setIcon(R.drawable.ic_launcher_background);

        AlertDialog alert = builder.create();
        alert.show();
    }

    private  void alertShow(
            String message ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "alertShow : You clicked on OK!");
                    }
                })
                .setTitle("Alert!")
                .setMessage(message)
                .setIcon(R.drawable.ic_launcher_background);

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void customDialog(
            String title,
            String message,
            final String cancelMethod,
            final String okMethod){

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setIcon(R.mipmap.ic_check)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "onClick: OK Called.");
                    }
                });


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}