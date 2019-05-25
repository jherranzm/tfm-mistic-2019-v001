package edu.uoc.mistic.tfm.jherranzm.tasks.filedataobjecttasks;

import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

import edu.uoc.mistic.tfm.jherranzm.ReceivedInvoicesRecyclerViewActivity;
import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.FileDataObject;

public class GetFileDataObjectByFilenameTask extends AsyncTask<String, Void, FileDataObject> {

    private static final String TAG = GetFileDataObjectByFilenameTask.class.getSimpleName();

    private final WeakReference<ReceivedInvoicesRecyclerViewActivity> mActivityRef;

    public GetFileDataObjectByFilenameTask(ReceivedInvoicesRecyclerViewActivity activity){
        mActivityRef = new WeakReference<>(activity);
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected FileDataObject doInBackground(String... params) {
        Log.i(TAG, "Implemented with WeakReference..." );
        return DatabaseClient
                .getInstance(mActivityRef.get())
                .getAppDatabase()
                .fileDataObjectDao()
                .findByFilename(params[0]);
    }

}
