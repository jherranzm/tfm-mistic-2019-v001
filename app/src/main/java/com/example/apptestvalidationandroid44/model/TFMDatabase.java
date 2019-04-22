package com.example.apptestvalidationandroid44.model;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {LocalSymKey.class}, version = 2, exportSchema = false)
public abstract class TFMDatabase extends RoomDatabase {
    public abstract LocalSymKeyDao localSymKeyDao();
}
