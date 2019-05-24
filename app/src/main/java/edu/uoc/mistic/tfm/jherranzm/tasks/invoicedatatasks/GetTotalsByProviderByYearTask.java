package edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.InvoiceApp;
import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.TotalByProviderByYearVO;

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