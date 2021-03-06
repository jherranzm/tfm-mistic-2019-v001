package edu.uoc.mistic.tfm.jherranzm.ui.activities;

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
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import edu.uoc.mistic.tfm.jherranzm.R;
import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.crypto.AsymmetricDecryptor;
import edu.uoc.mistic.tfm.jherranzm.crypto.SymmetricDecryptor;
import edu.uoc.mistic.tfm.jherranzm.model.Invoice;
import edu.uoc.mistic.tfm.jherranzm.services.InvoiceDataService;
import edu.uoc.mistic.tfm.jherranzm.services.InvoiceService;
import edu.uoc.mistic.tfm.jherranzm.tasks.invoicetasks.GetInvoiceByIdTask;
import edu.uoc.mistic.tfm.jherranzm.ui.adapters.UploadedInvoicesRecyclerViewAdapter;
import edu.uoc.mistic.tfm.jherranzm.util.TFMSecurityManager;
import edu.uoc.mistic.tfm.jherranzm.util.UIDGenerator;
import edu.uoc.mistic.tfm.jherranzm.util.UtilDocument;
import edu.uoc.mistic.tfm.jherranzm.util.UtilFacturae;
import edu.uoc.mistic.tfm.jherranzm.util.UtilValidator;
import es.facturae.facturae.v3.facturae.Facturae;

public class UploadedInvoicesRecyclerViewActivity extends AppCompatActivity {
    public static final String IV = "iv";
    public static final String SIM_KEY = "simKey";
    public static final String SIGNED_INVOICE = "signedInvoice";
    private Context mContext;
    private RecyclerView.Adapter mAdapter;

    private static final String TAG = UploadedInvoicesRecyclerViewActivity.class.getSimpleName();

    // Security
    private TFMSecurityManager tfmSecurityManager;

