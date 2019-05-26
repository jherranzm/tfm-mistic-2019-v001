package edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;

public class DeleteInvoiceDataTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = DeleteInvoiceDataTask.class.getSimpleName();

    private final WeakReference<Activity> mActivityRef;

    private final InvoiceData invoiceData;

    public DeleteInvoiceDataTask(Activity activity, InvoiceData data){
        mActivityRef = new WeakReference<>(activity);
        this.invoiceData = data;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        Log.i(TAG, "Implemented with WeakReference..." );
        DatabaseClient
                .getInstance(mActivityRef.get())
                .getAppDatabase()
                .invoiceDataDao()
                .delete(this.invoiceData);
        Log.i(TAG, String.format("Deleted local invoice : %s", this.invoiceData.toString()));

        return null;

    }

}