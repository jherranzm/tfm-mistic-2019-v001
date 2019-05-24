package edu.uoc.mistic.tfm.jherranzm.model;

public class TotalByProviderVO {

    public String taxIdentificationNumber;
    public String corporateName;
    public double totalAmount;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TotalByProviderVO {");
        sb.append("taxIdentificationNumber='").append(taxIdentificationNumber).append('\'');
        sb.append(", corporateName='").append(corporateName).append('\'');
        sb.append(", totalAmount=").append(totalAmount);
        sb.append('}');
        return sb.toString();
    }
}
