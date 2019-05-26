package edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;

public class GetAllInvoiceDataByUserTask extends AsyncTask<String, Void, List<InvoiceData>> {

    private static final String TAG = GetAllInvoiceDataByUserTask.class.getSimpleName();

    private final WeakReference<Activity> mActivityRef;

    private final String user;

    public GetAllInvoiceDataByUserTask(Activity activity, String user) {
        this.user = user;
        mActivityRef = new WeakReference<>(activity);
    }


    @Override
    protected List<InvoiceData> doInBackground(String... params) {

        List<InvoiceData> taskList = DatabaseClient
                .getInstance(mActivityRef.get())
                .getAppDatabase()
                .invoiceDataDao()
                .getAllByUser(user);
        Log.i(TAG, String.format("InvoiceData.length : %d", taskList.size()));

        return taskList;
    }
}