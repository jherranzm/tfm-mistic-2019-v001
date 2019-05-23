package com.example.apptestvalidationandroid44.tasks.filedataobjecttasks;

import android.os.AsyncTask;

import com.example.apptestvalidationandroid44.InvoiceApp;
import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.FileDataObject;

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
