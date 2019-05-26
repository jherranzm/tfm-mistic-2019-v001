package edu.uoc.mistic.tfm.jherranzm.tasks.localsymkeytasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.LocalSymKey;

public class GetByFLocalSymKeyTask extends AsyncTask<String, Void, LocalSymKey> {

    private static final String TAG = GetByFLocalSymKeyTask.class.getSimpleName();

    private final WeakReference<Activity> mActivityRef;

    public GetByFLocalSymKeyTask(Activity activity){
        mActivityRef = new WeakReference<>(activity);
    }


    @Override
    protected LocalSymKey doInBackground(String... params) {
        Log.i(TAG, "Implemented with WeakReference..." );

        return DatabaseClient
                .getInstance(mActivityRef.get())
                .getAppDatabase()
                .localSymKeyDao()
                .findLocalSimKeyByF(params[0]);
    }

}
