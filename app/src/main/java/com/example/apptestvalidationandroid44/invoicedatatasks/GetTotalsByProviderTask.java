package com.example.apptestvalidationandroid44.invoicedatatasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.InvoiceApp;
import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.InvoiceData;
import com.example.apptestvalidationandroid44.model.TotalByProviderVO;

import java.util.List;

public class GetTotalsByProviderTask extends AsyncTask<String, Void, List<TotalByProviderVO>> {

    private static final String TAG = "GetTotalsByProviderTask";

    private InvoiceData invoiceData;

    public GetTotalsByProviderTask(){
    }

    @Override
    protected List<TotalByProviderVO> doInBackground(String... params) {

        List<TotalByProviderVO> totalsByProvider = DatabaseClient
                .getInstance(InvoiceApp.getContext())
                .getAppDatabase()
                .invoiceDataDao()
                .findTotalsByProvider();

        Log.i(TAG, "TotalByProviderVO.length : " + totalsByProvider.size());

        return totalsByProvider;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }

}