package com.example.apptestvalidationandroid44.tasks.localsymkeytasks;

import android.os.AsyncTask;

import com.example.apptestvalidationandroid44.InvoiceApp;
import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.LocalSymKey;

public class GetByFLocalSymKeyTask extends AsyncTask<String, Void, LocalSymKey> {

    private static final String TAG = "GetByFLocalSymKeyTask";

    public GetByFLocalSymKeyTask(){
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected LocalSymKey doInBackground(String... params) {

        return DatabaseClient
                .getInstance(InvoiceApp.getContext())
                .getAppDatabase()
                .localSymKeyDao()
                .findLocalSimKeyByF(params[0]);
    }

}