    private List<Invoice> invoices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uploaded_invoices_recycler_view);

        tfmSecurityManager = TFMSecurityManager.getInstance();

        initView();
    }

    private void initView() {
        RecyclerView mRecyclerView;
        RecyclerView.LayoutManager mLayoutManager;

        mContext = getApplicationContext();
        mRecyclerView = findViewById(R.id.uploaded_invoices_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        invoices = InvoiceService.getUploadedInvoicesFromServer(this, tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.USER_LOGGED));
        mAdapter = new UploadedInvoicesRecyclerViewAdapter(invoices);

        TextView textViewNumberItems = findViewById(R.id.textViewNumUploadedInvoices);
        textViewNumberItems.setText(String.format("Number of Invoices in Server %d", invoices.size()));

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

                        downloadInvoiceFromServerDialog("Download invoice from server?",
                                String.format("Do you want to download invoice %s of provider %s from server and process it?",
                                        invoices.get(position).getInvoiceNumber(),
                                        invoices.get(position).getCorporateName()
                                ),
                        "cancel",
                        "ok",
                        position);
                    }
                });
    }

    private void downloadInvoice(int position) {
        try{

            //String url = Constants.URL_FACTURAS + "/" + (position+1);
            String url = Constants.URL_FACTURAS + "/" + invoices.get(position).getUid();
            GetInvoiceByIdTask getInvoiceByIdTask = new GetInvoiceByIdTask();

            String res = getInvoiceByIdTask.execute(url).get();
            Log.i(TAG, String.format("Received from server : %s", res));
            JSONObject jsonInvoice = new JSONObject(res);
            Log.i(TAG, String.format("Received from server [iv]: %s", jsonInvoice.get("iv")));
            Log.i(TAG, String.format("Received from server [simKey]: %s", jsonInvoice.get("simKey")));
            Log.i(TAG, String.format("Received from server [signedInvoice]: %s", jsonInvoice.get("signedInvoice")));

            //TFMSecurityManager tfmSecurityManager = TFMSecurityManager.getInstance();

            // Desencriptació amb clau privada de iv i simKey
            byte[] ivBytesDec = Base64.decode((String)jsonInvoice.get(IV), Base64.NO_WRAP);
            byte[] ivBytesEncDec = AsymmetricDecryptor.decryptData(ivBytesDec, tfmSecurityManager.getPrivateKey());
            String ivStringDec = new String(ivBytesEncDec);

            byte[] simKeyBytesDec = Base64.decode((String)jsonInvoice.get(SIM_KEY), Base64.NO_WRAP);
            byte[] simKeyBytesEncDec = AsymmetricDecryptor.decryptData(simKeyBytesDec, tfmSecurityManager.getPrivateKey());
            String simKeyStringDec = new String(simKeyBytesEncDec);

            Log.i(TAG, String.format("Received from server (decrypted) [iv]    : %s", ivStringDec));
            Log.i(TAG, String.format("Received from server (decrypted) [simKey]: %s", simKeyStringDec));

            SymmetricDecryptor simDec = new SymmetricDecryptor();
            simDec.setIv(ivStringDec);
            simDec.setKey(simKeyStringDec);
            String signedInvoiceDecrypted   = simDec.decrypt((String)jsonInvoice.get(SIGNED_INVOICE));
            Log.i(TAG, String.format("Received from server [signedInvoice]: %s", signedInvoiceDecrypted));

            Document doc = UtilDocument.getDocument(IOUtils.toInputStream(signedInvoiceDecrypted));
            Log.i(TAG, String.format("Received from server [doc]: %s", doc.getNodeName()));

            boolean valid = UtilValidator.validateSignedInvoice(doc);
            Log.i(TAG, String.format("Received from server [doc]: isValid? : %s", valid));

            Log.i(TAG, "Received from server [doc]: removing signature...");
            Document document = UtilDocument.removeSignature(doc);


            // Show basic info of the invoice downloaded
            showInfoOfDownloadedInvoice(document);

            includeDownloadInvoiceInLocalDatabaseDialog(document, signedInvoiceDecrypted);

            String root_sd = Environment.getExternalStorageDirectory().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmSS", Locale.getDefault());
            String fileName = sdf.format(Calendar.getInstance().getTime());
            try (PrintWriter out = new PrintWriter( new FileOutputStream(root_sd + "/Download/" + fileName + ".xsig"))) {
                out.println(signedInvoiceDecrypted);
            }

        }catch(Exception e){
            Log.e(TAG, String.format("ERROR : %s", e.getLocalizedMessage()));
            Log.i(TAG, String.format("ERROR : %s", e.getLocalizedMessage()));
        }
    }

    private void showInfoOfDownloadedInvoice(Document document) throws Exception {
        Facturae facturae = UtilFacturae.getFacturaeFromFactura(UtilDocument.documentToString(document));
        if(facturae == null){
            throw new Exception("Invoice document received from server is null");
        }
        if(facturae.getInvoices() == null){
            throw new Exception("Invoice document received from server contains NO invoices");
        }
        Log.i(TAG, String.format("Received from server [facturae]: InvoiceNumber   : %s", facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber()));
        Log.i(TAG, String.format("Received from server [facturae]: ItemDescription : %s", facturae.getInvoices().getInvoiceList().get(0).getItems().getInvoiceLineList().get(0).getItemDescription()));
        Log.i(TAG, String.format("Received from server [facturae]: Quantity        : %s", facturae.getInvoices().getInvoiceList().get(0).getItems().getInvoiceLineList().get(0).getQuantity()));
        Log.i(TAG, String.format("Received from server [facturae]: UnitOfMeasure   : %s", facturae.getInvoices().getInvoiceList().get(0).getItems().getInvoiceLineList().get(0).getUnitOfMeasure().toString()));
        Log.i(TAG, String.format("Received from server [facturae]: UnitPriceWithoutTax : %s", facturae.getInvoices().getInvoiceList().get(0).getItems().getInvoiceLineList().get(0).getUnitPriceWithoutTax()));
        Log.i(TAG, String.format("Received from server [facturae]: TotalCost       : %s", facturae.getInvoices().getInvoiceList().get(0).getItems().getInvoiceLineList().get(0).getTotalCost()));

        StringBuilder sb = new StringBuilder();
        sb.append(Constants.CR_LF);
        sb.append("InvoiceNumber:");
        sb.append(facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber());
        sb.append(Constants.CR_LF);
        sb.append("ItemDescription:");
        sb.append(facturae.getInvoices().getInvoiceList().get(0).getItems().getInvoiceLineList().get(0).getItemDescription());
        sb.append(Constants.CR_LF);
        sb.append("Quantity        : ");
        sb.append(facturae.getInvoices().getInvoiceList().get(0).getItems().getInvoiceLineList().get(0).getQuantity());
        sb.append(Constants.CR_LF);
        sb.append("UnitOfMeasure   : ");
        sb.append(facturae.getInvoices().getInvoiceList().get(0).getItems().getInvoiceLineList().get(0).getUnitOfMeasure().toString());
        sb.append(Constants.CR_LF);
        sb.append("GrossAmount   : ");
        sb.append(facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalGrossAmount());
        sb.append(Constants.CR_LF);
        sb.append("TaxAmount   : ");
        sb.append(facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalTaxOutputs());
        sb.append(Constants.CR_LF);
        sb.append("TotalCost   : ");
        sb.append(facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal());
        sb.append(Constants.CR_LF);

        infoDialog(
                "Invoice Info",
                sb.toString(),
                "ok");
    }

    private void downloadInvoiceFromServerDialog(
            String title,
            String message,
            final String cancelMethod,
            final String okMethod,
            final int position){

        final android.support.v7.app.AlertDialog.Builder builderSingle = new android.support.v7.app.AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.ic_warning_icon);
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

    private void includeDownloadInvoiceInLocalDatabaseDialog(final Document document, final String signedInvoiceDecrypted){

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // TODO : Change icon
        builder.setIcon(R.mipmap.ic_launcher_round);
        builder.setTitle("Process and include");
        builder.setMessage("Do you want to process and include this invoice in local database?");

        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "Operation cancelled!");
                     }
                });

        builder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "onClick: OK Called.");
                        try {
                            Facturae facturae = UtilFacturae.getFacturaeFromFactura(UtilDocument.documentToString(document));
                            String UIDInvoiceHash = UIDGenerator.generate(facturae);
                            InvoiceDataService invoiceDataService = InvoiceDataService.getInstance(UploadedInvoicesRecyclerViewActivity.this);
                            invoiceDataService.saveInvoiceDataInLocalDatabase(facturae, UIDInvoiceHash, signedInvoiceDecrypted, true);

                            initView();

                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                });


        builder.show();
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
