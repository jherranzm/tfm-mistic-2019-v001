package edu.uoc.mistic.tfm.jherranzm.tasks.remotesymkeytasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import edu.uoc.mistic.tfm.jherranzm.https.UtilConnection;
import edu.uoc.mistic.tfm.jherranzm.util.UtilStreamReader;

public class GetCertificateFromServerTask extends AsyncTask<String, Void, String> {

    private static final String TAG = GetCertificateFromServerTask.class.getSimpleName();

    public GetCertificateFromServerTask(Map<String, String> postData){
        if (postData != null) {
            this.postData = new JSONObject(postData);
        }
    }

    // This is the JSON body of the post
    private JSONObject postData;

    @Override
    protected String doInBackground(String... params) {
        try {
            URL url;
            String server_response;

            url = new URL(params[0]);
            Log.i(TAG, url.toString());

            HttpsURLConnection urlConnection = UtilConnection.getHttpsURLConnection(url);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestMethod("POST");

            // Send the post body
            if (this.postData != null) {
                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(postData.toString());
                writer.flush();
                writer.close();
                Log.i(TAG, "POST DATA : " + postData.toString());
            }


            int responseCode = urlConnection.getResponseCode();

            Log.i(TAG, "Server response code: ["+responseCode+"]");

            if(responseCode == HttpURLConnection.HTTP_OK){
                InputStream serverInputStream = urlConnection.getInputStream();
                server_response = UtilStreamReader.readStream(serverInputStream);
                Log.i(TAG,"Server response: " + server_response);
                serverInputStream.close();
                urlConnection.disconnect();
                return server_response;
            }else{
                //server_response = readStream(urlConnection.getErrorStream());
                Log.i(TAG, "ERROR. Server error.");
                urlConnection.disconnect();
                return "{\"id\" : -1}";
            }

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
