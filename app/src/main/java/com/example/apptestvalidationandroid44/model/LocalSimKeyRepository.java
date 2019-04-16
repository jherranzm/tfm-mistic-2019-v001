package com.example.apptestvalidationandroid44.model;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

public class LocalSimKeyRepository {

    public static String DB_NAME = "LocalSimKeyDB";
    private static final String TAG = "LocalSimKeyRepository";

    private TFMDatabase tfmDatabase;


    public LocalSimKeyRepository(Context context) {
        tfmDatabase = Room.databaseBuilder(context, TFMDatabase.class, DB_NAME).build();
    }

    public void insert(final LocalSimKey lsk) {

        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                long lastInsertedId = tfmDatabase.localSimKeyDao().insert(lsk);
                Log.i(TAG, "Se ha insertado el objeto con Id:" + lastInsertedId);
                return lastInsertedId;
            }
        }.execute();
    }

    public void update(final LocalSimKey lsk) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                tfmDatabase.localSimKeyDao().update(lsk);
                return null;
            }
        }.execute();
    }

    public void deleteTask(final LocalSimKey lsk) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                tfmDatabase.localSimKeyDao().delete(lsk);
                return null;
            }
        }.execute();
    }


    public LocalSimKey getByF(String theF) {
        return tfmDatabase.localSimKeyDao().findLocalSimKeyByF(theF);
    }

    public List<LocalSimKey> getAll() {

        return tfmDatabase.localSimKeyDao().getAll();
    }
}
