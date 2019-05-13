package com.example.apptestvalidationandroid44.https;

import com.example.apptestvalidationandroid44.InvoiceApp;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

public class UtilConnection {

    public static HttpsURLConnection getHttpsURLConnection(URL url) throws IOException, GeneralSecurityException {

        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        HttpsURLConnection.setDefaultHostnameVerifier(new CustomHostNameVerifier());

        urlConnection.setSSLSocketFactory(CustomSSLSocketFactory.getSSLSocketFactory(InvoiceApp.getContext()));
        urlConnection.setConnectTimeout(20000);
        urlConnection.setReadTimeout(20000);

        String userpass = "user" + ":" + "user";
        String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
        urlConnection.setRequestProperty ("Authorization", basicAuth);
        return urlConnection;
    }

    public static HttpsURLConnection getHttpsURLConnection(URL url, String user, String pass) throws IOException, GeneralSecurityException {

        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        HttpsURLConnection.setDefaultHostnameVerifier(new CustomHostNameVerifier());

        urlConnection.setSSLSocketFactory(CustomSSLSocketFactory.getSSLSocketFactory(InvoiceApp.getContext()));
        urlConnection.setConnectTimeout(20000);
        urlConnection.setReadTimeout(20000);

        String userpass = user + ":" + pass;
        String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
        urlConnection.setRequestProperty ("Authorization", basicAuth);
        return urlConnection;
    }
}
