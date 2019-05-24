package edu.uoc.mistic.tfm.jherranzm.model;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import edu.uoc.mistic.tfm.jherranzm.dao.FileDataObjectDao;
import edu.uoc.mistic.tfm.jherranzm.dao.InvoiceDataDao;
import edu.uoc.mistic.tfm.jherranzm.dao.LocalSymKeyDao;

@Database(entities = {LocalSymKey.class, InvoiceData.class, FileDataObject.class}, version = 6, exportSchema = false)
@TypeConverters({DateTypeConverter.class})
public abstract class TFMDatabase extends RoomDatabase {
    public abstract LocalSymKeyDao localSymKeyDao();
    public abstract InvoiceDataDao invoiceDataDao();
    public abstract FileDataObjectDao fileDataObjectDao();
}
