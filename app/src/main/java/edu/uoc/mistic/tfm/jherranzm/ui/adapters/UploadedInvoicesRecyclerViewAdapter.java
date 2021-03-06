package edu.uoc.mistic.tfm.jherranzm.ui.adapters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import edu.uoc.mistic.tfm.jherranzm.R;
import edu.uoc.mistic.tfm.jherranzm.model.Invoice;

public class UploadedInvoicesRecyclerViewAdapter extends RecyclerView
        .Adapter<UploadedInvoicesRecyclerViewAdapter
        .DataObjectHolder> {
    private final static String TAG = UploadedInvoicesRecyclerViewAdapter.class.getSimpleName();
    private final List<Invoice> mDataset;
    private static UploadedInvoicesClickListener myClickListener;
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        final TextView taxIdentificationNumber;
        final TextView corporateName;
        final TextView invoiceNumber;
        final TextView totalAmount;
        final TextView totalTaxOutputs;
        final TextView issueDate;
        final Button buttonLocalDatabase;

        DataObjectHolder(View itemView) {
            super(itemView);
            taxIdentificationNumber = itemView.findViewById(R.id.textViewTaxItentificationNumber);
            corporateName = itemView.findViewById(R.id.textViewCorporateName);
            invoiceNumber = itemView.findViewById(R.id.textViewInvoiceNumber);
            totalAmount = itemView.findViewById(R.id.textViewTotalAmount);
            totalTaxOutputs = itemView.findViewById(R.id.textViewTotalTaxOutputs);
            issueDate = itemView.findViewById(R.id.textViewIssueDate);
            buttonLocalDatabase = itemView.findViewById(R.id.buttonLocalDatabase);

            Drawable iconFileDownloaded = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_file_download);
            if (iconFileDownloaded != null) {
                iconFileDownloaded.setBounds(0, 0, iconFileDownloaded.getMinimumWidth(),
                        iconFileDownloaded.getMinimumHeight());
                buttonLocalDatabase.setCompoundDrawables(iconFileDownloaded, null, null, null);
            }

            Log.i(TAG, "Adding Listener");
            buttonLocalDatabase.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(UploadedInvoicesClickListener myClickListener) {
        UploadedInvoicesRecyclerViewAdapter.myClickListener = myClickListener;
    }

    public UploadedInvoicesRecyclerViewAdapter(List<Invoice> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.uploaded_invoices_recycler_view_item, parent, false);

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        holder.taxIdentificationNumber.setText(String.format(
                Locale.forLanguageTag("es-ES"),
                "TIN: %s", mDataset.get(position).getTaxIdentificationNumber()));
        holder.corporateName.setText(String.format(
                Locale.forLanguageTag("es-ES"),
                "%s", mDataset.get(position).getCorporateName()));
        holder.invoiceNumber.setText(String.format(
                Locale.forLanguageTag("es-ES"),
                "Invoice: %s", mDataset.get(position).getInvoiceNumber()));
        holder.totalAmount.setText(String.format(
                Locale.forLanguageTag("es-ES"),
                "Total: %.2f", mDataset.get(position).getInvoiceTotal()));
        holder.totalTaxOutputs.setText(String.format(
                Locale.forLanguageTag("es-ES"),
                "Taxes: %.2f", mDataset.get(position).getTotalTaxOutputs()));
        holder.issueDate.setText(String.format(
                Locale.forLanguageTag("es-ES"),
                "Date: %s", DATE_FORMAT.format(mDataset.get(position).getIssueDate())));

        holder.buttonLocalDatabase.setText((mDataset.get(position).isInLocalDatabase()) ? "In Local Database" : "NOT in Local Database" );
        holder.buttonLocalDatabase.setBackgroundColor((mDataset.get(position).isInLocalDatabase()) ? Color.GREEN : Color.parseColor("#F7941E") );
        holder.buttonLocalDatabase.setEnabled(!mDataset.get(position).isInLocalDatabase());
    }

    public void addItem(Invoice dataObj, int index) {
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

    public interface UploadedInvoicesClickListener {
        void onItemClick(int position, View v);
    }
}