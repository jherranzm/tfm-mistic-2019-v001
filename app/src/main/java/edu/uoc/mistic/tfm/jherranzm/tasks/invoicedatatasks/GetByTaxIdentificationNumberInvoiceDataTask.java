package edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;

public class GetByTaxIdentificationNumberInvoiceDataTask extends AsyncTask<String, Void, List<InvoiceData>> {

    private static final String TAG = GetByTaxIdentificationNumberInvoiceDataTask.class.getSimpleName();

    private final WeakReference<Activity> mActivityRef;

    private InvoiceData invoiceData;

    public GetByTaxIdentificationNumberInvoiceDataTask(Activity activity){
        mActivityRef = new WeakReference<>(activity);
    }

    @Override
    protected List<InvoiceData> doInBackground(String... params) {

        List<InvoiceData> taskList = DatabaseClient
                .getInstance(mActivityRef.get())
                .getAppDatabase()
                .invoiceDataDao()
                .findAllInvoiceDataTaxIdentificationNumber(params[0]);

        Log.i(TAG, String.format("InvoiceData.length : %d", taskList.size()));

        return taskList;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }

}