package edu.uoc.mistic.tfm.jherranzm.model;

import java.io.Serializable;

public class TotalByProviderVO implements Serializable {

    public String taxIdentificationNumber;
    public String corporateName;
    public double totalAmount;

    @Override
    public String toString() {
        String sb;
        sb = "TotalByProviderVO {" + "taxIdentificationNumber='" + taxIdentificationNumber + '\'' +
                ", corporateName='" + corporateName + '\'' +
                ", totalAmount=" + totalAmount +
                '}';
        return sb;
    }
}
