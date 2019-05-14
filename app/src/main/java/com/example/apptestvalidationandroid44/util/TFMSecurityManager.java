package com.example.apptestvalidationandroid44.util;

import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.apptestvalidationandroid44.CsrHelper;
import com.example.apptestvalidationandroid44.InvoiceApp;
import com.example.apptestvalidationandroid44.PostDataToUrlTask;
import com.example.apptestvalidationandroid44.config.Constants;
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
import org.spongycastle.operator.OperatorCreationException;
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
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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
    private KeyStore keyStore;

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


    /**
     *
     */
    private void manageSecurity() {

        char[] keystorePassword = Constants.PKCS12_PASSWORD.toCharArray();

        // Security
        try {

            CertificateFactory certFactory = CertificateFactory.getInstance(Constants.X_509, Constants.BC);
            Log.i(TAG, "KeyStore.getDefaultType() : " + KeyStore.getDefaultType());

            // Keystore creation
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            Log.i(TAG, "KeyStore.getType() : " + keyStore.getType());


            // Load Keystore if exists...
            File keyStoreFile = new File(InvoiceApp.getAppDir(), "keyStoreInvoiceApp.bks");
            if(keyStoreFile.exists()){
                Log.i(TAG,"KeyStore file already exists in : " + keyStoreFile.getAbsolutePath());
                keyStore.load(new FileInputStream(keyStoreFile), keystorePassword);

                showAliasesInKeyStore(keyStore);

            }else{

                // First time initialization...
                Log.i(TAG,"KeyStore: creating file in  : " + keyStoreFile.getAbsolutePath());
                if(!keyStoreFile.createNewFile()){
                    throw new Exception("ERROR : Can NOT create KeyStore!");
                }

                keyStore.load(null, null);
                showAliasesInKeyStore(keyStore);

                loadCertificateFromAssets(certFactory, Constants.CA_CERTIFICATE_FILE, "ca");
                loadCertificateFromAssets(certFactory, Constants.SERVER_CERTIFICATE_FILE, "Server");

                // Load Server Private Key from assets : needed to encrypt, will be changed for user private key
                if(!keyStore.getType().equals("BKS")) {
                    Log.i(TAG, "Loading Server Private Key from assets...");
                    InputStream isServerKey = InvoiceApp.getContext().getAssets().open(Constants.SERVER_KEY_P12); //.getResources().openRawResource(R.raw.serverkey);
                    keyStore.load(isServerKey, keystorePassword);
                    isServerKey.close();
                    showAliasesInKeyStore(keyStore);
                }
            }

            saveKeyStoreToFileSystem(keystorePassword, keyStore, keyStoreFile);

            // Retrieve Server Certificate from KeyStore
            X509Certificate serverCertificate = (X509Certificate) keyStore.getCertificate("Server");
            if(serverCertificate == null){
                throw new Exception("ERROR : NO Server Certificate in KeyStore!");
            }

            // Retrieve CA Certificate from KeyStore
            X509Certificate caCertificate = (X509Certificate) keyStore.getCertificate("ca");
            if(caCertificate == null){
                caCertificate = (X509Certificate) keyStore.getCertificate("CA TFM");
                if(caCertificate == null) {
                    throw new Exception("ERROR : NO CA Certificate in KeyStore!");
                }
            }

            this.setCertificate(serverCertificate);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
            Log.i(TAG,"TrustManager : Algorithm : " + tmfAlgorithm);
            Log.i(TAG,"TrustManager : numItems : " + tmf.getTrustManagers().length);
            for(TrustManager tm : tmf.getTrustManagers()){
                Log.i(TAG,"TrustManager  Item : " + tm.toString());
            }

            // First connection to server
            String status = getStatusFromServer();

            if(!"ACTIVE".equals(status)){
                throw new Exception("Server NOT active");
            }

            String defaultUser = "UsuarioApp";

            setCertificateAndPrivateKey(keystorePassword, certFactory, keyStoreFile, defaultUser);

            deleteAllLocalSymKeys();

            String[] fields = {
                    Constants.UID_FACTURA,
                    Constants.TAX_IDENTIFICATION_NUMBER,
                    Constants.CORPORATE_NAME,
                    Constants.INVOICE_NUMBER,
                    Constants.INVOICE_TOTAL,
                    Constants.TOTAL_GROSS_AMOUNT,
                    Constants.TOTAL_TAX_OUTPUTS,
                    Constants.ISSUE_DATE
            };

            getSymmetricKeys(fields);

            saveKeyStoreToFileSystem(keystorePassword, keyStore, keyStoreFile);


        }catch(Exception e){
            Log.e(TAG,e.getClass().getCanonicalName() + ": " + e.getLocalizedMessage());
            Toast.makeText(InvoiceApp.getContext(), "ERROR : "+e.getClass().getCanonicalName() + ": " + e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
        }
    }

    private void setCertificateAndPrivateKey(
            char[] keystorePassword,
            CertificateFactory certFactory,
            File keyStoreFile, String defaultUser)
            throws KeyStoreException, NoSuchAlgorithmException, IOException, OperatorCreationException, ExecutionException, InterruptedException, CertificateException, UnrecoverableEntryException {

        KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(keystorePassword);
        // User certificate
        X509Certificate userCertificate = (X509Certificate) keyStore.getCertificate(defaultUser);

        if(userCertificate == null){
            //Generate KeyPair
            Log.i(TAG,"KeyPair : generating...");
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(4096, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();
            Log.i(TAG,"KeyPair : generated!");
            Log.i(TAG, "KeyPair.getPrivate() : " + keyPair.getPrivate().toString());

            //Generate CSR in PKCS#10 format encoded in DER
            PKCS10CertificationRequest csr = CsrHelper.generateCSR(keyPair, defaultUser);

            JcaPKCS10CertificationRequest req2 = new JcaPKCS10CertificationRequest(csr.getEncoded()).setProvider("SC");

            StringWriter sw = new StringWriter();
            JcaPEMWriter pemWriter = new JcaPEMWriter(sw);
            pemWriter.writeObject(req2);
            pemWriter.close();

            Log.i(TAG, "Request : " + sw.toString());


            Map<String, String> params = new HashMap<>();
            params.put("csr", sw.toString());

            GetCertificateFromServerTask getCertificateFromServerTask = new GetCertificateFromServerTask(params);

            String response_server = getCertificateFromServerTask.execute(Constants.URL_CERT).get();
            String decoded = new String(java.util.Base64.getDecoder().decode(response_server.getBytes()));
            Log.i(TAG, "CSRder.Response : " + decoded);

            InputStream isReceivedCertificate = IOUtils.toInputStream(decoded, "UTF-8");
            X509Certificate receivedCertificate = (X509Certificate) certFactory.generateCertificate(isReceivedCertificate);
            isReceivedCertificate.close();
            Log.i(TAG, "receivedCertificate Subject : " + receivedCertificate.getSubjectDN().getName());
            Log.i(TAG, "receivedCertificate Issuer  : " + receivedCertificate.getIssuerDN().getName());

            keyStore.setCertificateEntry(defaultUser, receivedCertificate);

            userCertificate = (X509Certificate) keyStore.getCertificate(defaultUser);

            //
            Log.i(TAG, "privateKey : saving...");
            keyStore.setKeyEntry(defaultUser,
                    keyPair.getPrivate(),
                    keystorePassword,
                    new java.security.cert.Certificate[]{receivedCertificate});
            Log.i(TAG, "privateKey : saved!");


            saveKeyStoreToFileSystem(keystorePassword, keyStore, keyStoreFile);

        }else{
            Log.i(TAG,"Tenemos el certificado de usuario!");
            Log.i(TAG, "User Certificate Subject : " + userCertificate.getSubjectDN().getName());
            Log.i(TAG, "User Certificate Issuer  : " + userCertificate.getIssuerDN().getName());
        }

        PrivateKeyEntry retrievedPrivateKeyEntry = (PrivateKeyEntry) keyStore.getEntry(defaultUser, protectionParameter);
        PrivateKey retrievedPrivateKey = retrievedPrivateKeyEntry.getPrivateKey();

        this.setCertificate(userCertificate);
        this.setPrivateKey(retrievedPrivateKey);
    }

    private String getStatusFromServer() throws ExecutionException, InterruptedException {
        String status = "";
        int max_attempts = 10;
        int current_attempt = 0;
        while (!"ACTIVE".equals(status) && current_attempt < max_attempts) {
            GetServerStatusTask getServerStatusTask = new GetServerStatusTask();
            status = getServerStatusTask.execute(Constants.URL_STATUS).get();
            Log.i(TAG, "Server status : " + status);
            current_attempt++;
        }
        return status;
    }

    /**
     *
     * @param keystorePassword
     * @param keyStore
     * @param keyStoreFile
     * @throws CertificateException
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     */
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

    /**
     *
     * @param keyStore
     * @throws KeyStoreException
     */
    private void showAliasesInKeyStore(KeyStore keyStore)
            throws
            KeyStoreException {
        Enumeration<String> aliases = keyStore.aliases();
        Log.i(TAG,"Showing aliases in KeyStore...");
        while(aliases.hasMoreElements()){
            Log.i(TAG,"alias : " + aliases.nextElement());
        }
    }

    /**
     *
     * @param fields
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws CertificateEncodingException
     * @throws CMSException
     * @throws IOException
     * @throws JSONException
     */
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

                    String url = Constants.URL_KEYS + "/" + str;
                    String res = getByFRemoteSymKeyTask.execute(url).get();

                    if(res == null || res.isEmpty()){
                        Log.i(TAG, "LocalSymKey NO localizada: " + str);
                        createLocalSymKey(rsg, str);
                    }else{
                        Log.i(TAG, "Recibida del servidor la clave : ["+str+"]" );

                        JSONObject receivedRemoteSymKey = new JSONObject(res);

                        LocalSymKey lskReceived = new LocalSymKey();
                        lskReceived.setF(receivedRemoteSymKey.getString("f"));
                        lskReceived.setK(receivedRemoteSymKey.getString("k"));

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

    /**
     *
     * @param str the local symmetric key name
     * @param lskF1 LocalSymKey
     * @throws CMSException
     */
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

    /**
     *
     * @param rsg
     * @param str
     * @throws CertificateEncodingException
     * @throws CMSException
     * @throws IOException
     * @throws java.util.concurrent.ExecutionException
     * @throws InterruptedException
     */
    private void createLocalSymKey(RandomStringGenerator rsg, String str) throws
            CertificateEncodingException,
            CMSException,
            IOException,
            java.util.concurrent.ExecutionException,
            InterruptedException {


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

        String res = getData.execute(Constants.URL_KEYS).get();
        Log.i(TAG, "res : " + res);

        this.getSimKeys().put(str, simKey);


        createSymmetricKeyInKeyStore(str);

    }

    /**
     *
     * @param str
     */
    private void createSymmetricKeyInKeyStore(String str){

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(Constants.AES);
            SecureRandom secureRandom = new SecureRandom();
            int keyBitSize = 128;

            keyGenerator.init(keyBitSize, secureRandom);

            SecretKey secretKey = keyGenerator.generateKey();

            byte[] data = secretKey.getEncoded();
            String keyString = java.util.Base64.getEncoder().encodeToString(data);
            Log.i(TAG, "keyString : " + keyString);

            byte[] data2 = java.util.Base64.getDecoder().decode(keyString);
            SecretKey key2 = new SecretKeySpec(data2, 0, data.length, Constants.AES);
            byte[] data3 = key2.getEncoded();
            String keyString3 = java.util.Base64.getEncoder().encodeToString(data3);

            if(secretKey.equals(key2)){
                Log.i(TAG, String.format("Bien! [%s] : [%s]", keyString, keyString3));

                KeyStore.ProtectionParameter protParam =
                        new KeyStore.PasswordProtection(Constants.PKCS12_PASSWORD.toCharArray());
                keyStore.setEntry(
                        str,
                        new KeyStore.SecretKeyEntry(secretKey),
                        protParam);
                showAliasesInKeyStore(keyStore);

                String symmetricKeyStored = getSymmetricKeyFromKeyStore(str, protParam);
                Log.i(TAG, String.format("symmetricKeyStored [%s] : [%s]. Original : [%s]", str, symmetricKeyStored, keyString));

            }else{
                Log.e(TAG, String.format("ERROR : Something went really wrong... Keys are not equal!!"));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param str
     * @param protParam
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableEntryException
     */
    private String getSymmetricKeyFromKeyStore(String str, KeyStore.ProtectionParameter protParam)
            throws
            KeyStoreException,
            NoSuchAlgorithmException,
            UnrecoverableEntryException {
        String recoveredSecret = "";
        KeyStore.SecretKeyEntry recoveredEntry = (KeyStore.SecretKeyEntry)keyStore.getEntry(str, protParam);
        byte[] bytes = recoveredEntry.getSecretKey().getEncoded();
        recoveredSecret = java.util.Base64.getEncoder().encodeToString(bytes);
        Log.i(TAG, "recovered " + recoveredSecret);
        return recoveredSecret;
    }

    /**
     *
     * @throws java.util.concurrent.ExecutionException
     * @throws InterruptedException
     */
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


    private void loadCertificateFromAssets(CertificateFactory certFactory, String certFile, String alias)
            throws
            Exception {

        // Load Certificate from assets
        Log.i(TAG,"Retrieving " + alias + " Certificate from file in assets...");
        InputStream isCrt = InvoiceApp.getContext().getAssets().open(certFile);
        X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(isCrt);
        isCrt.close();
        if(certificate == null){
            throw new Exception("ERROR : NO " + alias + " Certificate in "+ certFile +"!");
        }
        Log.i(TAG,"Retrieved " + alias + " Certificate from file in assets! SubjectDN : " + certificate.getSubjectDN().getName());
        keyStore.setCertificateEntry(alias, certificate);
        showAliasesInKeyStore(keyStore);
    }

    /**
     *
     * @param str
     * @param secret
     */
    public void saveUserLoggedDataInKeyStore(String str, String secret){

        try {
            Log.i(TAG, String.format("saveUserLoggedDataInKeyStore : [%s] : [%s]", str, secret));
            byte[] data2 = secret.getBytes();
            SecretKey key2 = new SecretKeySpec(data2, 0, data2.length, Constants.AES);

            KeyStore.ProtectionParameter protParam =
                    new KeyStore.PasswordProtection(Constants.PKCS12_PASSWORD.toCharArray());
            keyStore.setEntry(
                    str,
                    new KeyStore.SecretKeyEntry(key2),
                    protParam);
            showAliasesInKeyStore(keyStore);

        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param str
     * @return
     */
    public String getUserLoggedDataFromKeyStore(String str){

        try {
            Log.i(TAG, String.format("getUserLoggedDataFromKeyStore : [%s]", str));
            KeyStore.ProtectionParameter protParam =
                    new KeyStore.PasswordProtection(Constants.PKCS12_PASSWORD.toCharArray());

            KeyStore.SecretKeyEntry recoveredEntry = (KeyStore.SecretKeyEntry)keyStore.getEntry(str, protParam);
            byte[] bytes = recoveredEntry.getSecretKey().getEncoded();
            return new String(bytes);

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        }

        return null;

    }


    // Getters and Setters

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

}
