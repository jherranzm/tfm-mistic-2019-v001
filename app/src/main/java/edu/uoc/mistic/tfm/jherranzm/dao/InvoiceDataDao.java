package edu.uoc.mistic.tfm.jherranzm.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;
import edu.uoc.mistic.tfm.jherranzm.model.TotalByProviderByYearVO;
import edu.uoc.mistic.tfm.jherranzm.model.TotalByProviderVO;

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

    @Query("SELECT * " +
        "FROM InvoiceData " +
        "WHERE user = :user")
    List<InvoiceData> getAllByUser(String user);


    @Query("SELECT CAST(strftime('%Y', datetime(issueDate/1000, 'unixepoch')) AS int) AS year " +
            "FROM InvoiceData " +
            "GROUP BY CAST(strftime('%Y', datetime(issueDate/1000, 'unixepoch')) AS int)")
    List<String> findDistinctYears();

}
