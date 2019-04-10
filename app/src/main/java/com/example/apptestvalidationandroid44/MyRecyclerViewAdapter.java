package com.example.apptestvalidationandroid44;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.apptestvalidationandroid44.model.Invoice;

import java.text.SimpleDateFormat;
import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView
        .Adapter<MyRecyclerViewAdapter
        .DataObjectHolder> {
    private static String LOG_TAG = "MyRecyclerViewAdapter";
    private List<Invoice> mDataset;
    private static MyClickListener myClickListener;
    private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        TextView taxItentificationNumber;
        TextView invoiceNumber;
        TextView totalAmount;
        TextView totalTaxOutputs;
        TextView issueDate;

        public DataObjectHolder(View itemView) {
            super(itemView);
            taxItentificationNumber = itemView.findViewById(R.id.textViewTaxItentificationNumber);
            invoiceNumber = itemView.findViewById(R.id.textViewInvoiceNumber);
            totalAmount = itemView.findViewById(R.id.textViewTotalAmount);
            totalTaxOutputs = itemView.findViewById(R.id.textViewTotalTaxOutputs);
            issueDate = itemView.findViewById(R.id.textViewIssueDate);
            Log.i(LOG_TAG, "Adding Listener");
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        MyRecyclerViewAdapter.myClickListener = myClickListener;
    }

    //public MyRecyclerViewAdapter(ArrayList<DataObject> myDataset) {mDataset = myDataset; }
    public MyRecyclerViewAdapter(List<Invoice> myDataset) {mDataset = myDataset; }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);

        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        holder.taxItentificationNumber.setText(String.format("TIN: %s", mDataset.get(position).getTaxIdentificationNumber()));
        holder.invoiceNumber.setText(String.format("Invoice: %s", mDataset.get(position).getInvoiceNumber()));
        holder.totalAmount.setText(String.format("Total: %f", mDataset.get(position).getInvoiceTotal()));
        holder.totalTaxOutputs.setText(String.format("Taxes: %f", mDataset.get(position).getTotalTaxOutputs()));
        holder.issueDate.setText(String.format("Date: %s", DATE_FORMAT.format(mDataset.get(position).getIssueDate())));
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

    public interface MyClickListener {
        void onItemClick(int position, View v);
    }
}