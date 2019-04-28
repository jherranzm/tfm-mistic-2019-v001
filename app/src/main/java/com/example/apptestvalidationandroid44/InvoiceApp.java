package com.example.apptestvalidationandroid44;

import android.app.Application;
import android.content.Context;

import com.example.apptestvalidationandroid44.util.TFMSecurityManager;

import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.pkcs.PKCS10CertificationRequest;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class InvoiceApp extends Application {

    private static Context mContext;

    public static File getAppDir() {
        return mContext.getFilesDir();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        TFMSecurityManager tfmSecurityManager = TFMSecurityManager.getInstance();

        try {
            //Generate KeyPair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();

            //Generate CSR in PKCS#10 format encoded in DER
            PKCS10CertificationRequest csr = CsrHelper.generateCSR(keyPair, "MISTIC_INVOICE");
            byte  CSRder[] = csr.getEncoded();
        } catch (OperatorCreationException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Context getContext(){
        return mContext;
    }
}