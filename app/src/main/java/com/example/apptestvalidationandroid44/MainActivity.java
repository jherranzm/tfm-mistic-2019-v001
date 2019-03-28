package com.example.apptestvalidationandroid44;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
//import android.util.Base64;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.ElementProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.Properties;

import java.security.Provider.Service;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.security.signature.XMLSignature;

import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.core.signers.AOSigner;
import es.gob.afirma.signers.xades.AOFacturaESigner;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_MESSAGE = "com.example.appsecond.MESSAGE";

    public static final String PKCS_12 = "PKCS12";
    public final static String PKCS12_PASSWORD = "Th2S5p2rStr4ngP1ss";
    public final static String CR_LF = "\n";


    public EditText editText;
    public EditText editText2;
    public EditText urlEditText;
    public InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);
        urlEditText = (EditText) findViewById(R.id.urlEditText);

        editText2.setText("Looking for...");

        //Get the ID of button that will perform the network call
        Button btn =  (Button) findViewById(R.id.button);
        assert btn != null;

        String url ="https://www.google.com";
        editText2.setText(url);

        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);



    }

    @Override
    public void onClick(View v) {
        editText.getText().clear(); //or you can use editText.setText("");
    }

    public void verifySignedInvoice(View v){
        Intent intent = new Intent(this, DisplayMessageActivity.class);

        String message = "Nothing";
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "SC");

            InputStream isServerCrt = this.getResources().openRawResource(R.raw.server);

            InputStream isSignedInvoice = this.getResources().openRawResource(R.raw.invoice_990001_xml_signed);
            Document doc = getDocument(isSignedInvoice);
            isSignedInvoice.close();

            isSignedInvoice = this.getResources().openRawResource(R.raw.invoice_990001_xml_signed);
            byte[] baInvoiceSigned = IOUtils.toByteArray(isSignedInvoice);
            isSignedInvoice.close();

            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(isServerCrt);

            isServerCrt.close();

            char[] keystorePassword = PKCS12_PASSWORD.toCharArray();
            char[] keyPassword = PKCS12_PASSWORD.toCharArray();

            //message += CR_LF + "Longitud del fichero firmado : ["+baInvoiceSigned.length+"]";

            //isSignedInvoice = this.getResources().openRawResource(R.raw.invoice_990001_xml_signed);

            message += CR_LF + "Root Element : ["+doc.getDocumentElement().getTagName()+"]";
            NodeList nl = doc.getElementsByTagNameNS(Constants.SignatureSpecNS, "Signature");
            if (nl.getLength() == 0) {
                throw new Exception("No XML Digital Signature Found, document is discarded");
            }

            Element sigElement = (Element) nl.item(0);
            message += CR_LF + "sigElement : ["+sigElement.getTagName()+"]";

            XMLSignature signature = new XMLSignature(sigElement, "");

            boolean valid = signature.checkSignatureValue(certificate.getPublicKey());

            //message += CR_LF + new String(signature.getSignatureValue());

            if(valid){
                message += CR_LF + "La firma es válida!";
            }else{
                message += CR_LF + "ERROR : La firma NO es válida!";
            }

        }catch (Exception e) {
            e.printStackTrace();
            message = "Exception:" + e.getLocalizedMessage();
        }

        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void signInvoice(View v){

        Intent intent = new Intent(this, DisplayMessageActivity.class);

        String message = "Nothing";

        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");

            InputStream isServerCrt = this.getResources().openRawResource(R.raw.server);
            InputStream isServerKey = this.getResources().openRawResource(R.raw.serverkey);

            InputStream isInvoice = this.getResources().openRawResource(R.raw.invoice990001);
            byte[] baInvoiceSigned = IOUtils.toByteArray(isInvoice);
            isInvoice.close();

            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(isServerCrt);

            isServerCrt.close();

            char[] keystorePassword = PKCS12_PASSWORD.toCharArray();
            char[] keyPassword = PKCS12_PASSWORD.toCharArray();

            KeyStore keystore = KeyStore.getInstance(PKCS_12, "BC");
            keystore.load(isServerKey, keystorePassword);
            isServerKey.close();

            PrivateKey key = (PrivateKey) keystore.getKey("Server", keyPassword);
            if(key == null) {
                Log.e(getLocalClassName(),"ERROR NO hay key!");
            };
            Log.i("", "key.getAlgorithm : " + key.getAlgorithm());

            Provider provider = keystore.getProvider();

            message = "Provider : [" +provider.getName()+"] : [" +provider.getInfo()+"]";

            PrivateKeyEntry pke = (PrivateKeyEntry) keystore.getEntry("Server", new KeyStore.PasswordProtection(PKCS12_PASSWORD.toCharArray()));

            message += CR_LF + "Longitud del fichero firmado : ["+baInvoiceSigned.length+"]";


            final AOSigner signer = new AOFacturaESigner();

             Log.i("APP", "************* Fin!");


        }catch (Exception e) {
            e.printStackTrace();
            message = "Exception:" + e.getLocalizedMessage();
        }

        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);

    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);

        String message = "Nothing";
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");

            InputStream isServerCrt = this.getResources().openRawResource(R.raw.server);
            InputStream isServerKey = this.getResources().openRawResource(R.raw.serverkey);

            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(isServerCrt);

            isServerCrt.close();

            char[] keystorePassword = PKCS12_PASSWORD.toCharArray();
            char[] keyPassword = PKCS12_PASSWORD.toCharArray();

            KeyStore keystore = KeyStore.getInstance(PKCS_12, "BC");
            keystore.load(isServerKey, keystorePassword);
            isServerKey.close();

            PrivateKey key = (PrivateKey) keystore.getKey("Server", keyPassword);
            if(key == null) {
                Log.e(getLocalClassName(),"ERROR NO hay key!");
            };
            Log.i("", "key.getAlgorithm : " + key.getAlgorithm());

            Provider provider = keystore.getProvider();

            //getInfoOverProvider(provider);
            getInfoOverAllProviders();

            String inputText = editText.getText().toString();

            // Encriptem el text
            byte[] messageEncrypted = Encryptor.encryptData(inputText.getBytes(), certificate);
            Log.i(getLocalClassName(),"messageEncrypted : [" + new String(messageEncrypted) + "]");

            // Codifiquem el text en Base64 per poder-lo enviar
            byte[] messageEncryptedEncodedB64 = Base64.encode(messageEncrypted,Base64.DEFAULT);//.encode(message.getBytes());
            Log.i(getLocalClassName(),"messageEncryptedEncodedB64 : [" + new String(messageEncryptedEncodedB64) + "]");
            message = new String(messageEncryptedEncodedB64);

            // Decodifiquem el text de Base64
            byte[] messageEncryptedDecodedB64 = Base64.decode(message, Base64.DEFAULT);
            Log.i(getLocalClassName(),"messageEncryptedDecodedB64 : [" + new String(messageEncryptedDecodedB64) + "]");
            message = new String(messageEncryptedDecodedB64);

            // Desencriptem el text
            byte[] messageDecodedB64Decrypted = Decryptor.decryptData(messageEncryptedDecodedB64, key);
            Log.i(getLocalClassName(),"messageDecodedB64Decrypted : [" + new String(messageDecodedB64Decrypted) + "]");
            message = inputText + "**:**" + new String(messageDecodedB64Decrypted);


            InputStream isInvoice = this.getResources().openRawResource(R.raw.invoice990001);
            InputStream isInvoiceSigned = this.getResources().openRawResource(R.raw.invoice_990001_xml_signed);


            byte[] baInvoiceSigned = IOUtils.toByteArray(isInvoiceSigned);
            byte[] fileContent = IOUtils.toByteArray(isInvoice);
            byte[] fileContentEncrypted = Encryptor.encryptData(fileContent, certificate);
            byte[] fileContentEncryptedDecrypted = Decryptor.decryptData(fileContentEncrypted, key);

            message = inputText + "**:**" + new String(messageDecodedB64Decrypted);
            message +="\n**:**["+fileContentEncryptedDecrypted.length+"]";
            message +="\n**:**["+fileContent.length+"]";

            isInvoice.close();

        } catch (CertificateException e) {
            e.printStackTrace();
            message = e.getLocalizedMessage();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            message = e.getLocalizedMessage();
        } catch (KeyStoreException e) {
            e.printStackTrace();
            message = e.getLocalizedMessage();
        } catch (IOException e) {
            e.printStackTrace();
            message = e.getLocalizedMessage();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            message = e.getLocalizedMessage();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
            message = e.getLocalizedMessage();
        } catch (Exception e) {
            e.printStackTrace();
            message = "Exception:" + e.getLocalizedMessage();
        }

        //String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    private void getInfoOverProvider(Provider provider) {
        Log.i(getLocalClassName(),"provider.getName(): ["+provider.getName()+"]");
        Log.i(getLocalClassName(),"provider.getInfo(): ["+provider.getInfo()+"]");

        Set<String> messageDigest = Security.getAlgorithms("MessageDigest");
        for(String s : messageDigest){
            Log.i("MessageDigest","messageDigest: ["+s+"]");
        }

        Set<String> algorithms = Security.getAlgorithms("Algorithm");
        for(String s : algorithms){
            Log.i("Algorithm","algorithm: ["+s+"]");
        }
    }

    private void getAlgorithmsAvailableByProvider(Provider provider, String service) {
        Log.i(getLocalClassName(),"provider.getName(): ["+provider.getName()+"]");
        Log.i(getLocalClassName(),"provider.getInfo(): ["+provider.getInfo()+"]");

        Set<String> algorithms = Security.getAlgorithms(service);
        for(String s : algorithms){
            Log.i(service,"algorithm: ["+s+"]");
        }
    }

    private void getInfoOverAllProviders(){
        System.out.println("Availble Providers are:");
        Provider [] providerList = Security.getProviders();
        for (Provider provider : providerList)
        {
            System.out.println("Name: "  + provider.getName());
            System.out.println("Information:\n" + provider.getInfo());

            Set<Service> serviceList = provider.getServices();
            for (Service service : serviceList)
            {
                StringBuffer sb = new StringBuffer();
                sb.append(provider.getName());
                sb.append(";");
                sb.append(provider.getInfo());
                sb.append(";");
                sb.append(service.getType());
                sb.append(";");
                sb.append(service.getAlgorithm());
                sb.append(";");
                Log.i("Providers",sb.toString());
            }
        }
    }

    /** Called when the user taps the Send button */
    public void getURL(View view) {

        // Ocultar teclat...
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        GetDataFromUrlTask getData = new GetDataFromUrlTask();

        try {
            String url = urlEditText.getText().toString();
            String res = getData.execute(url).get();
            editText2.setText(res);
        } catch (Exception e) {
            Log.i("APP", "public void getURL() — get item number " + e.getMessage());
            editText2.setText("Ups... error en GetDataFromUrlTask " + e.getMessage());
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

    protected Document getDocument(InputStream isDocument) {
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            doc = dbf.newDocumentBuilder().parse(isDocument);
        } catch (ParserConfigurationException ex) {
            System.err.println("Error al parsear el documento");
            ex.printStackTrace();
            System.exit(-1);
        } catch (SAXException ex) {
            System.err.println("Error al parsear el documento");
            ex.printStackTrace();
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("Error al parsear el documento");
            ex.printStackTrace();
            System.exit(-1);
        } catch (IllegalArgumentException ex) {
            System.err.println("Error al parsear el documento");
            ex.printStackTrace();
            System.exit(-1);
        }
        return doc;
    }

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        org.apache.xml.security.Init.init();
    }
}
