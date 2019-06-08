package edu.uoc.mistic.tfm.jherranzm.util;

import android.util.Base64;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;

import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.crypto.AsymmetricEncryptor;
import edu.uoc.mistic.tfm.jherranzm.crypto.SymmetricEncryptor;
import edu.uoc.mistic.tfm.jherranzm.vo.EncryptedInvoice;
import es.facturae.facturae.v3.facturae.Facturae;

public class UtilEncryptInvoice {

    // Constants
    private static final String TAG = UtilEncryptInvoice.class.getSimpleName();

    // Security
    private TFMSecurityManager tfmSecurityManager;

    public UtilEncryptInvoice(){
        tfmSecurityManager = TFMSecurityManager.getInstance();
    }

    public EncryptedInvoice getEncryptedInvoice(byte[] byteArraySignedInvoice, Facturae facturae) throws Exception {
        // Data encrypting
        Log.i(TAG, "Encryption...");
        RandomStringGenerator rsg = new RandomStringGenerator();

        // IV and Symmetric Key
        String iv = rsg.getRandomString(16);
        Log.i(TAG, String.format("iv     : [%s]", iv));
        String simKey = rsg.getRandomString(16);
        Log.i(TAG, String.format("simKey : [%s]", simKey));


        SymmetricEncryptor simEnc = new SymmetricEncryptor();
        simEnc.setIv(iv);
        simEnc.setKey(simKey);

        if(facturae.getParties() == null){
            throw new Exception("ERROR: invoice has NOT parties.");
        }

        EncryptedInvoice encryptedInvoice = new EncryptedInvoice();

        // taxIdentificationNumber
        String taxIdentificationNumberEncrypted = simEnc.encrypt(
                facturae.getParties().getSellerParty().getTaxIdentification().getTaxIdentificationNumber(),
                tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.TAX_IDENTIFICATION_NUMBER)
        );
        encryptedInvoice.setTaxIdentificationNumberEncrypted(taxIdentificationNumberEncrypted);

