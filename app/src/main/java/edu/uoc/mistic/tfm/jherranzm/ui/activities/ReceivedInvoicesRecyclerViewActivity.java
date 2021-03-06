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
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import edu.uoc.mistic.tfm.jherranzm.R;
import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.crypto.EnvelopedSignature;
import edu.uoc.mistic.tfm.jherranzm.model.FileDataObject;
import edu.uoc.mistic.tfm.jherranzm.services.InvoiceDataService;
import edu.uoc.mistic.tfm.jherranzm.tasks.filedataobjecttasks.GetFileDataObjectByFilenameAndUserTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.filedataobjecttasks.InsertFileDataObjectTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.filedataobjecttasks.UpdateFileDataObjectTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.posttasks.PostDataAuthenticatedToUrlTask;
import edu.uoc.mistic.tfm.jherranzm.ui.adapters.ReceivedInvoicesRecyclerViewAdapter;
import edu.uoc.mistic.tfm.jherranzm.util.TFMSecurityManager;
import edu.uoc.mistic.tfm.jherranzm.util.UIDGenerator;
import edu.uoc.mistic.tfm.jherranzm.util.UtilDocument;
import edu.uoc.mistic.tfm.jherranzm.util.UtilEncryptInvoice;
import edu.uoc.mistic.tfm.jherranzm.util.UtilFacturae;
import edu.uoc.mistic.tfm.jherranzm.util.UtilValidator;
import edu.uoc.mistic.tfm.jherranzm.vo.EncryptedInvoice;
import es.facturae.facturae.v3.facturae.Facturae;

public class ReceivedInvoicesRecyclerViewActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = ReceivedInvoicesRecyclerViewActivity.class.getSimpleName();

    // Widgets
    private RecyclerView.Adapter mAdapter;

    // Vars
    private List<FileDataObject> signedInvoices;

    // Security
    private TFMSecurityManager tfmSecurityManager;

    // Context
    private static WeakReference<Context> sContextReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.received_invoices_recycler_view);

        tfmSecurityManager = TFMSecurityManager.getInstance();

        sContextReference = new WeakReference<Context>(this);
        signedInvoices = getFileInvoicesInSystem();

        initView();
    }

    private void initView(){

        ProgressBar spinner = findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);

        RecyclerView mRecyclerView = findViewById(R.id.local_invoices_rv);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);


        mAdapter = new ReceivedInvoicesRecyclerViewAdapter(signedInvoices);
        TextView textViewNumberItems = findViewById(R.id.textViewNumInvoiceFilesInSystem);
        textViewNumberItems.setText(String.format(Locale.getDefault(), "Number of File Invoices in System [%d]", signedInvoices.size()));


        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);


        spinner.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((ReceivedInvoicesRecyclerViewAdapter) mAdapter).setOnItemClickListener(
                new ReceivedInvoicesRecyclerViewAdapter.ReceivedInvoicesClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        Log.i(TAG, String.format(" Clicked on Item %d", position));
                        Toast.makeText(sContextReference.get(),
                                String.format("Invoice %s", signedInvoices.get(position).getFileName()),
                                Toast.LENGTH_SHORT).show();

                        customDialog("Verify and Upload Invoice?",
                                String.format("Do you want to process invoice %s", signedInvoices.get(position).getFileName()),
                                "cancel",
                                "ok",
                                position);

                    }
                });
    }

    private Document getDocumentFromSignedInvoice(int position) throws IOException {
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "Download/" + signedInvoices.get(position).getFileName());

        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        Toast.makeText(sContextReference.get(), "Loading signed file...", Toast.LENGTH_SHORT).show();
        InputStream isSignedInvoice = new FileInputStream(file);
        return UtilDocument.getDocument(isSignedInvoice);
    }

    private String getStringFromSignedInvoice(int position) throws IOException {
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "Download/" + signedInvoices.get(position).getFileName());

        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        Toast.makeText(sContextReference.get(), "Loading signed file...", Toast.LENGTH_SHORT).show();
        InputStream isSignedInvoice = new FileInputStream(file);
        return IOUtils.toString(isSignedInvoice);
    }

    private byte[] getByteArrayFromSignedInvoice(int position) throws IOException {
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "Download/" + signedInvoices.get(position).getFileName());

        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        Toast.makeText(sContextReference.get(), "Loading signed file...", Toast.LENGTH_SHORT).show();
        InputStream isSignedInvoice = new FileInputStream(file);

        return IOUtils.toByteArray(isSignedInvoice);
    }

    private void validateAndUploadSignedInvoice(int position) {
        try {
            Log.i(TAG, String.format("Retrieve document from invoice file in position [%d]", position));
            Document doc = getDocumentFromSignedInvoice(position);

            Toast.makeText(sContextReference.get(), "Invoice processed!", Toast.LENGTH_SHORT).show();

            Log.i(TAG, String.format("Validating document from invoice file in position [%d] ...", position));
            boolean valid = UtilValidator.validateSignedInvoice(doc);

            if(!valid){
                //Toast.makeText(mContext, "ERROR : La firma NO es válida!", Toast.LENGTH_LONG).show();
                Log.i(TAG, Constants.ALERT_INVOICE_SIGNATURE_NOT_VALID);
                alertShow(Constants.ALERT_INVOICE_SIGNATURE_NOT_VALID);
            }else{
                Log.i(TAG, String.format("Valid signed document! [%d] ...", position));
                Toast.makeText(sContextReference.get(), "Valid signed document!", Toast.LENGTH_SHORT).show();

                Document document = UtilDocument.removeSignature(doc);
                Log.i(TAG, String.format("Signature erased! [%d] ...", position));
                Toast.makeText(sContextReference.get(), "Signature erased!", Toast.LENGTH_SHORT).show();

                Log.i(TAG, String.format("Retrieving invoice data! [%d] ...", position));
                Toast.makeText(sContextReference.get(), "Retrieving invoice data...", Toast.LENGTH_SHORT).show();
                Facturae facturae = UtilFacturae.getFacturaeFromFactura(UtilDocument.documentToString(document));
                if(facturae == null){
                    throw new Exception("ERROR: Invoice is NOT correct!");
                }

                showInfoOfReceivedInvoice(document);

                Toast.makeText(sContextReference.get(), "Encrypting invoice data...", Toast.LENGTH_SHORT).show();


                String UIDInvoiceHash = UIDGenerator.generate(facturae);
                Log.i(TAG, String.format("UIDInvoiceHash : [%s]", UIDInvoiceHash));


                boolean invoiceBackedUp = false;
                if (tfmSecurityManager.isServerOnLine()) {
                    invoiceBackedUp = encryptAndUploadInvoice(position, facturae, UIDInvoiceHash);
                }

                boolean ret = EnvelopedSignature.signXMLFile(document);
                Log.i(TAG, String.format("EnvelopedSignature.signXMLFile...%s", ret ? "Signed!!" : "NOT signed..."));

                String signedInvoiceFile = getStringFromSignedInvoice(position);

                InvoiceDataService invoiceDataService = InvoiceDataService.getInstance(this);
                invoiceDataService.saveInvoiceDataInLocalDatabase(facturae, UIDInvoiceHash, signedInvoiceFile, invoiceBackedUp);

                updateFileDataObjectInLocalDatabase(position);

            } // if(valid)
        }catch (Exception e){
            Toast.makeText(sContextReference.get(), String.format("ERROR:%s", e.getMessage()), Toast.LENGTH_LONG).show();
            Log.e(TAG, String.format("ERROR:%s", e.getMessage()));
        }
    }

    private void updateFileDataObjectInLocalDatabase(int position) {

        try {
            GetFileDataObjectByFilenameAndUserTask getFileDataObjectByFilenameAndUserTask = new GetFileDataObjectByFilenameAndUserTask(this);
            FileDataObject existing = getFileDataObjectByFilenameAndUserTask
                    .execute(signedInvoices.get(position).getFileName(), tfmSecurityManager.getEmailUserLogged())
                    .get();

            existing.setStatus(Constants.FILE_VALID);

            UpdateFileDataObjectTask updateFileDataObjectTask = new UpdateFileDataObjectTask(this, existing);
            FileDataObject updated = updateFileDataObjectTask.execute().get();

            Log.i(TAG, String.format("FileDataObject updated : %s", updated.toString()));

        } catch (Exception e) {
            Toast.makeText(sContextReference.get(), String.format("ERROR:%s", e.getMessage()), Toast.LENGTH_LONG).show();
            Log.e(TAG, String.format("ERROR:%s", e.getMessage()));
        }

    }

    private boolean encryptAndUploadInvoice(
            int position,
            Facturae facturae,
            String UIDInvoiceHash)
            throws
            Exception {

        UtilEncryptInvoice utilEncryptInvoice = new UtilEncryptInvoice();
        EncryptedInvoice encryptedInvoice = utilEncryptInvoice.getEncryptedInvoice(getByteArrayFromSignedInvoice(position), facturae);


        // Prepare data to upload to server
        Map<String, String> params = new HashMap<>();
        params.put("uidfactura", (UIDInvoiceHash == null ? "---" : UIDInvoiceHash));
        params.put("tax_identification_number", encryptedInvoice.getTaxIdentificationNumberEncrypted());
        params.put("invoice_number", encryptedInvoice.getInvoiceNumberEncrypted());
        params.put("corporate_name", encryptedInvoice.getCorporateNameEncrypted());

        params.put("total", encryptedInvoice.getTotalEncrypted());
        params.put("total_tax_outputs", encryptedInvoice.getTotalTaxOutputsEncrypted());
        params.put("total_gross_amount", encryptedInvoice.getTotalGrossAmountEncrypted());

        params.put("issue_data", encryptedInvoice.getDataEncrypted());
        params.put("file", encryptedInvoice.getSignedInvoiceEncrypted());
        params.put("iv", encryptedInvoice.getIvStringEnc());
        params.put("key", encryptedInvoice.getSimKeyStringEnc());

        PostDataAuthenticatedToUrlTask getData = new PostDataAuthenticatedToUrlTask(params);

        String res = getData.execute(Constants.URL_FACTURAS).get();
        Log.i(TAG, String.format("Response from server : %s", res));

        JSONObject receivedInvoice = new JSONObject(res);
        String id = receivedInvoice.getString("id");

        boolean invoiceBackedUp = false;

        String message = "";

        if (getData.getResponseCode() == HttpURLConnection.HTTP_OK){

            infoShow(String.format(Constants.INFO_INVOICE_CORRECTLY_BACKED_UP_IN_SERVER, id));
            invoiceBackedUp = true;

        }else if (getData.getResponseCode() == HttpURLConnection.HTTP_CONFLICT){

            message += Constants.CR_LF + String.format(Constants.ALERT_INVOICE_ALREADY_IN_SERVER, id);
            alertShow(message);
            invoiceBackedUp = true;

        }else if (getData.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR){

            message += Constants.CR_LF + "ERROR: Server error.";
            alertShow(message);
        }

        return invoiceBackedUp;
    }



    private void customDialog(
            String title,
            String message,
            final String cancelMethod,
            final String okMethod,
            final int position){

        final android.support.v7.app.AlertDialog.Builder builderSingle = new android.support.v7.app.AlertDialog.Builder(this);
        builderSingle.setIcon(R.mipmap.ic_check);
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
                            FileDataObject fdo = signedInvoices.get(position);
                            fdo.setStatus(Constants.FILE_VALID);
                            signedInvoices.set(position, fdo);
                            mAdapter.notifyItemChanged(position);
                        }
                    }
                });


        builderSingle.show();
    }

    private void infoShow(
            String message ) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "alertShow : You clicked on OK!");
                    }
                })
                .setTitle("Info")
                .setMessage(message)
                .setIcon(R.drawable.ic_launcher_background);
        AlertDialog alert = builder.create();
        alert.show();

    }

    private  void alertShow(
            String message ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "alertShow : You clicked on OK!");
                    }
                })
                .setTitle("Alert!")
                .setMessage(message)
                .setIcon(R.drawable.ic_launcher_background);

        AlertDialog alert = builder.create();
        alert.show();
    }

    private List<FileDataObject> getFileInvoicesInSystem(){
        String root_sd = Environment.getExternalStorageDirectory().toString();
        File file = new File( root_sd + "/Download" ) ;
        File list[] = file.listFiles();

        List<FileDataObject> signedInvoices = new ArrayList<>();

        for (File f : list) {
            //Log.i(TAG, f.getName());
            //Log.i(TAG, tfmSecurityManager.getEmailUserLogged());
            FileDataObject obj = new FileDataObject(f.getName(), tfmSecurityManager.getEmailUserLogged(), Constants.FILE_PENDING);

            try {
                // Is the file already in local database
                GetFileDataObjectByFilenameAndUserTask getFileDataObjectByFilenameTask = new GetFileDataObjectByFilenameAndUserTask(this);
                FileDataObject existing = getFileDataObjectByFilenameTask.execute(f.getName(), tfmSecurityManager.getEmailUserLogged()).get();

                if(existing == null){
                    InsertFileDataObjectTask insertFileDataObjectTask = new InsertFileDataObjectTask(this, obj);
                    FileDataObject inserted = insertFileDataObjectTask.execute().get();
                    //Log.i(TAG, String.format("%s inserted in local database ", inserted.getFileName()));
                    signedInvoices.add(inserted);
                }else{
                    //Log.i(TAG, String.format("%s ALREADY in local database ", obj.getFileName()));
                    signedInvoices.add(existing);
                }

            } catch (ExecutionException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : " + e.getClass().getCanonicalName() + " : "+ e.getLocalizedMessage() + " : " + e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : " + e.getClass().getCanonicalName() + " : "+ e.getLocalizedMessage() + " : " + e.getMessage());
            }

        }

        return signedInvoices;

    }

    private void showInfoOfReceivedInvoice(Document document) throws Exception {
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

    private void infoDialog(
            String title,
            String message,
            final String okMethod){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_check);
        builder.setTitle("Info");
        builder.setMessage(message);


        builder.setPositiveButton(
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


        builder.show();
    }
}
