package com.example.apptestvalidationandroid44.repositories;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.model.LocalSymKey;
import com.example.apptestvalidationandroid44.model.TFMDatabase;

import java.util.List;

public class LocalSymKeyRepository {

    public static String DB_NAME = "LocalSimKeyDB";
    private static final String TAG = "LocalSymKeyRepository";

    private TFMDatabase tfmDatabase;


    public LocalSymKeyRepository(Context context) {
        tfmDatabase = Room.databaseBuilder(context, TFMDatabase.class, DB_NAME).build();
    }

    public void insert(final LocalSymKey lsk) {

        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                long lastInsertedId = tfmDatabase.localSymKeyDao().insert(lsk);
                Log.i(TAG, "Se ha insertado el objeto con Id:" + lastInsertedId);
                return lastInsertedId;
            }
        }.execute();
    }

    public void update(final LocalSymKey lsk) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                tfmDatabase.localSymKeyDao().update(lsk);
                return null;
            }
        }.execute();
    }

    public void deleteTask(final LocalSymKey lsk) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                tfmDatabase.localSymKeyDao().delete(lsk);
                return null;
            }
        }.execute();
    }


    public LocalSymKey getByF(String theF) {
        return tfmDatabase.localSymKeyDao().findLocalSimKeyByF(theF);
    }

    public List<LocalSymKey> getAll() {

        return tfmDatabase.localSymKeyDao().getAll();
    }
}
