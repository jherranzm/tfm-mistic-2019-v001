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

import com.example.apptestvalidationandroid44.config.Configuration;
import com.example.apptestvalidationandroid44.model.FileDataObject;
import com.example.apptestvalidationandroid44.model.Invoice;
import com.example.apptestvalidationandroid44.remotesymkeytasks.GetAllUploadedInvoicesTask;

import java.io.File;
import java.security.Security;
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

        // Get the application context
        mProgressBar = findViewById(R.id.progressBar1);
        mProgressBar.setVisibility(View.INVISIBLE);

        Button goToShowUploadedInvoices = findViewById(R.id.buttonGoToShowUploadedInvoice);
        Button goToShowLocalInvoices = findViewById(R.id.buttonShowLocalInvoices);

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

                    List<Invoice> invoices = getAllUploadedInvoicesTask.execute(Configuration.URL).get();

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

                Intent intent = new Intent(InvoiceApp.getContext(), LocalInvoicesRecyclerViewActivity.class);
                intent.putExtra(FILE_LIST, signedInvoices);
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
