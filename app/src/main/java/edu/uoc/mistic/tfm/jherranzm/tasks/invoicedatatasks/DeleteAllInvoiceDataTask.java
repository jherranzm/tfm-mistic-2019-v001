package edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks;

import android.os.AsyncTask;
import android.util.Log;

import edu.uoc.mistic.tfm.jherranzm.InvoiceApp;
import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;

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
