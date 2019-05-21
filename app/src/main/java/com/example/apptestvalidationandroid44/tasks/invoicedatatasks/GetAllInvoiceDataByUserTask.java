package com.example.apptestvalidationandroid44.tasks.invoicedatatasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.InvoiceApp;
import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.InvoiceData;

import java.util.List;

public class GetAllInvoiceDataByUserTask extends AsyncTask<String, Void, List<InvoiceData>> {

    private static final String TAG = "GetAllInvoiceDataByUserTask";

    public GetAllInvoiceDataByUserTask() {

    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected List<InvoiceData> doInBackground(String... params) {

        List<InvoiceData> taskList = DatabaseClient
                .getInstance(InvoiceApp.getContext())
                .getAppDatabase()
                .invoiceDataDao()
                .getAllByUser(params[0]);
        Log.i(TAG, "InvoiceData.length : " + taskList.size());

        return taskList;
    }
}