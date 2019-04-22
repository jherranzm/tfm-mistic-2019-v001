package com.example.apptestvalidationandroid44;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.apptestvalidationandroid44.config.Configuration;
import com.example.apptestvalidationandroid44.crypto.AsymmetricDecryptor;
import com.example.apptestvalidationandroid44.crypto.AsymmetricEncryptor;
import com.example.apptestvalidationandroid44.crypto.SymmetricDecryptor;
import com.example.apptestvalidationandroid44.localsymkeytasks.DeleteLocalSymKeyTask;
import com.example.apptestvalidationandroid44.localsymkeytasks.GetAllLocalSymKeyTask;
import com.example.apptestvalidationandroid44.localsymkeytasks.GetByFLocalSymKeyTask;
import com.example.apptestvalidationandroid44.localsymkeytasks.InsertLocalSymKeyTask;
import com.example.apptestvalidationandroid44.model.FileDataObject;
import com.example.apptestvalidationandroid44.model.Invoice;
import com.example.apptestvalidationandroid44.model.LocalSymKey;
import com.example.apptestvalidationandroid44.remotesymkeytasks.GetByFRemoteSymKeyTask;
import com.example.apptestvalidationandroid44.util.RandomStringGenerator;
import com.example.apptestvalidationandroid44.util.TFMSecurityManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.spongycastle.cms.CMSException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class MainActivity
        extends AppCompatActivity{

    // Constants
    private static final String TAG = "MAIN_ACTIVITY";

    public static final String INVOICE_LIST = "INVOICE_LIST";
    public static final String FILE_LIST = "FILE_LIST";

    // Widgets
    private Context mContext;
    private ProgressBar mProgressBar;

    // Security
    private TFMSecurityManager tfmSecurityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Get the application context
        mContext = getApplicationContext();
        mProgressBar = findViewById(R.id.progressBar1);
        mProgressBar.setVisibility(View.INVISIBLE);

        tfmSecurityManager = TFMSecurityManager.getInstance();
        manageSecurity();

        Button goToShowUploadedInvoices = findViewById(R.id.buttonGoToShowUploadedInvoice);
        Button goToShowLocalInvoices = findViewById(R.id.buttonShowLocalInvoices);

        // 2019-03-30
        // Check whether this app has write external storage permission or not.
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // If do not grant write external storage permission.
        if(writeExternalStoragePermission!= PackageManager.PERMISSION_GRANTED)
        {
            // Request user to grant write external storage permission.
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        goToShowUploadedInvoices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgressBar.setVisibility(View.VISIBLE);

                // Initialize a new RequestQueue instance
                RequestQueue requestQueue = Volley.newRequestQueue(mContext);

                // Initialize a new JsonArrayRequest instance
                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                        Request.Method.GET,
                        Configuration.URL,
                        null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                // Do something with response

                                Log.i(TAG, response.toString());

                                // Process the JSON
                                try{
                                    ArrayList<Invoice> invoices;

                                    invoices = getInvoicesFromResponse(response);

                                    mProgressBar.setVisibility(View.INVISIBLE);

                                    Intent intent = new Intent(mContext, UploadedInvoicesRecyclerViewActivity.class);
                                    intent.putExtra(INVOICE_LIST, invoices);
                                    startActivity(intent);

                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error){
                                // Do something when error occurred
                                Toast.makeText(mContext, "ERROR : genérico."+error.getLocalizedMessage() , Toast.LENGTH_LONG).show();
                                mProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                );

                // Add JsonArrayRequest to the RequestQueue
                requestQueue.add(jsonArrayRequest);

            }
        });

        goToShowLocalInvoices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);

                String root_sd = Environment.getExternalStorageDirectory().toString();
                File file = new File( root_sd + "/Download" ) ;
                File list[] = file.listFiles();

                ArrayList<FileDataObject> signedInvoices = new ArrayList<>();

                for (File f : list) {
                    Log.i(TAG, f.getName());
                    FileDataObject obj = new FileDataObject(f.getName());
                    signedInvoices.add(obj);
                }

                Intent intent = new Intent(mContext, LocalInvoicesRecyclerViewActivity.class);
                intent.putExtra(FILE_LIST, signedInvoices);
                startActivity(intent);

                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void manageSecurity() {
        // Security
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509", Configuration.BC);
            InputStream isServerCrt = this.getResources().openRawResource(R.raw.server);
            InputStream isServerKey = this.getResources().openRawResource(R.raw.serverkey);
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(isServerCrt);
            tfmSecurityManager.setCertificate(certificate);


            isServerCrt.close();

            char[] keystorePassword = tfmSecurityManager.PKCS12_PASSWORD.toCharArray();
            char[] keyPassword = tfmSecurityManager.PKCS12_PASSWORD.toCharArray();

            KeyStore keystore = KeyStore.getInstance(tfmSecurityManager.PKCS_12, Configuration.BC);
            keystore.load(isServerKey, keystorePassword);
            isServerKey.close();

            PrivateKey key = (PrivateKey) keystore.getKey("Server", keyPassword);
            if(key == null) {
                Log.e(getLocalClassName(),"ERROR NO hay key!");
                throw new Exception("ERROR no hay Key!");
            }
            tfmSecurityManager.setKey(key);


            deleteAllLocalSymKeys();

            String[] fields = {
                    Configuration.UID_FACTURA,
                    Configuration.TAX_IDENTIFICATION_NUMBER,
                    Configuration.CORPORATE_NAME,
                    Configuration.INVOICE_NUMBER,
                    Configuration.INVOICE_TOTAL,
                    Configuration.TOTAL_GROSS_AMOUNT,
                    Configuration.TOTAL_TAX_OUTPUTS,
                    Configuration.ISSUE_DATE
            };
            RandomStringGenerator rsg = new RandomStringGenerator();

            for(String str : fields){
                GetByFLocalSymKeyTask gbflskTask = new GetByFLocalSymKeyTask(mContext);
                Log.i(TAG, "LocalSymKey : " + str);
                LocalSymKey lskF1 = gbflskTask.execute(str).get();
                if(lskF1 == null){

                    Log.i(TAG, "LocalSymKey : [" + str + "] NO està a la base de datos local!");

                    Log.i(TAG, "Buscando [" + str + "] en la base de datos REMOTA...");
                    // Recuperem la clau del servidor
                    GetByFRemoteSymKeyTask gbfrskTask = new GetByFRemoteSymKeyTask();
                    String url = Configuration.URL_KEYS + "/" + str;
                    String res = gbfrskTask.execute(url).get();
                    if(res == null || res.isEmpty()){
                        createLocalSymKey(rsg, str);
                    }else{
                        Log.i(TAG, "Recibida del servidor la clave : ["+str+"] : " + res );

                        JSONObject receivedRemoteSymKey = new JSONObject(res);

                        LocalSymKey lskReceived = new LocalSymKey();
                        lskReceived.setF(receivedRemoteSymKey.getString("f"));
                        lskReceived.setK(receivedRemoteSymKey.getString("k"));

                        Log.i(TAG, "Recibida del servidor la clave : ["+str+"] : " + lskReceived.toString() );

                        if(lskReceived.getF().isEmpty() || lskReceived.getK().isEmpty()){
                            createLocalSymKey(rsg, str);
                        }else{
                            getLocalSymKey(str, lskReceived);
                        }

                    }

                }else{
                    getLocalSymKey(str, lskF1);
                }

            }

        } catch (CertificateEncodingException e) {
            Log.e("ERROR", ""+e.getLocalizedMessage());
            Toast.makeText(mContext, "ERROR : Certificate " + e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
        } catch (CertificateException e) {
            Log.e("ERROR", ""+e.getLocalizedMessage());
            Toast.makeText(mContext, "ERROR : Certificate " + e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
        } catch (NoSuchProviderException e) {
            Log.e("ERROR", ""+e.getLocalizedMessage());
            Toast.makeText(mContext, "ERROR : Provider " + e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
        } catch (NoSuchAlgorithmException e) {
            Log.e("ERROR", ""+e.getLocalizedMessage());
            Toast.makeText(mContext, "ERROR : Algorithm " + e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
        } catch (UnrecoverableKeyException e) {
            Log.e("ERROR", ""+e.getLocalizedMessage());
            Toast.makeText(mContext, "ERROR : UnrecoverableKey " + e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
        } catch (KeyStoreException e) {
            Log.e("ERROR", ""+e.getLocalizedMessage());
            Toast.makeText(mContext, "ERROR : KeyStore " + e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
        } catch (CMSException e) {
            Log.e("ERROR", ""+e.getLocalizedMessage());
            Toast.makeText(mContext, "ERROR : CMS " + e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("ERROR", ""+e.getLocalizedMessage());
            Toast.makeText(mContext, "ERROR : IO " + e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
        } catch (ExecutionException e) {
            Log.e("ERROR", ""+e.getLocalizedMessage());
            Toast.makeText(mContext, "ERROR : Execution " + e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
        } catch (InterruptedException e) {
            Log.e("ERROR", ""+e.getLocalizedMessage());
            Toast.makeText(mContext, "ERROR : Interrupted " + e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
        }catch(Exception e){
            Log.e("ERROR", ""+e.getLocalizedMessage());
            Toast.makeText(mContext, "ERROR : genérico."+e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
        }
    }

    private void getLocalSymKey(String str, LocalSymKey lskF1) throws CMSException {
        Log.i(TAG, "LocalSymKey recuperada: " + lskF1.toString());

        // Desencriptació amb clau privada de iv i simKey
        byte[] simKeyBytesDec = Base64.decode(lskF1.getK(), Base64.NO_WRAP);
        byte[] simKeyStringDec = AsymmetricDecryptor.decryptData(simKeyBytesDec, tfmSecurityManager.getKey());
        String strKey = new String(simKeyStringDec);

        Log.i(TAG, "CLave simétrica del campo ["+str+"]: [" + strKey + "]");

        tfmSecurityManager.getSimKeys().put(str, strKey);
    }

    private void createLocalSymKey(RandomStringGenerator rsg, String str) throws
            CertificateEncodingException,
            CMSException,
            IOException,
            java.util.concurrent.ExecutionException,
            InterruptedException {

        Log.i(TAG, "LocalSymKey NO localizada: " + str);
        LocalSymKey lsk = new LocalSymKey();
        lsk.setF(str);
        String simKey = rsg.getRandomString(16);
        byte[] simKeyBytesEnc = AsymmetricEncryptor.encryptData(simKey.getBytes(), tfmSecurityManager.getCertificate());
        String simKeyStringEnc = new String(Base64.encode(simKeyBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);
        lsk.setK(simKeyStringEnc);

        InsertLocalSymKeyTask ilskTask = new InsertLocalSymKeyTask(mContext, lsk);
        LocalSymKey lskF = ilskTask.execute().get();
        Log.i(TAG, "LocalSymKey ingresada: " + lskF.toString());

        Map<String, String> params = new HashMap<>();
        params.put("f", str);
        params.put("k", simKeyStringEnc);

        PostDataToUrlTask getData = new PostDataToUrlTask(params);

        String res = getData.execute(Configuration.URL_KEYS).get();
        Log.i(TAG, "res : " + res);

        tfmSecurityManager.getSimKeys().put(str, simKey);
    }

    private void deleteAllLocalSymKeys() throws java.util.concurrent.ExecutionException, InterruptedException {
        GetAllLocalSymKeyTask galskTask = new GetAllLocalSymKeyTask(mContext);
        List<LocalSymKey> lskList = galskTask.execute().get();
        for(LocalSymKey aLocalSimKey : lskList){
            DeleteLocalSymKeyTask dlskTask = new DeleteLocalSymKeyTask(mContext, aLocalSimKey);
            LocalSymKey deleted = dlskTask.execute().get();
            Log.i(TAG, "Deleted: " + deleted.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                byte[] ivBytesEncDec = AsymmetricDecryptor.decryptData(ivBytesDec, tfmSecurityManager.getKey());
                String ivStringDec = new String(ivBytesEncDec);

                byte[] simKeyBytesDec = Base64.decode(simKey, Base64.NO_WRAP);
                byte[] simKeyBytesEncDec = AsymmetricDecryptor.decryptData(simKeyBytesDec, tfmSecurityManager.getKey());
                String simKeyStringDec = new String(simKeyBytesEncDec);

                SymmetricDecryptor simDec = new SymmetricDecryptor();
                simDec.setIv(ivStringDec);
                simDec.setKey(simKeyStringDec);

                String taxIdentificationNumberDecrypted = simDec.decrypt(
                        taxIdentificationNumber,
                        tfmSecurityManager.getSimKeys().get(Configuration.TAX_IDENTIFICATION_NUMBER));
                String invoiceNumberDecrypted = simDec.decrypt(
                        invoiceNumber,
                        tfmSecurityManager.getSimKeys().get(Configuration.INVOICE_NUMBER));
                String issueDateDecrypted = simDec.decrypt(
                        issueDate,
                        tfmSecurityManager.getSimKeys().get(Configuration.ISSUE_DATE));

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
            e.printStackTrace();
        }

        return invoices;

    }

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        // Works correctly with apache.santuario v1.5.8
        org.apache.xml.security.Init.init();
    }
}
