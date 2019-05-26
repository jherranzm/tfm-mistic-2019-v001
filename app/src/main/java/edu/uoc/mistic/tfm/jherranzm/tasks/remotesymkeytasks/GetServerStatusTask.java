package edu.uoc.mistic.tfm.jherranzm.tasks.remotesymkeytasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import edu.uoc.mistic.tfm.jherranzm.https.UtilConnection;

public class GetServerStatusTask extends AsyncTask<String, Void, String> {

    private static final String TAG = GetServerStatusTask.class.getSimpleName();

    public GetServerStatusTask(){
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            URL url;
            String server_response;

            url = new URL(params[0]);
            Log.i(TAG, "URL_FACTURAS:" + url.toString());

            HttpsURLConnection urlConnection = UtilConnection.getHttpsURLConnection(url);
            urlConnection.setRequestMethod("GET");

            int responseCode = urlConnection.getResponseCode();
            Log.i(TAG, "responseCode:" + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                server_response = readStream(urlConnection.getInputStream());
                Log.i(TAG,"Respuesta servidor: " + server_response);

                return server_response;
            }else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                return "";
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
