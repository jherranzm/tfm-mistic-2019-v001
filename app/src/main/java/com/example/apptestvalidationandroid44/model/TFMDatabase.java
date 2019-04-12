package com.example.apptestvalidationandroid44.model;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {LocalSimKey.class}, version = 1, exportSchema = false)
public abstract class TFMDatabase extends RoomDatabase {
    public abstract LocalSimKeyDao localSimKeyDao();
}
