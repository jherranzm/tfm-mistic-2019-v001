package com.example.apptestvalidationandroid44;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apptestvalidationandroid44.config.Constants;
import com.example.apptestvalidationandroid44.crypto.AsymmetricEncryptor;
import com.example.apptestvalidationandroid44.crypto.EnvelopedSignature;
import com.example.apptestvalidationandroid44.crypto.SymmetricEncryptor;
import com.example.apptestvalidationandroid44.model.FileDataObject;
import com.example.apptestvalidationandroid44.model.InvoiceData;
import com.example.apptestvalidationandroid44.tasks.invoicedatatasks.GetByBatchIdentifierInvoiceDataTask;
import com.example.apptestvalidationandroid44.tasks.invoicedatatasks.InsertInvoiceDataTask;
import com.example.apptestvalidationandroid44.util.RandomStringGenerator;
import com.example.apptestvalidationandroid44.util.TFMSecurityManager;
import com.example.apptestvalidationandroid44.util.UIDGenerator;
import com.example.apptestvalidationandroid44.util.UtilDocument;
import com.example.apptestvalidationandroid44.util.UtilFacturae;
import com.example.apptestvalidationandroid44.util.UtilValidator;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.facturae.facturae.v3.facturae.Facturae;

public class ReceivedInvoicesRecyclerViewActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = "ReceivedInvoicesRAV";

    public static final String INFO_LA_FACTURA_S_HA_QUEDADO_CORRECTAMENTE_REGISTRADA_EN_EL_SISTEMA = "Remote backup. INFO: Invoice  %s correctly backed up!";
    public static final String ALERTA_LA_FACTURA_S_YA_ESTA_REGISTRADA_EN_EL_SISTEMA = "Remote backup. ALERT: Invoice  %s already backed up in system!";
    public static final String ALERTA_LA_FIRMA_NO_ES_VALIDA = "ALERT: Invoice signature NOT valid!";

    // Widgets
    private RecyclerView.Adapter mAdapter;

    // Vars
    private List<FileDataObject> signedInvoices;

    // Security
    private TFMSecurityManager tfmSecurityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.received_invoices_recycler_view);

        RecyclerView mRecyclerView = findViewById(R.id.local_invoices_rv);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        signedInvoices = getFileInvoicesInSystem();
        mAdapter = new ReceivedInvoicesRecyclerViewAdapter(signedInvoices);
        TextView textViewNumberItems = findViewById(R.id.textViewNumInvoiceFilesInSystem);
        textViewNumberItems.setText("Number of File Invoices in System " + signedInvoices.size());


        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

        tfmSecurityManager = TFMSecurityManager.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((ReceivedInvoicesRecyclerViewAdapter) mAdapter).setOnItemClickListener(
                new ReceivedInvoicesRecyclerViewAdapter.ReceivedInvoicesClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        Log.i(TAG, " Clicked on Item " + position);
                        Toast.makeText(InvoiceApp.getContext(),
                                "Invoice " + signedInvoices.get(position).getFileName(),
                                Toast.LENGTH_SHORT).show();

                        customDialog("Verify and Upload Invoice?",
                                "Do you want to process invoice " + signedInvoices.get(position).getFileName(),
                                "cancel",
                                "ok",
                                position);

                    }
                });
    }

    private boolean validateSignedInvoice(Document doc){
        boolean valid = false;
        try {
            //valid = UtilValidator.isValid(tfmSecurityManager.getCertificate(), doc);
            valid = UtilValidator.isValid(doc);
        } catch (IOException e) {
            Toast.makeText(InvoiceApp.getContext(), "ERROR IO " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            Log.i(TAG, "ERROR IO : " + e.getLocalizedMessage());
        } catch (Exception e) {
            Toast.makeText(InvoiceApp.getContext(), "ERROR Genérico " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            Log.i(TAG, "ERROR Generic : " + e.getLocalizedMessage());
        }

        return valid;
    }

    private Document getDocumentFromSignedInvoice(int position) throws IOException {
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "Download/" + signedInvoices.get(position).getFileName());

        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        Toast.makeText(InvoiceApp.getContext(), "Loading signed file...", Toast.LENGTH_SHORT).show();
        InputStream isSignedInvoice = new FileInputStream(file);

        byte[] baInvoiceSigned = IOUtils.toByteArray(isSignedInvoice);
        Toast.makeText(InvoiceApp.getContext(), "Info: file signed long : ["+baInvoiceSigned.length+"]", Toast.LENGTH_SHORT).show();

        isSignedInvoice = new FileInputStream(file);
        return UtilDocument.getDocument(isSignedInvoice);
    }

    private byte[] getByteArrayFromSignedInvoice(int position) throws IOException {
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "Download/" + signedInvoices.get(position).getFileName());

        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        Toast.makeText(InvoiceApp.getContext(), "Loading signed file...", Toast.LENGTH_SHORT).show();
        InputStream isSignedInvoice = new FileInputStream(file);

        return IOUtils.toByteArray(isSignedInvoice);
    }

    private void validateAndUploadSignedInvoice(int position) {
        try {
             Document doc = getDocumentFromSignedInvoice(position);

            Toast.makeText(InvoiceApp.getContext(), "Invoice processed!", Toast.LENGTH_SHORT).show();

            boolean valid = validateSignedInvoice(doc);

            String message = "";

            if(!valid){
                //Toast.makeText(mContext, "ERROR : La firma NO es válida!", Toast.LENGTH_LONG).show();
                alertShow(ALERTA_LA_FIRMA_NO_ES_VALIDA);
            }else{
                Toast.makeText(InvoiceApp.getContext(), "Documento factura válida!", Toast.LENGTH_SHORT).show();

                Document document = UtilDocument.removeSignature(doc);
                Toast.makeText(InvoiceApp.getContext(), "Firma eliminada!", Toast.LENGTH_SHORT).show();


                Toast.makeText(InvoiceApp.getContext(), "Recuperando datos de factura...", Toast.LENGTH_SHORT).show();
                Facturae facturae = UtilFacturae.getFacturaeFromFactura(UtilDocument.documentToString(document));
                if(facturae == null){
                    throw new Exception("ERROR:La factura NO es correcta!");
                }

                Toast.makeText(InvoiceApp.getContext(), "Encriptando datos de factura...", Toast.LENGTH_SHORT).show();

                // Encriptación de los datos
                Log.i(TAG, "Encryption...");
                RandomStringGenerator rsg = new RandomStringGenerator();

                String iv = rsg.getRandomString(16);
                Log.i(TAG, "iv     : ["+iv+"]");
                String simKey = rsg.getRandomString(16);
                Log.i(TAG, "simKey : ["+simKey+"]");

                SymmetricEncryptor simEnc = new SymmetricEncryptor();
                simEnc.setIv(iv);
                simEnc.setKey(simKey);

                if(facturae.getParties() == null){
                    throw new Exception("ERROR en la factura NO vienen informadas las partes.");
                }

                // taxIdentificationNumber
                String taxIdentificationNumberEncrypted = simEnc.encrypt(
                        facturae.getParties().getSellerParty().getTaxIdentification().getTaxIdentificationNumber(),
                        tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.TAX_IDENTIFICATION_NUMBER)
                );

                // invoiceNumber
                String invoiceNumberEncrypted = simEnc.encrypt(
                        facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber(),
                        tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.INVOICE_NUMBER)
                );

                // total
                String totalEncrypted = simEnc.encrypt(
                        ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal(),
                        tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.INVOICE_TOTAL)
                );

                // total_tax_outputs
                String totalTaxOutputsEncrypted = simEnc.encrypt(
                        ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalTaxOutputs(),
                        tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.TOTAL_TAX_OUTPUTS)
                );

                // total_gross_amount
                String totalGrossAmountEncrypted = simEnc.encrypt(
                        ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalGrossAmount(),
                        tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.TOTAL_GROSS_AMOUNT)
                );


                String dataEncrypted   = simEnc.encrypt(
                        ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getIssueDate(),
                        tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.ISSUE_DATE)
                );

                String signedInvoiceEncrypted   = simEnc.encrypt(getByteArrayFromSignedInvoice(position));

                String UIDFacturaHash = UIDGenerator.generate(facturae);
                Log.i(TAG, "UIDFacturaHash : ["+UIDFacturaHash+"]");

                // Encriptació amb clau pública de iv i simKey
                byte[] ivBytesEnc = AsymmetricEncryptor.encryptData(iv.getBytes(), tfmSecurityManager.getCertificate());
                String ivStringEnc = new String(Base64.encode(ivBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);
                byte[] simKeyBytesEnc = AsymmetricEncryptor.encryptData(simKey.getBytes(), tfmSecurityManager.getCertificate());
                String simKeyStringEnc = new String(Base64.encode(simKeyBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);


                Map<String, String> params = new HashMap<>();
                params.put("uidfactura", (UIDFacturaHash == null ? "---" : UIDFacturaHash));
                params.put("tax_identification_number", taxIdentificationNumberEncrypted);
                params.put("invoice_number", invoiceNumberEncrypted);

                params.put("total", totalEncrypted);
                params.put("total_tax_outputs", totalTaxOutputsEncrypted);
                params.put("total_gross_amount", totalGrossAmountEncrypted);

                params.put("issue_data", dataEncrypted);
                params.put("file", signedInvoiceEncrypted);
                params.put("iv", ivStringEnc);
                params.put("key", simKeyStringEnc);

                PostDataAuthenticatedToUrlTask getData = new PostDataAuthenticatedToUrlTask(params);

                String res = getData.execute(Constants.URL_FACTURAS).get();
                Log.i(TAG, "res : " + res);

                JSONObject receivedInvoice = new JSONObject(res);
                String id = receivedInvoice.getString("id");

                boolean invoiceBackedUp = false;

                if (getData.getResponseCode() == HttpURLConnection.HTTP_OK){

                    infoShow(String.format(INFO_LA_FACTURA_S_HA_QUEDADO_CORRECTAMENTE_REGISTRADA_EN_EL_SISTEMA, id));
                    invoiceBackedUp = true;

                }else if (getData.getResponseCode() == HttpURLConnection.HTTP_CONFLICT){

                    message += Constants.CR_LF + String.format(ALERTA_LA_FACTURA_S_YA_ESTA_REGISTRADA_EN_EL_SISTEMA, id);
                    alertShow(message);
                    invoiceBackedUp = true;

                }else if (getData.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR){

                    message += Constants.CR_LF + "ERROR de Servidor";
                    alertShow(message);
                }

                Log.i(TAG, "Respuesta del Servidor : ["+res+"]");

                boolean ret = EnvelopedSignature.signXMLFile(document);
                Log.i(TAG, "EnvelopedSignature.signXMLFile..." + (ret ? "Firmada!!" : "Sin firmar..."));

                InvoiceData invoiceData = getInvoiceData(facturae, UIDFacturaHash);
                invoiceData.setUser(tfmSecurityManager.getUserLoggedDataFromKeyStore(Constants.USER_LOGGED));
                invoiceData.setBackedUp(invoiceBackedUp);

                GetByBatchIdentifierInvoiceDataTask getByBatchIdentifierInvoiceDataTask = new GetByBatchIdentifierInvoiceDataTask();
                List<InvoiceData> alreadySaved = getByBatchIdentifierInvoiceDataTask.execute(
                        invoiceData.getBatchIdentifier(),
                        tfmSecurityManager.getUserLoggedDataFromKeyStore(Constants.USER_LOGGED)
                ).get();

                if(alreadySaved.size()>0){

                    Log.i(TAG, "InvoiceData already in system : nothing to be done!" + invoiceData.toString());
                    alertShow( "Local Database: Invoice "+ invoiceData.getInvoiceNumber() +" already in system : nothing to be done!" );
                }else{

                    InsertInvoiceDataTask insertInvoiceDataTask = new InsertInvoiceDataTask(invoiceData);
                    InvoiceData invoiceDataInserted = insertInvoiceDataTask.execute().get();
                    infoShow( "Local Database: Invoice "+ invoiceData.getInvoiceNumber() +" loaded in system!" );
                    Log.i(TAG, "InvoiceData ingresada: " + invoiceDataInserted.toString());
                }

            } // if(valid)
        }catch (Exception e){
            Toast.makeText(InvoiceApp.getContext(), "ERROR:" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "ERROR:" + e.getMessage());
        }
    }

    private InvoiceData getInvoiceData(Facturae facturae, String UIDFacturaHash) {

        InvoiceData invoiceData = new InvoiceData();

        invoiceData.setBatchIdentifier(UIDFacturaHash);

        invoiceData.setTaxIdentificationNumber(facturae.getParties().getSellerParty().getTaxIdentification().getTaxIdentificationNumber());
        invoiceData.setCorporateName(facturae.getParties().getSellerParty().getLegalEntity().getCorporateName());

        invoiceData.setInvoiceNumber(facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber());

        invoiceData.setTaxAmount(facturae.getInvoices().getInvoiceList().get(0).getTaxesOutputList().get(0).getTaxAmount().getTotalAmount());
        invoiceData.setTaxBase(facturae.getInvoices().getInvoiceList().get(0).getTaxesOutputList().get(0).getTaxableBase().getTotalAmount());
        invoiceData.setTotalAmount(facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal());
        invoiceData.setTotalGrossAmount(facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalGrossAmount());

        invoiceData.setIssueDate(facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getIssueDate());
        invoiceData.setStartDate(facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getInvoicingPeriod().getStartDate());
        invoiceData.setEndDate(facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getInvoicingPeriod().getStartDate());
        return invoiceData;
    }

    private void customDialog(
            String title,
            String message,
            final String cancelMethod,
            final String okMethod,
            final int position){
        final android.support.v7.app.AlertDialog.Builder builderSingle = new android.support.v7.app.AlertDialog.Builder(this);
        builderSingle.setIcon(R.mipmap.ic_launcher_round);
        builderSingle.setTitle(title);
        builderSingle.setMessage(message);

        builderSingle.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Cancel Called.");
                        if(cancelMethod.equals("cancel")){
                            //cancelMethod1();
                            Log.i(TAG, "Operation cancelled!");
                        }

                    }
                });

         builderSingle.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "onClick: OK Called.");
                        if(okMethod.equals("ok")){
                            validateAndUploadSignedInvoice(position);
                        }
                    }
                });


        builderSingle.show();
    }

    private void infoShow( String message ) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Info");
        alertDialog.setMessage(message);
        alertDialog.setIcon(R.drawable.ic_info_name);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "You clicked on OK");
            }
        });

        alertDialog.show();
    }

    private  void alertShow( String message ) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert!");
        alertDialog.setMessage(message);
        alertDialog.setIcon(R.drawable.ic_stat_name);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "You clicked on OK");
            }
        });

        alertDialog.show();
    }

    private List<FileDataObject> getFileInvoicesInSystem(){
        String root_sd = Environment.getExternalStorageDirectory().toString();
        File file = new File( root_sd + "/Download" ) ;
        File list[] = file.listFiles();

        List<FileDataObject> signedInvoices = new ArrayList<>();

        for (File f : list) {
            Log.i(TAG, f.getName());
            FileDataObject obj = new FileDataObject(f.getName());
            signedInvoices.add(obj);
        }

        return signedInvoices;

    }
}