        // corporateName
        String corporateNameEncrypted = simEnc.encrypt(
                facturae.getParties().getSellerParty().getLegalEntity().getCorporateName(),
                tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.CORPORATE_NAME)
        );
        encryptedInvoice.setCorporateNameEncrypted(corporateNameEncrypted);

        // invoiceNumber
        String invoiceNumberEncrypted = simEnc.encrypt(
                facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber(),
                tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.INVOICE_NUMBER)
        );
        encryptedInvoice.setInvoiceNumberEncrypted(invoiceNumberEncrypted);

        // total
        String totalEncrypted = simEnc.encrypt(
                ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal(),
                tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.INVOICE_TOTAL)
        );
        encryptedInvoice.setTotalEncrypted(totalEncrypted);

        // total_tax_outputs
        String totalTaxOutputsEncrypted = simEnc.encrypt(
                ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalTaxOutputs(),
                tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.TOTAL_TAX_OUTPUTS)
        );
        encryptedInvoice.setTotalTaxOutputsEncrypted(totalTaxOutputsEncrypted);

        // total_gross_amount
        String totalGrossAmountEncrypted = simEnc.encrypt(
                ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalGrossAmount(),
                tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.TOTAL_GROSS_AMOUNT)
        );
        encryptedInvoice.setTotalGrossAmountEncrypted(totalGrossAmountEncrypted);

        // issue Date
        String dataEncrypted   = simEnc.encrypt(
                ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getIssueDate(),
                tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.ISSUE_DATE)
        );
        encryptedInvoice.setDataEncrypted(dataEncrypted);

        // Encrypt file
        String signedInvoiceEncrypted   = simEnc.encrypt(byteArraySignedInvoice);
        encryptedInvoice.setSignedInvoiceEncrypted(signedInvoiceEncrypted);

        // Encrypt iv and symmetric key with public key
        byte[] ivBytesEnc = AsymmetricEncryptor.encryptData(iv.getBytes(), tfmSecurityManager.getCertificate());
        String ivStringEnc = new String(Base64.encode(ivBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);
        encryptedInvoice.setIvStringEnc(ivStringEnc);

        byte[] simKeyBytesEnc = AsymmetricEncryptor.encryptData(simKey.getBytes(), tfmSecurityManager.getCertificate());
        String simKeyStringEnc = new String(Base64.encode(simKeyBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);
        encryptedInvoice.setSimKeyStringEnc(simKeyStringEnc);
        return encryptedInvoice;
    }

    public EncryptedInvoice getEncryptedInvoice(String signedInvoiceFile, Facturae facturae) throws Exception {
        // Data encrypting
        Log.i(TAG, "Encryption...");
        RandomStringGenerator rsg = new RandomStringGenerator();

        // IV and Symmetric Key
        String iv = rsg.getRandomString(16);
        Log.i(TAG, String.format("iv     : [%s]", iv));
        String simKey = rsg.getRandomString(16);
        Log.i(TAG, String.format("simKey : [%s]", simKey));


        SymmetricEncryptor simEnc = new SymmetricEncryptor();
        simEnc.setIv(iv);
        simEnc.setKey(simKey);

        if(facturae.getParties() == null){
            throw new Exception("ERROR: invoice has NOT parties.");
        }

        EncryptedInvoice encryptedInvoice = new EncryptedInvoice();

        // taxIdentificationNumber
        String taxIdentificationNumberEncrypted = simEnc.encrypt(
                facturae.getParties().getSellerParty().getTaxIdentification().getTaxIdentificationNumber(),
                tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.TAX_IDENTIFICATION_NUMBER)
        );
        encryptedInvoice.setTaxIdentificationNumberEncrypted(taxIdentificationNumberEncrypted);

        // corporateName
        String corporateNameEncrypted = simEnc.encrypt(
                facturae.getParties().getSellerParty().getLegalEntity().getCorporateName(),
                tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.CORPORATE_NAME)
        );
        encryptedInvoice.setCorporateNameEncrypted(corporateNameEncrypted);

        // invoiceNumber
        String invoiceNumberEncrypted = simEnc.encrypt(
                facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber(),
                tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.INVOICE_NUMBER)
        );
        encryptedInvoice.setInvoiceNumberEncrypted(invoiceNumberEncrypted);

        // total
        String totalEncrypted = simEnc.encrypt(
                ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal(),
                tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.INVOICE_TOTAL)
        );
        encryptedInvoice.setTotalEncrypted(totalEncrypted);

        // total_tax_outputs
        String totalTaxOutputsEncrypted = simEnc.encrypt(
                ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalTaxOutputs(),
                tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.TOTAL_TAX_OUTPUTS)
        );
        encryptedInvoice.setTotalTaxOutputsEncrypted(totalTaxOutputsEncrypted);

        // total_gross_amount
        String totalGrossAmountEncrypted = simEnc.encrypt(
                ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalGrossAmount(),
                tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.TOTAL_GROSS_AMOUNT)
        );
        encryptedInvoice.setTotalGrossAmountEncrypted(totalGrossAmountEncrypted);

        // issue Date
        String dataEncrypted   = simEnc.encrypt(
                ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getIssueDate(),
                tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.ISSUE_DATE)
        );
        encryptedInvoice.setDataEncrypted(dataEncrypted);

        // Encrypt file
        String signedInvoiceEncrypted   = simEnc.encrypt(IOUtils.toByteArray(signedInvoiceFile));
        encryptedInvoice.setSignedInvoiceEncrypted(signedInvoiceEncrypted);

        // Encrypt iv and symmetric key with public key
        byte[] ivBytesEnc = AsymmetricEncryptor.encryptData(iv.getBytes(), tfmSecurityManager.getCertificate());
        String ivStringEnc = new String(Base64.encode(ivBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);
        encryptedInvoice.setIvStringEnc(ivStringEnc);

        byte[] simKeyBytesEnc = AsymmetricEncryptor.encryptData(simKey.getBytes(), tfmSecurityManager.getCertificate());
        String simKeyStringEnc = new String(Base64.encode(simKeyBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);
        encryptedInvoice.setSimKeyStringEnc(simKeyStringEnc);
        return encryptedInvoice;
    }
}
