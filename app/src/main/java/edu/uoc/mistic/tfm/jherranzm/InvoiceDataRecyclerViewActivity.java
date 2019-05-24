package edu.uoc.mistic.tfm.jherranzm;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.example.apptestvalidationandroid44.R;

import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;
import edu.uoc.mistic.tfm.jherranzm.services.InvoiceDataDataManagerService;

public class InvoiceDataRecyclerViewActivity extends AppCompatActivity {

    private static final String TAG = "InvoiceDataRecyclerViewActivity";

    private RecyclerView.Adapter mAdapter;

    private List<InvoiceData> invoices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invoice_data_recycler_view);

        RecyclerView mRecyclerView;
        RecyclerView.LayoutManager mLayoutManager;

        mRecyclerView = findViewById(R.id.invoice_data_rv);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        invoices = InvoiceDataDataManagerService.getInvoiceDataFromDatabase();
        mAdapter = new InvoiceDataRecyclerViewAdapter(invoices);
        TextView textViewNumberItems = findViewById(R.id.textViewNumInvoiceFilesInSystem);
        textViewNumberItems.setText("Number of Invoices Processed in System " + invoices.size());

        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

     }

    @Override
    protected void onResume() {
        super.onResume();
        //setContentView(R.layout.invoice_data_recycler_view);
        invoices = InvoiceDataDataManagerService.getInvoiceDataFromDatabase();
        mAdapter = new InvoiceDataRecyclerViewAdapter(invoices);
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
                        if(okMethod.equals("ok")){
                            //downloadInvoice(position);
                        }
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
