package edu.uoc.mistic.tfm.jherranzm.util;

import android.app.Activity;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

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
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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

import edu.uoc.mistic.tfm.jherranzm.CsrHelper;
import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.crypto.AsymmetricDecryptor;
import edu.uoc.mistic.tfm.jherranzm.crypto.AsymmetricEncryptor;
import edu.uoc.mistic.tfm.jherranzm.crypto.SymmetricEncryptor;
import edu.uoc.mistic.tfm.jherranzm.model.LocalSymKey;
import edu.uoc.mistic.tfm.jherranzm.services.LocalSymKeyDataManagerService;
import edu.uoc.mistic.tfm.jherranzm.services.RemoteSymKeyDataManagerService;
import edu.uoc.mistic.tfm.jherranzm.tasks.localsymkeytasks.DeleteLocalSymKeyTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.localsymkeytasks.GetAllLocalSymKeyTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.localsymkeytasks.GetByFLocalSymKeyTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.localsymkeytasks.InsertLocalSymKeyTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.posttasks.PostDataAuthenticatedToUrlTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.remotesymkeytasks.GetByFRemoteSymKeyTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.remotesymkeytasks.GetCertificateFromServerTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.remotesymkeytasks.GetServerStatusTask;

public class TFMSecurityManager {

    private static final String TAG = TFMSecurityManager.class.getSimpleName();

    private final Map<String, String> simKeys = new HashMap<>();

    private X509Certificate certificate;
    private PrivateKey privateKey;
    private PrivateKeyEntry pke;
    private TrustManagerFactory tmf;

    private static TFMSecurityManager instance;
    private KeyStore keyStore;
    private static CertificateFactory certFactory;

    private final char[] keystorePassword = Constants.PKCS12_PASSWORD.toCharArray();
    private File keyStoreFile;

    private WeakReference<Activity> mActivityRef;

    private TFMSecurityManager(){}

    public static TFMSecurityManager getInstance() {
        return instance;
    }

    public static TFMSecurityManager getInstance(Activity activity){
        if (instance == null){
            Log.i(TAG, "TFMSecurityManager initialization: begin...");

            try {
                certFactory = CertificateFactory.getInstance(Constants.X_509, Constants.BC);
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            }

            // if instance is null, initialize
            instance = new TFMSecurityManager();

            instance.manageSecurity(activity);

            Log.i(TAG, "TFMSecurityManager initialization: end...");
        }else{
            Log.i(TAG, "TFMSecurityManager already initialized!");
        }
        return instance;
    }


