package edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks;

import android.os.AsyncTask;
import android.util.Log;

import edu.uoc.mistic.tfm.jherranzm.InvoiceApp;
import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;

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