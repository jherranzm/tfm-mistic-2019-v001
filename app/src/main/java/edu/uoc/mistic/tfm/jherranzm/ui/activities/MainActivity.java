package edu.uoc.mistic.tfm.jherranzm.ui.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.R;
import edu.uoc.mistic.tfm.jherranzm.model.TotalByProviderByYearVO;
import edu.uoc.mistic.tfm.jherranzm.services.InvoiceDataService;
import edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks.GetTotalsByProviderByYearTask;
import edu.uoc.mistic.tfm.jherranzm.util.TFMSecurityManager;


public class MainActivity
        extends AppCompatActivity{

    // Constants
    private static final String TAG = "MAIN_ACTIVITY";

    // Security
    private TFMSecurityManager tfmSecurityManager;

    // Context
    private static WeakReference<Context> sContextReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        tfmSecurityManager = TFMSecurityManager.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sContextReference = new WeakReference<Context>(this);

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

    private void initView() {

        // Button invoices backed up in server
        Button goToShowUploadedInvoices = findViewById(R.id.buttonGoToShowUploadedInvoice);
        Drawable iconUploadCloud = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_upload_cloud );
        if(iconUploadCloud != null){
            iconUploadCloud.setBounds(0, 0, iconUploadCloud.getMinimumWidth(),
                    iconUploadCloud.getMinimumHeight());
            goToShowUploadedInvoices.setCompoundDrawables(iconUploadCloud, null, null, null);
        }

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

        // Button sign up button
        Button goToSignUp = findViewById(R.id.buttonGoToSignUp);
        Drawable iconSignUp = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_sign_up);
        if (iconSignUp != null) {
            iconSignUp.setBounds(0, 0, iconSignUp.getMinimumWidth(),
                    iconSignUp.getMinimumHeight());
            goToSignUp.setCompoundDrawables(iconSignUp, null, null, null);
        }

        Button goToLogIn = findViewById(R.id.buttonGoToLogin);
        Drawable iconLogIn = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_log_in);
        if (iconLogIn != null) {
            iconLogIn.setBounds(0, 0, iconLogIn.getMinimumWidth(),
                    iconLogIn.getMinimumHeight());
            goToLogIn.setCompoundDrawables(iconLogIn, null, null, null);
        }


        Button goToShowInfoByProviderAndYear = findViewById(R.id.buttonShowInfoByProviderByYear);

        Button goToDeleteAllInvoices = findViewById(R.id.buttonDeleteAllInvoices);

        // Is serverOnline
        CheckedTextView isServerOnLine = findViewById(R.id.checkedServerOnline);
        isServerOnLine.setChecked(tfmSecurityManager.isServerOnLine());
        isServerOnLine.setText((tfmSecurityManager.isServerOnLine() ? "Server Online" : "Server offline"));
        isServerOnLine.setBackgroundColor((tfmSecurityManager.isServerOnLine() ? Color.GREEN : Color.RED));

        // Show who is logged
        TextView textViewUserLogged = findViewById(R.id.textViewUserLogged);

        String commonName = "Default User";

        if (tfmSecurityManager.getCertificate() != null) {
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

        goToDeleteAllInvoices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InvoiceDataService.deleteAllInvoiceData(MainActivity.this);
            }
        });

        goToShowInfoByProviderAndYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    GetTotalsByProviderByYearTask getTotalsByProviderTask = new GetTotalsByProviderByYearTask(MainActivity.this);
                    List<TotalByProviderByYearVO> totals = getTotalsByProviderTask.execute().get();

                    for(TotalByProviderByYearVO totalByProviderByYearVO : totals){
                        Log.i(TAG, totalByProviderByYearVO.toString());
                    }
                } catch (Exception e) {
                    Log.i(TAG, e.getClass().getCanonicalName() + " : " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
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
        }


        return super.onOptionsItemSelected(item);
    }

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        // Works correctly with apache.santuario v1.5.8
        org.apache.xml.security.Init.init();
    }
}
