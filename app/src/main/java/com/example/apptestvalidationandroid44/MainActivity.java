package com.example.apptestvalidationandroid44;

import android.Manifest;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
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
import com.example.apptestvalidationandroid44.crypto.AsymmetricDecryptor;
import com.example.apptestvalidationandroid44.crypto.AsymmetricEncryptor;
import com.example.apptestvalidationandroid44.crypto.SymmetricDecryptor;
import com.example.apptestvalidationandroid44.crypto.SymmetricEncryptor;
import com.example.apptestvalidationandroid44.model.FileDataObject;
import com.example.apptestvalidationandroid44.model.Invoice;
import com.example.apptestvalidationandroid44.model.LocalSimKey;
import com.example.apptestvalidationandroid44.model.LocalSimKeyRepository;
import com.example.apptestvalidationandroid44.util.FacturaeNamespaceContext;
import com.example.apptestvalidationandroid44.util.RandomStringGenerator;
import com.example.apptestvalidationandroid44.util.TFMSecurityManager;
import com.example.apptestvalidationandroid44.util.UIDGenerator;

import org.apache.commons.io.IOUtils;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.Constants;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.json.JSONArray;
import org.json.JSONObject;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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



public class MainActivity
        extends AppCompatActivity{

    private Context mContext;
    private ProgressBar mProgressBar;

    public static final String EXTRA_MESSAGE = "com.example.apptestvalidationandroid44.MESSAGE";
    public static final String INVOICE_LIST = "com.example.apptestvalidationandroid44.INVOICE_LIST";
    public static final String FILE_LIST = "com.example.apptestvalidationandroid44.FILE_LIST";
    public static final String CR_LF = "\n";
    //public static final String PKCS_12 = "PKCS12";
    //public static final String PKCS12_PASSWORD = "Th2S5p2rStr4ngP1ss";

    private EditText editText;
    private EditText urlEditText;
    InputMethodManager imm;

    private static final String TAG = "MAIN_ACTIVITY";

    private TFMSecurityManager tfmSecurityManager;

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

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

        editText = findViewById(R.id.editTextDataToEncrypt);
        urlEditText = findViewById(R.id.editTextURL);

        Button goToShowUploadedInvoices = findViewById(R.id.buttonGoToShowUploadedInvoice);
        Button goToShowLocalInvoices = findViewById(R.id.buttonShowLocalInvoices);
        Button btn =  findViewById(R.id.buttonEncrypt);
        assert btn != null;

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



        goToShowUploadedInvoices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

                                Log.i(TAG, response.toString());

                                // Process the JSON
                                try{
                                    ArrayList<Invoice> invoices;

                                    invoices = getInvoicesFromResponse(response);

                                    StringBuilder message = new StringBuilder();
                                    for(Invoice invoice : invoices) {
                                        message.append("ID : ").append(invoice.getUid()).append(CR_LF);
                                        message.append("CIF: ").append(invoice.getTaxIdentificationNumber()).append(CR_LF);
                                        message.append("Num.Factura: ").append(invoice.getInvoiceNumber()).append(CR_LF);
                                        message.append("Data Factura: ").append(invoice.getIssueDate()).append(CR_LF);
                                        message.append("Total Factura : ").append(invoice.getInvoiceTotal()).append(CR_LF).append(CR_LF);
                                    }

                                    mProgressBar.setVisibility(View.INVISIBLE);

                                    Intent intent = new Intent(mContext, UploadedInvoicesRecyclerViewActivity.class);
                                    intent.putExtra(EXTRA_MESSAGE, message.toString());
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
                    FileDataObject obj = new FileDataObject();
                    obj.setFileName(f.getName());
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
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");
            InputStream isServerCrt = this.getResources().openRawResource(R.raw.server);
            InputStream isServerKey = this.getResources().openRawResource(R.raw.serverkey);
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(isServerCrt);
            tfmSecurityManager.setCertificate(certificate);


            isServerCrt.close();

            char[] keystorePassword = tfmSecurityManager.PKCS12_PASSWORD.toCharArray();
            char[] keyPassword = tfmSecurityManager.PKCS12_PASSWORD.toCharArray();

            KeyStore keystore = KeyStore.getInstance(tfmSecurityManager.PKCS_12, "BC");
            keystore.load(isServerKey, keystorePassword);
            isServerKey.close();

            PrivateKey key = (PrivateKey) keystore.getKey("Server", keyPassword);
            if(key == null) {
                Log.e(getLocalClassName(),"ERROR NO hay key!");
                throw new Exception("ERROR no hay Key!");
            }
            tfmSecurityManager.setKey(key);

            LocalSimKeyRepository repo = new LocalSimKeyRepository(mContext);
            Log.i(TAG, "repo es nulo ? " + (repo == null ? "Nulo!":"Correcto!"));
            //GetAllLocalSimKeysTask galskTask = new GetAllLocalSimKeysTask(mContext);

            final List<LocalSimKey> keyList = new ArrayList<>();
            repo.getAll().observe(this, new Observer<List<LocalSimKey>>() {
                @Override
                public void onChanged(@Nullable List<LocalSimKey> notes) {
                    //keyList = notes;
                    for(LocalSimKey note : notes) {
                        keyList.add(note);
                        Log.i(TAG, "-----------------------");
                        Log.i(TAG, ""+note.getId());
                        Log.i(TAG, note.getF());
                        Log.i(TAG, note.getK());
                    }

                }
            });
            Log.i(TAG, "keyList es nulo ? " + (keyList == null ? "Nulo!":"Correcto!"));
            Log.i(TAG, "keyList:size() :  " + keyList.size());

//            Log.i(TAG, "keyList es nulo ? " + (keyList == null ? "Nulo!":"Correcto!"));
//            if(keyList == null){
//                LocalSimKey lsk = new LocalSimKey();
//                lsk.setF("f1");
//                RandomStringGenerator rsg = new RandomStringGenerator();
//                String simKey = rsg.getRandomString(16);
//                byte[] simKeyBytesEnc = AsymmetricEncryptor.encryptData(simKey.getBytes(), tfmSecurityManager.getCertificate());
//                String simKeyStringEnc = new String(Base64.encode(simKeyBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);
//                lsk.setK(simKeyStringEnc);
//
//                repo.insert(lsk);
//                Log.i(TAG, "Se supone que ahora hemos ingresado un objeto...");
//                List<LocalSimKey> newKeyList = repo.getAll().getValue();
//                Log.i(TAG, "newKeyList es nulo ? " + (newKeyList == null ? "Nulo!":"Correcto!"));
//
//            }else{
//                Log.i(TAG, ""+keyList.size());
//                for( LocalSimKey l : keyList){
//                    Log.i(TAG, l.toString());
//                }
//            }


//            List<LocalSimKey> taskList = DatabaseClient
//                    .getInstance(getApplicationContext())
//                    .getAppDatabase()
//                    .localSimKeyDao()
//                    .getAll();
//            Log.i(TAG, "LocalSimKey.length : " + taskList.size());
//
//            if(taskList.size() == 0) {
//                LocalSimKey lsk = new LocalSimKey();
//                lsk.setF("f1");
//                RandomStringGenerator rsg = new RandomStringGenerator();
//                String simKey = rsg.getRandomString(16);
//                byte[] simKeyBytesEnc = AsymmetricEncryptor.encryptData(simKey.getBytes(), tfmSecurityManager.getCertificate());
//                String simKeyStringEnc = new String(Base64.encode(simKeyBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);
//                lsk.setK(simKeyStringEnc);
//                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
//                        .localSimKeyDao()
//                        .insert(lsk);
//            }

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
    }

    public void verifySignedInvoice(View v){
        Intent intent = new Intent(this, DisplayMessageActivity.class);

        String message;
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            //File file = new File(sdcard,"Download/invoice_990001_xml_20190329_2020707_xml.xsig");
            //File file = new File(sdcard,"Download/invoice_990002_xml_20190329_2020915_xml.xsig");
            File file = new File(sdcard,"Download/invoice_990003_xml_20190329_2020102_xml.xsig");
            //File file = new File(sdcard,"Download/invoice_990004_xml_20190329_2020276_xml.xsig");
            //File file = new File(sdcard,"Download/invoice_990005_xml_20190329_2020430_xml.xsig");
            if(!file.exists()){
                throw new FileNotFoundException();
            }

            Toast.makeText(mContext, "Cargando el fichero firmado...", Toast.LENGTH_SHORT).show();
            InputStream isSignedInvoice = new FileInputStream(file);

            message = "";

            byte[] baInvoiceSigned = IOUtils.toByteArray(isSignedInvoice);
            Toast.makeText(mContext, "Longitud del fichero firmado : ["+baInvoiceSigned.length+"]", Toast.LENGTH_SHORT).show();

            isSignedInvoice = new FileInputStream(file);
            Document doc = getDocument(isSignedInvoice);

            Toast.makeText(mContext, "Documento factura procesado!", Toast.LENGTH_SHORT).show();

            boolean valid = isValid(tfmSecurityManager.getCertificate(), doc);

            if(!valid){
                message += CR_LF;
                message += CR_LF + "ERROR : La firma NO es válida!";
                Toast.makeText(mContext, "ERROR : La firma NO es válida!", Toast.LENGTH_LONG).show();
            }else{
                message += CR_LF;
                message += CR_LF + "La firma es válida!";
                Toast.makeText(mContext, "Documento factura válida!", Toast.LENGTH_SHORT).show();

                Document document = removeSignature(doc);

                Toast.makeText(mContext, "Firma eliminada!", Toast.LENGTH_SHORT).show();

                Facturae facturae = getFacturaeFromFactura(documentToString(document));

                Toast.makeText(mContext, "Recuperando datos de factura...", Toast.LENGTH_SHORT).show();

                message += CR_LF;
                assert facturae != null;
                message += CR_LF + String.format("Seller          : [%s]", facturae.getParties().getSellerParty().getTaxIdentification().getTaxIdentificationNumber());
                message += CR_LF + String.format("Seller          : [%s]", facturae.getParties().getSellerParty().getLegalEntity().getCorporateName());
                message += CR_LF + String.format("Factura         : [%s]", facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber());
                message += CR_LF + String.format("Import  factura : [%s]", facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal());
                message += CR_LF + String.format("Import brut     : [%s]", facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalGrossAmount());
                message += CR_LF + String.format("Impostos        : [%s]", facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalTaxOutputs());
                message += CR_LF + String.format("Data            : [%s]", facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getIssueDate());


                Toast.makeText(mContext, "Encriptando datos de factura...", Toast.LENGTH_SHORT).show();
                // Encriptación de los datos
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

                Invoice invoice = new Invoice(
                        UIDFacturaHash
                        , facturae.getParties().getSellerParty().getTaxIdentification().getTaxIdentificationNumber()
                        , facturae.getParties().getSellerParty().getLegalEntity().getCorporateName()
                        , facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber()
                        , facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal()
                        , facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalTaxOutputs()
                        , facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getIssueDate()
                );

                Log.i(TAG, "invoice : ["+invoice.toString()+"]");

                // Encriptació amb clau pública de iv i simKey
                byte[] ivBytesEnc = AsymmetricEncryptor.encryptData(iv.getBytes(), tfmSecurityManager.getCertificate());
                String ivStringEnc = new String(Base64.encode(ivBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);
                byte[] simKeyBytesEnc = AsymmetricEncryptor.encryptData(simKey.getBytes(), tfmSecurityManager.getCertificate());
                String simKeyStringEnc = new String(Base64.encode(simKeyBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);


                Map<String, String> params = new HashMap<>();
                params.put("uidfactura", (UIDFacturaHash == null ? "---" : UIDFacturaHash));
                params.put("seller", taxIdentificationNumberEncrypted);
                params.put("invoicenumber", invoiceNumberEncrypted);
                params.put("total", totalEncrypted);
                params.put("totaltaxoutputs", ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalTaxOutputs());
                params.put("totalgrossamount", ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalGrossAmount());
                params.put("data", dataEncrypted);
                params.put("file", signedInvoiceEncrypted);
                params.put("iv", ivStringEnc);
                params.put("key", simKeyStringEnc);

                PostDataToUrlTask getData = new PostDataToUrlTask(params);

                String url = urlEditText.getText().toString();
                String res = getData.execute(url).get();

                if (getData.getResponseCode() == 400){
                    message += CR_LF + String.format("res : [%s]", res);
                }else if (getData.getResponseCode() == 409){
                    message += CR_LF + String.format("ALERTA: La factura ya está registrada en el sistema! %s", "");
                }

                Log.i(TAG, "Respuesta del Servidor : ["+res+"]");

            } // if(valid)

        }catch (Exception e) {
            Log.e(TAG, "ERROR: " + e.getMessage());
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
            byte[] messageEncrypted = AsymmetricEncryptor.encryptData(inputText.getBytes(), tfmSecurityManager.getCertificate());
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
            byte[] messageDecodedB64Decrypted = AsymmetricDecryptor.decryptData(messageEncryptedDecodedB64, tfmSecurityManager.getKey());
            Log.i(getLocalClassName(),"messageDecodedB64Decrypted : [" + new String(messageDecodedB64Decrypted) + "]");
            //message = inputText + "**:**" + new String(messageDecodedB64Decrypted);


            InputStream isInvoice = this.getResources().openRawResource(R.raw.invoice990001);
            Log.i(getLocalClassName(),"inputStreamInvoice ...");


            byte[] fileContent = IOUtils.toByteArray(isInvoice);
            Log.i(getLocalClassName(),"inputStreamInvoice : fileContent.length " + fileContent.length);

            byte[] fileContentEncrypted = AsymmetricEncryptor.encryptData(fileContent, tfmSecurityManager.getCertificate());
            Log.i(getLocalClassName(),"inputStreamInvoice : fileContentEncrypted.length " + fileContentEncrypted.length);

            byte[] fileContentEncryptedDecrypted = AsymmetricDecryptor.decryptData(fileContentEncrypted, tfmSecurityManager.getKey());
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

        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
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

    private ArrayList<Invoice> getInvoicesFromResponse(JSONArray response){
        ArrayList<Invoice> invoices = new ArrayList<>();

        try{
            // Loop through the array elements
            for(int i=0;i<response.length();i++){
                // Get current json object
                JSONObject factura = response.getJSONObject(i);

                // Get the current factura (json object) data
                //long id = factura.getLong("id");
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

                String taxIdentificationNumberDecrypted = simDec.decrypt(taxIdentificationNumber);
                String invoiceNumberDecrypted = simDec.decrypt(invoiceNumber);
                String issueDateDecrypted = simDec.decrypt(issueDate);

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
        org.apache.xml.security.Init.init();
    }
}
