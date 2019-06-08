package edu.uoc.mistic.tfm.jherranzm.vo;

public class EncryptedInvoice {

    // taxIdentificationNumber
    String taxIdentificationNumberEncrypted;

    // corporateName
    String corporateNameEncrypted;

    // invoiceNumber
    String invoiceNumberEncrypted;

    // total
    String totalEncrypted;

    // total_tax_outputs
    String totalTaxOutputsEncrypted;

    // total_gross_amount
    String totalGrossAmountEncrypted;

    // issue Date
    String dataEncrypted;

    // Encrypt file
    String signedInvoiceEncrypted;

    // Encrypt iv
    String ivStringEnc;

    // Encrypt symmetricKey
    String simKeyStringEnc;



    public String getTaxIdentificationNumberEncrypted() {
        return taxIdentificationNumberEncrypted;
    }

    public void setTaxIdentificationNumberEncrypted(String taxIdentificationNumberEncrypted) {
        this.taxIdentificationNumberEncrypted = taxIdentificationNumberEncrypted;
    }

    public String getCorporateNameEncrypted() {
        return corporateNameEncrypted;
    }

    public void setCorporateNameEncrypted(String corporateNameEncrypted) {
        this.corporateNameEncrypted = corporateNameEncrypted;
    }

    public String getInvoiceNumberEncrypted() {
        return invoiceNumberEncrypted;
    }

    public void setInvoiceNumberEncrypted(String invoiceNumberEncrypted) {
        this.invoiceNumberEncrypted = invoiceNumberEncrypted;
    }

    public String getTotalEncrypted() {
        return totalEncrypted;
    }

    public void setTotalEncrypted(String totalEncrypted) {
        this.totalEncrypted = totalEncrypted;
    }

    public String getTotalTaxOutputsEncrypted() {
        return totalTaxOutputsEncrypted;
    }

    public void setTotalTaxOutputsEncrypted(String totalTaxOutputsEncrypted) {
        this.totalTaxOutputsEncrypted = totalTaxOutputsEncrypted;
    }

    public String getTotalGrossAmountEncrypted() {
        return totalGrossAmountEncrypted;
    }

    public void setTotalGrossAmountEncrypted(String totalGrossAmountEncrypted) {
        this.totalGrossAmountEncrypted = totalGrossAmountEncrypted;
    }

    public String getDataEncrypted() {
        return dataEncrypted;
    }

    public void setDataEncrypted(String dataEncrypted) {
        this.dataEncrypted = dataEncrypted;
    }

    public String getSignedInvoiceEncrypted() {
        return signedInvoiceEncrypted;
    }

    public void setSignedInvoiceEncrypted(String signedInvoiceEncrypted) {
        this.signedInvoiceEncrypted = signedInvoiceEncrypted;
    }

    public String getIvStringEnc() {
        return ivStringEnc;
    }

    public void setIvStringEnc(String ivStringEnc) {
        this.ivStringEnc = ivStringEnc;
    }

    public String getSimKeyStringEnc() {
        return simKeyStringEnc;
    }

    public void setSimKeyStringEnc(String simKeyStringEnc) {
        this.simKeyStringEnc = simKeyStringEnc;
    }
}
