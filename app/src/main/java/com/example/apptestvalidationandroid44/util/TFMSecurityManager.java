package com.example.apptestvalidationandroid44.util;

import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.apptestvalidationandroid44.CsrHelper;
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
import com.example.apptestvalidationandroid44.remotesymkeytasks.GetCertificateFromServerTask;
import com.example.apptestvalidationandroid44.remotesymkeytasks.GetServerStatusTask;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.cms.CMSException;
import org.spongycastle.openssl.jcajce.JcaPEMWriter;
import org.spongycastle.pkcs.PKCS10CertificationRequest;
import org.spongycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class TFMSecurityManager {

    private static final String TAG = "TFMSecurityManager";

    private Map<String, String> simKeys = new HashMap<>();

    private X509Certificate certificate;
    private PrivateKey privateKey;
    private PrivateKeyEntry pke;
    private TrustManagerFactory tmf;

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

    private void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    private void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public PrivateKeyEntry getPke() { return pke; }

    public void setPke(PrivateKeyEntry pke) { this.pke = pke; }

    public TrustManagerFactory getTmf() {
        return tmf;
    }

    public Map<String, String> getSimKeys() {
        return simKeys;
    }

    private void manageSecurity() {

        char[] keystorePassword = Configuration.PKCS12_PASSWORD.toCharArray();
        char[] keyPassword = Configuration.PKCS12_PASSWORD.toCharArray();
        //X509Certificate[] certificateChain = new X509Certificate[2];


        // Security
        try {


            CertificateFactory certFactory = CertificateFactory.getInstance(Configuration.X_509, Configuration.BC);

            KeyStore keyStore = KeyStore.getInstance(Configuration.PKCS_12, Configuration.BC);
            KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(keystorePassword);

            File keyStoreFile = new File(InvoiceApp.getAppDir(), "keyStoreInvoiceApp");
            if(keyStoreFile.exists()){
                Log.i(TAG,"KeyStore file already exists in : " + keyStoreFile.getAbsolutePath());
                keyStore.load(new FileInputStream(keyStoreFile), keystorePassword);

                showAliasesInKeyStore(keyStore);

                // Retrieve CA Certificate from KeyStore
                X509Certificate caCertificate = (X509Certificate) keyStore.getCertificate("CA TFM");
                if(caCertificate == null){
                    throw new Exception("ERROR : NO CA Certificate in KeyStore!");
                }
                //certificateChain[1] = caCertificate;
                Log.i(TAG,"Retrieved CA Certificate from KeyStore! SubjectDN : " + caCertificate.getSubjectDN().getName());

                // Retrieve Server Certificate from KeyStore
                X509Certificate serverCertificate = (X509Certificate) keyStore.getCertificate("Server");
                if(serverCertificate == null){
                    throw new Exception("ERROR : NO Server Certificate in KeyStore!");
                }
                //certificateChain[0] = serverCertificate;
                Log.i(TAG,"Retrieved Server Certificate from KeyStore! SubjectDN : " + serverCertificate.getSubjectDN().getName());

                // Certificate with PublicKey to encrypt or decrypt
                this.setCertificate(serverCertificate);

            }else{
                Log.i(TAG,"KeyStore: creating file in  : " + keyStoreFile.getAbsolutePath());
                if(!keyStoreFile.createNewFile()){
                    throw new Exception("ERROR : Can NOT create KeyStore!");
                }

                keyStore.load(null, null);
                showAliasesInKeyStore(keyStore);

                // Load CA Certificate from assets
                Log.i(TAG,"Retrieving CA Certificate from file in assets...");
                InputStream isCACrt = InvoiceApp.getContext().getAssets().open(Configuration.CA_CERTIFICATE_FILE); // .getResources().openRawResource(R.raw.server);
                X509Certificate caCertificate = (X509Certificate) certFactory.generateCertificate(isCACrt);
                isCACrt.close();
                if(caCertificate == null){
                    throw new Exception("ERROR : NO CA Certificate in "+Configuration.CA_CERTIFICATE_FILE+"!");
                }
                Log.i(TAG,"Retrieved CA Certificate from file in assets! SubjectDN : " + caCertificate.getSubjectDN().getName());
                //certificateChain[1] = caCertificate;
                keyStore.setCertificateEntry("ca", caCertificate);
                showAliasesInKeyStore(keyStore);

                String caCertificateAlias = keyStore.getCertificateAlias(caCertificate);
                if(caCertificateAlias == null){
                    throw new Exception("ERROR : NO CA Certificate in KeyStore!");
                }
                Log.i(TAG,"caCertificateAlias : " + caCertificateAlias);

                // Load Server Certificate from assets
                Log.i(TAG,"Retrieving Server Certificate from file in assets...");
                InputStream isServerCrt = InvoiceApp.getContext().getAssets().open(Configuration.SERVER_CERTIFICATE_FILE); // .getResources().openRawResource(R.raw.server);
                X509Certificate serverCertificate = (X509Certificate) certFactory.generateCertificate(isServerCrt);
                isServerCrt.close();
                if(serverCertificate == null){
                    throw new Exception("ERROR : NO Server Certificate in "+Configuration.SERVER_CERTIFICATE_FILE+"!");
                }
                Log.i(TAG,"Retrieved Server Certificate from file in assets! SubjectDN : " + serverCertificate.getSubjectDN().getName());
                //certificateChain[0] = serverCertificate;
                keyStore.setCertificateEntry("Server", serverCertificate);
                showAliasesInKeyStore(keyStore);

                String serverCertificateAlias = keyStore.getCertificateAlias(serverCertificate);
                if(serverCertificateAlias == null){
                    throw new Exception("ERROR : NO Server Certificate in KeyStore!");
                }
                Log.i(TAG,"serverCertificateAlias : " + serverCertificateAlias);

                // Load Server Private Key from assets : needed to encrypt, will be changed for user private key
                Log.i(TAG,"Loading Server Private Key from assets...");
                InputStream isServerKey = InvoiceApp.getContext().getAssets().open(Configuration.SERVER_KEY_P12); //.getResources().openRawResource(R.raw.serverkey);
                keyStore.load(isServerKey, keystorePassword);
                isServerKey.close();
                showAliasesInKeyStore(keyStore);

                // Certificate with PublicKey to encrypt or decrypt
                this.setCertificate(serverCertificate);
            }

            saveKeyStoreToFileSystem(keystorePassword, keyStore, keyStoreFile);

            // Retrieve Server Certificate from KeyStore
            X509Certificate serverCertificate = (X509Certificate) keyStore.getCertificate("Server");
            if(serverCertificate == null){
                throw new Exception("ERROR : NO Server Certificate in KeyStore!");
            }
            //X509Certificate caCertificate = (X509Certificate) keyStore.getCertificate("ca");
            X509Certificate caCertificate = (X509Certificate) keyStore.getCertificate("CA TFM");
            if(caCertificate == null){
                throw new Exception("ERROR : NO CA Certificate in KeyStore!");
            }


            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
            Log.i(TAG,"TrustManager : Algoritmo : " + tmfAlgorithm);
            Log.i(TAG,"TrustManager : numItems : " + tmf.getTrustManagers().length);
            for(TrustManager tm : tmf.getTrustManagers()){
                Log.i(TAG,"TrustManager  Item : " + tm.toString());
            }


            this.pke = (PrivateKeyEntry) keyStore.getEntry("Server", protParam);


            PrivateKey key = (PrivateKey) keyStore.getKey("Server", keyPassword);
            if(key == null) {
                Log.e(TAG,"ERROR: NO hay clave privada!");
                throw new Exception("ERROR NO hay clave privada!");
            }
            Log.i(TAG,"Tenemos clave privada!");
            this.setPrivateKey(key);

            // First connection to server
            String status = "";
            while (!"ACTIVE".equals(status)) {
                GetServerStatusTask getServerStatusTask = new GetServerStatusTask();
                status = getServerStatusTask.execute(Configuration.URL_STATUS).get();
                Log.i(TAG, "Server status : " + status);
            }


            X509Certificate userCertificate = (X509Certificate) keyStore.getCertificate("UsuarioApp");
            //if(keyStore.containsAlias("UsuarioApp")){
            if(userCertificate == null){
                //Generate KeyPair
                Log.i(TAG,"KeyPair : generating...");
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(4096, new SecureRandom());
                KeyPair keyPair = keyGen.generateKeyPair();
                Log.i(TAG,"KeyPair : generated!");
                Log.i(TAG, "KeyPair.getPrivate() : " + keyPair.getPrivate().toString());

                //Generate CSR in PKCS#10 format encoded in DER
                PKCS10CertificationRequest csr = CsrHelper.generateCSR(keyPair, "UsuarioApp");

                JcaPKCS10CertificationRequest req2 = new JcaPKCS10CertificationRequest(csr.getEncoded()).setProvider("SC");

                StringWriter sw = new StringWriter();
                JcaPEMWriter pemWriter = new JcaPEMWriter(sw);
                pemWriter.writeObject(req2);
                pemWriter.close();

                Log.i(TAG, "Request : " + sw.toString());


                Map<String, String> params = new HashMap<>();
                params.put("csr", sw.toString());
                GetCertificateFromServerTask getCertificateFromServerTask = new GetCertificateFromServerTask(params);
                String response_server = getCertificateFromServerTask.execute(Configuration.URL_CERT).get();
                String decoded = new String(java.util.Base64.getDecoder().decode(response_server.getBytes()));
                Log.i(TAG, "CSRder.Response : " + decoded);

                InputStream isReceivedCertificate = IOUtils.toInputStream(decoded, "UTF-8");
                X509Certificate receivedCertificate = (X509Certificate) certFactory.generateCertificate(isReceivedCertificate);
                isReceivedCertificate.close();
                Log.i(TAG, "receivedCertificate Subject : " + receivedCertificate.getSubjectDN().getName());
                Log.i(TAG, "receivedCertificate Issuer  : " + receivedCertificate.getIssuerDN().getName());

                keyStore.setCertificateEntry("UsuarioApp", receivedCertificate);

                userCertificate = (X509Certificate) keyStore.getCertificate("UsuarioApp");

                //
                Log.i(TAG, "privateKey : saving...");
                keyStore.setKeyEntry("UsuarioApp",
                        keyPair.getPrivate(),
                        keystorePassword,
                        new java.security.cert.Certificate[]{receivedCertificate});
                Log.i(TAG, "privateKey : saved!");


                saveKeyStoreToFileSystem(keystorePassword, keyStore, keyStoreFile);

            }else{
                Log.i(TAG,"Tenemos el certificado de usuario!");
                //X509Certificate certificate = (X509Certificate) keyStore.getCertificate("UsuarioApp");
                Log.i(TAG, "User Certificate Subject : " + userCertificate.getSubjectDN().getName());
                Log.i(TAG, "User Certificate Issuer  : " + userCertificate.getIssuerDN().getName());
                Log.i(TAG, "");
            }

            KeyStore.PrivateKeyEntry retrievedPrivateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry("UsuarioApp", protParam);
            PrivateKey retrievedPrivateKey = retrievedPrivateKeyEntry.getPrivateKey();

            this.setCertificate(userCertificate);
            this.setPrivateKey(retrievedPrivateKey);


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

    private void saveKeyStoreToFileSystem(char[] keystorePassword, KeyStore keyStore, File keyStoreFile)
            throws
            CertificateException,
            IOException,
            KeyStoreException,
            NoSuchAlgorithmException {
        Log.i(TAG, "KeyStore: Guardando...");
        FileOutputStream out = new FileOutputStream(keyStoreFile);
        keyStore.store(out, keystorePassword);
        out.close();
        Log.i(TAG, "KeyStore: Guardado!");
    }

    private void showAliasesInKeyStore(KeyStore keyStore)
            throws
            KeyStoreException {
        Enumeration<String> aliases = keyStore.aliases();
        Log.i(TAG,"Showing aliases in KeyStore...");
        while(aliases.hasMoreElements()){
            Log.i(TAG,"alias : " + aliases.nextElement());
        }
    }

    private void getSymmetricKeys(String[] fields)
            throws
            ExecutionException,
            InterruptedException,
            CertificateEncodingException,
            CMSException,
            IOException,
            JSONException {
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

    private void getLocalSymKey(String str, LocalSymKey lskF1)
            throws
            CMSException {
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

    private void deleteAllLocalSymKeys()
            throws
            java.util.concurrent.ExecutionException,
            InterruptedException {
        GetAllLocalSymKeyTask galskTask = new GetAllLocalSymKeyTask();
        List<LocalSymKey> lskList = galskTask.execute().get();
        for(LocalSymKey aLocalSimKey : lskList){
            DeleteLocalSymKeyTask dlskTask = new DeleteLocalSymKeyTask(aLocalSimKey);
            LocalSymKey deleted = dlskTask.execute().get();
            Log.i(TAG, "Deleted: " + deleted.toString());
        }
    }


}
