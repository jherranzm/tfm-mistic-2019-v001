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

import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.https.UtilConnection;
import edu.uoc.mistic.tfm.jherranzm.util.TFMSecurityManager;

public class GetByFRemoteSymKeyTask  extends AsyncTask<String, Void, String> {

    private static final String TAG = "GetByFRemoteSymKeyTask";

    private TFMSecurityManager tfmSecurityManager;

    public GetByFRemoteSymKeyTask(){ tfmSecurityManager = TFMSecurityManager.getInstance(); }

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
            Log.i(TAG, url.toString());

            HttpsURLConnection urlConnection = UtilConnection.getHttpsURLConnection(url,
                    tfmSecurityManager.getUserLoggedDataFromKeyStore(Constants.USER_LOGGED),
                    tfmSecurityManager.getUserLoggedDataFromKeyStore(Constants.USER_PASS));
            urlConnection.setRequestMethod("GET");

            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                server_response = readStream(urlConnection.getInputStream());
                Log.i(TAG,"Respuesta servidor: " + server_response);
                return server_response;
            }

            return null;

        } catch (Exception e) {
            Log.e(TAG,e.getClass().getCanonicalName() + ": " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String aVoid) {
        super.onPostExecute(aVoid);

    }


    /**
     *
     * Converting InputStream to String
     *
     * @param in
     * @return
     */
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
