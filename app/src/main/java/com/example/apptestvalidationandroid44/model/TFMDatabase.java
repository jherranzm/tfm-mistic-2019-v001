package com.example.apptestvalidationandroid44.model;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

@Database(entities = {LocalSymKey.class, InvoiceData.class}, version = 3, exportSchema = false)
@TypeConverters({DateTypeConverter.class})
public abstract class TFMDatabase extends RoomDatabase {
    public abstract LocalSymKeyDao localSymKeyDao();
    public abstract InvoiceDataDao invoiceDataDao();
}
