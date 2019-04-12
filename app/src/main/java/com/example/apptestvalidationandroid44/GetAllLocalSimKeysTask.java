package com.example.apptestvalidationandroid44;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.LocalSimKey;

import java.util.List;

public class GetAllLocalSimKeysTask extends AsyncTask<Void, Void, List<LocalSimKey>> {

    private static final String TAG = "GetAllLocalSimKeysTask";

    private Context mContext;

    GetAllLocalSimKeysTask(Context theContext){
        this.mContext = theContext;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected List<LocalSimKey> doInBackground(Void... voids) {

                    List<LocalSimKey> taskList = (List<LocalSimKey>) DatabaseClient
                    .getInstance(this.mContext)
                    .getAppDatabase()
                    .localSimKeyDao()
                    .getAll();
            Log.i(TAG, "LocalSimKey.length : " + taskList.size());

        return taskList;
    }

}
