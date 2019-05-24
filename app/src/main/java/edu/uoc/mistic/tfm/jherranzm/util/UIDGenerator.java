package edu.uoc.mistic.tfm.jherranzm.util;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import es.facturae.facturae.v3.facturae.Facturae;

public class UIDGenerator {

    public static String generate(String str){
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] key = md.digest(str.getBytes(StandardCharsets.UTF_8));
                return Base64.encodeToString(key, Base64.NO_WRAP);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
    }

    public static String generate(Facturae facturae){
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");

                StringBuilder sb = new StringBuilder();
                sb.append(facturae.getParties().getSellerParty().getTaxIdentification().getTaxIdentificationNumber());
                sb.append("|");
                sb.append(facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber());
                sb.append("|");
                sb.append(facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal());
                sb.append("|");
                sb.append(facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getIssueDate());


                byte[] key = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
                return Base64.encodeToString(key, Base64.NO_WRAP);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
    }
}
