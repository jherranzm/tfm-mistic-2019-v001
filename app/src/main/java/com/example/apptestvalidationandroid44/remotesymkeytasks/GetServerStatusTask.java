package com.example.apptestvalidationandroid44.remotesymkeytasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.apptestvalidationandroid44.InvoiceApp;
import com.example.apptestvalidationandroid44.https.CustomSSLSocketFactory;
import com.example.apptestvalidationandroid44.https.NullHostNameVerifier;
import com.example.apptestvalidationandroid44.util.TFMSecurityManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class GetServerStatusTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "GetServerStatusTask";

    private TFMSecurityManager tfmSecurityManager;

    public GetServerStatusTask(){
        tfmSecurityManager = TFMSecurityManager.getInstance();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            URL url;
            String server_response;

            url = new URL(params[0]);
            Log.i(TAG, "URL:" + url.toString());

            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
            urlConnection.setRequestMethod("GET");
            urlConnection.setSSLSocketFactory(CustomSSLSocketFactory.getSSLSocketFactory(InvoiceApp.getContext()));
            urlConnection.setConnectTimeout(20000);
            urlConnection.setReadTimeout(20000);

            int responseCode = urlConnection.getResponseCode();
            Log.i(TAG, "responseCode:" + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                server_response = readStream(urlConnection.getInputStream());
                Log.i(TAG,"Respuesta servidor: " + server_response);

                return server_response;
            }else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                return new String();
            }else{
                return "ERROR";
            }

        }catch (Exception e) {
            Log.e(TAG,e.getClass().getCanonicalName() + ": " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String aVoid) {
        super.onPostExecute(aVoid);

    }

    // Converting InputStream to String
    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response;
        response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }
}
