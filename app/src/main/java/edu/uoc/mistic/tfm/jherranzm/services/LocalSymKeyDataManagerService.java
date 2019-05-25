package edu.uoc.mistic.tfm.jherranzm.services;

import android.util.Log;

import edu.uoc.mistic.tfm.jherranzm.tasks.localsymkeytasks.DeleteAllByUserLocalSymKeyTask;

public class LocalSymKeyDataManagerService {

    private static final String TAG = LocalSymKeyDataManagerService.class.getSimpleName();

    public static void deleteAllByUser(String user) {

        try {

            DeleteAllByUserLocalSymKeyTask deleteAllByUserLocalSymKeyTask = new DeleteAllByUserLocalSymKeyTask(user);
            deleteAllByUserLocalSymKeyTask.execute().get();

        } catch (Exception e) {
            Log.i(TAG, e.getClass().getCanonicalName() + " : " + e.getLocalizedMessage());
            e.printStackTrace();
        };
    }
}