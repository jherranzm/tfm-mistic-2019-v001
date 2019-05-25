package edu.uoc.mistic.tfm.jherranzm.tasks.filedataobjecttasks;

import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

import edu.uoc.mistic.tfm.jherranzm.ReceivedInvoicesRecyclerViewActivity;
import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.FileDataObject;

public class InsertFileDataObjectTask extends AsyncTask<Void, Void, FileDataObject> {

    private static final String TAG = InsertFileDataObjectTask.class.getSimpleName();

    private FileDataObject object;

    private final WeakReference<ReceivedInvoicesRecyclerViewActivity> mActivityRef;

    public InsertFileDataObjectTask(ReceivedInvoicesRecyclerViewActivity activity, FileDataObject object){
        mActivityRef = new WeakReference<>(activity);
        this.object = object;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected FileDataObject doInBackground(Void... voids) {

        Log.i(TAG, "Implemented with WeakReference..." );
        long idInserted= DatabaseClient
                    .getInstance(mActivityRef.get())
                    .getAppDatabase()
                    .fileDataObjectDao()
                    .insert(this.object);
        Log.i(TAG, String.format("Inserted! [%d]", idInserted));

        return object;
    }

}
