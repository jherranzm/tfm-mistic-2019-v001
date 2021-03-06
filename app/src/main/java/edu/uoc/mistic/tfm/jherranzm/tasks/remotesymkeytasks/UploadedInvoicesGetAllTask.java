package edu.uoc.mistic.tfm.jherranzm.tasks.remotesymkeytasks;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.crypto.AsymmetricDecryptor;
import edu.uoc.mistic.tfm.jherranzm.crypto.SymmetricDecryptor;
import edu.uoc.mistic.tfm.jherranzm.https.UtilConnection;
import edu.uoc.mistic.tfm.jherranzm.model.Invoice;
import edu.uoc.mistic.tfm.jherranzm.util.TFMSecurityManager;

public class UploadedInvoicesGetAllTask extends AsyncTask<String, Void, List<Invoice>> {

    private static final String TAG = UploadedInvoicesGetAllTask.class.getSimpleName();

    private final TFMSecurityManager tfmSecurityManager;

    public UploadedInvoicesGetAllTask(){
        tfmSecurityManager = TFMSecurityManager.getInstance();
    }

    @Override
    protected List<Invoice> doInBackground(String... params) {
        try {
            URL url;
            String server_response;

            url = new URL(params[0]);
            Log.i(TAG, "URL :" + url.toString());

            HttpsURLConnection urlConnection = UtilConnection.getHttpsURLConnection(url,
                    tfmSecurityManager.getUserLoggedDataFromKeyStore(Constants.USER_LOGGED),
                    tfmSecurityManager.getUserLoggedDataFromKeyStore(Constants.USER_PASS));
            urlConnection.setRequestMethod("GET");

            int responseCode = urlConnection.getResponseCode();
            Log.i(TAG, String.format("responseCode:%d", responseCode));

            if (responseCode == HttpURLConnection.HTTP_OK) {
                server_response = readStream(urlConnection.getInputStream());
                Log.i(TAG, String.format("Respuesta servidor: %s", server_response));

                JSONArray array = new JSONArray(server_response);

                ArrayList<Invoice> invoices;

                invoices = getInvoicesFromResponse(array);
                Log.i(TAG, String.format("invoices: %d", invoices.size()));
                return invoices;
            }else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                return new ArrayList<>();
            }else{
                return new ArrayList<>();
            }

        }catch (Exception e) {
            Log.e(TAG, String.format("ERROR : Class - %s:  Message - %s", e.getClass().getCanonicalName(), e.getLocalizedMessage()));
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
                String corporateName = factura.getString("corporateName");
                String invoiceNumber = factura.getString("invoiceNumber");
                String issueDate = factura.getString("issueDate");

                String invoiceTotal = factura.getString("invoiceTotal");
                String totalTaxOutputs = factura.getString("totalTaxOutputs");

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
                        tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.TAX_IDENTIFICATION_NUMBER)
                );

                String corporateNameDecrypted = simDec.decrypt(
                        corporateName,
                        tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.CORPORATE_NAME)
                );
                String invoiceNumberDecrypted = simDec.decrypt(
                        invoiceNumber,
                        tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.INVOICE_NUMBER)
                );
                String invoiceTotalDecrypted = simDec.decrypt(
                        invoiceTotal,
                        tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.INVOICE_TOTAL)
                );
                String totalTaxOutputsDecrypted = simDec.decrypt(
                        totalTaxOutputs,
                        tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.TOTAL_TAX_OUTPUTS)
                );

                String issueDateDecrypted = simDec.decrypt(
                        issueDate,
                        tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.ISSUE_DATE)
                );

                Invoice invoice = new Invoice(uid
                        , taxIdentificationNumberDecrypted
                        , corporateNameDecrypted
                        , invoiceNumberDecrypted
                        , Double.parseDouble(invoiceTotalDecrypted)// invoiceTotal
                        , Double.parseDouble(totalTaxOutputsDecrypted)// totalTaxOutputs
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
