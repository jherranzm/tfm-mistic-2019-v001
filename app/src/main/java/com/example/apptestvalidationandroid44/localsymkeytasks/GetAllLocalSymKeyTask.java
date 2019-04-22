package com.example.apptestvalidationandroid44.localsymkeytasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.model.DatabaseClient;
import com.example.apptestvalidationandroid44.model.LocalSymKey;

import java.util.List;

public class GetAllLocalSymKeyTask extends AsyncTask<Void, Void, List<LocalSymKey>> {

    private static final String TAG = "GetAllLocalSymKeyTask";

    private Context mContext;

    public GetAllLocalSymKeyTask(Context theContext){
        this.mContext = theContext;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected List<LocalSymKey> doInBackground(Void... voids) {

                    List<LocalSymKey> taskList = DatabaseClient
                    .getInstance(this.mContext)
                    .getAppDatabase()
                    .localSymKeyDao()
                    .getAll();
            Log.i(TAG, "LocalSymKey.length : " + taskList.size());

        return taskList;
    }

}
