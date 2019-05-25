package edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;

public class GetAllInvoiceDataTask  extends AsyncTask<Void, Void, List<InvoiceData>> {

    private static final String TAG = GetAllInvoiceDataTask.class.getSimpleName();

    private final WeakReference<Activity> mActivityRef;

    public GetAllInvoiceDataTask(Activity activity) {
        mActivityRef = new WeakReference<>(activity);
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected List<InvoiceData> doInBackground(Void... voids) {
        Log.i(TAG, "Implemented with WeakReference..." );

        List<InvoiceData> taskList = DatabaseClient
                .getInstance(mActivityRef.get())
                .getAppDatabase()
                .invoiceDataDao()
                .getAll();
        Log.i(TAG, String.format("InvoiceData.length : %d", taskList.size()));

        return taskList;
    }
}