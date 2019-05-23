package com.example.apptestvalidationandroid44.services;

import android.util.Log;

import com.example.apptestvalidationandroid44.tasks.localsymkeytasks.DeleteAllByUserLocalSymKeyTask;

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
