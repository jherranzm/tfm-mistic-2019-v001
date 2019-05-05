package com.example.apptestvalidationandroid44.repositories;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.model.InvoiceData;
import com.example.apptestvalidationandroid44.model.TFMDatabase;

import java.util.List;

public class InvoiceDataRepository {

    public static String DB_NAME = "LocalSimKeyDB";
    private static final String TAG = "InvoiceDataRepository";

    private TFMDatabase tfmDatabase;


    public InvoiceDataRepository(Context context) {
        tfmDatabase = Room.databaseBuilder(context, TFMDatabase.class, DB_NAME).build();
    }

    public void insert(final InvoiceData obj) {

        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                long lastInsertedId = tfmDatabase.invoiceDataDao().insert(obj);
                Log.i(TAG, "Se ha insertado el objeto con Id:" + lastInsertedId);
                return lastInsertedId;
            }
        }.execute();
    }

    public void update(final InvoiceData obj) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                tfmDatabase.invoiceDataDao().update(obj);
                return null;
            }
        }.execute();
    }

    public void deleteTask(final InvoiceData obj) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                tfmDatabase.invoiceDataDao().delete(obj);
                return null;
            }
        }.execute();
    }


    public List<InvoiceData> getAllInvoiceDataTaxIdentificationNumber(String theTIN) {
        return tfmDatabase.invoiceDataDao().findAllInvoiceDataTaxIdentificationNumber(theTIN);
    }

    public List<InvoiceData> getAll() {
        return tfmDatabase.invoiceDataDao().getAll();
    }
}
