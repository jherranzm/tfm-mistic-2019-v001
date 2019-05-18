package com.example.apptestvalidationandroid44.invoicedatatasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.InvoiceApp;
import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.InvoiceData;

import java.util.List;

public class GetByBatchIdentifierInvoiceDataTask extends AsyncTask<String, Void, List<InvoiceData>> {

    private static final String TAG = "GetByBatchIdentifierInvoiceDataTask";

    public GetByBatchIdentifierInvoiceDataTask(){
    }

    @Override
    protected List<InvoiceData> doInBackground(String... params) {

        List<InvoiceData> taskList = DatabaseClient
                .getInstance(InvoiceApp.getContext())
                .getAppDatabase()
                .invoiceDataDao()
                .findByBatchIdentifierAndUser(params[0], params[1]);

        Log.i(TAG, "InvoiceData.length : " + taskList.size());

        return taskList;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }

}