    /**
     *
     */
    private void manageSecurity(Activity activity) {

        mActivityRef = new WeakReference<>(activity);

        // Security
        try {

            //CertificateFactory certFactory = CertificateFactory.getInstance(Constants.X_509, Constants.BC);
            Log.i(TAG, String.format("KeyStore.getDefaultType() : %s", KeyStore.getDefaultType()));

            // Keystore creation
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            Log.i(TAG, String.format("KeyStore.getType() : %s", keyStore.getType()));


            keyStoreFile = loadOrCreateKeyStore();

            saveKeyStoreToFileSystem();
            saveKeyStoreToSDCard();

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

            //this.setCertificate(serverCertificate);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
            Log.i(TAG, String.format("TrustManager : Algorithm : %s", tmfAlgorithm));
            Log.i(TAG, String.format("TrustManager : numItems : %d", tmf.getTrustManagers().length));
            for(TrustManager tm : tmf.getTrustManagers()){
                Log.i(TAG, String.format("TrustManager  Item : %s", tm.toString()));
            }

            // First connection to server
            String status = getStatusFromServer();

            if(!"ACTIVE".equals(status)){
                throw new Exception("Server NOT active");
            }

            if(getSecretFromKeyInKeyStore(Constants.USER_LOGGED) == null){
                Log.i(TAG, "NOT a default user logged.");
            }else{
                setCertificatePrivateKeyAndSymmetricKeysForUserLogged(
                        getSecretFromKeyInKeyStore(Constants.USER_LOGGED),
                        getSecretFromKeyInKeyStore(Constants.USER_PASS),
                        getSecretFromKeyInKeyStore(Constants.USER_LOGGED));

                saveKeyStoreToFileSystem();
                saveKeyStoreToSDCard();
                saveKeyStoreToServer();

            }

            loadKeyStoreFromSDCard();


        }catch(Exception e){
            Log.e(TAG,e.getClass().getCanonicalName() + ": " + e.getLocalizedMessage());
            Toast.makeText(mActivityRef.get(), String.format("ERROR : %s: %s", e.getClass().getCanonicalName(), e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private void saveKeyStoreToServer() {

        try {

            // Read KeyStore to String
            InputStream isKeyStoreFile = new FileInputStream(keyStoreFile);

            // Encrypt with the user's password
            byte[] baIsKeyStoreFile = IOUtils.toByteArray(isKeyStoreFile);

            RandomStringGenerator rsg = new RandomStringGenerator();

            // IV and Symmetric Key
            String iv = rsg.getRandomString(16);
            Log.i(TAG, String.format("iv     : [%s]", iv));

            String simKey = getKeyToEncryptTheKeyStore();
            Log.i(TAG, String.format("simKey : [%s]", simKey));

            SymmetricEncryptor simEnc = new SymmetricEncryptor();
            simEnc.setIv(iv);
            simEnc.setKey(simKey);

            String keyStoreFileEncrypted   = simEnc.encrypt(baIsKeyStoreFile);
            Log.i(TAG,keyStoreFileEncrypted);

            Map<String, String> params = new HashMap<>();
            params.put("op", "store");
            params.put("iv", iv);
            params.put("enc", keyStoreFileEncrypted);

            PostDataAuthenticatedToUrlTask getData = new PostDataAuthenticatedToUrlTask(params);

            String res = getData.execute(Constants.URL_KEYSTORE).get();
            Log.i(TAG, String.format("Response from server : %s", res));



        } catch (Exception e) {
            Log.e(TAG,String.format("ERROR : %s: %s", e.getClass().getCanonicalName(), e.getLocalizedMessage()));
        }


        // Convert to Base64

        // Push to server

        // Retrieve server response
    }

    private String getKeyToEncryptTheKeyStore() {
        StringBuilder sb = new StringBuilder();
        sb.append(getSecretFromKeyInKeyStore(Constants.USER_PASS));
        sb.reverse();
        sb.append("M3st3C!Tr3b4llF3n4lD3M4st3r");
        sb.substring(0,15);
        return sb.substring(0,15);
    }

    private void loadKeyStoreFromSDCard() {

        // Load Keystore if exists...
        //File keyStoreFile = new File(Environment.getExternalStorageDirectory(), Constants.KEY_STORE_BKS_FILE);
        String root_sd = Environment.getExternalStorageDirectory().toString();
        File keyStoreFile = new File(root_sd, Constants.KEY_STORE_BKS_FILE);

        try {
            if(keyStoreFile.exists()){
                Log.i(TAG, String.format("KeyStore file exists in : %s", keyStoreFile.getAbsolutePath()));
                // clean...
                keyStore.load(null, null);
                keyStore.load(new FileInputStream(keyStoreFile), keystorePassword);

                showAliasesInKeyStore();

            }
        } catch (Exception e) {
            Log.e(TAG, String.format("ERROR : %s: %s", e.getClass().getCanonicalName(), e.getLocalizedMessage()));
            e.printStackTrace();
        }
    }

    public void setCertificatePrivateKeyAndSymmetricKeysForUserLogged(
            String defaultUser,
            String userPass,
            String label)
            throws
            KeyStoreException,
            NoSuchAlgorithmException,
            IOException,
            ExecutionException,
            InterruptedException,
            CertificateException,
            UnrecoverableEntryException,
            CMSException,
            JSONException {

        setCertificateAndPrivateKey(label, defaultUser);

        saveKeyAndSecretInKeyStore(Constants.USER_LOGGED, defaultUser);
        saveKeyAndSecretInKeyStore(Constants.USER_PASS, userPass);


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

        saveKeyStoreToFileSystem();
        saveKeyStoreToSDCard();
        saveKeyStoreToServer();
    }

    /**
     * If KeyStore file exists, load it. Else, create and load the CA and Server Certificates from assets
     *
     * @return the KeyStore File
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     */
    private File loadOrCreateKeyStore()
            throws
            IOException,
            CertificateException,
            NoSuchAlgorithmException,
            KeyStoreException {

        // Load Keystore if exists...
        Log.i(TAG, String.format("Application getFilesDir : [%s]", mActivityRef.get().getApplicationContext().getFilesDir()));
        File keyStoreFile = new File(mActivityRef.get().getApplicationContext().getFilesDir(), Constants.KEY_STORE_BKS_FILE);

        if(keyStoreFile.exists()){
            Log.i(TAG, String.format("KeyStore file already exists in : [%s]", keyStoreFile.getAbsolutePath()));
            keyStore.load(new FileInputStream(keyStoreFile), keystorePassword);

            showAliasesInKeyStore();

        }else{

            // First time initialization...
            Log.i(TAG, String.format("KeyStore: creating file in  : [%s]", keyStoreFile.getAbsolutePath()));
            if(!keyStoreFile.createNewFile()){
                throw new FileSystemException("ERROR : Can NOT create KeyStore!");
            }

            keyStore.load(null, null);
            showAliasesInKeyStore();

            loadCertificateFromAssets(Constants.CA_CERTIFICATE_FILE, "ca");
            loadCertificateFromAssets(Constants.SERVER_CERTIFICATE_FILE, "Server");

        }
        return keyStoreFile;
    }

    /**
     *
     * GEt Certificate from KeyStore by label.
     *
     * If it do not exists then it's created
     *
     * @param label, the label used to identify the Certificate and PrivateKey in the KeyStore
     * @param defaultUser, the name of the current user (email)
     *
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws OperatorCreationException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws CertificateException
     * @throws UnrecoverableEntryException
     */
    private void setCertificateAndPrivateKey(
            String label,
            String defaultUser)
            throws
            KeyStoreException,
            NoSuchAlgorithmException,
            IOException,
            ExecutionException,
            InterruptedException,
            CertificateException,
            UnrecoverableEntryException {

        KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(keystorePassword);
        // User certificate
        X509Certificate userCertificate = (X509Certificate) keyStore.getCertificate(label);

        if(userCertificate == null){

            // Cleaning just in case
            // Delete Local Symmetric Keys from Database
            LocalSymKeyDataManagerService.deleteAllByUser(mActivityRef.get(), defaultUser);

            // Delete Local Symmetric Keys from KeyStore
            deleteAllLocalSymKeys();

            //Delete Local Symmetric Keys from RemoteDatabase
            RemoteSymKeyDataManagerService.deleteAllByUser();


            //Generate KeyPair
            Log.i(TAG,"KeyPair : generating...");
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(4096, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();
            Log.i(TAG,"KeyPair : generated!");

            //Generate CSR in PKCS#10 format encoded in DER
            PKCS10CertificationRequest csr = CsrHelper.generateCSR(keyPair, defaultUser);

            JcaPKCS10CertificationRequest req2 = new JcaPKCS10CertificationRequest(csr.getEncoded()).setProvider("SC");

            StringWriter sw = new StringWriter();
            JcaPEMWriter pemWriter = new JcaPEMWriter(sw);
            pemWriter.writeObject(req2);
            pemWriter.close();

            Log.i(TAG, String.format("Request : [%s]", sw.toString()));


            Map<String, String> params = new HashMap<>();
            params.put("csr", sw.toString());

            GetCertificateFromServerTask getCertificateFromServerTask = new GetCertificateFromServerTask(params);

            String response_server = getCertificateFromServerTask.execute(Constants.URL_CERT).get();
            String decoded = new String(java.util.Base64.getDecoder().decode(response_server.getBytes()));
            Log.i(TAG, String.format("CSR.Response : [%s]", decoded));

            InputStream isReceivedCertificate = IOUtils.toInputStream(decoded, "UTF-8");
            X509Certificate receivedCertificate = (X509Certificate) certFactory.generateCertificate(isReceivedCertificate);
            isReceivedCertificate.close();
            Log.i(TAG, String.format("receivedCertificate Subject : [%s]", receivedCertificate.getSubjectDN().getName()));
            Log.i(TAG, String.format("receivedCertificate Issuer  : [%s]", receivedCertificate.getIssuerDN().getName()));

            keyStore.setCertificateEntry(label, receivedCertificate);

            userCertificate = (X509Certificate) keyStore.getCertificate(label);

            //
            Log.i(TAG, String.format("PrivateKey : saving with label [%s]...", label));
            keyStore.setKeyEntry(label,
                    keyPair.getPrivate(),
                    keystorePassword,
                    new java.security.cert.Certificate[]{receivedCertificate});
            Log.i(TAG, String.format("PrivateKey : saved label [%s]!", label));


            saveKeyStoreToFileSystem();
            saveKeyStoreToSDCard();

        }else{
            Log.d(TAG,"We've got user's certificate!");
            Log.d(TAG, String.format("User Certificate Subject : [%s]", userCertificate.getSubjectDN().getName()));
            Log.d(TAG, String.format("User Certificate Issuer  : [%s]", userCertificate.getIssuerDN().getName()));
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
            Log.i(TAG, String.format("Server status : [%s]", status));
            current_attempt++;
        }
        return status;
    }

    /**
     *
     * @throws CertificateException
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     */
    private void saveKeyStoreToFileSystem()
            throws
            CertificateException,
            IOException,
            KeyStoreException,
            NoSuchAlgorithmException {
        Log.i(TAG, "KeyStore: Saving in default app directory...");
        FileOutputStream out = new FileOutputStream(keyStoreFile);
        keyStore.store(out, keystorePassword);
        out.close();
        Log.i(TAG, "KeyStore: Saved in default app directory!");
    }

    /**
     *
     * @throws CertificateException
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     */
    private void saveKeyStoreToSDCard()
            throws
            CertificateException,
            IOException,
            KeyStoreException,
            NoSuchAlgorithmException {
        Log.i(TAG, "KeyStore: Saving in SDCard...");
        String root_sd = Environment.getExternalStorageDirectory().toString();
        File keyStoreFile = new File(root_sd, Constants.KEY_STORE_BKS_FILE);
        FileOutputStream out = new FileOutputStream(keyStoreFile);
        keyStore.store(out, keystorePassword);
        out.close();
        Log.i(TAG, "KeyStore: Saved in SDCard!");
    }

    /**
     *
     * @throws KeyStoreException
     */
    private void showAliasesInKeyStore()
            throws
            KeyStoreException {
        Enumeration<String> aliases = keyStore.aliases();
        Log.i(TAG,"Showing aliases in KeyStore...");
        while(aliases.hasMoreElements()){
            String alias = aliases.nextElement();

            if(keyStore.isKeyEntry(alias) && alias.startsWith("f")) {
                Log.i(TAG, String.format("alias : %s : [%s]", alias, getSecretFromKeyInKeyStore(alias)));
            }else{
                Log.i(TAG, String.format("alias : [%s]", alias));
            }
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
                GetByFLocalSymKeyTask getByFLocalSymKeyTask = new GetByFLocalSymKeyTask(mActivityRef.get());
                Log.i(TAG, String.format("LocalSymKey : [%s]", str));

                LocalSymKey lskF1 = getByFLocalSymKeyTask.execute(str).get();
                if(lskF1 == null){
                    Log.i(TAG, String.format("LocalSymKey : [%s] NOT in LOCAL database!", str));

                    // Recuperem la clau del servidor
                    Log.i(TAG, String.format("Searching [%s] in REMOTE database...", str));
                    GetByFRemoteSymKeyTask getByFRemoteSymKeyTask = new GetByFRemoteSymKeyTask();

                    String url = Constants.URL_KEYS + "/" + str;
                    String res = getByFRemoteSymKeyTask.execute(url).get();

                    if(res == null || res.isEmpty()){
                        Log.i(TAG, String.format("LocalSymKey NOT located in REMOTE: [%s]", str));
                        createLocalSymKey(rsg, str);
                    }else{
                        Log.i(TAG, String.format("LocalSymKey received from REMOTE: [%s]", str));

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
                    Log.i(TAG, String.format("LocalSymKey : [%s] IS in LOCAL database!", str));
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
        Log.i(TAG, String.format("LocalSymKey retrieved: [%s]", lskF1.getF()));

        // Desencriptació amb clau privada de iv i simKey
        byte[] simKeyBytesDec = Base64.decode(lskF1.getK(), Base64.NO_WRAP);
        byte[] simKeyStringDec = AsymmetricDecryptor.decryptData(simKeyBytesDec, this.getPrivateKey());
        String strKey = new String(simKeyStringDec);

        Log.i(TAG, String.format("Field [%s]: symmetric key [%s]", str, strKey));

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
        lsk.setUser(getUserLoggedDataFromKeyStore(Constants.USER_LOGGED));
        lsk.setF(str);

        String simKey = rsg.getRandomString(16);
        byte[] simKeyBytesEnc = AsymmetricEncryptor.encryptData(simKey.getBytes(), this.getCertificate());
        String simKeyStringEnc = new String(Base64.encode(simKeyBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);
        lsk.setK(simKeyStringEnc);

        InsertLocalSymKeyTask insertLocalSymKeyTask = new InsertLocalSymKeyTask(mActivityRef.get(), lsk);
        LocalSymKey lskF = insertLocalSymKeyTask.execute().get();
        Log.i(TAG, String.format("LocalSymKey saved: [%s]", lskF.toString()));

        Map<String, String> params = new HashMap<>();
        params.put("f", str);
        params.put("k", simKeyStringEnc);

        PostDataAuthenticatedToUrlTask getData = new PostDataAuthenticatedToUrlTask(params);

        String res = getData.execute(Constants.URL_KEYS).get();
        Log.i(TAG, String.format("Insert remote SymKey Task: server response : [%s]", res));

        this.getSimKeys().put(str, simKey);

        //createSymmetricKeyInKeyStore(str);

        Log.i(TAG, String.format("Save key/secret in KeyStore : %s / %s ", str, simKey));
        saveKeyAndSecretInKeyStore(str, simKey);
        String retrievedSecret = getSecretFromKeyInKeyStore(str);
        Log.i(TAG, String.format("Retrieved key/secret from KeyStore : %s / %s ", str, retrievedSecret));

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
            Log.i(TAG, String.format("createSymmetricKeyInKeyStore.keyString : [%s]", keyString));

            byte[] data2 = java.util.Base64.getDecoder().decode(keyString);
            SecretKey key2 = new SecretKeySpec(data2, 0, data2.length, Constants.AES);

            byte[] data3 = key2.getEncoded();
            String keyString3 = java.util.Base64.getEncoder().encodeToString(data3);

            if(secretKey.equals(key2)){
                Log.i(TAG, String.format("Bien! [%s] : [%s]", keyString, keyString3));

                KeyStore.ProtectionParameter protectionParameter =
                        new KeyStore.PasswordProtection(Constants.PKCS12_PASSWORD.toCharArray());
                keyStore.setEntry(
                        str,
                        new KeyStore.SecretKeyEntry(secretKey),
                        protectionParameter);
                showAliasesInKeyStore();

                String symmetricKeyStored = getSymmetricKeyFromKeyStore(str, protectionParameter);
                Log.i(TAG, String.format("symmetricKeyStored [%s] : [%s]. Original : [%s]", str, symmetricKeyStored, keyString));

            }else{
                Log.e(TAG, "ERROR : Something went really wrong... Keys are not equal!!");
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
     * @param protectionParameter
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableEntryException
     */
    private String getSymmetricKeyFromKeyStore(String str, KeyStore.ProtectionParameter protectionParameter)
            throws
            KeyStoreException,
            NoSuchAlgorithmException,
            UnrecoverableEntryException {

        KeyStore.SecretKeyEntry recoveredEntry = (KeyStore.SecretKeyEntry)keyStore.getEntry(str, protectionParameter);
        byte[] bytes = recoveredEntry.getSecretKey().getEncoded();
        String recoveredSecret = java.util.Base64.getEncoder().encodeToString(bytes);
        Log.i(TAG, String.format("recovered [%s]", recoveredSecret));
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
        GetAllLocalSymKeyTask getAllLocalSymKeyTask = new GetAllLocalSymKeyTask(mActivityRef.get());
        List<LocalSymKey> lskList = getAllLocalSymKeyTask.execute().get();
        for(LocalSymKey aLocalSimKey : lskList){
            DeleteLocalSymKeyTask dlskTask = new DeleteLocalSymKeyTask(mActivityRef.get(), aLocalSimKey);
            LocalSymKey deleted = dlskTask.execute().get();
            Log.i(TAG, "Deleted: " + deleted.toString());
        }
    }


    private void loadCertificateFromAssets(String certFile, String alias)
            throws IOException, CertificateException, KeyStoreException{

            // Load Certificate from assets
            Log.i(TAG, String.format("Retrieving %s Certificate from file in assets...", alias));
            InputStream isCrt = mActivityRef.get().getAssets().open(certFile);
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(isCrt);
            isCrt.close();
            if(certificate == null){
                throw new CertificateException(String.format("ERROR : NO %s Certificate in %s!", alias, certFile));
            }
            Log.i(TAG, String.format("Retrieved %s Certificate from file in assets! SubjectDN : [%s]", alias, certificate.getSubjectDN().getName()));
            keyStore.setCertificateEntry(alias, certificate);
            showAliasesInKeyStore();
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

            KeyStore.ProtectionParameter protectionParameter =
                    new KeyStore.PasswordProtection(Constants.PKCS12_PASSWORD.toCharArray());
            keyStore.setEntry(
                    str,
                    new KeyStore.SecretKeyEntry(key2),
                    protectionParameter);
            showAliasesInKeyStore();

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
            KeyStore.ProtectionParameter protectionParameter =
                    new KeyStore.PasswordProtection(Constants.PKCS12_PASSWORD.toCharArray());

            KeyStore.SecretKeyEntry recoveredEntry = (KeyStore.SecretKeyEntry)keyStore.getEntry(str, protectionParameter);
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


    /**
     *
     * @param str
     * @param secret
     */
    public void saveKeyAndSecretInKeyStore(String str, String secret){

        try {
            Log.i(TAG, String.format("saveKeyAndSecretInKeyStore : [%s] : [%s]", str, secret));
            byte[] secretBytes = secret.getBytes();
            SecretKey key2 = new SecretKeySpec(secretBytes, 0, secretBytes.length, Constants.AES);

            KeyStore.ProtectionParameter protectionParameter =
                    new KeyStore.PasswordProtection(Constants.PKCS12_PASSWORD.toCharArray());
            keyStore.setEntry(
                    str,
                    new KeyStore.SecretKeyEntry(key2),
                    protectionParameter);
            showAliasesInKeyStore();

        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param str
     * @return
     */
    public String getSecretFromKeyInKeyStore(String str){


        try {
            if(!keyStore.containsAlias(str)){
                return null;
            }

            Log.i(TAG, String.format("getSecretFromKeyInKeyStore : [%s]", str));
            KeyStore.ProtectionParameter protectionParameter =
                    new KeyStore.PasswordProtection(Constants.PKCS12_PASSWORD.toCharArray());

            KeyStore.SecretKeyEntry recoveredEntry = (KeyStore.SecretKeyEntry)keyStore.getEntry(str, protectionParameter);
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
