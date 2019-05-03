package com.example.apptestvalidationandroid44;

import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.Toast;

import com.example.apptestvalidationandroid44.config.Configuration;
import com.example.apptestvalidationandroid44.crypto.AsymmetricEncryptor;
import com.example.apptestvalidationandroid44.crypto.EnvelopedSignature;
import com.example.apptestvalidationandroid44.crypto.SymmetricEncryptor;
import com.example.apptestvalidationandroid44.model.FileDataObject;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.facturae.facturae.v3.facturae.Facturae;

public class LocalInvoicesRecyclerViewActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = "LocalInvoicesRAV";

    public static final String INFO_LA_FACTURA_S_HA_QUEDADO_CORRECTAMENTE_REGISTRADA_EN_EL_SISTEMA = "INFO: La factura  %s ha quedado correctamente registrada en el sistema!";
    public static final String ALERTA_LA_FACTURA_S_YA_ESTA_REGISTRADA_EN_EL_SISTEMA = "ALERTA: La factura  %s ya está registrada en el sistema!";
    public static final String ALERTA_LA_FIRMA_NO_ES_VALIDA = "ALERTA: La firma NO es válida!";

    // Widgets
    private Context mContext;
    private RecyclerView.Adapter mAdapter;

    // Vars
    private List<FileDataObject> signedInvoices;

    // Security
    private TFMSecurityManager tfmSecurityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_invoices_recycler_view);

        mContext = getApplicationContext();
        RecyclerView mRecyclerView = findViewById(R.id.local_invoices_rv);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        signedInvoices = (ArrayList<FileDataObject>)getIntent().getSerializableExtra(MainActivity.FILE_LIST);
        mAdapter = new LocalInvoicesRecyclerViewAdapter(signedInvoices);

        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

        tfmSecurityManager = TFMSecurityManager.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((LocalInvoicesRecyclerViewAdapter) mAdapter).setOnItemClickListener(
                new LocalInvoicesRecyclerViewAdapter.LocalInvoicesClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        Log.i(TAG, " Clicked on Item " + position);
                        Toast.makeText(mContext, "Factura " + signedInvoices.get(position).getFileName(), Toast.LENGTH_SHORT).show();

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
            Toast.makeText(mContext, "ERROR IO " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            Log.i(TAG, "ERROR IO : " + e.getLocalizedMessage());
        } catch (Exception e) {
            Toast.makeText(mContext, "ERROR Genérico " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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

        Toast.makeText(mContext, "Cargando el fichero firmado...", Toast.LENGTH_SHORT).show();
        InputStream isSignedInvoice = new FileInputStream(file);

        byte[] baInvoiceSigned = IOUtils.toByteArray(isSignedInvoice);
        Toast.makeText(mContext, "Longitud del fichero firmado : ["+baInvoiceSigned.length+"]", Toast.LENGTH_SHORT).show();

        isSignedInvoice = new FileInputStream(file);
        return UtilDocument.getDocument(isSignedInvoice);
    }

    private byte[] getByteArrayFromSignedInvoice(int position) throws IOException {
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "Download/" + signedInvoices.get(position).getFileName());

        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        Toast.makeText(mContext, "Cargando el fichero firmado...", Toast.LENGTH_SHORT).show();
        InputStream isSignedInvoice = new FileInputStream(file);

        return IOUtils.toByteArray(isSignedInvoice);
    }

    private void validateAndUploadSignedInvoice(int position) {
        try {
             Document doc = getDocumentFromSignedInvoice(position);

            Toast.makeText(mContext, "Documento factura procesado!", Toast.LENGTH_SHORT).show();

            boolean valid = validateSignedInvoice(doc);

            String message = "";

            if(!valid){
                //Toast.makeText(mContext, "ERROR : La firma NO es válida!", Toast.LENGTH_LONG).show();
                alertShow(ALERTA_LA_FIRMA_NO_ES_VALIDA);
            }else{
                Toast.makeText(mContext, "Documento factura válida!", Toast.LENGTH_SHORT).show();

                Document document = UtilDocument.removeSignature(doc);
                Toast.makeText(mContext, "Firma eliminada!", Toast.LENGTH_SHORT).show();


                Toast.makeText(mContext, "Recuperando datos de factura...", Toast.LENGTH_SHORT).show();
                Facturae facturae = UtilFacturae.getFacturaeFromFactura(UtilDocument.documentToString(document));
                if(facturae == null){
                    throw new Exception("ERROR:La factura NO es correcta!");
                }

                Toast.makeText(mContext, "Encriptando datos de factura...", Toast.LENGTH_SHORT).show();

                // Encriptación de los datos
                Log.i(TAG, "Inici...");
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
                        tfmSecurityManager.getSimKeys().get(Configuration.TAX_IDENTIFICATION_NUMBER));

                // invoiceNumber
                String invoiceNumberEncrypted = simEnc.encrypt(
                        facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber(),
                        tfmSecurityManager.getSimKeys().get(Configuration.INVOICE_NUMBER));


                String dataEncrypted   = simEnc.encrypt(
                        ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getIssueDate(),
                        tfmSecurityManager.getSimKeys().get(Configuration.ISSUE_DATE));

                String signedInvoiceEncrypted   = simEnc.encrypt(getByteArrayFromSignedInvoice(position));

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

                // Encriptació amb clau pública de iv i simKey
                byte[] ivBytesEnc = AsymmetricEncryptor.encryptData(iv.getBytes(), tfmSecurityManager.getCertificate());
                String ivStringEnc = new String(Base64.encode(ivBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);
                byte[] simKeyBytesEnc = AsymmetricEncryptor.encryptData(simKey.getBytes(), tfmSecurityManager.getCertificate());
                String simKeyStringEnc = new String(Base64.encode(simKeyBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);


                Map<String, String> params = new HashMap<>();
                params.put("uidfactura", (UIDFacturaHash == null ? "---" : UIDFacturaHash));
                params.put("tax_identification_number", taxIdentificationNumberEncrypted);
                params.put("invoice_number", invoiceNumberEncrypted);

                // Versió inicial: No s'encripten els imports, ja que si no el sistema NO pot fer càlculs
                params.put("total", ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal());
                params.put("total_tax_outputs", ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalTaxOutputs());
                params.put("total_gross_amount", ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalGrossAmount());

                params.put("issue_data", dataEncrypted);
                params.put("file", signedInvoiceEncrypted);
                params.put("iv", ivStringEnc);
                params.put("key", simKeyStringEnc);

                PostDataToUrlTask getData = new PostDataToUrlTask(params);

                String res = getData.execute(Configuration.URL).get();
                Log.i(TAG, "res : " + res);

                JSONObject receivedInvoice = new JSONObject(res);
                String id = receivedInvoice.getString("id");

                if (getData.getResponseCode() == 200){
                    alertShow(String.format(INFO_LA_FACTURA_S_HA_QUEDADO_CORRECTAMENTE_REGISTRADA_EN_EL_SISTEMA, id));
                }else if (getData.getResponseCode() == 409){

                    message += Configuration.CR_LF + String.format(ALERTA_LA_FACTURA_S_YA_ESTA_REGISTRADA_EN_EL_SISTEMA, id);
                    alertShow(message);
                }else if (getData.getResponseCode() == 500){

                    message += Configuration.CR_LF + "ERROR de Servidor";
                    alertShow(message);
                }

                Log.i(TAG, "Respuesta del Servidor : ["+res+"]");

                boolean ret = EnvelopedSignature.signXMLFile(document);
                Log.i(TAG, "EnvelopedSignature.signXMLFile..." + (ret ? "Firmada!!" : "Sin firmar..."));

            } // if(valid)
        }catch (Exception e){
            Toast.makeText(mContext, "ERROR:" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "ERROR:" + e.getMessage());
        }
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

    private void alertShow( String message ) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert!");
        alertDialog.setMessage(message);
        alertDialog.setIcon(R.drawable.ic_launcher_foreground);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "You clicked on OK");
            }
        });

        alertDialog.show();
    }
}
