package edu.uoc.mistic.tfm.jherranzm.tasks.localsymkeytasks;

import android.os.AsyncTask;
import android.util.Log;

import edu.uoc.mistic.tfm.jherranzm.InvoiceApp;
import edu.uoc.mistic.tfm.jherranzm.model.DatabaseClient;

public class DeleteAllByUserLocalSymKeyTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "DeleteAllByUserLocalSymKeyTask";

    private String user;

    public DeleteAllByUserLocalSymKeyTask(String theUser){
        this.user = theUser;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected Void doInBackground(Void... voids) {

        DatabaseClient
                    .getInstance(InvoiceApp.getContext())
                    .getAppDatabase()
                    .localSymKeyDao()
                    .deleteAllByUser(this.user);
        Log.i(TAG, "Deleted all from user : ["+this.user+"]" );
        return null;
    }

}
