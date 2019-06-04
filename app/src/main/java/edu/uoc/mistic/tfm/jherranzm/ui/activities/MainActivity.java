package edu.uoc.mistic.tfm.jherranzm.ui.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import org.spongycastle.asn1.x500.RDN;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.asn1.x500.style.IETFUtils;
import org.spongycastle.cert.jcajce.JcaX509CertificateHolder;

import java.lang.ref.WeakReference;
import java.security.Security;
import java.security.cert.CertificateEncodingException;

import edu.uoc.mistic.tfm.jherranzm.R;
import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.services.FileDataObjectService;
import edu.uoc.mistic.tfm.jherranzm.services.InvoiceDataService;
import edu.uoc.mistic.tfm.jherranzm.services.ServerInfoService;
import edu.uoc.mistic.tfm.jherranzm.util.TFMSecurityManager;


public class MainActivity
        extends AppCompatActivity{

    // Constants
    private static final String TAG = MainActivity.class.getSimpleName();

    // Security
    private TFMSecurityManager tfmSecurityManager;

    // Context
    private static WeakReference<Context> sContextReference;

    private Handler mHandler;

    private CheckedTextView isServerOnLine;

    // Widgets
    Button goToSignUp;
    Button goToLogIn;
    Button goToShowUploadedInvoices;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        tfmSecurityManager = TFMSecurityManager.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sContextReference = new WeakReference<Context>(this);

        mHandler = new Handler();
        startRepeatingTask();

        // 2019-03-30
        // Check whether this app has write external storage permission or not.
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // If do not grant write external storage permission.
        if(writeExternalStoragePermission!= PackageManager.PERMISSION_GRANTED)
        {
            // Request user to grant write external storage permission.
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        initView();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    private void initView() {

        // Button invoices backed up in server
        goToShowUploadedInvoices = findViewById(R.id.buttonGoToShowUploadedInvoice);
        Drawable iconUploadCloud = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_upload_cloud );
        if(iconUploadCloud != null){
            iconUploadCloud.setBounds(0, 0, iconUploadCloud.getMinimumWidth(),
                    iconUploadCloud.getMinimumHeight());
            goToShowUploadedInvoices.setCompoundDrawables(iconUploadCloud, null, null, null);
        }
        goToShowUploadedInvoices.setEnabled(tfmSecurityManager.isUserLogged() && tfmSecurityManager.isServerOnLine());

        // Button invoice files downloaded in device (in th SDCard)
        Button goToShowLocalInvoices = findViewById(R.id.buttonShowLocalInvoices);
        Drawable iconFileDownloaded = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_file_download);
        if (iconFileDownloaded != null) {
            iconFileDownloaded.setBounds(0, 0, iconFileDownloaded.getMinimumWidth(),
                    iconFileDownloaded.getMinimumHeight());
            goToShowLocalInvoices.setCompoundDrawables(iconFileDownloaded, null, null, null);
        }

        // Button invoices already processed and in local database
        Button goToLocalInvoices = findViewById(R.id.buttonShowInvoices);
        Drawable iconLocalInvoice = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_local_invoice);
        if (iconLocalInvoice != null) {
            iconLocalInvoice.setBounds(0, 0, iconLocalInvoice.getMinimumWidth(),
                    iconLocalInvoice.getMinimumHeight());
            goToLocalInvoices.setCompoundDrawables(iconLocalInvoice, null, null, null);
        }
        goToLocalInvoices.setEnabled(tfmSecurityManager.isUserLogged());

        // Button sign up button
        goToSignUp = findViewById(R.id.buttonGoToSignUp);
        Drawable iconSignUp = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_sign_up);
        if (iconSignUp != null) {
            iconSignUp.setBounds(0, 0, iconSignUp.getMinimumWidth(),
                    iconSignUp.getMinimumHeight());
            goToSignUp.setCompoundDrawables(iconSignUp, null, null, null);
        }
        goToSignUp.setEnabled(tfmSecurityManager.isServerOnLine());
        goToSignUp.setVisibility(View.VISIBLE);
        if(tfmSecurityManager.isUserLogged()){
            goToSignUp.setVisibility(View.GONE);
        }

        goToLogIn = findViewById(R.id.buttonGoToLogin);
        Drawable iconLogIn = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_log_in);
        if (iconLogIn != null) {
            iconLogIn.setBounds(0, 0, iconLogIn.getMinimumWidth(),
                    iconLogIn.getMinimumHeight());
            goToLogIn.setCompoundDrawables(iconLogIn, null, null, null);
        }
        goToLogIn.setEnabled(tfmSecurityManager.isServerOnLine());
        goToLogIn.setVisibility(View.VISIBLE);
        if(tfmSecurityManager.isUserLogged()){
            goToLogIn.setVisibility(View.GONE);
        }

        // Is serverOnline
        isServerOnLine = findViewById(R.id.checkedServerOnline);
        isServerOnLine.setChecked(tfmSecurityManager.isServerOnLine());
        isServerOnLine.setText((tfmSecurityManager.isServerOnLine() ? "Server Online" : "Server offline"));
        isServerOnLine.setBackgroundColor((tfmSecurityManager.isServerOnLine() ? Color.GREEN : Color.RED));

        // Show who is logged
        TextView textViewUserLogged = findViewById(R.id.textViewUserLogged);

        String commonName = "No user logged";

        if (tfmSecurityManager.isUserLogged() && tfmSecurityManager.getCertificate() != null) {
            try {
                X500Name x500name = new JcaX509CertificateHolder(tfmSecurityManager.getCertificate()).getSubject();
                RDN cn = x500name.getRDNs(BCStyle.CN)[0];
                commonName = IETFUtils.valueToString(cn.getFirst().getValue());
            } catch (CertificateEncodingException e) {
                e.printStackTrace();
            }
        }
        textViewUserLogged.setText(commonName);

        goToShowUploadedInvoices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sContextReference.get(), UploadedInvoicesRecyclerViewActivity.class);
                startActivity(intent);
            }
        });

        goToShowLocalInvoices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sContextReference.get(), ReceivedInvoicesRecyclerViewActivity.class);
                startActivity(intent);
            }
        });

        goToLocalInvoices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sContextReference.get(), InvoiceDataRecyclerViewActivity.class);
                startActivity(intent);
            }
        });

        goToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sContextReference.get(), SignUpActivity.class);
                startActivity(intent);
            }
        });

        goToLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sContextReference.get(), LogInActivity.class);
                startActivity(intent);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        Intent intent;
        switch (id){
            case R.id.action_uploaded_invoices:
                Toast.makeText(sContextReference.get(), item.getTitle(), Toast.LENGTH_SHORT).show();
                intent = new Intent(sContextReference.get(), UploadedInvoicesRecyclerViewActivity.class);
                startActivity(intent);
                break;
            case R.id.action_received_invoices:
                Toast.makeText(sContextReference.get(), item.getTitle(), Toast.LENGTH_SHORT).show();
                intent = new Intent(sContextReference.get(), ReceivedInvoicesRecyclerViewActivity.class);
                startActivity(intent);
                break;
            case R.id.action_local_invoices:
                Toast.makeText(sContextReference.get(), item.getTitle(), Toast.LENGTH_SHORT).show();
                intent = new Intent(sContextReference.get(), InvoiceDataRecyclerViewActivity.class);
                startActivity(intent);
                break;
            case R.id.action_totals_by_provider:
                Toast.makeText(sContextReference.get(), item.getTitle(), Toast.LENGTH_SHORT).show();
                intent = new Intent(sContextReference.get(), TotalsByProviderRecyclerViewActivity.class);
                startActivity(intent);
                break;
            case R.id.action_delete_all_local_invoices:
                Toast.makeText(sContextReference.get(), item.getTitle(), Toast.LENGTH_SHORT).show();
                InvoiceDataService.deleteAllInvoiceData(MainActivity.this);
                FileDataObjectService.deleteAllFileDataObject(MainActivity.this);
                break;
            case R.id.action_log_out:
                Toast.makeText(sContextReference.get(), item.getTitle(), Toast.LENGTH_SHORT).show();
                tfmSecurityManager.logOut();
                recreate();
                break;
            case R.id.action_log_in:
                Toast.makeText(sContextReference.get(), item.getTitle(), Toast.LENGTH_SHORT).show();
                intent = new Intent(sContextReference.get(), LogInActivity.class);
                startActivity(intent);
            case R.id.action_sign_up:
                Toast.makeText(sContextReference.get(), item.getTitle(), Toast.LENGTH_SHORT).show();
                intent = new Intent(sContextReference.get(), SignUpActivity.class);
                startActivity(intent);
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                //updateStatus(); //this function can change value of mInterval.
                String status = ServerInfoService.getStatusFromServer();
                if ("ACTIVE".equals(status)) {
                    tfmSecurityManager.setServerStatus(Constants.SERVER_ACTIVE);
                }else{
                    tfmSecurityManager.setServerStatus(Constants.SERVER_INACTIVE);
                }
                isServerOnLine.setChecked(tfmSecurityManager.isServerOnLine());
                isServerOnLine.setText((tfmSecurityManager.isServerOnLine() ? "Server Online" : "Server offline"));
                isServerOnLine.setBackgroundColor((tfmSecurityManager.isServerOnLine() ? Color.GREEN : Color.RED));

                goToSignUp.setEnabled(tfmSecurityManager.isServerOnLine());
                goToLogIn.setEnabled(tfmSecurityManager.isServerOnLine());
                goToShowUploadedInvoices.setEnabled(tfmSecurityManager.isUserLogged() && tfmSecurityManager.isServerOnLine());
                recreate();

            }catch (Exception e){
                Log.e(TAG, "Error trying to locate server");
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                // 5 seconds by default, can be changed later
                int mInterval = 10000;
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        // Works correctly with apache.santuario v1.5.8
        org.apache.xml.security.Init.init();
    }
}
