package edu.uoc.mistic.tfm.jherranzm.tasks.localsymkeytasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.LocalSymKey;

public class DeleteLocalSymKeyTask extends AsyncTask<Void, Void, LocalSymKey> {

    private static final String TAG = DeleteLocalSymKeyTask.class.getSimpleName();

    private final WeakReference<Activity> mActivityRef;

    private final LocalSymKey lsk;

    public DeleteLocalSymKeyTask(Activity activity, LocalSymKey theLsk){
        mActivityRef = new WeakReference<>(activity);
        this.lsk = theLsk;
    }


    @Override
    protected LocalSymKey doInBackground(Void... voids) {
        Log.i(TAG, "Implemented with WeakReference..." );
        DatabaseClient
                    .getInstance(mActivityRef.get())
                    .getAppDatabase()
                    .localSymKeyDao()
                    .delete(this.lsk);
            Log.i(TAG, String.format("Deleted : [%s]", this.lsk));

        return lsk;
    }

}
