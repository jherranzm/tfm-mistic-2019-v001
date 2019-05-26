package edu.uoc.mistic.tfm.jherranzm.tasks.localsymkeytasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.LocalSymKey;
import edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks.DeleteAllInvoiceDataTask;

public class InsertLocalSymKeyTask extends AsyncTask<Void, Void, LocalSymKey> {

    private static final String TAG = DeleteAllInvoiceDataTask.class.getSimpleName();

    private final WeakReference<Activity> mActivityRef;

    private final LocalSymKey lsk;

    public InsertLocalSymKeyTask(Activity activity, LocalSymKey theLsk){
        mActivityRef = new WeakReference<>(activity);
        this.lsk = theLsk;
    }


    @Override
    protected LocalSymKey doInBackground(Void... voids) {
        Log.i(TAG, "Implemented with WeakReference..." );

        long idInserted= DatabaseClient
                    .getInstance(mActivityRef.get())
                    .getAppDatabase()
                    .localSymKeyDao()
                    .insert(this.lsk);
            Log.i(TAG, String.format("Inserted! [%d]", idInserted));

        return lsk;
    }

}
