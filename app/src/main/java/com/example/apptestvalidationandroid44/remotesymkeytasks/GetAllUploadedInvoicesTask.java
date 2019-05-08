package com.example.apptestvalidationandroid44.remotesymkeytasks;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.example.apptestvalidationandroid44.config.Constants;
import com.example.apptestvalidationandroid44.crypto.AsymmetricDecryptor;
import com.example.apptestvalidationandroid44.crypto.SymmetricDecryptor;
import com.example.apptestvalidationandroid44.https.UtilConnection;
import com.example.apptestvalidationandroid44.model.Invoice;
import com.example.apptestvalidationandroid44.util.TFMSecurityManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class GetAllUploadedInvoicesTask extends AsyncTask<String, Void, List<Invoice>> {

    private static final String TAG = "GetAllUploadedInvoicesTask";

    private TFMSecurityManager tfmSecurityManager;

    public GetAllUploadedInvoicesTask(){
        tfmSecurityManager = TFMSecurityManager.getInstance();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Invoice> doInBackground(String... params) {
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

                JSONArray array = new JSONArray(server_response);

                ArrayList<Invoice> invoices;

                invoices = getInvoicesFromResponse(array);
                Log.i(TAG,"invoices: " + invoices.size());
                return invoices;
            }else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                return new ArrayList<>();
            }else{
                return new ArrayList<>();
            }

        }catch (JSONException e){
            Log.e(TAG,e.getClass().getCanonicalName() + ": " + e.getLocalizedMessage());
            e.printStackTrace();
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
    protected void onPostExecute(List<Invoice> aVoid) {
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


    private ArrayList<Invoice> getInvoicesFromResponse(JSONArray response){
        ArrayList<Invoice> invoices = new ArrayList<>();

        try{
            // Loop through the array elements
            for(int i=0;i<response.length();i++){
                // Get current json object
                JSONObject factura = response.getJSONObject(i);

                // Get the current factura (json object) data
                String uid = factura.getString("uid");
                String taxIdentificationNumber = factura.getString("taxIdentificationNumber");
                String invoiceNumber = factura.getString("invoiceNumber");
                String issueDate = factura.getString("issueDate");
                double invoiceTotal = factura.getDouble("invoiceTotal");
                double totalTaxOutputs = factura.getDouble("totalTaxOutputs");

                String iv = factura.getString("iv");
                String simKey = factura.getString("simKey");

                // Desencriptació amb clau privada de iv i simKey
                byte[] ivBytesDec = Base64.decode(iv, Base64.NO_WRAP);
                byte[] ivBytesEncDec = AsymmetricDecryptor.decryptData(ivBytesDec, tfmSecurityManager.getPrivateKey());
                String ivStringDec = new String(ivBytesEncDec);

                byte[] simKeyBytesDec = Base64.decode(simKey, Base64.NO_WRAP);
                byte[] simKeyBytesEncDec = AsymmetricDecryptor.decryptData(simKeyBytesDec, tfmSecurityManager.getPrivateKey());
                String simKeyStringDec = new String(simKeyBytesEncDec);

                SymmetricDecryptor simDec = new SymmetricDecryptor();
                simDec.setIv(ivStringDec);
                simDec.setKey(simKeyStringDec);

                String taxIdentificationNumberDecrypted = simDec.decrypt(
                        taxIdentificationNumber,
                        tfmSecurityManager.getSimKeys().get(Constants.TAX_IDENTIFICATION_NUMBER));
                String invoiceNumberDecrypted = simDec.decrypt(
                        invoiceNumber,
                        tfmSecurityManager.getSimKeys().get(Constants.INVOICE_NUMBER));
                String issueDateDecrypted = simDec.decrypt(
                        issueDate,
                        tfmSecurityManager.getSimKeys().get(Constants.ISSUE_DATE));

                Invoice invoice = new Invoice(uid
                        , taxIdentificationNumberDecrypted
                        , ""
                        , invoiceNumberDecrypted
                        , invoiceTotal
                        , totalTaxOutputs
                        , new SimpleDateFormat("yyyy-MM-dd", new Locale("ES-es")).parse(issueDateDecrypted)
                );

                Log.i(TAG, "invoice : ["+invoice.toString()+"]");
                invoices.add(invoice);

            }

        }catch (Exception e){
            Log.e(TAG,e.getClass().getCanonicalName() + ": " + e.getLocalizedMessage());
            e.printStackTrace();
        }

        return invoices;

    }
}
