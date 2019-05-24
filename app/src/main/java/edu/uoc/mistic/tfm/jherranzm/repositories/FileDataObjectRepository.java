package edu.uoc.mistic.tfm.jherranzm.repositories;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.model.FileDataObject;
import edu.uoc.mistic.tfm.jherranzm.model.TFMDatabase;

public class FileDataObjectRepository {

    public static String DB_NAME = "LocalSimKeyDB";
    private static final String TAG = "FileDataObjectRepository";

    private static TFMDatabase tfmDatabase;


    public FileDataObjectRepository(Context context) {
        tfmDatabase = Room.databaseBuilder(context, TFMDatabase.class, DB_NAME).build();
    }

    public static void insert(final FileDataObject obj) {

        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                long lastInsertedId = tfmDatabase.fileDataObjectDao().insert(obj);
                Log.i(TAG, "Se ha insertado el objeto con Id:" + lastInsertedId);
                return lastInsertedId;
            }
        }.execute();
    }

    public static void update(final FileDataObject obj) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                tfmDatabase.fileDataObjectDao().update(obj);
                return null;
            }
        }.execute();
    }

    public static void deleteTask(final FileDataObject obj) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                tfmDatabase.fileDataObjectDao().delete(obj);
                return null;
            }
        }.execute();
    }


    public FileDataObject getByFilename(String theFilename) {
        return tfmDatabase.fileDataObjectDao().findByFilename(theFilename);
    }

    public List<FileDataObject> getAllByUser(String theUser) {
        return tfmDatabase.fileDataObjectDao().findByUser(theUser);
    }

    public List<FileDataObject> getAll() {
        return tfmDatabase.fileDataObjectDao().getAll();
    }
}
