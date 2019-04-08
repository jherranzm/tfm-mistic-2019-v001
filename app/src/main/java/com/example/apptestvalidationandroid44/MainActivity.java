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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.apptestvalidationandroid44.com.example.apptestvalidationandroid44.model.Invoice;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.cms.CMSException;
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
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

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



public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    //private Activity mActivity;
    ProgressBar mProgressBar;

    public static final String EXTRA_MESSAGE = "com.example.appsecond.MESSAGE";

    private static final String PKCS_12 = "PKCS12";
    private final static String PKCS12_PASSWORD = "Th2S5p2rStr4ngP1ss";
    private final static String CR_LF = "\n";


    private EditText editText;
    //private EditText editText2;
    private EditText urlEditText;
    private InputMethodManager imm;

    private static final String TAG = "MAIN_ACTIVITY";

    private X509Certificate certificate;
    private PrivateKey key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Get the application context
        mContext = getApplicationContext();
        mProgressBar = findViewById(R.id.progressBar1);
        mProgressBar.setVisibility(View.INVISIBLE);

        // Security
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");
            InputStream isServerCrt = this.getResources().openRawResource(R.raw.server);
            InputStream isServerKey = this.getResources().openRawResource(R.raw.serverkey);
            certificate = (X509Certificate) certFactory.generateCertificate(isServerCrt);

            isServerCrt.close();

            char[] keystorePassword = PKCS12_PASSWORD.toCharArray();
            char[] keyPassword = PKCS12_PASSWORD.toCharArray();

            KeyStore keystore = KeyStore.getInstance(PKCS_12, "BC");
            keystore.load(isServerKey, keystorePassword);
            isServerKey.close();

            key = (PrivateKey) keystore.getKey("Server", keyPassword);
            if(key == null) {
                Log.e(getLocalClassName(),"ERROR NO hay key!");
                throw new Exception("ERROR no hay Key!");
            }

        } catch (NoSuchAlgorithmException e) {
            Log.e("ERROR", ""+e.getLocalizedMessage());
            Toast.makeText(mContext, "ERROR : Algorithm " + e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
        } catch (UnrecoverableKeyException e) {
            Log.e("ERROR", ""+e.getLocalizedMessage());
            Toast.makeText(mContext, "ERROR : UnrecoverableKey " + e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
        } catch (KeyStoreException e) {
            Log.e("ERROR", ""+e.getLocalizedMessage());
            Toast.makeText(mContext, "ERROR : KeyStore " + e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
        }catch(Exception e){
            Log.e("ERROR", ""+e.getLocalizedMessage());
            Toast.makeText(mContext, "ERROR : genérico."+e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
        }


        editText = findViewById(R.id.editTextDataToEncrypt);
        //editText2 = findViewById(R.id.editTextResult);
        urlEditText = findViewById(R.id.editTextURL);
        Button getTitleURLButton = findViewById(R.id.buttonGetFacturasFromServer);
        Button goToShowUploadedInvoice = findViewById(R.id.buttonGoToShowUploadedInvoice);

        //Get the ID of button that will perform the network call
        Button btn =  findViewById(R.id.buttonEncrypt);
        assert btn != null;

        //String url ="https://www.google.com";
        //editText2.setText(url);

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


        // Set a click listener for button widget
        getTitleURLButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Empty the TextView
                //editText2.setText("");

                mProgressBar.setVisibility(View.VISIBLE);

                // Initialize a new RequestQueue instance
                RequestQueue requestQueue = Volley.newRequestQueue(mContext);

                // Initialize a new JsonArrayRequest instance
                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                        Request.Method.GET,
                        urlEditText.getText().toString(),
                        null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                // Do something with response
                                String message = "";

                                Log.i(TAG, response.toString());

                                // Process the JSON
                                try{
                                    // Loop through the array elements
                                    for(int i=0;i<response.length();i++){
                                        // Get current json object
                                        JSONObject factura = response.getJSONObject(i);

                                        // Get the current factura (json object) data
                                        long id = factura.getLong("id");
                                        String uid = factura.getString("uid");
                                        String taxIdentificationNumber = factura.getString("taxIdentificationNumber");
                                        String invoiceNumber = factura.getString("invoiceNumber");
                                        String issueDate = factura.getString("issueDate");
                                        double invoiceTotal = factura.getDouble("invoiceTotal");

                                        String iv = factura.getString("iv");
                                        String simKey = factura.getString("simKey");

                                        // Desencriptació amb clau privada de iv i simKey
                                        byte[] ivBytesDec = Base64.decode(iv, Base64.NO_WRAP);
                                        byte[] ivBytesEncDec = AsymmetricDecryptor.decryptData(ivBytesDec, key);
                                        String ivStringDec = new String(ivBytesEncDec);

                                        byte[] simKeyBytesDec = Base64.decode(simKey, Base64.NO_WRAP);
                                        byte[] simKeyBytesEncDec = AsymmetricDecryptor.decryptData(simKeyBytesDec, key);
                                        String simKeyStringDec = new String(simKeyBytesEncDec);

                                        SymmetricDecryptor simDec = new SymmetricDecryptor();
                                        simDec.setIv(ivStringDec);
                                        simDec.setKey(simKeyStringDec);

                                        String taxIdentificationNumberDecrypted = simDec.decrypt(taxIdentificationNumber);
                                        String invoiceNumberDecrypted = simDec.decrypt(invoiceNumber);
                                        String issueDateDecrypted = simDec.decrypt(issueDate);

                                        message += "ID : " +id + CR_LF;
                                        message += "CIF: " + taxIdentificationNumberDecrypted + CR_LF;
                                        message += "Num.Factura: " + invoiceNumberDecrypted + CR_LF;
                                        message += "Data Factura: " + issueDateDecrypted + CR_LF;
                                        message += "Total Factura : " + invoiceTotal + CR_LF + CR_LF;

                                        mProgressBar.setVisibility(View.INVISIBLE);

                                        Intent intent = new Intent(mContext, DisplayMessageActivity.class);
                                        intent.putExtra(EXTRA_MESSAGE, message);
                                        startActivity(intent);
                                    }
                                }catch (JSONException e){
                                    e.printStackTrace();
                                }catch (CMSException e){
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error){
                                // Do something when error occurred
                                Toast.makeText(mContext, "ERROR : genérico."+error.getLocalizedMessage() , Toast.LENGTH_LONG).show();
                            }
                        }
                );

                // Add JsonArrayRequest to the RequestQueue
                requestQueue.add(jsonArrayRequest);

                //mProgressBar.setVisibility(View.GONE);
            }
        });

        goToShowUploadedInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ShowUploadedInvoiceActivity.class);
                //intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onClick(View v) {
        editText.getText().clear();
    }

    public void verifySignedInvoice(View v){
        Intent intent = new Intent(this, DisplayMessageActivity.class);

        String message;
        try {


            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard,"Download/invoice_990001_xml_20190329_2020707_xml.xsig");
            if(!file.exists()){
                throw new FileNotFoundException();
            }

            InputStream isSignedInvoice = new FileInputStream(file);

            Log.i(TAG, "key.getAlgorithm : " + key.getAlgorithm());
            Toast.makeText(this.getApplicationContext(), "key.getAlgorithm : " + key.getAlgorithm(), Toast.LENGTH_SHORT).show();


            message = "";

            byte[] baInvoiceSigned = IOUtils.toByteArray(isSignedInvoice);

            message += CR_LF + "Longitud del fichero firmado : ["+baInvoiceSigned.length+"]";

            isSignedInvoice = new FileInputStream(file);
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
            assert facturae != null;
            message += CR_LF + String.format("Seller          : [%s]", facturae.getParties().getSellerParty().getTaxIdentification().getTaxIdentificationNumber());
            message += CR_LF + String.format("Seller          : [%s]", facturae.getParties().getSellerParty().getLegalEntity().getCorporateName());
            message += CR_LF + String.format("Factura         : [%s]", facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber());
            message += CR_LF + String.format("Import  factura : [%s]", facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal());
            message += CR_LF + String.format("Import brut     : [%s]", facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalGrossAmount());
            message += CR_LF + String.format("Impostos        : [%s]", facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalTaxOutputs());
            message += CR_LF + String.format("Data            : [%s]", facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getIssueDate());

            facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalGrossAmount();


            Log.i(TAG, "Inici...");
            RandomStringGenerator rsg = new RandomStringGenerator();

            String iv = rsg.getRandomString(16);
            Log.i(TAG, "iv : ["+iv+"]");
            String simKey = rsg.getRandomString(16);
            Log.i(TAG, "simKey : ["+simKey+"]");

            SymmetricEncryptor simEnc = new SymmetricEncryptor();
            simEnc.setIv(iv);
            simEnc.setKey(simKey);

            String taxIdentificationNumberEncrypted = simEnc.encrypt(facturae.getParties().getSellerParty().getTaxIdentification().getTaxIdentificationNumber());
            String invoiceNumberEncrypted = simEnc.encrypt(facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber());

            // Versió inicial: No s'encripten els imports, ja que si no el sistema NO pot fer càlculs
            String totalEncrypted  = ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal();
            String dataEncrypted   = simEnc.encrypt(""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getIssueDate());


            String signedInvoiceEncrypted   = simEnc.encrypt(baInvoiceSigned);

            StringBuilder sb = new StringBuilder();
            sb.append(facturae.getParties().getSellerParty().getTaxIdentification().getTaxIdentificationNumber());
            sb.append("|");
            sb.append(facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber());
            sb.append("|");
            sb.append(facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal());
            sb.append("|");
            sb.append(facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getIssueDate());

            Log.i(TAG, "UIDFacturaHash : ["+sb.toString()+"]");
            String UIDFacturaHash = UIDGenerator.generate(sb.toString());
            Log.i(TAG, "UIDFacturaHash : ["+UIDFacturaHash+"]");

            Invoice invoice = new Invoice(UIDFacturaHash
                    , facturae.getParties().getSellerParty().getTaxIdentification().getTaxIdentificationNumber()
                    , facturae.getParties().getSellerParty().getLegalEntity().getCorporateName()
                    , facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber()
                    , facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal()
                    , facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalTaxOutputs()
                    , facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getIssueDate()
                    );

            Log.i(TAG, "invoice : ["+invoice.toString()+"]");

            message += CR_LF;
            message += CR_LF + String.format("taxIdentificationNumberEncrypted : [%s]", taxIdentificationNumberEncrypted);
            message += CR_LF + String.format("invoiceNumberEncrypted : [%s]", invoiceNumberEncrypted);
            message += CR_LF + String.format("totalEncrypted  : [%s]", totalEncrypted);
            message += CR_LF + String.format("dataEncrypted   : [%s]", dataEncrypted);
            Log.i(TAG, String.format("taxIdentificationNumberEncrypted : [%d][%s]", taxIdentificationNumberEncrypted.length(), taxIdentificationNumberEncrypted));
            Log.i(TAG, String.format("invoiceNumberEncrypted : [%d][%s]", invoiceNumberEncrypted.length(), invoiceNumberEncrypted));
            Log.i(TAG, String.format("totalEncrypted  : [%s]", totalEncrypted));
            Log.i(TAG, String.format("dataEncrypted   : [%d][%s]", dataEncrypted.length(), dataEncrypted));
            Log.i(TAG, String.format("signedInvoiceEncrypted   : [%d][%s]", signedInvoiceEncrypted.length(), signedInvoiceEncrypted));

            // Encriptació amb clau pública de iv i simKey
            byte[] ivBytesEnc = AsymmetricEncryptor.encryptData(iv.getBytes(), certificate);
            String ivStringEnc = new String(Base64.encode(ivBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);
            byte[] simKeyBytesEnc = AsymmetricEncryptor.encryptData(simKey.getBytes(), certificate);
            String simKeyStringEnc = new String(Base64.encode(simKeyBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);

            message += CR_LF;
            //message += CR_LF + String.format("ivStringEnc      : [%s]", ivStringEnc);
            //message += CR_LF + String.format("simKeyStringEnc  : [%s]", simKeyStringEnc);
            Log.i(TAG, String.format("ivStringEnc      : [%d][%s]", ivStringEnc.length(), ivStringEnc));
            Log.i(TAG, String.format("simKeyStringEnc  : [%d][%s]", simKeyStringEnc.length(), simKeyStringEnc));

            // Desencriptació amb clau privada de iv i simKey
            byte[] ivBytesDec = Base64.decode(ivStringEnc, Base64.NO_WRAP);
            byte[] ivBytesEncDec = AsymmetricDecryptor.decryptData(ivBytesDec, key);
            String ivStringDec = new String(ivBytesEncDec);

            byte[] simKeyBytesDec = Base64.decode(simKeyStringEnc, Base64.NO_WRAP);
            byte[] simKeyBytesEncDec = AsymmetricDecryptor.decryptData(simKeyBytesDec, key);
            String simKeyStringDec = new String(simKeyBytesEncDec);

            Log.i(TAG, "iv : ["+ivStringDec+"]");
            Log.i(TAG, "simKey : ["+simKeyStringDec+"]");

            Log.i(TAG, "iv : ["+iv+"]["+ivStringDec+"]");
            Log.i(TAG, "simKey : ["+simKey+"]["+simKeyStringDec+"]");


            SymmetricDecryptor simDec = new SymmetricDecryptor();
            simDec.setIv(ivStringDec);
            simDec.setKey(simKeyStringDec);

            String sellerDecrypted = simDec.decrypt(taxIdentificationNumberEncrypted);
            String totalDecrypted  = totalEncrypted; //simDec.decrypt(totalEncrypted);
            String dataDecrypted   = simDec.decrypt(dataEncrypted);

            message += CR_LF;
            message += CR_LF + String.format("sellerDecrypted : [%s]", sellerDecrypted);
            message += CR_LF + String.format("totalDecrypted  : [%s]", totalDecrypted);
            message += CR_LF + String.format("dataDecrypted   : [%s]", dataDecrypted);
            Log.i(TAG, String.format("sellerDecrypted : [%s]", sellerDecrypted));
            Log.i(TAG, String.format("totalDecrypted  : [%s]", totalDecrypted));
            Log.i(TAG, String.format("dataDecrypted   : [%s]", dataDecrypted));

            Map<String, String> params = new HashMap<>();
            params.put("uidfactura", (UIDFacturaHash == null ? "---" : UIDFacturaHash));
            params.put("seller", taxIdentificationNumberEncrypted);
            params.put("invoicenumber", invoiceNumberEncrypted);
            params.put("total", totalEncrypted);
            params.put("totaltaxoutputs", ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalTaxOutputs());
            params.put("data", dataEncrypted);
            params.put("file", signedInvoiceEncrypted);
            params.put("iv", ivStringEnc);
            params.put("key", simKeyStringEnc);

            PostDataToUrlTask getData = new PostDataToUrlTask(params);

            try {
                String url = urlEditText.getText().toString();
                String res = getData.execute(url).get();
                //editText2.setText(res);
                message += CR_LF;
                message += CR_LF + String.format("res : [%s]", res);
            } catch (Exception e) {
                Log.i(TAG, "public void getURL() — get item number " + e.getMessage());
                //String res = "Ups... error en GetDataFromUrlTask " + e.getMessage();
                //editText2.setText(res);
            }

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


    public void onClickEncryptButton(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);

        String message;
        try {
            String inputText = editText.getText().toString();
            Log.i(getLocalClassName(),"inputText : [" + inputText + "]");

            // Encriptem el text
            byte[] messageEncrypted = AsymmetricEncryptor.encryptData(inputText.getBytes(), certificate);
            Log.i(getLocalClassName(),"messageEncrypted : [" + new String(messageEncrypted) + "]");

            // Codifiquem el text en Base64 per poder-lo enviar
            byte[] messageEncryptedEncodedB64 = Base64.encode(messageEncrypted, Base64.NO_WRAP);//.encode(message.getBytes());
            Log.i(getLocalClassName(),"messageEncryptedEncodedB64 : [" + new String(messageEncryptedEncodedB64) + "]");
            //message = new String(messageEncryptedEncodedB64);

            // Decodifiquem el text de Base64
            byte[] messageEncryptedDecodedB64 = Base64.decode(messageEncryptedEncodedB64, Base64.NO_WRAP);
            Log.i(getLocalClassName(),"messageEncryptedDecodedB64 : [" + new String(messageEncryptedDecodedB64) + "]");
            //message = new String(messageEncryptedDecodedB64);

            // Desencriptem el text
            byte[] messageDecodedB64Decrypted = AsymmetricDecryptor.decryptData(messageEncryptedDecodedB64, key);
            Log.i(getLocalClassName(),"messageDecodedB64Decrypted : [" + new String(messageDecodedB64Decrypted) + "]");
            //message = inputText + "**:**" + new String(messageDecodedB64Decrypted);


            InputStream isInvoice = this.getResources().openRawResource(R.raw.invoice990001);
            Log.i(getLocalClassName(),"inputStreamInvoice ...");


            byte[] fileContent = IOUtils.toByteArray(isInvoice);
            Log.i(getLocalClassName(),"inputStreamInvoice : fileContent.length " + fileContent.length);

            byte[] fileContentEncrypted = AsymmetricEncryptor.encryptData(fileContent, certificate);
            Log.i(getLocalClassName(),"inputStreamInvoice : fileContentEncrypted.length " + fileContentEncrypted.length);

            byte[] fileContentEncryptedDecrypted = AsymmetricDecryptor.decryptData(fileContentEncrypted, key);
            Log.i(getLocalClassName(),"inputStreamInvoice : fileContentEncryptedDecrypted.length " + fileContentEncryptedDecrypted.length);


            message = CR_LF +"Text original : ["+inputText+"]";
            message += CR_LF + "Text encriptat i desencriptat :  [" + new String(messageDecodedB64Decrypted) +"]";
            message += CR_LF + "Longitud fitxer original :  [" + fileContent.length +"]";
            message += CR_LF + "Longitud fitxer encriptat i desencriptat :  [" + fileContentEncryptedDecrypted.length +"]";

            isInvoice.close();

        } catch (CertificateException e) {
            e.printStackTrace();
            message = e.getLocalizedMessage();
        } catch (IOException e) {
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

    /** Called when the user taps the Send button */
    public void getURL(View view) {

        // Ocultar teclat...
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        //GetDataFromUrlTask getData = new GetDataFromUrlTask();

        try {
            String url = urlEditText.getText().toString();
            Log.i(TAG, "getURL(): " + url);
            //String res = getData.execute(url).get();
            //editText2.setText(res);
        } catch (Exception e) {
            Log.i(TAG, "public void getURL() — get item number " + e.getMessage());
            //String res = "Ups... error en GetDataFromUrlTask " + e.getMessage();
            //editText2.setText(res);
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

    private Facturae getFacturaeFromFactura(String factura){
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
