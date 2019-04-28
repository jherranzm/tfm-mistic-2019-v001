package com.example.apptestvalidationandroid44.util;

import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.apptestvalidationandroid44.InvoiceApp;
import com.example.apptestvalidationandroid44.PostDataToUrlTask;
import com.example.apptestvalidationandroid44.config.Configuration;
import com.example.apptestvalidationandroid44.crypto.AsymmetricDecryptor;
import com.example.apptestvalidationandroid44.crypto.AsymmetricEncryptor;
import com.example.apptestvalidationandroid44.localsymkeytasks.DeleteLocalSymKeyTask;
import com.example.apptestvalidationandroid44.localsymkeytasks.GetAllLocalSymKeyTask;
import com.example.apptestvalidationandroid44.localsymkeytasks.GetByFLocalSymKeyTask;
import com.example.apptestvalidationandroid44.localsymkeytasks.InsertLocalSymKeyTask;
import com.example.apptestvalidationandroid44.model.LocalSymKey;
import com.example.apptestvalidationandroid44.remotesymkeytasks.GetByFRemoteSymKeyTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.cms.CMSException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class TFMSecurityManager {

    private static final String TAG = "TFMSecurityManager";

    private Map<String, String> simKeys = new HashMap<>();

    private X509Certificate certificate;
    private PrivateKey privateKey;

    private static TFMSecurityManager instance;

    // Pendiente de generar un Singleton
    private TFMSecurityManager(){}

    public static TFMSecurityManager getInstance(){
        if (instance == null){
            Log.i(TAG, "Inicialización de TFMSecurityManager");
            // if instance is null, initialize
            instance = new TFMSecurityManager();
            instance.manageSecurity();
        }else{
            Log.i(TAG, "TFMSecurityManager ya inicializado...");
        }
        return instance;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    private void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public Map<String, String> getSimKeys() {
        return simKeys;
    }

    public void setSimKeys(Map<String, String> simKeys) {
        this.simKeys = simKeys;
    }

    private void manageSecurity() {

        char[] keystorePassword = Configuration.PKCS12_PASSWORD.toCharArray();
        char[] keyPassword = Configuration.PKCS12_PASSWORD.toCharArray();

        // Security
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance(Configuration.X_509, Configuration.BC);

            InputStream isServerCrt = InvoiceApp.getContext().getAssets().open("server.crt"); // .getResources().openRawResource(R.raw.server);
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(isServerCrt);
            isServerCrt.close();
            this.setCertificate(certificate);



            InputStream isServerKey = InvoiceApp.getContext().getAssets().open("serverkey.p12"); //.getResources().openRawResource(R.raw.serverkey);

            KeyStore keystore = KeyStore.getInstance(Configuration.PKCS_12, Configuration.BC);
            keystore.load(isServerKey, keystorePassword);
            isServerKey.close();

            PrivateKey key = (PrivateKey) keystore.getKey("Server", keyPassword);
            if(key == null) {
                Log.e(TAG,"ERROR: NO hay clave privada!");
                throw new Exception("ERROR NO hay clave privada!");
            }
            Log.i(TAG,"Tenemos clave privada!");
            this.setPrivateKey(key);


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

            getSymmetricKeys(fields);

        }catch(Exception e){
            Log.e(TAG,e.getClass().getCanonicalName() + ": " + e.getLocalizedMessage());
            Toast.makeText(InvoiceApp.getContext(), "ERROR : "+e.getClass().getCanonicalName() + ": " + e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
        }
    }

    private void getSymmetricKeys(String[] fields) throws ExecutionException, InterruptedException, CertificateEncodingException, CMSException, IOException, JSONException {
        RandomStringGenerator rsg = new RandomStringGenerator();

        for(String str : fields){
            if(this.getSimKeys().get(str) == null){
                GetByFLocalSymKeyTask getByFLocalSymKeyTask = new GetByFLocalSymKeyTask();
                Log.i(TAG, "LocalSymKey : " + str);
                LocalSymKey lskF1 = getByFLocalSymKeyTask.execute(str).get();
                if(lskF1 == null){
                    Log.i(TAG, "LocalSymKey : [" + str + "] NO està a la base de datos local!");

                    // Recuperem la clau del servidor
                    Log.i(TAG, "Buscando [" + str + "] en la base de datos REMOTA...");
                    GetByFRemoteSymKeyTask getByFRemoteSymKeyTask = new GetByFRemoteSymKeyTask();

                    String url = Configuration.URL_KEYS + "/" + str;
                    String res = getByFRemoteSymKeyTask.execute(url).get();

                    if(res == null || res.isEmpty()){
                        createLocalSymKey(rsg, str);
                    }else{
                        Log.i(TAG, "Recibida del servidor la clave : ["+str+"]" );

                        JSONObject receivedRemoteSymKey = new JSONObject(res);

                        LocalSymKey lskReceived = new LocalSymKey();
                        lskReceived.setF(receivedRemoteSymKey.getString("f"));
                        lskReceived.setK(receivedRemoteSymKey.getString("k"));

                        Log.i(TAG, "Recibida del servidor la clave : ["+str+"]" );

                        if(lskReceived.getF().isEmpty() || lskReceived.getK().isEmpty()){
                            createLocalSymKey(rsg, str);
                        }else{
                            getLocalSymKey(str, lskReceived);
                        }

                    }

                }else{
                    Log.i(TAG, "LocalSymKey : [" + str + "] ESTÀ a la base de datos local!");
                    getLocalSymKey(str, lskF1);
                }

            }

        }
    }

    private void getLocalSymKey(String str, LocalSymKey lskF1) throws CMSException {
        Log.i(TAG, "LocalSymKey recuperada: ["+lskF1.getF()+"]");

        // Desencriptació amb clau privada de iv i simKey
        byte[] simKeyBytesDec = Base64.decode(lskF1.getK(), Base64.NO_WRAP);
        byte[] simKeyStringDec = AsymmetricDecryptor.decryptData(simKeyBytesDec, this.getPrivateKey());
        String strKey = new String(simKeyStringDec);

        Log.i(TAG, "Clave simétrica del campo ["+str+"]: [" + strKey + "]");

        this.getSimKeys().put(str, strKey);
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
        byte[] simKeyBytesEnc = AsymmetricEncryptor.encryptData(simKey.getBytes(), this.getCertificate());
        String simKeyStringEnc = new String(Base64.encode(simKeyBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);
        lsk.setK(simKeyStringEnc);

        InsertLocalSymKeyTask ilskTask = new InsertLocalSymKeyTask(lsk);
        LocalSymKey lskF = ilskTask.execute().get();
        Log.i(TAG, "LocalSymKey ingresada: " + lskF.toString());

        Map<String, String> params = new HashMap<>();
        params.put("f", str);
        params.put("k", simKeyStringEnc);

        PostDataToUrlTask getData = new PostDataToUrlTask(params);

        String res = getData.execute(Configuration.URL_KEYS).get();
        Log.i(TAG, "res : " + res);

        this.getSimKeys().put(str, simKey);
    }

    private void deleteAllLocalSymKeys() throws java.util.concurrent.ExecutionException, InterruptedException {
        GetAllLocalSymKeyTask galskTask = new GetAllLocalSymKeyTask();
        List<LocalSymKey> lskList = galskTask.execute().get();
        for(LocalSymKey aLocalSimKey : lskList){
            DeleteLocalSymKeyTask dlskTask = new DeleteLocalSymKeyTask(aLocalSimKey);
            LocalSymKey deleted = dlskTask.execute().get();
            Log.i(TAG, "Deleted: " + deleted.toString());
        }
    }


}
