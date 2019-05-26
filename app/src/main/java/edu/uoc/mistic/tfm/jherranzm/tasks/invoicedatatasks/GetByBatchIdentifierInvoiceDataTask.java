package edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;

public class GetByBatchIdentifierInvoiceDataTask extends AsyncTask<String, Void, List<InvoiceData>> {

    private static final String TAG = GetByBatchIdentifierInvoiceDataTask.class.getSimpleName();

    private final WeakReference<Activity> mActivityRef;

    public GetByBatchIdentifierInvoiceDataTask(Activity activity){
        mActivityRef = new WeakReference<>(activity);
    }

    @Override
    protected List<InvoiceData> doInBackground(String... params) {

        List<InvoiceData> taskList = DatabaseClient
                .getInstance(mActivityRef.get())
                .getAppDatabase()
                .invoiceDataDao()
                .findByBatchIdentifierAndUser(params[0], params[1]);

        Log.i(TAG, String.format("InvoiceData.length : %d", taskList.size()));

        return taskList;
    }

}