package edu.uoc.mistic.tfm.jherranzm.tasks.localsymkeytasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;

public class DeleteAllByUserLocalSymKeyTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = DeleteAllByUserLocalSymKeyTask.class.getSimpleName();

    private final WeakReference<Activity> mActivityRef;

    private final String user;

    public DeleteAllByUserLocalSymKeyTask(Activity activity, String theUser){
        mActivityRef = new WeakReference<>(activity);
        this.user = theUser;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        Log.i(TAG, "Implemented with WeakReference..." );
        DatabaseClient
                    .getInstance(mActivityRef.get())
                    .getAppDatabase()
                    .localSymKeyDao()
                    .deleteAllByUser(this.user);
        Log.i(TAG, String.format("Deleted all from user : [%s]", this.user));
        return null;
    }

}
