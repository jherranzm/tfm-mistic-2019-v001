package com.example.apptestvalidationandroid44.model;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.example.apptestvalidationandroid44.dao.InvoiceDataDao;
import com.example.apptestvalidationandroid44.dao.LocalSymKeyDao;

@Database(entities = {LocalSymKey.class, InvoiceData.class}, version = 4, exportSchema = false)
@TypeConverters({DateTypeConverter.class})
public abstract class TFMDatabase extends RoomDatabase {
    public abstract LocalSymKeyDao localSymKeyDao();
    public abstract InvoiceDataDao invoiceDataDao();
}
