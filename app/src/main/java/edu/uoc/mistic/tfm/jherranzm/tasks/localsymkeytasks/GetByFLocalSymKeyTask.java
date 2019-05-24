package edu.uoc.mistic.tfm.jherranzm.tasks.localsymkeytasks;

import android.os.AsyncTask;

import edu.uoc.mistic.tfm.jherranzm.InvoiceApp;
import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.LocalSymKey;

public class GetByFLocalSymKeyTask extends AsyncTask<String, Void, LocalSymKey> {

    private static final String TAG = "GetByFLocalSymKeyTask";

    public GetByFLocalSymKeyTask(){
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected LocalSymKey doInBackground(String... params) {

        return DatabaseClient
                .getInstance(InvoiceApp.getContext())
                .getAppDatabase()
                .localSymKeyDao()
                .findLocalSimKeyByF(params[0]);
    }

}
