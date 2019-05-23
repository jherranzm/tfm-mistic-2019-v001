package com.example.apptestvalidationandroid44.tasks.localsymkeytasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.InvoiceApp;
import com.example.apptestvalidationandroid44.model.DatabaseClient;

public class DeleteAllByUserLocalSymKeyTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "DeleteAllByUserLocalSymKeyTask";

    private String user;

    public DeleteAllByUserLocalSymKeyTask(String theUser){
        this.user = theUser;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected Void doInBackground(Void... voids) {

        DatabaseClient
                    .getInstance(InvoiceApp.getContext())
                    .getAppDatabase()
                    .localSymKeyDao()
                    .deleteAllByUser(this.user);
        Log.i(TAG, "Deleted all from user : ["+this.user+"]" );
        return null;
    }

}
