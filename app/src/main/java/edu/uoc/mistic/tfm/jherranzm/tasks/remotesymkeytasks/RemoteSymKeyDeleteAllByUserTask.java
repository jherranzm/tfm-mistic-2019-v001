package edu.uoc.mistic.tfm.jherranzm.tasks.remotesymkeytasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.https.UtilConnection;
import edu.uoc.mistic.tfm.jherranzm.util.TFMSecurityManager;
import edu.uoc.mistic.tfm.jherranzm.util.UtilStreamReader;

public class RemoteSymKeyDeleteAllByUserTask extends AsyncTask<String, Void, String> {

    private static final String TAG = RemoteSymKeyDeleteAllByUserTask.class.getSimpleName();

    private final TFMSecurityManager tfmSecurityManager;

    public RemoteSymKeyDeleteAllByUserTask(){
        tfmSecurityManager = TFMSecurityManager.getInstance();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            URL url;
            String server_response;

            url = new URL(params[0]);

            Log.i(TAG, url.toString());
            Log.i(TAG, String.format("tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.USER_LOGGED) = [%s]", tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.USER_LOGGED)));
            Log.i(TAG, String.format("tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.USER_PASS) = [%s]", tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.USER_PASS)));

            HttpsURLConnection urlConnection = UtilConnection.getHttpsURLConnection(url,
                    tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.USER_LOGGED),
                    tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.USER_PASS));
            urlConnection.setRequestMethod("DELETE");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream serverInputStream = urlConnection.getInputStream();
                server_response = UtilStreamReader.readStream(serverInputStream);
                Log.i(TAG,"Server response: " + server_response);
                serverInputStream.close();
                urlConnection.disconnect();
                return server_response;
            }else{
                InputStream serverInputStream = urlConnection.getInputStream();
                server_response = UtilStreamReader.readStream(serverInputStream);
                Log.i(TAG,"Server response: " + server_response);
                serverInputStream.close();
                urlConnection.disconnect();
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
}
