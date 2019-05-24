package edu.uoc.mistic.tfm.jherranzm.tasks.localsymkeytasks;

import android.os.AsyncTask;
import android.util.Log;

import edu.uoc.mistic.tfm.jherranzm.InvoiceApp;
import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;
import edu.uoc.mistic.tfm.jherranzm.model.LocalSymKey;

public class DeleteLocalSymKeyTask extends AsyncTask<Void, Void, LocalSymKey> {

    private static final String TAG = "DeleteLocalSymKeyTask";

    private LocalSymKey lsk;

    public DeleteLocalSymKeyTask(LocalSymKey theLsk){
        this.lsk = theLsk;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected LocalSymKey doInBackground(Void... voids) {

        DatabaseClient
                    .getInstance(InvoiceApp.getContext())
                    .getAppDatabase()
                    .localSymKeyDao()
                    .delete(this.lsk);
            Log.i(TAG, "Deleted : ["+this.lsk+"]" );

        return lsk;
    }

}
