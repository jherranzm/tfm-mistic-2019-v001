package edu.uoc.mistic.tfm.jherranzm.tasks.localsymkeytasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.InvoiceApp;
import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.LocalSymKey;

public class GetAllLocalSymKeyTask extends AsyncTask<Void, Void, List<LocalSymKey>> {

    private static final String TAG = "GetAllLocalSymKeyTask";

    public GetAllLocalSymKeyTask(){

    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected List<LocalSymKey> doInBackground(Void... voids) {

                    List<LocalSymKey> taskList = DatabaseClient
                    .getInstance(InvoiceApp.getContext())
                    .getAppDatabase()
                    .localSymKeyDao()
                    .getAll();
            Log.i(TAG, "LocalSymKey.length : " + taskList.size());

        return taskList;
    }

}
