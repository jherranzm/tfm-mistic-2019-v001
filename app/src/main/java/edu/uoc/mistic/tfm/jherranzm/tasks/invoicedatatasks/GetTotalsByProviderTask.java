package edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.InvoiceApp;
import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;
import edu.uoc.mistic.tfm.jherranzm.model.TotalByProviderVO;

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