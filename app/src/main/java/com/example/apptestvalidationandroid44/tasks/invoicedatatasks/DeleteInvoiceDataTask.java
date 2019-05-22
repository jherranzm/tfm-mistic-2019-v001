package com.example.apptestvalidationandroid44.tasks.invoicedatatasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.InvoiceApp;
import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.InvoiceData;

public class DeleteInvoiceDataTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "DeleteInvoiceDataTask";

    private InvoiceData invoiceData;

    public DeleteInvoiceDataTask(InvoiceData data){
        this.invoiceData = data;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected Void doInBackground(Void... voids) {

        DatabaseClient
                .getInstance(InvoiceApp.getContext())
                .getAppDatabase()
                .invoiceDataDao()
                .delete(this.invoiceData);
        Log.i(TAG, "Deleted!" );

        return null;

    }

}