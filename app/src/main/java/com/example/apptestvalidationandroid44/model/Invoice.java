package com.example.apptestvalidationandroid44.model;

import java.io.Serializable;

public class Invoice implements Serializable {

    private String uid;
    private String taxIdentificationNumber;
    private String corporateName;
    private String invoiceNumber;
    private Double invoiceTotal;
    private Double totalTaxOutputs;
    private java.util.Date issueDate;

    public Invoice(){}

    public Invoice(String uid
            , String taxIdentificationNumber
            , String corporateName
            , String invoiceNumber
            , Double invoiceTotal
            , Double totalTaxOutputs
            , java.util.Date issueDate) {
        this.uid = uid;
        this.taxIdentificationNumber = taxIdentificationNumber;
        this.corporateName = corporateName;
        this.invoiceNumber = invoiceNumber;
        this.invoiceTotal = invoiceTotal;
        this.totalTaxOutputs = totalTaxOutputs;
        this.issueDate = issueDate;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public Double getInvoiceTotal() {
        return invoiceTotal;
    }

    public void setInvoiceTotal(Double invoiceTotal) {
        this.invoiceTotal = invoiceTotal;
    }

    public Double getTotalTaxOutputs() {
        return totalTaxOutputs;
    }

    public void setTotalTaxOutputs(Double totalTaxOutputs) {
        this.totalTaxOutputs = totalTaxOutputs;
    }

    public java.util.Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(java.util.Date issueDate) {
        this.issueDate = issueDate;
    }

    @Override
    public String toString() {
        String sb = "Invoice{" + "uid='" + uid + '\'' +
                ", taxIdentificationNumber='" + taxIdentificationNumber + '\'' +
                ", corporateName='" + corporateName + '\'' +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", invoiceTotal=" + invoiceTotal +
                ", totalTaxOutputs=" + totalTaxOutputs +
                ", issueDate=" + issueDate +
                '}';
        return sb;
    }
}