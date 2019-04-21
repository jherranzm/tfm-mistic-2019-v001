package com.example.apptestvalidationandroid44.localsymkeytasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.LocalSimKey;

public class InsertLocalSymKeyTask extends AsyncTask<Void, Void, LocalSimKey> {

    private static final String TAG = "InsertLocalSymKeyTask";

    private Context mContext;
    private LocalSimKey lsk;

    public InsertLocalSymKeyTask(Context theContext, LocalSimKey theLsk){
        this.mContext = theContext;
        this.lsk = theLsk;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected LocalSimKey doInBackground(Void... voids) {

        //LocalSimKey lsk = new LocalSimKey();
        long idInserted= DatabaseClient
                    .getInstance(this.mContext)
                    .getAppDatabase()
                    .localSimKeyDao()
                    .insert(this.lsk);
            Log.i(TAG, "Inserted! ["+idInserted+"]" );

        return lsk;
    }

}
