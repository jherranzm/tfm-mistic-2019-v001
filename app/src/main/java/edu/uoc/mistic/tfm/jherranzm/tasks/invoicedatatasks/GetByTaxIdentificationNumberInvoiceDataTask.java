package edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.InvoiceApp;
import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;

public class GetByTaxIdentificationNumberInvoiceDataTask extends AsyncTask<String, Void, List<InvoiceData>> {

    private static final String TAG = GetByTaxIdentificationNumberInvoiceDataTask.class.getSimpleName();

    //private final WeakReference<ReceivedInvoicesRecyclerViewActivity> mActivityRef;

    private InvoiceData invoiceData;

    public GetByTaxIdentificationNumberInvoiceDataTask(){
    }

    @Override
    protected List<InvoiceData> doInBackground(String... params) {

        List<InvoiceData> taskList = DatabaseClient
                .getInstance(InvoiceApp.getContext())
                .getAppDatabase()
                .invoiceDataDao()
                .findAllInvoiceDataTaxIdentificationNumber(params[0]);

        Log.i(TAG, "InvoiceData.length : " + taskList.size());

        return taskList;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }

}