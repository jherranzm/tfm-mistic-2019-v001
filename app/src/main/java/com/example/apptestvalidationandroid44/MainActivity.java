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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.apptestvalidationandroid44.com.example.apptestvalidationandroid44.util.FacturaeNamespaceContext;
import com.example.apptestvalidationandroid44.com.example.apptestvalidationandroid44.util.RandomStringGenerator;
import com.example.apptestvalidationandroid44.com.example.apptestvalidationandroid44.util.UIDGenerator;
import com.example.apptestvalidationandroid44.crypto.AsymmetricDecryptor;
import com.example.apptestvalidationandroid44.crypto.AsymmetricEncryptor;
import com.example.apptestvalidationandroid44.crypto.SymmetricDecryptor;
import com.example.apptestvalidationandroid44.crypto.SymmetricEncryptor;

import org.apache.commons.io.IOUtils;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.Constants;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import es.facturae.facturae.v3.facturae.Facturae;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.core.signers.AOSigner;
import es.gob.afirma.signers.xades.AOFacturaESigner;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_MESSAGE = "com.example.appsecond.MESSAGE";

    private static final String PKCS_12 = "PKCS12";
    private final static String PKCS12_PASSWORD = "Th2S5p2rStr4ngP1ss";
    private final static String CR_LF = "\n";


    private EditText editText;
    private EditText editText2;
    private EditText urlEditText;
    private InputMethodManager imm;

    private static final String TAG = "MAIN_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        editText2 = findViewById(R.id.editText2);
        urlEditText = findViewById(R.id.urlEditText);

        //Get the ID of button that will perform the network call
        Button btn =  findViewById(R.id.button);
        assert btn != null;

        String url ="https://www.google.com";
        editText2.setText(url);

        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        // 2019-03-30
        // Check whether this app has write external storage permission or not.
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // If do not grant write external storage permission.
        if(writeExternalStoragePermission!= PackageManager.PERMISSION_GRANTED)
        {
            // Request user to grant write external storage permission.
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onClick(View v) {
        editText.getText().clear();
    }

    public void verifySignedInvoice(View v){
        Intent intent = new Intent(this, DisplayMessageActivity.class);

        String message;
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");

            InputStream isServerCrt = this.getResources().openRawResource(R.raw.server);
            InputStream isServerKey = this.getResources().openRawResource(R.raw.serverkey);

            //InputStream isSignedInvoice = this.getResources().openRawResource(R.raw.invoice_990001_xml_20190329_2020707_xml);

            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard,"Download/invoice_990001_xml_20190329_2020707_xml.xsig");
            if(!file.exists()){
                throw new FileNotFoundException();
            }

            InputStream isSignedInvoice = new FileInputStream(file);

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
                throw new Exception("ERROR no hay Key!");
            }
            Log.i(TAG, "key.getAlgorithm : " + key.getAlgorithm());
            Toast.makeText(this.getApplicationContext(), "key.getAlgorithm : " + key.getAlgorithm(), Toast.LENGTH_SHORT);


            Provider provider = keystore.getProvider();

            message = "Provider : [" +provider.getName()+"] : [" +provider.getInfo()+"]";

            byte[] baInvoiceSigned = IOUtils.toByteArray(isSignedInvoice);

            message += CR_LF + "Longitud del fichero firmado : ["+baInvoiceSigned.length+"]";


            isSignedInvoice = this.getResources().openRawResource(R.raw.invoice_990001_xml_20190329_2020707_xml);

            Document doc = getDocument(isSignedInvoice);

            message += CR_LF + "root : " + doc.getDocumentElement().getTagName();

            boolean valid = isValid(certificate, doc);

            if(valid){
                message += CR_LF;
                message += CR_LF + "La firma es válida!";
            }else{
                message += CR_LF;
                message += CR_LF + "ERROR : La firma NO es válida!";
            }

            Document document = removeSignature(doc);

            Facturae facturae = getFacturaeFromFactura(documentToString(document));

            message += CR_LF;
            message += CR_LF + String.format("Seller          : [%s]", facturae.getParties().getSellerParty().getTaxIdentification().getTaxIdentificationNumber());
            message += CR_LF + String.format("Seller          : [%s]", facturae.getParties().getSellerParty().getLegalEntity().getCorporateName());
            message += CR_LF + String.format("Factura         : [%s]", facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber());
            message += CR_LF + String.format("Import  factura : [%s]", facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal());
            message += CR_LF + String.format("Impostos        : [%s]", facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalTaxOutputs());
            message += CR_LF + String.format("Data            : [%s]", facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getIssueDate());


            Log.i(TAG, "Inici...");
            RandomStringGenerator rsg = new RandomStringGenerator();

            String iv = rsg.getRandomString(16);
            Log.i(TAG, "iv : ["+iv+"]");
            String simKey = rsg.getRandomString(16);
            Log.i(TAG, "simKey : ["+simKey+"]");

            SymmetricEncryptor simEnc = new SymmetricEncryptor();
            simEnc.setIv(iv);
            simEnc.setKey(simKey);

            String sellerEncripted = simEnc.encrypt(facturae.getParties().getSellerParty().getTaxIdentification().getTaxIdentificationNumber());
            String totalEncripted  = simEnc.encrypt(""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal());
            String dataEncripted   = simEnc.encrypt(""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getIssueDate());

            StringBuffer sb = new StringBuffer();
            sb.append(facturae.getParties().getSellerParty().getTaxIdentification().getTaxIdentificationNumber());
            sb.append("|");
            sb.append(facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber());
            sb.append("|");
            sb.append(facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal());

            Log.i(TAG, "UIDFacturaHash : ["+sb.toString()+"]");
            String UIDFacturaHash = UIDGenerator.generate(sb.toString());
            Log.i(TAG, "UIDFacturaHash : ["+UIDFacturaHash+"]");

            message += CR_LF;
            message += CR_LF + String.format("sellerEncripted : [%s]", sellerEncripted);
            message += CR_LF + String.format("totalEncripted  : [%s]", totalEncripted);
            message += CR_LF + String.format("dataEncripted   : [%s]", dataEncripted);
            Log.i("Enmascarament", String.format("sellerEncripted : [%s]", sellerEncripted));
            Log.i("Enmascarament", String.format("totalEncripted  : [%s]", totalEncripted));
            Log.i("Enmascarament", String.format("dataEncripted   : [%s]", dataEncripted));

            // Encriptació amb clau pública de iv i simKey
            byte[] ivBytesEnc = AsymmetricEncryptor.encryptData(iv.getBytes(), certificate);
            String ivStringEnc = new String(Base64.encode(ivBytesEnc, Base64.DEFAULT), StandardCharsets.UTF_8);
            byte[] simKeyBytesEnc = AsymmetricEncryptor.encryptData(simKey.getBytes(), certificate);
            String simKeyStringEnc = new String(Base64.encode(simKeyBytesEnc, Base64.DEFAULT), StandardCharsets.UTF_8);

            message += CR_LF;
            //message += CR_LF + String.format("ivStringEnc      : [%s]", ivStringEnc);
            //message += CR_LF + String.format("simKeyStringEnc  : [%s]", simKeyStringEnc);
            Log.i(TAG, String.format("ivStringEnc      : [%d][%s]", ivStringEnc.length(), ivStringEnc));
            Log.i(TAG, String.format("simKeyStringEnc  : [%d][%s]", simKeyStringEnc.length(), simKeyStringEnc));

            // Desencriptació amb clau privada de iv i simKey
            byte[] ivBytesDec = Base64.decode(ivStringEnc, Base64.DEFAULT);
            byte[] ivBytesEncDec = AsymmetricDecryptor.decryptData(ivBytesDec, key);
            String ivStringDec = new String(ivBytesEncDec);

            byte[] simKeyBytesDec = Base64.decode(simKeyStringEnc, Base64.DEFAULT);
            byte[] simKeyBytesEncDec = AsymmetricDecryptor.decryptData(simKeyBytesDec, key);
            String simKeyStringDec = new String(simKeyBytesEncDec);

            Log.i(TAG, "iv : ["+ivStringDec+"]");
            Log.i(TAG, "simKey : ["+simKeyStringDec+"]");

            Log.i(TAG, "iv : ["+iv+"]["+ivStringDec+"]");
            Log.i(TAG, "simKey : ["+simKey+"]["+simKeyStringDec+"]");


            SymmetricDecryptor simDec = new SymmetricDecryptor();
            simDec.setIv(ivStringDec);
            simDec.setKey(simKeyStringDec);

            String sellerDecripted = simDec.decrypt(sellerEncripted);
            String totalDecripted  = simDec.decrypt(totalEncripted);
            String dataDecripted   = simDec.decrypt(dataEncripted);

            message += CR_LF;
            message += CR_LF + String.format("sellerDecripted : [%s]", sellerDecripted);
            message += CR_LF + String.format("totalDecripted  : [%s]", totalDecripted);
            message += CR_LF + String.format("dataDecripted   : [%s]", dataDecripted);
            Log.i(TAG, String.format("sellerDecripted : [%s]", sellerDecripted));
            Log.i(TAG, String.format("totalDecripted  : [%s]", totalDecripted));
            Log.i(TAG, String.format("dataDecripted   : [%s]", dataDecripted));


        }catch (Exception e) {
            e.printStackTrace();
            message = "Exception:" + e.getLocalizedMessage();
        }

        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    private boolean isValid(X509Certificate certificate, Document doc) throws Exception {
        NodeList nl = doc.getElementsByTagNameNS(Constants.SignatureSpecNS, "Signature");
        if (nl.getLength() == 0) {
            throw new Exception("No XML Digital Signature Found, document is discarded");
        }

        Element sigElement = (Element) nl.item(0);
        XMLSignature signature = new XMLSignature(sigElement, "");

        return signature.checkSignatureValue(certificate.getPublicKey());
    }

    public void signInvoice(View v){

        Intent intent = new Intent(this, DisplayMessageActivity.class);

        String message;

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
                throw new Exception("ERROR no hay Key!");
            }
            Log.i("", "key.getAlgorithm : " + key.getAlgorithm());

            Provider provider = keystore.getProvider();

            message = "Provider : [" +provider.getName()+"] : [" +provider.getInfo()+"]";

            PrivateKeyEntry pke = (PrivateKeyEntry) keystore.getEntry("Server", new KeyStore.PasswordProtection(PKCS12_PASSWORD.toCharArray()));



            message += CR_LF + "Longitud del fichero firmado : ["+baInvoiceSigned.length+"]";


            final AOSigner signer = new AOFacturaESigner();

            final byte[] result = signer.sign(
                    baInvoiceSigned,
                    AOSignConstants.SIGN_ALGORITHM_SHA256WITHRSA,
                    key,
                    pke.getCertificateChain(),
                    new Properties()
            );


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

        String message;
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
                throw new Exception("ERROR no hay Key!");
            }
            Log.i(TAG, "key.getAlgorithm : " + key.getAlgorithm());

            Provider provider = keystore.getProvider();

            //getInfoOverProviders(provider);
            //getInfoOverAllProviders();

            String inputText = editText.getText().toString();

            // Encriptem el text
            byte[] messageEncrypted = AsymmetricEncryptor.encryptData(inputText.getBytes(), certificate);
            Log.i(getLocalClassName(),"messageEncrypted : [" + new String(messageEncrypted) + "]");

            // Codifiquem el text en Base64 per poder-lo enviar
            byte[] messageEncryptedEncodedB64 = Base64.encode(messageEncrypted, Base64.DEFAULT);//.encode(message.getBytes());
            Log.i(getLocalClassName(),"messageEncryptedEncodedB64 : [" + new String(messageEncryptedEncodedB64) + "]");
            //message = new String(messageEncryptedEncodedB64);

            // Decodifiquem el text de Base64
            byte[] messageEncryptedDecodedB64 = Base64.decode(messageEncryptedEncodedB64, Base64.DEFAULT);
            Log.i(getLocalClassName(),"messageEncryptedDecodedB64 : [" + new String(messageEncryptedDecodedB64) + "]");
            //message = new String(messageEncryptedDecodedB64);

            // Desencriptem el text
            byte[] messageDecodedB64Decrypted = AsymmetricDecryptor.decryptData(messageEncryptedDecodedB64, key);
            Log.i(getLocalClassName(),"messageDecodedB64Decrypted : [" + new String(messageDecodedB64Decrypted) + "]");
            //message = inputText + "**:**" + new String(messageDecodedB64Decrypted);


            InputStream isInvoice = this.getResources().openRawResource(R.raw.invoice990001);
            InputStream isInvoiceSigned = this.getResources().openRawResource(R.raw.invoice_990001_xml_20190329_2020707_xml);


            byte[] baInvoiceSigned = IOUtils.toByteArray(isInvoiceSigned);
            byte[] fileContent = IOUtils.toByteArray(isInvoice);
            byte[] fileContentEncrypted = AsymmetricEncryptor.encryptData(fileContent, certificate);
            byte[] fileContentEncryptedDecrypted = AsymmetricDecryptor.decryptData(fileContentEncrypted, key);

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

    private void getInfoOverProviders(Provider provider) {
        for (Provider p : Security.getProviders()) {

            Log.i(getLocalClassName(),"Provider:["+p.getName()+"] ["+p.getInfo()+"]");
        }

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
                StringBuilder sb = new StringBuilder();
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
            String res = "Ups... error en GetDataFromUrlTask " + e.getMessage();
            editText2.setText(res);
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

    private Document getDocument(InputStream isDocument) {
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

    private Document removeSignature(Document document){

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();

        xpath.setNamespaceContext(new FacturaeNamespaceContext());

        NodeList list = document.getElementsByTagNameNS(Constants.SignatureSpecNS, "Signature");
        list.item(0).getParentNode().removeChild(list.item(0));

        return document;
    }

    private String documentToString(Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }

    private String getInfoFromFactura(String factura){
        String res = "";
        try
        {
            IBindingFactory bfact = BindingDirectory.getFactory(Facturae.class);
            IUnmarshallingContext uctx = bfact.createUnmarshallingContext();

            Facturae facturae = (Facturae)uctx.unmarshalDocument(new ByteArrayInputStream(factura.getBytes()), null);
            res += CR_LF + String.format("Seller          : [%s]", facturae.getParties().getSellerParty().getTaxIdentification().getTaxIdentificationNumber());
            res += CR_LF + String.format("Seller          : [%s]", facturae.getParties().getSellerParty().getLegalEntity().getCorporateName());
            res += CR_LF + String.format("Factura         : [%s]", facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber());
            res += CR_LF + String.format("Importe factura : [%s]", facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal());
            res += CR_LF + String.format("Impuestos       : [%s]", facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalTaxOutputs());
        }catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    private Facturae getFacturaeFromFactura(String factura){
        String res = "";
        Facturae facturae;
        try
        {
            IBindingFactory bfact = BindingDirectory.getFactory(Facturae.class);
            IUnmarshallingContext uctx = bfact.createUnmarshallingContext();

            return (Facturae)uctx.unmarshalDocument(new ByteArrayInputStream(factura.getBytes()), null);
         }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        org.apache.xml.security.Init.init();
    }
}
