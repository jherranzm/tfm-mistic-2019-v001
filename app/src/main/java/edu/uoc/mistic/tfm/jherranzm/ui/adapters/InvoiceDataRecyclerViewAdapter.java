package edu.uoc.mistic.tfm.jherranzm.ui.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import org.json.JSONObject;
import org.w3c.dom.Document;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.uoc.mistic.tfm.jherranzm.R;
import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;
import edu.uoc.mistic.tfm.jherranzm.services.InvoiceDataService;
import edu.uoc.mistic.tfm.jherranzm.tasks.posttasks.PostDataAuthenticatedToUrlTask;
import edu.uoc.mistic.tfm.jherranzm.util.TFMSecurityManager;
import edu.uoc.mistic.tfm.jherranzm.util.UIDGenerator;
import edu.uoc.mistic.tfm.jherranzm.util.UtilDocument;
import edu.uoc.mistic.tfm.jherranzm.util.UtilEncryptInvoice;
import edu.uoc.mistic.tfm.jherranzm.util.UtilFacturae;
import edu.uoc.mistic.tfm.jherranzm.vo.EncryptedInvoice;
import es.facturae.facturae.v3.facturae.Facturae;

public class InvoiceDataRecyclerViewAdapter
        extends RecyclerView.Adapter<InvoiceDataRecyclerViewAdapter.DataObjectHolder> {

    private final static String TAG = InvoiceDataRecyclerViewAdapter.class.getSimpleName();
    private final List<InvoiceData> mDataset;
    private static InvoiceDataClickListener myClickListener;
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");

    private final WeakReference<Activity> mActivityRef;

    // Security
    private TFMSecurityManager tfmSecurityManager;


    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        final TextView taxIdentificationNumber;
        final TextView corporateName;
        final TextView invoiceNumber;
        final TextView totalAmount;
        final TextView totalTaxOutputs;
        final TextView issueDate;
        final CheckBox cbBackedUpInServer;
        final TextView buttonViewOption;

        DataObjectHolder(View itemView) {
            super(itemView);
            taxIdentificationNumber = itemView.findViewById(R.id.textViewTaxItentificationNumber);
            corporateName = itemView.findViewById(R.id.textViewCorporateName);
            invoiceNumber = itemView.findViewById(R.id.textViewInvoiceNumber);
            totalAmount = itemView.findViewById(R.id.textViewTotalAmount);
            totalTaxOutputs = itemView.findViewById(R.id.textViewTotalTaxOutputs);
            issueDate = itemView.findViewById(R.id.textViewIssueDate);

            cbBackedUpInServer = itemView.findViewById(R.id.checkBoxRemoteBackUp);
            buttonViewOption = itemView.findViewById(R.id.textViewOptions);

            Log.i(TAG, "Adding Listener");
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    void setOnItemClickListener(InvoiceDataClickListener myClickListener) {
        InvoiceDataRecyclerViewAdapter.myClickListener = myClickListener;
    }

    public InvoiceDataRecyclerViewAdapter(Activity activity, List<InvoiceData> myDataset) {
        mActivityRef = new WeakReference<>(activity);
        mDataset = myDataset;
        tfmSecurityManager = TFMSecurityManager.getInstance();
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.invoice_data_recycler_view_item, parent, false);

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(final DataObjectHolder holder, final int position) {
        holder.taxIdentificationNumber.setText(String.format(
                Locale.forLanguageTag("es-ES"),
                "%s", mDataset.get(position).getTaxIdentificationNumber()));
        holder.corporateName.setText(String.format(
                Locale.forLanguageTag("es-ES"),
                "%s", mDataset.get(position).getCorporateName()));
        holder.invoiceNumber.setText(String.format(
                Locale.forLanguageTag("es-ES"),
                "Invoice: %s", mDataset.get(position).getInvoiceNumber()));
        holder.totalAmount.setText(String.format(
                Locale.forLanguageTag("es-ES"),
                "%s", DECIMAL_FORMAT.format(mDataset.get(position).getTotalAmount())));
        holder.totalTaxOutputs.setText(String.format(
                Locale.forLanguageTag("es-ES"),
                "Taxes: %s", DECIMAL_FORMAT.format(mDataset.get(position).getTaxAmount())));
        holder.issueDate.setText(String.format(
                Locale.forLanguageTag("es-ES"),
                "Date: %s", DATE_FORMAT.format(mDataset.get(position).getIssueDate())));
        holder.cbBackedUpInServer.setChecked(mDataset.get(position).isBackedUp());

        holder.buttonViewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                //creating a popup menu
                PopupMenu popup = new PopupMenu(view.getRootView().getContext(), holder.buttonViewOption);

                Menu popupMenu = popup.getMenu();

                //inflating menu from xml resource
                popup.inflate(R.menu.invoice_data_popup_menu);

                if(mDataset.get(position).isBackedUp()){
                    popupMenu.findItem(R.id.uploadInvoiceDataOption).setEnabled(false);
                }else{
                    popupMenu.findItem(R.id.uploadInvoiceDataOption).setEnabled(true);
                }


                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.deleteInvoiceDataOption:
                                //handle menu1 click
                                AlertDialog.Builder alertBox = new AlertDialog.Builder(view.getRootView().getContext());
                                alertBox.setMessage("Do you want to DELETE this invoice?");
                                alertBox.setTitle("Warning");
                                alertBox.setIcon(R.drawable.ic_launcher_background);

                                alertBox.setNegativeButton(
                                        "Cancel",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Log.d("deleteInvoiceDataOption", "onClick: Cancel Called. Operation cancelled!");
                                            }
                                        });

                                alertBox.setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Log.d("deleteInvoiceDataOption", "onClick: OK Called.");

                                                InvoiceDataService.deleteInvoiceData(mActivityRef.get(), mDataset.get(position));

                                                mDataset.remove(position);
                                                notifyItemRemoved(position);
                                                mActivityRef.get().recreate();
                                                // TODO: refresh info of number of invoices in system.
                                            }
                                        });

                                alertBox.show();
                                break;

                            case R.id.uploadInvoiceDataOption:

                                // TODO : InvoiceData add field signedInvoiceFile

                                InvoiceData invoiceData = mDataset.get(position);

                                Log.i(TAG, String.format("invoiceData : [%s]", invoiceData.toString()));

                                String signedInvoiceFile = invoiceData.getSignedInvoiceFile();
                                Log.i(TAG, String.format("signedInvoiceFile : [%s]", signedInvoiceFile));

                                Document doc = UtilDocument.documentFromString(signedInvoiceFile);

                                Log.i(TAG, String.format("doc.getNodeName() : [%s]", doc.getDocumentElement()));

                                Document document = UtilDocument.removeSignature(doc);

                                Log.i(TAG, String.format("document.getNodeName() : [%s]", document.getDocumentElement()));

                                Facturae facturae = UtilFacturae.getFacturaeFromFactura(UtilDocument.documentToString(document));

                                Log.i(TAG, String.format("facturae : [%s]", facturae.getParties().getSellerParty().getLegalEntity().getCorporateName()));

                                String UIDInvoiceHash = UIDGenerator.generate(facturae);
                                Log.i(TAG, String.format("UIDInvoiceHash : [%s]", UIDInvoiceHash));


                                boolean invoiceBackedUp = false;
                                if (tfmSecurityManager.isServerOnLine()) {
                                    invoiceBackedUp = encryptAndUploadInvoice(signedInvoiceFile, facturae, UIDInvoiceHash);
                                }

                                //boolean invoiceBackedUp = false;
                                //if (tfmSecurityManager.isServerOnLine()) {
                                //    invoiceBackedUp = encryptAndUploadInvoice(position, facturae, UIDInvoiceHash);
                                //}

                                mDataset.get(position).setBackedUp(invoiceBackedUp);
                                mActivityRef.get().recreate();

                                break;
                        }
                        return false;
                    }
                });
                //displaying the popup
                popup.show();

            }
        });
    }

    private boolean encryptAndUploadInvoice(
            String signedInvoiceFile,
            Facturae facturae,
            String uidInvoiceHash) {

        boolean invoiceBackedUp = false;

        try {
            UtilEncryptInvoice utilEncryptInvoice = new UtilEncryptInvoice();
            EncryptedInvoice encryptedInvoice = utilEncryptInvoice.getEncryptedInvoice(signedInvoiceFile, facturae);


            // Prepare data to upload to server
            Map<String, String> params = new HashMap<>();
            params.put("uidfactura", (uidInvoiceHash == null ? "---" : uidInvoiceHash));
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

        } catch (Exception e) {
            e.printStackTrace();
        }

        return invoiceBackedUp;

    }

    private void infoShow(String message) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mActivityRef.get());
        builder
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "alertShow : You clicked on OK!");
                    }
                })
                .setTitle("Info")
                .setMessage(message)
                .setIcon(R.drawable.ic_launcher_background);
        android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    private  void alertShow(
            String message ) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mActivityRef.get());
        builder
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "alertShow : You clicked on OK!");
                    }
                })
                .setTitle("Alert!")
                .setMessage(message)
                .setIcon(R.drawable.ic_launcher_background);

        android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    public void addItem(InvoiceData dataObj, int index) {
        mDataset.add(dataObj);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        mDataset.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface InvoiceDataClickListener {
        void onItemClick(int position, View v);
    }


}