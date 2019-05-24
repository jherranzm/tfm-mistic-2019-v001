package edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks;

import android.os.AsyncTask;
import android.util.Log;

import edu.uoc.mistic.tfm.jherranzm.InvoiceApp;
import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;

public class InsertInvoiceDataTask extends AsyncTask<Void, Void, InvoiceData> {

    private static final String TAG = "InsertInvoiceDataTask";

    private InvoiceData invoiceData;

    public InsertInvoiceDataTask(InvoiceData data){
        this.invoiceData = data;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected InvoiceData doInBackground(Void... voids) {

        //LocalSymKey invoiceData = new LocalSymKey();
        long idInserted= DatabaseClient
                .getInstance(InvoiceApp.getContext())
                .getAppDatabase()
                .invoiceDataDao()
                .insert(this.invoiceData);
        Log.i(TAG, "Inserted! ["+idInserted+"]" );

        return invoiceData;
    }

}