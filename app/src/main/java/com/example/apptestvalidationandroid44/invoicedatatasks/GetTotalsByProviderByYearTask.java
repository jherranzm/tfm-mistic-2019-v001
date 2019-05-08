package com.example.apptestvalidationandroid44.invoicedatatasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.InvoiceApp;
import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.TotalByProviderByYearVO;

import java.util.List;

public class GetTotalsByProviderByYearTask extends AsyncTask<String, Void, List<TotalByProviderByYearVO>> {

    private static final String TAG = "GetTotalsByProviderByYearTask";

    public GetTotalsByProviderByYearTask(){
    }

    @Override
    protected List<TotalByProviderByYearVO> doInBackground(String... params) {

        List<TotalByProviderByYearVO> totalsByProvider = DatabaseClient
                .getInstance(InvoiceApp.getContext())
                .getAppDatabase()
                .invoiceDataDao()
                .findTotalsByProviderAndYear();

        Log.i(TAG, "TotalByProviderByYearVO.length : " + totalsByProvider.size());

        return totalsByProvider;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }

}