package edu.uoc.mistic.tfm.jherranzm.tasks.localsymkeytasks;

import android.os.AsyncTask;
import android.util.Log;

import edu.uoc.mistic.tfm.jherranzm.InvoiceApp;
import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.LocalSymKey;

public class InsertLocalSymKeyTask extends AsyncTask<Void, Void, LocalSymKey> {

    private static final String TAG = "InsertLocalSymKeyTask";

    private LocalSymKey lsk;

    public InsertLocalSymKeyTask(LocalSymKey theLsk){
        this.lsk = theLsk;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected LocalSymKey doInBackground(Void... voids) {

        //LocalSymKey lsk = new LocalSymKey();
        long idInserted= DatabaseClient
                    .getInstance(InvoiceApp.getContext())
                    .getAppDatabase()
                    .localSymKeyDao()
                    .insert(this.lsk);
            Log.i(TAG, "Inserted! ["+idInserted+"]" );

        return lsk;
    }

}
