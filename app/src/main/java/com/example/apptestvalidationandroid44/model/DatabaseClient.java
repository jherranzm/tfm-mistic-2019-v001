package com.example.apptestvalidationandroid44.model;

import android.arch.persistence.room.Room;
import android.content.Context;

public class DatabaseClient {

    private static DatabaseClient mInstance;

    //our app database object
    private TFMDatabase appDatabase;

    private DatabaseClient(Context mCtx) {
        // creating the app database with Room database builder
        // LocalSimKeyDB is the name of the database
        appDatabase = Room.databaseBuilder(mCtx, TFMDatabase.class, "LocalSimKeyDB").build();
    }

    public static synchronized DatabaseClient getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new DatabaseClient(mCtx);
        }
        return mInstance;
    }

    public TFMDatabase getAppDatabase() {
        return appDatabase;
    }
}
