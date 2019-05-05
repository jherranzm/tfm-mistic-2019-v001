package com.example.apptestvalidationandroid44.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface InvoiceDataDao {

        @Query("SELECT * FROM InvoiceData")
        List<InvoiceData> getAll();

        @Query("SELECT * FROM InvoiceData WHERE taxIdentificationNumber = :theTIN")
        List<InvoiceData> findAllInvoiceDataTaxIdentificationNumber(String theTIN);

        @Insert
        long insert(InvoiceData invoiceData);

        @Delete
        void delete(InvoiceData invoiceData);

        @Update
        void update(InvoiceData invoiceData);
}
