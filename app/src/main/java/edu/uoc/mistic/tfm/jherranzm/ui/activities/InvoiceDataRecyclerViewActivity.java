package edu.uoc.mistic.tfm.jherranzm.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.R;
import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;
import edu.uoc.mistic.tfm.jherranzm.services.InvoiceDataService;
import edu.uoc.mistic.tfm.jherranzm.ui.adapters.InvoiceDataRecyclerViewAdapter;
import edu.uoc.mistic.tfm.jherranzm.util.TFMSecurityManager;

public class InvoiceDataRecyclerViewActivity extends AppCompatActivity {

    private static final String TAG = InvoiceDataRecyclerViewActivity.class.getSimpleName();

    private RecyclerView.Adapter mAdapter;

    private List<InvoiceData> invoices;

    // Security
    private TFMSecurityManager tfmSecurityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invoice_data_recycler_view);

        tfmSecurityManager = TFMSecurityManager.getInstance();

        initView();

    }

    private void initView() {
        RecyclerView mRecyclerView;
        RecyclerView.LayoutManager mLayoutManager;

        mRecyclerView = findViewById(R.id.invoice_data_rv);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Button invoices backed up in server
        Button goToShowTotalsByProvider = findViewById(R.id.buttonShowInfoByProvider);
        Drawable iconShowTotalsByProvider = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_totals_by_provider );
        if (iconShowTotalsByProvider != null) {
            iconShowTotalsByProvider.setBounds(0, 0, iconShowTotalsByProvider.getMinimumWidth(),
                    iconShowTotalsByProvider.getMinimumHeight());
            goToShowTotalsByProvider.setCompoundDrawables(iconShowTotalsByProvider, null, null, null);
        }

        goToShowTotalsByProvider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TotalsByProviderRecyclerViewActivity.class);
                startActivity(intent);
            }
        });
        invoices = InvoiceDataService.getInvoiceDataFromDatabase(this, tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.USER_LOGGED));
        mAdapter = new InvoiceDataRecyclerViewAdapter(this, invoices);
        TextView textViewNumberItems = findViewById(R.id.textViewNumInvoiceFilesInSystem);
        textViewNumberItems.setText(String.format("Number of Invoices Processed in System %d", invoices.size()));

        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setContentView(R.layout.invoice_data_recycler_view);
        invoices = InvoiceDataService.getInvoiceDataFromDatabase(this, tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.USER_LOGGED));
        mAdapter = new InvoiceDataRecyclerViewAdapter(this, invoices);
        TextView textViewNumberItems = findViewById(R.id.textViewNumInvoiceFilesInSystem);
        textViewNumberItems.setText("Number of Invoices Processed in System " + invoices.size());

    }

    private void customDialog(
            String title,
            String message,
            final String cancelMethod,
            final String okMethod,
            final int position){
        final android.support.v7.app.AlertDialog.Builder builderSingle = new android.support.v7.app.AlertDialog.Builder(this);
        builderSingle.setIcon(R.mipmap.ic_launcher_round);
        builderSingle.setTitle(title);
        builderSingle.setMessage(message);

        builderSingle.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Cancel Called.");
                        if(cancelMethod.equals("cancel")){
                            //cancelMethod1();
                            Log.i(TAG, "Operation cancelled!");
                        }

                    }
                });

        builderSingle.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "onClick: OK Called.");
                    }
                });


        builderSingle.show();
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
