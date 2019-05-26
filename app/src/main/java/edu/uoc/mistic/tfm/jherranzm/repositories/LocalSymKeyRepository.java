package edu.uoc.mistic.tfm.jherranzm.repositories;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.model.LocalSymKey;
import edu.uoc.mistic.tfm.jherranzm.model.TFMDatabase;

public class LocalSymKeyRepository {

    private static final String DB_NAME = "LocalSimKeyDB";
    private static final String TAG = "LocalSymKeyRepository";

    private static TFMDatabase tfmDatabase;


    public LocalSymKeyRepository(Context context) {
        tfmDatabase = Room.databaseBuilder(context, TFMDatabase.class, DB_NAME).build();
    }

    public static void insert(final LocalSymKey lsk) {

        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                long lastInsertedId = tfmDatabase.localSymKeyDao().insert(lsk);
                Log.i(TAG, "Se ha insertado el objeto con Id:" + lastInsertedId);
                return lastInsertedId;
            }
        }.execute();
    }

    public static void update(final LocalSymKey lsk) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                tfmDatabase.localSymKeyDao().update(lsk);
                return null;
            }
        }.execute();
    }

    public static void deleteTask(final LocalSymKey lsk) {
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
