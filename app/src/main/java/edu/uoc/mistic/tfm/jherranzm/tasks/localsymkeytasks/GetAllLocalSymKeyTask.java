package edu.uoc.mistic.tfm.jherranzm.tasks.localsymkeytasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.LocalSymKey;

public class GetAllLocalSymKeyTask extends AsyncTask<Void, Void, List<LocalSymKey>> {

    private static final String TAG = GetAllLocalSymKeyTask.class.getSimpleName();

    private final WeakReference<Activity> mActivityRef;

    public GetAllLocalSymKeyTask(Activity activity){
        mActivityRef = new WeakReference<>(activity);
    }


    @Override
    protected List<LocalSymKey> doInBackground(Void... voids) {
        Log.i(TAG, "Implemented with WeakReference..." );

        List<LocalSymKey> taskList = DatabaseClient
            .getInstance(mActivityRef.get())
            .getAppDatabase()
            .localSymKeyDao()
            .getAll();
        Log.i(TAG, String.format("LocalSymKey.length : %d", taskList.size()));

        return taskList;
    }

}
