package edu.uoc.mistic.tfm.jherranzm.tasks.filedataobjecttasks;

import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.FileDataObject;
import edu.uoc.mistic.tfm.jherranzm.ui.activities.ReceivedInvoicesRecyclerViewActivity;

public class UpdateFileDataObjectTask extends AsyncTask<Void, Void, FileDataObject> {

    private static final String TAG = UpdateFileDataObjectTask.class.getSimpleName();

    private final FileDataObject object;

    private final WeakReference<ReceivedInvoicesRecyclerViewActivity> mActivityRef;

    public UpdateFileDataObjectTask(ReceivedInvoicesRecyclerViewActivity activity, FileDataObject object){
        mActivityRef = new WeakReference<>(activity);
        this.object = object;
    }


    @Override
    protected FileDataObject doInBackground(Void... voids) {

        Log.i(TAG, "Implemented with WeakReference..." );
        DatabaseClient
                    .getInstance(mActivityRef.get())
                    .getAppDatabase()
                    .fileDataObjectDao()
                    .update(this.object);
        Log.i(TAG, String.format("Updated! [%s]", object.getFileName()));

        return object;
    }

}
