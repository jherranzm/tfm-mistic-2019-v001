package edu.uoc.mistic.tfm.jherranzm.https;

import android.util.Log;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class CustomHostNameVerifier implements HostnameVerifier {

    private static final String TAG = CustomHostNameVerifier.class.getSimpleName();

    @Override
    public boolean verify(String hostname, SSLSession session) {
        Log.i(TAG, "Approving certificate for " + hostname);
        return "10.0.2.2".equals(hostname);
    }

}
