package edu.uoc.mistic.tfm.jherranzm.tasks.filedataobjecttasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;

public class DeleteAllFileDataObjectTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = DeleteAllFileDataObjectTask.class.getSimpleName();

    private final WeakReference<Activity> mActivityRef;

    public DeleteAllFileDataObjectTask(Activity activity){
        mActivityRef = new WeakReference<>(activity);
    }


    @Override
    protected Boolean doInBackground(Void... voids) {
        Log.i(TAG, "Implemented with WeakReference..." );

        DatabaseClient
                    .getInstance(mActivityRef.get())
                    .getAppDatabase()
                    .fileDataObjectDao()
                    .deleteAll();
        Log.i(TAG, "Deleted all!" );

        return true;
    }

}
