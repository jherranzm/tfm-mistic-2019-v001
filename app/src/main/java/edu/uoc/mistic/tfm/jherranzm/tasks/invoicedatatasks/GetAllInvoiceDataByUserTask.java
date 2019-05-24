package edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.InvoiceApp;
import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;

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