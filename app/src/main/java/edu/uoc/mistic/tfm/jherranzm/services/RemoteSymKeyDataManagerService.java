package edu.uoc.mistic.tfm.jherranzm.services;

import android.util.Log;

import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.tasks.remotesymkeytasks.RemoteSymKeyDeleteAllByUserTask;

public class RemoteSymKeyDataManagerService {

    private static final String TAG = RemoteSymKeyDataManagerService.class.getSimpleName();

    public static void deleteAllByUser() {

        try {

            RemoteSymKeyDeleteAllByUserTask remoteSymKeyDeleteAllByUserTask = new RemoteSymKeyDeleteAllByUserTask();
            remoteSymKeyDeleteAllByUserTask.execute(Constants.URL_KEYS).get();

        } catch (Exception e) {
            Log.i(TAG, e.getClass().getCanonicalName() + " : " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
