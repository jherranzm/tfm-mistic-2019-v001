package edu.uoc.mistic.tfm.jherranzm.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.io.Serializable;
import java.util.Date;
import java.util.StringJoiner;

@Entity
public class InvoiceData implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user")
    private String user;

    @ColumnInfo(name = "batchIdentifier")
    private String batchIdentifier;

    @ColumnInfo(name = "totalAmount")
    private double totalAmount;

    @ColumnInfo(name = "taxIdentificationNumber")
    private String taxIdentificationNumber;

    @ColumnInfo(name = "corporateName")
    private String corporateName;

    @ColumnInfo(name = "invoiceNumber")
    private String invoiceNumber;

    @ColumnInfo(name = "issueDate")
    @TypeConverters({DateTypeConverter.class})
    private Date issueDate;

    @ColumnInfo(name = "startDate")
    @TypeConverters({DateTypeConverter.class})
    private Date startDate;

    @ColumnInfo(name = "endDate")
    @TypeConverters({DateTypeConverter.class})
    private Date endDate;

    @ColumnInfo(name = "taxBase")
    private double taxBase;

    @ColumnInfo(name = "taxAmount")
    private double taxAmount;

    @ColumnInfo(name = "totalGrossAmount")
    private double totalGrossAmount;


    @ColumnInfo(name = "isBackedUp")
    private boolean isBackedUp;

    @ColumnInfo(name = "signedInvoiceFile")
    private String signedInvoiceFile;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBatchIdentifier() {
        return batchIdentifier;
    }

    public void setBatchIdentifier(String batchIdentifier) {
        this.batchIdentifier = batchIdentifier;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTaxIdentificationNumber() {
        return taxIdentificationNumber;
    }

    public void setTaxIdentificationNumber(String taxIdentificationNumber) {
        this.taxIdentificationNumber = taxIdentificationNumber;
    }

    public String getCorporateName() {
        return corporateName;
    }

    public void setCorporateName(String corporateName) {
        this.corporateName = corporateName;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public double getTaxBase() {
        return taxBase;
    }

    public void setTaxBase(double taxBase) {
        this.taxBase = taxBase;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public double getTotalGrossAmount() {
        return totalGrossAmount;
    }

    public void setTotalGrossAmount(double totalGrossAmount) {
        this.totalGrossAmount = totalGrossAmount;
    }

    public boolean isBackedUp() {
        return isBackedUp;
    }

    public void setBackedUp(boolean backedUp) {
        isBackedUp = backedUp;
    }

    public String getSignedInvoiceFile() {
        return signedInvoiceFile;
    }

    public void setSignedInvoiceFile(String signedInvoiceFile) {
        this.signedInvoiceFile = signedInvoiceFile;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", InvoiceData.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("user='" + user + "'")
                .add("batchIdentifier='" + batchIdentifier + "'")
                .add("totalAmount=" + totalAmount)
                .add("taxIdentificationNumber='" + taxIdentificationNumber + "'")
                .add("corporateName='" + corporateName + "'")
                .add("invoiceNumber='" + invoiceNumber + "'")
                .add("issueDate=" + issueDate)
                .add("startDate=" + startDate)
                .add("endDate=" + endDate)
                .add("taxBase=" + taxBase)
                .add("taxAmount=" + taxAmount)
                .add("totalGrossAmount=" + totalGrossAmount)
                .add("isBackedUp=" + isBackedUp)
                .add("signedInvoiceFile='" + signedInvoiceFile + "'")
                .toString();
    }
}
