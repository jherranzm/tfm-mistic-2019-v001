package com.example.apptestvalidationandroid44.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.apptestvalidationandroid44.model.InvoiceData;

import java.util.List;

@Dao
public interface InvoiceDataDao {

        @Query("SELECT * FROM InvoiceData")
        List<InvoiceData> getAll();

        @Query("SELECT * FROM InvoiceData WHERE taxIdentificationNumber = :theTIN")
        List<InvoiceData> findAllInvoiceDataTaxIdentificationNumber(String theTIN);

        @Query("SELECT * FROM InvoiceData WHERE batchIdentifier = :bi")
        List<InvoiceData> findByBatchIdentifierInvoiceData(String bi);


        @Query("DELETE FROM InvoiceData")
        void deleteAll();

        @Insert
        long insert(InvoiceData invoiceData);

        @Delete
        void delete(InvoiceData invoiceData);

        @Update
        void update(InvoiceData invoiceData);
}
