package com.example.apptestvalidationandroid44.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.apptestvalidationandroid44.model.InvoiceData;
import com.example.apptestvalidationandroid44.model.TotalByProviderByYearVO;
import com.example.apptestvalidationandroid44.model.TotalByProviderVO;

import java.util.List;

@Dao
public interface InvoiceDataDao {

        @Query("SELECT * " +
                "FROM InvoiceData")
        List<InvoiceData> getAll();

        @Query("SELECT * " +
                "FROM InvoiceData " +
                "WHERE taxIdentificationNumber = :theTIN")
        List<InvoiceData> findAllInvoiceDataTaxIdentificationNumber(String theTIN);

        @Query("SELECT * " +
                "FROM InvoiceData " +
                "WHERE batchIdentifier = :bi " +
                "and user = :u")
        List<InvoiceData> findByBatchIdentifierAndUser(String bi, String u);

        @Query("SELECT * " +
                "FROM InvoiceData " +
                "WHERE batchIdentifier = :bi")
        List<InvoiceData> findByBatchIdentifierInvoiceData(String bi);

        @Query("SELECT taxIdentificationNumber, corporateName, SUM(totalAmount) as totalAmount " +
                "FROM InvoiceData " +
                "GROUP BY taxIdentificationNumber, corporateName")
        List<TotalByProviderVO> findTotalsByProvider();

        @Query("SELECT taxIdentificationNumber, corporateName, CAST(strftime('%Y', datetime(issueDate/1000, 'unixepoch')) AS int) AS year, SUM(totalAmount) as totalAmount " +
                "FROM InvoiceData " +
                "GROUP BY taxIdentificationNumber, corporateName, CAST(strftime('%Y', datetime(issueDate/1000, 'unixepoch')) AS int)")
        List<TotalByProviderByYearVO> findTotalsByProviderAndYear();

        @Query("DELETE FROM InvoiceData")
        void deleteAll();

        @Insert
        long insert(InvoiceData invoiceData);

        @Delete
        void delete(InvoiceData invoiceData);

        @Update
        void update(InvoiceData invoiceData);
}
