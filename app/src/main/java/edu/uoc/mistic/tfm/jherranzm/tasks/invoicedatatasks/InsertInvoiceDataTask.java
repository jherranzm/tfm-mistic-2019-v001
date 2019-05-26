package edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;

public class InsertInvoiceDataTask extends AsyncTask<Void, Void, InvoiceData> {

    private static final String TAG = InsertInvoiceDataTask.class.getSimpleName();

    private final InvoiceData invoiceData;

    private final WeakReference<Activity> mActivityRef;

    public InsertInvoiceDataTask(Activity activity, InvoiceData data){
        mActivityRef = new WeakReference<>(activity);
        this.invoiceData = data;
    }


    @Override
    protected InvoiceData doInBackground(Void... voids) {

        //LocalSymKey invoiceData = new LocalSymKey();
        long idInserted= DatabaseClient
                .getInstance(mActivityRef.get())
                .getAppDatabase()
                .invoiceDataDao()
                .insert(this.invoiceData);
        Log.i(TAG, String.format("Inserted! [%d]", idInserted));

        return invoiceData;
    }

}