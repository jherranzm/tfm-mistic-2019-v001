package edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.TotalByProviderVO;

public class GetTotalsByProviderTask extends AsyncTask<String, Void, List<TotalByProviderVO>> {

    private static final String TAG = GetTotalsByProviderTask.class.getSimpleName();

    private final WeakReference<Activity> mActivityRef;

    public GetTotalsByProviderTask(Activity activity){
        mActivityRef = new WeakReference<>(activity);
    }

    @Override
    protected List<TotalByProviderVO> doInBackground(String... params) {

        List<TotalByProviderVO> totalsByProvider = DatabaseClient
                .getInstance(mActivityRef.get())
                .getAppDatabase()
                .invoiceDataDao()
                .findTotalsByProvider();

        Log.i(TAG, String.format("TotalByProviderVO.length : %d", totalsByProvider.size()));

        return totalsByProvider;
    }

}