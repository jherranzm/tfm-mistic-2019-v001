package com.example.apptestvalidationandroid44.services;

import android.util.Log;

import com.example.apptestvalidationandroid44.config.Constants;
import com.example.apptestvalidationandroid44.tasks.remotesymkeytasks.RemoteSymKeyDeleteAllByUserTask;

public class RemoteSymKeyDataManagerService {

    private static final String TAG = RemoteSymKeyDataManagerService.class.getSimpleName();

    public static void deleteAllByUser() {

        try {

            RemoteSymKeyDeleteAllByUserTask remoteSymKeyDeleteAllByUserTask = new RemoteSymKeyDeleteAllByUserTask();
            remoteSymKeyDeleteAllByUserTask.execute(Constants.URL_KEYS).get();

        } catch (Exception e) {
            Log.i(TAG, e.getClass().getCanonicalName() + " : " + e.getLocalizedMessage());
            e.printStackTrace();
        };
    }
}
