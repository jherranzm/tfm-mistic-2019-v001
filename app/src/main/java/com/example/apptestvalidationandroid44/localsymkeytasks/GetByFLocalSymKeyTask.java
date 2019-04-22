package com.example.apptestvalidationandroid44.localsymkeytasks;

import android.content.Context;
import android.os.AsyncTask;

import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.LocalSymKey;

public class GetByFLocalSymKeyTask extends AsyncTask<String, Void, LocalSymKey> {

    private static final String TAG = "GetByFLocalSymKeyTask";

    private Context mContext;

    public GetByFLocalSymKeyTask(Context theContext){
        this.mContext = theContext;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected LocalSymKey doInBackground(String... params) {

        return DatabaseClient
                .getInstance(this.mContext)
                .getAppDatabase()
                .localSymKeyDao()
                .findLocalSimKeyByF(params[0]);
    }

}
