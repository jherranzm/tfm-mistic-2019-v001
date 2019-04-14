package com.example.apptestvalidationandroid44.localsimkeystasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.LocalSimKey;

public class DeleteLocalSimKeysTask extends AsyncTask<Void, Void, LocalSimKey> {

    private static final String TAG = "DeleteLocalSimKeysTask";

    private Context mContext;
    private LocalSimKey lsk;

    public DeleteLocalSimKeysTask(Context theContext, LocalSimKey theLsk){
        this.mContext = theContext;
        this.lsk = theLsk;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected LocalSimKey doInBackground(Void... voids) {

        DatabaseClient
                    .getInstance(this.mContext)
                    .getAppDatabase()
                    .localSimKeyDao()
                    .delete(this.lsk);
            Log.i(TAG, "Deleted : ["+this.lsk+"]" );

        return lsk;
    }

}
