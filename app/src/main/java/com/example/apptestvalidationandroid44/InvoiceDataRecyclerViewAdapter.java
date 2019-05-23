package com.example.apptestvalidationandroid44;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.apptestvalidationandroid44.model.InvoiceData;
import com.example.apptestvalidationandroid44.services.InvoiceDataDataManagerService;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class InvoiceDataRecyclerViewAdapter
        extends RecyclerView.Adapter<InvoiceDataRecyclerViewAdapter.DataObjectHolder> {

    private final static String LOG_TAG = "InvoiceDataRVA";
    private List<InvoiceData> mDataset;
    private static InvoiceDataClickListener myClickListener;
    private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView taxIdentificationNumber;
        TextView corporateName;
        TextView invoiceNumber;
        TextView totalAmount;
        TextView totalTaxOutputs;
        TextView issueDate;
        CheckBox cbBackedUpInServer;
        TextView buttonViewOption;

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

            Log.i(LOG_TAG, "Adding Listener");
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

    InvoiceDataRecyclerViewAdapter(List<InvoiceData> myDataset) {
        mDataset = myDataset;
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

                //inflating menu from xml resource
                popup.inflate(R.menu.invoice_data_popup_menu);

                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.deleteInvoiceDataOption:
                                //handle menu1 click
                                AlertDialog.Builder alertbox = new AlertDialog.Builder(view.getRootView().getContext());
                                alertbox.setMessage("Do your want to delete this invoice?");
                                alertbox.setTitle("Warning");
                                alertbox.setIcon(R.drawable.ic_warning_name);

                                alertbox.setNegativeButton(
                                        "Cancel",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Log.d("deleteInvoiceDataOption", "onClick: Cancel Called. Operation cancelled!");
                                            }
                                        });

                                alertbox.setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Log.d("deleteInvoiceDataOption", "onClick: OK Called.");
                                                InvoiceDataDataManagerService.deleteInvoiceData(mDataset.get(position));
                                                mDataset.remove(position);
                                                notifyItemRemoved(position);
                                                // TODO: refresh info of number of invoices in system.
                                            }
                                        });

                                alertbox.show();
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