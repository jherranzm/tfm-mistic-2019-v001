package com.example.apptestvalidationandroid44.https;

import android.util.Log;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class CustomHostNameVerifier implements HostnameVerifier {

    public static final String TAG = "CustomHostNameVerifier";

    @Override
    public boolean verify(String hostname, SSLSession session) {
        Log.i(TAG, "Approving certificate for " + hostname);
        return "10.0.2.2".equals(hostname);
    }

}
