package com.example.apptestvalidationandroid44;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.apptestvalidationandroid44.config.Constants;
import com.example.apptestvalidationandroid44.invoicedatatasks.DeleteAllInvoiceDataTask;
import com.example.apptestvalidationandroid44.invoicedatatasks.GetAllInvoiceDataTask;
import com.example.apptestvalidationandroid44.invoicedatatasks.GetTotalsByProviderByYearTask;
import com.example.apptestvalidationandroid44.invoicedatatasks.GetTotalsByProviderTask;
import com.example.apptestvalidationandroid44.model.FileDataObject;
import com.example.apptestvalidationandroid44.model.Invoice;
import com.example.apptestvalidationandroid44.model.InvoiceData;
import com.example.apptestvalidationandroid44.model.TotalByProviderByYearVO;
import com.example.apptestvalidationandroid44.model.TotalByProviderVO;
import com.example.apptestvalidationandroid44.remotesymkeytasks.GetAllUploadedInvoicesTask;
import com.example.apptestvalidationandroid44.util.TFMSecurityManager;

import org.spongycastle.asn1.x500.RDN;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.asn1.x500.style.IETFUtils;
import org.spongycastle.cert.jcajce.JcaX509CertificateHolder;

import java.io.File;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainActivity
        extends AppCompatActivity{

    // Constants
    private static final String TAG = "MAIN_ACTIVITY";

    public static final String INVOICE_LIST = "INVOICE_LIST";
    public static final String FILE_LIST = "FILE_LIST";

    // Widgets
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        TFMSecurityManager tfmSecurityManager = TFMSecurityManager.getInstance();

        // Get the application context
        mProgressBar = findViewById(R.id.progressBar1);
        mProgressBar.setVisibility(View.INVISIBLE);

        Button goToShowUploadedInvoices = findViewById(R.id.buttonGoToShowUploadedInvoice);
        Button goToShowLocalInvoices = findViewById(R.id.buttonShowLocalInvoices);
        Button goToShowInfoByProviders = findViewById(R.id.buttonShowInfoByProvider);
        Button goToShowInfoByProviderAndYear = findViewById(R.id.buttonShowInfoByProviderByYear);
        Button goToLocalInvoices = findViewById(R.id.buttonShowInvoices);
        Button goToDeleteAllInvoices = findViewById(R.id.buttonDeleteAllInvoices);

        Button goToSignUp = findViewById(R.id.buttonGoToSignUp);
        Button goToLogIn = findViewById(R.id.buttonGoToLogin);

        // Show who is logged
        TextView textViewUserLogged = findViewById(R.id.textViewUserLogged);

        String commonName = "Default User";
        try {
            X500Name x500name = new JcaX509CertificateHolder(tfmSecurityManager.getCertificate()).getSubject();
            RDN cn = x500name.getRDNs(BCStyle.CN)[0];
            commonName = IETFUtils.valueToString(cn.getFirst().getValue());
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
        textViewUserLogged.setText(commonName);

        // 2019-03-30
        // Check whether this app has write external storage permission or not.
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // If do not grant write external storage permission.
        if(writeExternalStoragePermission!= PackageManager.PERMISSION_GRANTED)
        {
            // Request user to grant write external storage permission.
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        goToShowUploadedInvoices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgressBar.setVisibility(View.VISIBLE);

                try {
                    GetAllUploadedInvoicesTask getAllUploadedInvoicesTask = new GetAllUploadedInvoicesTask();

                    List<Invoice> invoices = getAllUploadedInvoicesTask.execute(Constants.URL_FACTURAS).get();

                    Log.i(TAG, "getAllUploadedInvoicesTask : " + invoices.size());

                    mProgressBar.setVisibility(View.INVISIBLE);

                    Intent intent = new Intent(InvoiceApp.getContext(), UploadedInvoicesRecyclerViewActivity.class);
                    intent.putExtra(INVOICE_LIST, new ArrayList<>(invoices));
                    startActivity(intent);

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        goToShowLocalInvoices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);

                String root_sd = Environment.getExternalStorageDirectory().toString();
                File file = new File( root_sd + "/Download" ) ;
                File list[] = file.listFiles();

                ArrayList<FileDataObject> signedInvoices = new ArrayList<>();

                for (File f : list) {
                    Log.i(TAG, f.getName());
                    FileDataObject obj = new FileDataObject(f.getName());
                    signedInvoices.add(obj);
                }

                Intent intent = new Intent(InvoiceApp.getContext(), ReceivedInvoicesRecyclerViewActivity.class);
                intent.putExtra(FILE_LIST, signedInvoices);
                startActivity(intent);

                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        goToLocalInvoices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);

                try {
                    GetAllInvoiceDataTask getAllInvoiceDataTask = new GetAllInvoiceDataTask();

                    List<InvoiceData> invoices = getAllInvoiceDataTask.execute().get();

                    Log.i(TAG, "GetAllInvoiceDataTask : " + invoices.size());

                    mProgressBar.setVisibility(View.INVISIBLE);

                    Intent intent = new Intent(InvoiceApp.getContext(), InvoiceDataRecyclerViewActivity.class);
                    intent.putExtra(INVOICE_LIST, new ArrayList<>(invoices));
                    startActivity(intent);

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        goToDeleteAllInvoices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);

                try {
                    DeleteAllInvoiceDataTask deleteInvoiceDataTask = new DeleteAllInvoiceDataTask();

                    boolean deleted = deleteInvoiceDataTask.execute().get();



                    mProgressBar.setVisibility(View.INVISIBLE);

//                    Intent intent = new Intent(InvoiceApp.getContext(), UploadedInvoicesRecyclerViewActivity.class);
//                    intent.putExtra(INVOICE_LIST, new ArrayList<>(invoices));
//                    startActivity(intent);

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });


        goToShowInfoByProviders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);

                try {

                    GetTotalsByProviderTask getTotalsByProviderTask = new GetTotalsByProviderTask();
                    List<TotalByProviderVO> totals = getTotalsByProviderTask.execute().get();

                    for(TotalByProviderVO totalByProviderVO : totals){
                        Log.i(TAG, totalByProviderVO.toString());
                    }

                    mProgressBar.setVisibility(View.INVISIBLE);


                } catch (Exception e) {
                    Log.i(TAG, e.getClass().getCanonicalName() + " : " + e.getLocalizedMessage());
                    e.printStackTrace();
                }

                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        goToShowInfoByProviderAndYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);

                try {

                    GetTotalsByProviderByYearTask getTotalsByProviderTask = new GetTotalsByProviderByYearTask();
                    List<TotalByProviderByYearVO> totals = getTotalsByProviderTask.execute().get();

                    for(TotalByProviderByYearVO totalByProviderByYearVO : totals){
                        Log.i(TAG, totalByProviderByYearVO.toString());
                    }

                    mProgressBar.setVisibility(View.INVISIBLE);


                } catch (Exception e) {
                    Log.i(TAG, e.getClass().getCanonicalName() + " : " + e.getLocalizedMessage());
                    e.printStackTrace();
                }

                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        goToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);

                    Intent intent = new Intent(InvoiceApp.getContext(), SignUpActivity.class);

                    startActivity(intent);


                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        goToLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);

                    Intent intent = new Intent(InvoiceApp.getContext(), LogInActivity.class);

                    startActivity(intent);


                mProgressBar.setVisibility(View.INVISIBLE);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        // Works correctly with apache.santuario v1.5.8
        org.apache.xml.security.Init.init();
    }
}
