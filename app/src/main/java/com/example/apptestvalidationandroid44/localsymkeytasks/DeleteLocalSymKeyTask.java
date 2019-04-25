package com.example.apptestvalidationandroid44.localsymkeytasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.InvoiceApp;
import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.LocalSymKey;

public class DeleteLocalSymKeyTask extends AsyncTask<Void, Void, LocalSymKey> {

    private static final String TAG = "DeleteLocalSymKeyTask";

    private LocalSymKey lsk;

    public DeleteLocalSymKeyTask(LocalSymKey theLsk){
        this.lsk = theLsk;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected LocalSymKey doInBackground(Void... voids) {

        DatabaseClient
                    .getInstance(InvoiceApp.getContext())
                    .getAppDatabase()
                    .localSymKeyDao()
                    .delete(this.lsk);
            Log.i(TAG, "Deleted : ["+this.lsk+"]" );

        return lsk;
    }

}
