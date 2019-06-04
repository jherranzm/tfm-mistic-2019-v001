package edu.uoc.mistic.tfm.jherranzm.ui.activities;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import edu.uoc.mistic.tfm.jherranzm.R;
import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.tasks.posttasks.PostLoginTask;
import edu.uoc.mistic.tfm.jherranzm.util.TFMSecurityManager;

public class LogInActivity
        extends AppCompatActivity {

    private static final String TAG = LogInActivity.class.getSimpleName();

    // Security
    private TFMSecurityManager tfmSecurityManager;

    // Context
    private static WeakReference<Context> sContextReference;

    private ConstraintLayout constraintLayout;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.log_in_activity);

        sContextReference = new WeakReference<Context>(this);

        initView();

        tfmSecurityManager = TFMSecurityManager.getInstance();
    }

    private void initView() {

        constraintLayout = findViewById(R.id.loginScreenLayout);
        progressBar = findViewById(R.id.progressBarLogin);

        progressBar.setVisibility(View.INVISIBLE);
        constraintLayout.setVisibility(View.VISIBLE);


        final EditText username = findViewById(R.id.editTextUserName);
        final EditText password = findViewById(R.id.editTextPassword);

        Button login = findViewById(R.id.buttonLogin);
        Button cancel = findViewById(R.id.buttonCancel);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean formValid = false;

                if(username.getText().toString().equals("")){
                    alertShow(getString(R.string.username_not_null));
                    Toast.makeText(sContextReference.get(),
                            getString(R.string.username_not_null),Toast.LENGTH_LONG).show();

                }else if( !android.util.Patterns.EMAIL_ADDRESS.matcher(username.getText().toString()).matches() ) {
                    alertShow(getString(R.string.username_must_be_a_valid_email));
                    Toast.makeText(sContextReference.get(),
                            getString(R.string.username_must_be_a_valid_email), Toast.LENGTH_LONG).show();

                }else if(password.getText().toString().equals("")) {
                    alertShow(getString(R.string.password_must_not_be_null_or_void));
                    Toast.makeText(sContextReference.get(),
                            getString(R.string.password_must_not_be_null_or_void), Toast.LENGTH_LONG).show();

                }else{
                    formValid = true;
                }

                if(formValid){
                    Map<String, String> params = new HashMap<>();
                    params.put("op", "login");

                    try {
                        constraintLayout.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);

                        PostLoginTask getData = new PostLoginTask(params);

                        String res = getData.execute(Constants.URL_LOGIN,
                                username.getText().toString(),
                                password.getText().toString()).get();
                        Log.i(TAG, "res : " + res);

                        JSONObject jsonResponse = new JSONObject(res);
                        int responseCode = jsonResponse.getInt("responseCode");

                        if(responseCode == 200){
                            Log.i(TAG, String.format("OK : Username %s logged correctly!.", username.getText().toString()));
                            infoDialog(
                                    "User correctly logged in system!",
                                    String.format("OK : Username %s logged correctly!.", username.getText().toString()),
                                    "OK");

                            // Create KeyPair
                            // Ask for Certificate through CSR
                            // Save Received Certificate in KeyStore
                            // Save PrivateKey in KeyStore


                            tfmSecurityManager.setCertificatePrivateKeyAndSymmetricKeysForUserLogged(
                                    username.getText().toString(),
                                    password.getText().toString(),
                                    username.getText().toString()
                            );

                        }else{
                            errorDialog(
                                    "User NOT logged in system!",
                                    "ERROR : Username " + username.getText().toString()+ " NOT logged correctly!.",
                                    "OK");
                        }

                        progressBar.setVisibility(View.GONE);
                        constraintLayout.setVisibility(View.VISIBLE);

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

    private void infoDialog(
            String title,
            String message,
            final String okMethod){
        final android.support.v7.app.AlertDialog.Builder builderSingle = new android.support.v7.app.AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.ic_perm_device_info);
        builderSingle.setTitle(title);
        builderSingle.setMessage(message);

        builderSingle.setPositiveButton(
                okMethod,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Log.i(TAG, "onClick: OK Called.");
                        Intent intent = new Intent(sContextReference.get(), MainActivity.class);
                        startActivity(intent);
                    }
                });


        builderSingle.show();
    }

    private void errorDialog(
            String title,
            String message,
            final String okMethod){
        final android.support.v7.app.AlertDialog.Builder builderSingle = new android.support.v7.app.AlertDialog.Builder(this);
        builderSingle.setIcon(R.mipmap.ic_launcher);
        builderSingle.setTitle(title);
        builderSingle.setMessage(message);

        builderSingle.setPositiveButton(
                okMethod,
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
                .setIcon(R.drawable.ic_error);

        AlertDialog alert = builder.create();
        alert.show();
    }

}
