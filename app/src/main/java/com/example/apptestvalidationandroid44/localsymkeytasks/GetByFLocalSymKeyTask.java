package com.example.apptestvalidationandroid44.localsymkeytasks;

import android.content.Context;
import android.os.AsyncTask;

import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.LocalSimKey;

public class GetByFLocalSymKeyTask extends AsyncTask<String, Void, LocalSimKey> {

    private static final String TAG = "GetByFLocalSymKeyTask";

    private Context mContext;

    public GetByFLocalSymKeyTask(Context theContext){
        this.mContext = theContext;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected LocalSimKey doInBackground(String... params) {

        return DatabaseClient
                .getInstance(this.mContext)
                .getAppDatabase()
                .localSimKeyDao()
                .findLocalSimKeyByF(params[0]);
    }

}
