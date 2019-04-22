package com.example.apptestvalidationandroid44.localsymkeytasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.LocalSymKey;

public class InsertLocalSymKeyTask extends AsyncTask<Void, Void, LocalSymKey> {

    private static final String TAG = "InsertLocalSymKeyTask";

    private Context mContext;
    private LocalSymKey lsk;

    public InsertLocalSymKeyTask(Context theContext, LocalSymKey theLsk){
        this.mContext = theContext;
        this.lsk = theLsk;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected LocalSymKey doInBackground(Void... voids) {

        //LocalSymKey lsk = new LocalSymKey();
        long idInserted= DatabaseClient
                    .getInstance(this.mContext)
                    .getAppDatabase()
                    .localSymKeyDao()
                    .insert(this.lsk);
            Log.i(TAG, "Inserted! ["+idInserted+"]" );

        return lsk;
    }

}
