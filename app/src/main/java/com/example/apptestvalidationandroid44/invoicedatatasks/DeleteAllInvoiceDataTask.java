package com.example.apptestvalidationandroid44.invoicedatatasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.InvoiceApp;
import com.example.apptestvalidationandroid44.model.DatabaseClient;

public class DeleteAllInvoiceDataTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "DeleteAllInvoiceDataTask";

    public DeleteAllInvoiceDataTask(){

    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected Boolean doInBackground(Void... voids) {

        boolean ret = false;

        DatabaseClient
                    .getInstance(InvoiceApp.getContext())
                    .getAppDatabase()
                    .invoiceDataDao()
                    .deleteAll();
            Log.i(TAG, "Deleted all!" );
        ret = true;
        return ret;
    }

}
