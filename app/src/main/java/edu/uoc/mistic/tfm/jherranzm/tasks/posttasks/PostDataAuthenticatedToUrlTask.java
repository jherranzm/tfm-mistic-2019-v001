package edu.uoc.mistic.tfm.jherranzm.tasks.posttasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.https.UtilConnection;
import edu.uoc.mistic.tfm.jherranzm.util.TFMSecurityManager;


public class PostDataAuthenticatedToUrlTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "PostDataAuthenticatedToUrlTask";

    public PostDataAuthenticatedToUrlTask(){}

    // This is the JSON body of the post
    private JSONObject postData;

    private TFMSecurityManager tfmSecurityManager;

    private int responseCode;

    // This is a constructor that allows you to pass in the JSON body
    public PostDataAuthenticatedToUrlTask(Map<String, String> postData) {

        tfmSecurityManager = TFMSecurityManager.getInstance();

        if (postData != null) {
            this.postData = new JSONObject(postData);
        }
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            URL url;
            String server_response;

            url = new URL(params[0]);
            Log.i(TAG, "POST : " +url.toString());

            HttpsURLConnection urlConnection = UtilConnection.getHttpsURLConnection(url,
                    tfmSecurityManager.getUserLoggedDataFromKeyStore(Constants.USER_LOGGED),
                    tfmSecurityManager.getUserLoggedDataFromKeyStore(Constants.USER_PASS));

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


            responseCode = urlConnection.getResponseCode();

            Log.i(TAG, "Código de respuesta del servidor : ["+responseCode+"]");

            if(responseCode == HttpURLConnection.HTTP_OK){
                server_response = readStream(urlConnection.getInputStream());
                Log.i(TAG, "Respuesta servidor: " + server_response);
                urlConnection.disconnect();

                return server_response;
            }else if(responseCode == HttpURLConnection.HTTP_CONFLICT){
                server_response = readStream(urlConnection.getErrorStream());
                Log.e(TAG, "ERROR: Item already in system...");
                Log.e(TAG, "Respuesta servidor: " + server_response);
                urlConnection.disconnect();
                return server_response;
            }else if(responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                //server_response = readStream(urlConnection.getErrorStream());
                Log.i(TAG, "ERROR de servidor!");
                urlConnection.disconnect();
                return "{\"id\" : -1}";
            }else{
                return "{\"responseCode\" : "+ responseCode + "}";
            }

            //return null;

        }catch (CertificateException e){
            Log.e(TAG,e.getClass().getCanonicalName() + ": " + e.getLocalizedMessage());
            e.printStackTrace();
        }catch (GeneralSecurityException e){
            Log.e(TAG,e.getClass().getCanonicalName() + ": " + e.getLocalizedMessage());
            e.printStackTrace();
        } catch (MalformedURLException e) {
            Log.e(TAG,e.getClass().getCanonicalName() + ": " + e.getLocalizedMessage());
            e.printStackTrace();
        } catch (IOException e) {
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

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }


}
