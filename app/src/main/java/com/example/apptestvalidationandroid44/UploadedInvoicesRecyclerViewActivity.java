package com.example.apptestvalidationandroid44;

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
import com.example.apptestvalidationandroid44.crypto.AsymmetricDecryptor;
import com.example.apptestvalidationandroid44.crypto.SymmetricDecryptor;
import com.example.apptestvalidationandroid44.invoicetasks.GetInvoiceByIdTask;
import com.example.apptestvalidationandroid44.model.Invoice;
import com.example.apptestvalidationandroid44.util.TFMSecurityManager;
import com.example.apptestvalidationandroid44.util.UtilDocument;
import com.example.apptestvalidationandroid44.util.UtilFacturae;
import com.example.apptestvalidationandroid44.util.UtilValidator;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import es.facturae.facturae.v3.facturae.Facturae;

public class UploadedInvoicesRecyclerViewActivity extends AppCompatActivity {
    private Context mContext;
    private RecyclerView.Adapter mAdapter;

    private static String TAG = "UploadedInvoicesRVA";

    private List<Invoice> invoices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        RecyclerView mRecyclerView;
        RecyclerView.LayoutManager mLayoutManager;

        mContext = getApplicationContext();
        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        invoices = (ArrayList<Invoice>)getIntent().getSerializableExtra(MainActivity.INVOICE_LIST);
        mAdapter = new UploadedInvoicesRecyclerViewAdapter(invoices);

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
        ((UploadedInvoicesRecyclerViewAdapter) mAdapter).setOnItemClickListener(
                new UploadedInvoicesRecyclerViewAdapter.UploadedInvoicesClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        Log.i(TAG, " Clicked on Item " + position);
                        Toast.makeText(mContext, "Factura " + invoices.get(position).getInvoiceNumber(), Toast.LENGTH_SHORT).show();
                        Log.i(TAG, " Clicked on Item " + invoices.get(position).toString());

                        customDialog("Download invoice from server?",
                        "Do you want to download invoice " + position + " from server?",
                        "cancel",
                        "ok",
                        position);
                    }
                });
    }

    private void downloadInvoice(int position) {
        try{
            String url = Configuration.URL + "/" + (position+1);
            GetInvoiceByIdTask getInvoiceByIdTask = new GetInvoiceByIdTask();

            String res = getInvoiceByIdTask.execute(url).get();
            Log.i(TAG, "Received from server : " + res);
            JSONObject jsonInvoice = new JSONObject(res);
            Log.i(TAG, "Received from server [iv]: " + jsonInvoice.get("iv"));
            Log.i(TAG, "Received from server [simKey]: " + jsonInvoice.get("simKey"));
            Log.i(TAG, "Received from server [signedInvoice]: " + jsonInvoice.get("signedInvoice"));

            TFMSecurityManager tfmSecurityManager = TFMSecurityManager.getInstance();

            // Desencriptació amb clau privada de iv i simKey
            byte[] ivBytesDec = Base64.decode((String)jsonInvoice.get("iv"), Base64.NO_WRAP);
            byte[] ivBytesEncDec = AsymmetricDecryptor.decryptData(ivBytesDec, tfmSecurityManager.getKey());
            String ivStringDec = new String(ivBytesEncDec);

            byte[] simKeyBytesDec = Base64.decode((String)jsonInvoice.get("simKey"), Base64.NO_WRAP);
            byte[] simKeyBytesEncDec = AsymmetricDecryptor.decryptData(simKeyBytesDec, tfmSecurityManager.getKey());
            String simKeyStringDec = new String(simKeyBytesEncDec);

            Log.i(TAG, "Received from server [iv]    : " + ivStringDec);
            Log.i(TAG, "Received from server [simKey]: " + simKeyStringDec);

            SymmetricDecryptor simDec = new SymmetricDecryptor();
            simDec.setIv(ivStringDec);
            simDec.setKey(simKeyStringDec);
            String signedInvoiceDecrypted   = simDec.decrypt((String)jsonInvoice.get("signedInvoice"));
            Log.i(TAG, "Received from server [signedInvoice]: " + signedInvoiceDecrypted);

            Document doc = UtilDocument.getDocument(IOUtils.toInputStream(signedInvoiceDecrypted));
            Log.i(TAG, "Received from server [doc]: " + doc.getNodeName());

            boolean valid = UtilValidator.isValid(doc);
            Log.i(TAG, "Received from server [doc]: isValid? : " + valid);

            Log.i(TAG, "Received from server [doc]: removing signature...");
            Document document = UtilDocument.removeSignature(doc);

            Facturae facturae = UtilFacturae.getFacturaeFromFactura(UtilDocument.documentToString(document));
            if(facturae == null){
                throw new Exception("Invoice document received from server is null");
            }
            if(facturae.getInvoices() == null){
                throw new Exception("Invoice document received from server contains NO invoices");
            }
            Log.i(TAG, "Received from server [facturae]: InvoiceNumber   : " + facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber());
            Log.i(TAG, "Received from server [facturae]: ItemDescription : " + facturae.getInvoices().getInvoiceList().get(0).getItems().getInvoiceLineList().get(0).getItemDescription());
            Log.i(TAG, "Received from server [facturae]: Quantity        : " + facturae.getInvoices().getInvoiceList().get(0).getItems().getInvoiceLineList().get(0).getQuantity());
            Log.i(TAG, "Received from server [facturae]: UnitOfMeasure   : " + facturae.getInvoices().getInvoiceList().get(0).getItems().getInvoiceLineList().get(0).getUnitOfMeasure().toString());
            Log.i(TAG, "Received from server [facturae]: UnitPriceWithoutTax : " + facturae.getInvoices().getInvoiceList().get(0).getItems().getInvoiceLineList().get(0).getUnitPriceWithoutTax());
            Log.i(TAG, "Received from server [facturae]: TotalCost       : " + facturae.getInvoices().getInvoiceList().get(0).getItems().getInvoiceLineList().get(0).getTotalCost());

            StringBuilder sb = new StringBuilder();
            sb.append(Configuration.CR_LF);
            sb.append("InvoiceNumber:");
            sb.append(facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber());
            sb.append(Configuration.CR_LF);
            sb.append("ItemDescription:");
            sb.append(facturae.getInvoices().getInvoiceList().get(0).getItems().getInvoiceLineList().get(0).getItemDescription());
            sb.append(Configuration.CR_LF);
            sb.append("Quantity        : ");
            sb.append(facturae.getInvoices().getInvoiceList().get(0).getItems().getInvoiceLineList().get(0).getQuantity());
            sb.append(Configuration.CR_LF);
            sb.append("UnitOfMeasure   : ");
            sb.append(facturae.getInvoices().getInvoiceList().get(0).getItems().getInvoiceLineList().get(0).getUnitOfMeasure().toString());
            sb.append(Configuration.CR_LF);
            sb.append("GrossAmount   : ");
            sb.append(facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalGrossAmount());
            sb.append(Configuration.CR_LF);
            sb.append("TaxAmount   : ");
            sb.append(facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalTaxOutputs());
            sb.append(Configuration.CR_LF);
            sb.append("TotalCost   : ");
            sb.append(facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal());
            sb.append(Configuration.CR_LF);

            infoDialog(
                    "Invoice Info",
                    sb.toString(),
                    "ok");

            String root_sd = Environment.getExternalStorageDirectory().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmSS");
            String fileName = sdf.format(Calendar.getInstance().getTime());
            try (PrintWriter out = new PrintWriter( new FileOutputStream(root_sd + "/Download/" + fileName + ".xsig"))) {
                out.println(signedInvoiceDecrypted);
            }

        }catch(Exception e){
            Log.e(TAG, "ERROR : " + e.getLocalizedMessage());
            Log.i(TAG, "ERROR : " + e.getLocalizedMessage());
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
                            downloadInvoice(position);
                        }
                    }
                });


        builderSingle.show();
    }


    private void infoDialog(
            String title,
            String message,
            final String okMethod){
        final android.support.v7.app.AlertDialog.Builder builderSingle = new android.support.v7.app.AlertDialog.Builder(this);
        builderSingle.setIcon(R.mipmap.ic_launcher_round);
        builderSingle.setTitle("Info");
        builderSingle.setMessage(message);


        builderSingle.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "onClick: OK Called.");
                        if(okMethod.equals("ok")){
                            Log.d(TAG, "onClick: OK Called.");
                        }
                    }
                });


        builderSingle.show();
    }
}
