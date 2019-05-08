package com.example.apptestvalidationandroid44.model;

public class TotalByProviderByYearVO {
    
    public String taxIdentificationNumber;
    public String corporateName;
    public String year;
    public double totalAmount;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TotalByProviderByYearVO{");
        sb.append("taxIdentificationNumber='").append(taxIdentificationNumber).append('\'');
        sb.append(", corporateName='").append(corporateName).append('\'');
        sb.append(", year=").append(year);
        sb.append(", totalAmount=").append(totalAmount);
        sb.append('}');
        return sb.toString();
    }
}