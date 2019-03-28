package com.example.apptestvalidationandroid44;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class GetDataFromUrlTask extends AsyncTask<String, Void, String> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //progressDialog = new ProgressDialog(MainActivity.this);
        //progressDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        String title = "";
        try {

            //String url = "https://www.google.com/";

            //Connect to the website
            //Document document = Jsoup.connect(url[0]).get();

            //Get the logo source of the website
            //Element img = document.select("img").first();
            // Locate the src attribute
            //String imgSrc = img.absUrl("src");
            // Download image from URL
            //InputStream input = new java.net.URL(imgSrc).openStream();
            // Decode Bitmap
            //Bitmap bitmap = BitmapFactory.decodeStream(input);

            //Get the title of the website
            //title = document.title();

            URL url;
            HttpURLConnection urlConnection = null;
            String server_response;

            url = new URL(params[0]);
            Log.v("url: ", url.toString());
            urlConnection = (HttpURLConnection) url.openConnection();

            int responseCode = urlConnection.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_OK){
                server_response = readStream(urlConnection.getInputStream());
                Log.v("Respuesta servidor: ", server_response);
                return server_response;
            }


            return null;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
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
