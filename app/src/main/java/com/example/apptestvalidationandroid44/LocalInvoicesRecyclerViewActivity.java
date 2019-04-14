package com.example.apptestvalidationandroid44;

import android.content.Context;
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

import com.example.apptestvalidationandroid44.crypto.AsymmetricEncryptor;
import com.example.apptestvalidationandroid44.crypto.SymmetricEncryptor;
import com.example.apptestvalidationandroid44.model.FileDataObject;
import com.example.apptestvalidationandroid44.model.Invoice;
import com.example.apptestvalidationandroid44.util.RandomStringGenerator;
import com.example.apptestvalidationandroid44.util.TFMSecurityManager;
import com.example.apptestvalidationandroid44.util.UIDGenerator;
import com.example.apptestvalidationandroid44.util.UtilDocument;
import com.example.apptestvalidationandroid44.util.UtilFacturae;
import com.example.apptestvalidationandroid44.util.UtilValidator;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.facturae.facturae.v3.facturae.Facturae;

public class LocalInvoicesRecyclerViewActivity extends AppCompatActivity {

    public static final String TAX_IDENTIFICATION_NUMBER = "f2";
    public static final String INVOICE_NUMBER = "f4";

    private Context mContext;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static String LOG_TAG = "LocalInvoicesRVA";

    private List<FileDataObject> signedInvoices;

    private TFMSecurityManager tfmSecurityManager;

    private static final String TAG = "LocalInvoicesRAV";
    private static final String CR_LF = "\n";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_invoices_recycler_view);

        tfmSecurityManager = TFMSecurityManager.getInstance();

        mContext = getApplicationContext();
        mRecyclerView = findViewById(R.id.local_invoices_rv);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        signedInvoices = (ArrayList<FileDataObject>)getIntent().getSerializableExtra(MainActivity.FILE_LIST);
        mAdapter = new LocalInvoicesRecyclerViewAdapter(signedInvoices);

        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

        // Code to Add an item with default animation
        //((MyRecyclerViewAdapter) mAdapter).addItem(obj, index);

        // Code to remove an item with default animation
        //((MyAdapter) mAdapter).deleteItem(index);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((LocalInvoicesRecyclerViewAdapter) mAdapter).setOnItemClickListener(
                new LocalInvoicesRecyclerViewAdapter.LocalInvoicesClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        Log.i(LOG_TAG, " Clicked on Item " + position);
                        Toast.makeText(mContext, "Factura " + signedInvoices.get(position).getFileName(), Toast.LENGTH_SHORT).show();

                        try {
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
                            Document doc = UtilDocument.getDocument(isSignedInvoice);

                            Toast.makeText(mContext, "Documento factura procesado!", Toast.LENGTH_SHORT).show();

                            boolean valid = UtilValidator.isValid(tfmSecurityManager.getCertificate(), doc);

                            String message = "";

                            if(!valid){
                                Toast.makeText(mContext, "ERROR : La firma NO es válida!", Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(mContext, "Documento factura válida!", Toast.LENGTH_SHORT).show();

                                Document document = UtilDocument.removeSignature(doc);

                                Toast.makeText(mContext, "Firma eliminada!", Toast.LENGTH_SHORT).show();

                                Facturae facturae = UtilFacturae.getFacturaeFromFactura(UtilDocument.documentToString(document));

                                Toast.makeText(mContext, "Recuperando datos de factura...", Toast.LENGTH_SHORT).show();

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

                                String simKeyTaxIdentificationNumber = tfmSecurityManager.getSimKeys().get(TAX_IDENTIFICATION_NUMBER);// taxIdentificationNumber
                                String taxIdentificationNumberEncrypted = simEnc.encrypt(facturae.getParties().getSellerParty().getTaxIdentification().getTaxIdentificationNumber(), simKeyTaxIdentificationNumber);
                                String simKeyInvoiceNumber = tfmSecurityManager.getSimKeys().get(INVOICE_NUMBER);// invoiceNumber
                                String invoiceNumberEncrypted = simEnc.encrypt(facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber(), simKeyInvoiceNumber);

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

                                String url = "http://10.0.2.2:8080/facturas";
                                String res = getData.execute(url).get();

                                if (getData.getResponseCode() == 400){
                                    message += CR_LF + String.format("res : [%s]", res);
                                }else if (getData.getResponseCode() == 409){
                                    message += CR_LF + String.format("ALERTA: La factura ya está registrada en el sistema! %s", "");
                                }

                                Log.i(TAG, "Respuesta del Servidor : ["+res+"]");

                            } // if(valid)
                        }catch (Exception e){
                            Toast.makeText(mContext, "ERROR:" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}
