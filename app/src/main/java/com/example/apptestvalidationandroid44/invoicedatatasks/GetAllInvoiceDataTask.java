package com.example.apptestvalidationandroid44.invoicedatatasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.InvoiceApp;
import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.InvoiceData;

import java.util.List;

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