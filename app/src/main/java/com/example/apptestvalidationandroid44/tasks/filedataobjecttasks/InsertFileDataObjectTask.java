package com.example.apptestvalidationandroid44.tasks.filedataobjecttasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.InvoiceApp;
import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.FileDataObject;

public class InsertFileDataObjectTask extends AsyncTask<Void, Void, FileDataObject> {

    private static final String TAG = "InsertFileDataObjectTask";

    private FileDataObject object;

    public InsertFileDataObjectTask(FileDataObject object){
        this.object = object;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected FileDataObject doInBackground(Void... voids) {

        //LocalSymKey object = new LocalSymKey();
        long idInserted= DatabaseClient
                    .getInstance(InvoiceApp.getContext())
                    .getAppDatabase()
                    .fileDataObjectDao()
                    .insert(this.object);
            Log.i(TAG, "Inserted! ["+idInserted+"]" );

        return object;
    }

}
