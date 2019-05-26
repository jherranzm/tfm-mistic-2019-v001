package edu.uoc.mistic.tfm.jherranzm.https;

import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import edu.uoc.mistic.tfm.jherranzm.util.TFMSecurityManager;

public class CustomSSLSocketFactory {

    private CustomSSLSocketFactory() {
        super();
    }

    private static SSLSocketFactory sslSocketFactory;

    public static SSLSocketFactory getSSLSocketFactory()
            throws GeneralSecurityException {

        TFMSecurityManager tfmSecurityManager = TFMSecurityManager.getInstance();

        //sólo se instancia la primera vez que se necesite
        if (sslSocketFactory == null) {

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tfmSecurityManager.getTmf().getTrustManagers(), null);

            sslSocketFactory = sslContext.getSocketFactory();
        }
        return sslSocketFactory;
    }
}
