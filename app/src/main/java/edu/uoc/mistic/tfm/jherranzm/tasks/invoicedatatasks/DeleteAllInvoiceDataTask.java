package edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;

public class DeleteAllInvoiceDataTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = DeleteAllInvoiceDataTask.class.getSimpleName();

    private final WeakReference<Activity> mActivityRef;

    public DeleteAllInvoiceDataTask(Activity activity){
        mActivityRef = new WeakReference<>(activity);
    }


    @Override
    protected Boolean doInBackground(Void... voids) {
        Log.i(TAG, "Implemented with WeakReference..." );

        DatabaseClient
                    .getInstance(mActivityRef.get())
                    .getAppDatabase()
                    .invoiceDataDao()
                    .deleteAll();
            Log.i(TAG, "Deleted all!" );

        return true;
    }

}
