package com.example.apptestvalidationandroid44;

import android.content.Context;
import android.os.AsyncTask;

import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.LocalSimKey;

public class GetByFLocalSimKeysTask extends AsyncTask<String, Void, LocalSimKey> {

    private static final String TAG = "GetByFLocalSimKeysTask";

    private Context mContext;

    GetByFLocalSimKeysTask(Context theContext){
        this.mContext = theContext;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected LocalSimKey doInBackground(String... params) {

        LocalSimKey lsk = DatabaseClient
                    .getInstance(this.mContext)
                    .getAppDatabase()
                    .localSimKeyDao()
                    .findLocalSimKeyByF(params[0]);
        return lsk;
    }

}
