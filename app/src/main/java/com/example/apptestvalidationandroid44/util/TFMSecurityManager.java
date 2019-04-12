package com.example.apptestvalidationandroid44.util;

import android.util.Log;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class TFMSecurityManager {

    public final String PKCS_12 = "PKCS12";
    public final String PKCS12_PASSWORD = "Th2S5p2rStr4ngP1ss";

    private static final String TAG = "TFMSecurityManager";

    private Map<String, String> simKeys = new HashMap<>();


    private X509Certificate certificate;
    private PrivateKey key;

    private static TFMSecurityManager instance;

    // Pendiente de generar un Singleton
    private TFMSecurityManager(){}

    public static TFMSecurityManager getInstance(){
        if (instance == null){
            Log.i(TAG, "Inicialización de TFMSecurityManager");
            // if instance is null, initialize
            instance = new TFMSecurityManager();
        }else{
            Log.i(TAG, "TFMSecurityManager ya inicializado...");
        }
        return instance;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public PrivateKey getKey() {
        return key;
    }

    public void setKey(PrivateKey key) {
        this.key = key;
    }

    public Map<String, String> getSimKeys() {
        return simKeys;
    }

    public void setSimKeys(Map<String, String> simKeys) {
        this.simKeys = simKeys;
    }
}
