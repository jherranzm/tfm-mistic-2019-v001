package edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.InvoiceApp;
import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;

public class GetAllInvoiceDataTask  extends AsyncTask<Void, Void, List<InvoiceData>> {

    private static final String TAG = "GetAllInvoiceDataTask";

    public GetAllInvoiceDataTask() {

    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected List<InvoiceData> doInBackground(Void... voids) {

        List<InvoiceData> taskList = DatabaseClient
                .getInstance(InvoiceApp.getContext())
                .getAppDatabase()
                .invoiceDataDao()
                .getAll();
        Log.i(TAG, "InvoiceData.length : " + taskList.size());

        return taskList;
    }
}