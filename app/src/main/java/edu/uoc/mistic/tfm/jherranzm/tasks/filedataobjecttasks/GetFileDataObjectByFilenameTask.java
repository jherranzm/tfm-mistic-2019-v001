package edu.uoc.mistic.tfm.jherranzm.tasks.filedataobjecttasks;

import android.os.AsyncTask;

import edu.uoc.mistic.tfm.jherranzm.InvoiceApp;
import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.FileDataObject;

public class GetFileDataObjectByFilenameTask extends AsyncTask<String, Void, FileDataObject> {

    private static final String TAG = "GetFileDataObjectByFilenameTask";

    public GetFileDataObjectByFilenameTask(){
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected FileDataObject doInBackground(String... params) {

        return DatabaseClient
                .getInstance(InvoiceApp.getContext())
                .getAppDatabase()
                .fileDataObjectDao()
                .findByFilename(params[0]);
    }

}
