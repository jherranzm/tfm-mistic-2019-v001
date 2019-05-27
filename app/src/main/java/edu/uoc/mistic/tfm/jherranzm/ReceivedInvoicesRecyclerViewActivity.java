package edu.uoc.mistic.tfm.jherranzm;

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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.crypto.AsymmetricEncryptor;
import edu.uoc.mistic.tfm.jherranzm.crypto.EnvelopedSignature;
import edu.uoc.mistic.tfm.jherranzm.crypto.SymmetricEncryptor;
import edu.uoc.mistic.tfm.jherranzm.model.FileDataObject;
import edu.uoc.mistic.tfm.jherranzm.services.InvoiceDataDataManagerService;
import edu.uoc.mistic.tfm.jherranzm.tasks.filedataobjecttasks.GetFileDataObjectByFilenameTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.filedataobjecttasks.InsertFileDataObjectTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.filedataobjecttasks.UpdateFileDataObjectTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.posttasks.PostDataAuthenticatedToUrlTask;
import edu.uoc.mistic.tfm.jherranzm.util.RandomStringGenerator;
import edu.uoc.mistic.tfm.jherranzm.util.TFMSecurityManager;
import edu.uoc.mistic.tfm.jherranzm.util.UIDGenerator;
import edu.uoc.mistic.tfm.jherranzm.util.UtilDocument;
import edu.uoc.mistic.tfm.jherranzm.util.UtilFacturae;
import edu.uoc.mistic.tfm.jherranzm.util.UtilValidator;
import es.facturae.facturae.v3.facturae.Facturae;

public class ReceivedInvoicesRecyclerViewActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = ReceivedInvoicesRecyclerViewActivity.class.getSimpleName();

    private static final String INFO_INVOICE_CORRECTLY_BACKED_UP_IN_SERVER = "Remote backup. INFO: Invoice  %s correctly backed up!";
    private static final String ALERT_INVOICE_ALREADY_IN_SERVER = "Remote backup. ALERT: Invoice  %s already backed up in system!";
    private static final String ALERT_INVOICE_SIGNATURE_NOT_VALID = "ALERT: Invoice signature NOT valid!";

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

        sContextReference = new WeakReference<Context>(this);

        initView();
    }

    private void initView(){

        ProgressBar spinner = findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);

        RecyclerView mRecyclerView = findViewById(R.id.local_invoices_rv);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        signedInvoices = getFileInvoicesInSystem();
        mAdapter = new ReceivedInvoicesRecyclerViewAdapter(signedInvoices);
        TextView textViewNumberItems = findViewById(R.id.textViewNumInvoiceFilesInSystem);
        textViewNumberItems.setText(String.format(Locale.getDefault(), "Number of File Invoices in System [%d]", signedInvoices.size()));


        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

        tfmSecurityManager = TFMSecurityManager.getInstance();
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

        byte[] baInvoiceSigned = IOUtils.toByteArray(isSignedInvoice);
        Toast.makeText(sContextReference.get(),
                String.format(
                        new Locale("es-ES"),
                        "Info: file signed long : [%d]", baInvoiceSigned.length),
                Toast.LENGTH_SHORT).show();

        isSignedInvoice = new FileInputStream(file);
        return UtilDocument.getDocument(isSignedInvoice);
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
            Document doc = getDocumentFromSignedInvoice(position);

            Toast.makeText(sContextReference.get(), "Invoice processed!", Toast.LENGTH_SHORT).show();

            boolean valid = UtilValidator.validateSignedInvoice(doc);

            if(!valid){
                //Toast.makeText(mContext, "ERROR : La firma NO es válida!", Toast.LENGTH_LONG).show();
                alertShow(ALERT_INVOICE_SIGNATURE_NOT_VALID);
            }else{
                Toast.makeText(sContextReference.get(), "Valid signed document!", Toast.LENGTH_SHORT).show();

                Document document = UtilDocument.removeSignature(doc);
                Toast.makeText(sContextReference.get(), "Signature erased!", Toast.LENGTH_SHORT).show();


                Toast.makeText(sContextReference.get(), "Retrieving invoice data...", Toast.LENGTH_SHORT).show();
                Facturae facturae = UtilFacturae.getFacturaeFromFactura(UtilDocument.documentToString(document));
                if(facturae == null){
                    throw new Exception("ERROR: Invoice is NOT correct!");
                }

                Toast.makeText(sContextReference.get(), "Encrypting invoice data...", Toast.LENGTH_SHORT).show();


                String UIDInvoiceHash = UIDGenerator.generate(facturae);
                Log.i(TAG, String.format("UIDInvoiceHash : [%s]", UIDInvoiceHash));


                boolean invoiceBackedUp = encryptAndUploadInvoice(position, facturae, UIDInvoiceHash);

                boolean ret = EnvelopedSignature.signXMLFile(document);
                Log.i(TAG, String.format("EnvelopedSignature.signXMLFile...%s", ret ? "Signed!!" : "NOT signed..."));

                InvoiceDataDataManagerService invoiceDataDataManagerService = InvoiceDataDataManagerService.getInstance(this);
                invoiceDataDataManagerService.saveInvoiceDataInLocalDatabase(facturae, UIDInvoiceHash, invoiceBackedUp);

                updateFileDataObjectInLocalDatabase(position);

            } // if(valid)
        }catch (Exception e){
            Toast.makeText(sContextReference.get(), String.format("ERROR:%s", e.getMessage()), Toast.LENGTH_LONG).show();
            Log.e(TAG, String.format("ERROR:%s", e.getMessage()));
        }
    }

    private void updateFileDataObjectInLocalDatabase(int position) {

        try {
            GetFileDataObjectByFilenameTask getFileDataObjectByFilenameTask = new GetFileDataObjectByFilenameTask(this);
            FileDataObject existing = getFileDataObjectByFilenameTask.execute(signedInvoices.get(position).getFileName()).get();

            existing.setProcessed(true);

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


        // Data encrypting
        Log.i(TAG, "Encryption...");
        RandomStringGenerator rsg = new RandomStringGenerator();

        // IV and Symmetric Key
        String iv = rsg.getRandomString(16);
        Log.i(TAG, String.format("iv     : [%s]", iv));
        String simKey = rsg.getRandomString(16);
        Log.i(TAG, String.format("simKey : [%s]", simKey));


        SymmetricEncryptor simEnc = new SymmetricEncryptor();
        simEnc.setIv(iv);
        simEnc.setKey(simKey);

        if(facturae.getParties() == null){
            throw new Exception("ERROR: invoice has NOT parties.");
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

        // issue Date
        String dataEncrypted   = simEnc.encrypt(
                ""+facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getIssueDate(),
                tfmSecurityManager.getSecretFromKeyInKeyStore(Constants.ISSUE_DATE)
        );

        // Encrypt file
        String signedInvoiceEncrypted   = simEnc.encrypt(getByteArrayFromSignedInvoice(position));

        // Encrypt iv and symmetric key with public key
        byte[] ivBytesEnc = AsymmetricEncryptor.encryptData(iv.getBytes(), tfmSecurityManager.getCertificate());
        String ivStringEnc = new String(Base64.encode(ivBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);

        byte[] simKeyBytesEnc = AsymmetricEncryptor.encryptData(simKey.getBytes(), tfmSecurityManager.getCertificate());
        String simKeyStringEnc = new String(Base64.encode(simKeyBytesEnc, Base64.NO_WRAP), StandardCharsets.UTF_8);


        // Prepare data to upload to server
        Map<String, String> params = new HashMap<>();
        params.put("uidfactura", (UIDInvoiceHash == null ? "---" : UIDInvoiceHash));
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
        Log.i(TAG, String.format("Response from server : %s", res));

        JSONObject receivedInvoice = new JSONObject(res);
        String id = receivedInvoice.getString("id");

        boolean invoiceBackedUp = false;

        String message = "";

        if (getData.getResponseCode() == HttpURLConnection.HTTP_OK){

            infoShow(String.format(INFO_INVOICE_CORRECTLY_BACKED_UP_IN_SERVER, id));
            invoiceBackedUp = true;

        }else if (getData.getResponseCode() == HttpURLConnection.HTTP_CONFLICT){

            message += Constants.CR_LF + String.format(ALERT_INVOICE_ALREADY_IN_SERVER, id);
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
                            fdo.setProcessed(true);
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
            Log.i(TAG, f.getName());
            FileDataObject obj = new FileDataObject(f.getName(), "pepe");
            //signedInvoices.add(obj);

            try {
                GetFileDataObjectByFilenameTask getFileDataObjectByFilenameTask = new GetFileDataObjectByFilenameTask(this);
                FileDataObject existing = getFileDataObjectByFilenameTask.execute(f.getName()).get();

                if(existing == null){
                    InsertFileDataObjectTask insertFileDataObjectTask = new InsertFileDataObjectTask(this, obj);
                    FileDataObject inserted = insertFileDataObjectTask.execute().get();
                    Log.i(TAG, String.format("%s inserted in local database ", inserted.getFileName()));
                    signedInvoices.add(inserted);
                }else{
                    Log.i(TAG, String.format("%s ALREADY in local database ", obj.getFileName()));
                    signedInvoices.add(existing);
                }

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        return signedInvoices;

    }
}
