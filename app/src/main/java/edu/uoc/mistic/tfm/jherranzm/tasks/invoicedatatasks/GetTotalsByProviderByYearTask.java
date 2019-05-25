package edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.TotalByProviderByYearVO;

public class GetTotalsByProviderByYearTask extends AsyncTask<String, Void, List<TotalByProviderByYearVO>> {

    private static final String TAG = GetTotalsByProviderByYearTask.class.getSimpleName();

    private final WeakReference<Activity> mActivityRef;

    public GetTotalsByProviderByYearTask(Activity activity){
        mActivityRef = new WeakReference<>(activity);
    }

    @Override
    protected List<TotalByProviderByYearVO> doInBackground(String... params) {

        List<TotalByProviderByYearVO> totalsByProvider = DatabaseClient
                .getInstance(mActivityRef.get())
                .getAppDatabase()
                .invoiceDataDao()
                .findTotalsByProviderAndYear();

        Log.i(TAG, String.format("TotalByProviderByYearVO.length : %d", totalsByProvider.size()));

        return totalsByProvider;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }

}