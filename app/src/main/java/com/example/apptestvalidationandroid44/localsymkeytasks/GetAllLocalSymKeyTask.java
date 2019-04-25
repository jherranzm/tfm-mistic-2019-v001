package com.example.apptestvalidationandroid44.localsymkeytasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.InvoiceApp;
import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.LocalSymKey;

import java.util.List;

public class GetAllLocalSymKeyTask extends AsyncTask<Void, Void, List<LocalSymKey>> {

    private static final String TAG = "GetAllLocalSymKeyTask";

    public GetAllLocalSymKeyTask(){

    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected List<LocalSymKey> doInBackground(Void... voids) {

                    List<LocalSymKey> taskList = DatabaseClient
                    .getInstance(InvoiceApp.getContext())
                    .getAppDatabase()
                    .localSymKeyDao()
                    .getAll();
            Log.i(TAG, "LocalSymKey.length : " + taskList.size());

        return taskList;
    }

}
