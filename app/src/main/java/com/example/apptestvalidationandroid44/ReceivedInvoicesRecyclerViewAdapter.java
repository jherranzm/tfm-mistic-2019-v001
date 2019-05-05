package com.example.apptestvalidationandroid44;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.apptestvalidationandroid44.model.FileDataObject;

import java.util.List;

public class ReceivedInvoicesRecyclerViewAdapter extends RecyclerView
        .Adapter<ReceivedInvoicesRecyclerViewAdapter
        .DataObjectHolder> {
    private final static String LOG_TAG = "LocalInvoicesRVA";
    private List<FileDataObject> mDataset;
    private static ReceivedInvoicesClickListener theClickListener;


    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        TextView fileName;

        DataObjectHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.textViewFileName);
            Log.i(LOG_TAG, "Adding Listener");
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            theClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    void setOnItemClickListener(ReceivedInvoicesClickListener aClickListener) {
        ReceivedInvoicesRecyclerViewAdapter.theClickListener = aClickListener;
    }

    ReceivedInvoicesRecyclerViewAdapter(List<FileDataObject> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.received_invoice_recyclerview_item, parent, false);

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        holder.fileName.setText(String.format("TIN: %s", mDataset.get(position).getFileName()));
    }

    public void addItem(FileDataObject dataObj, int index) {
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

    public interface ReceivedInvoicesClickListener {
        void onItemClick(int position, View v);
    }
}