package edu.uoc.mistic.tfm.jherranzm.ui.adapters;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.R;
import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.model.FileDataObject;
import edu.uoc.mistic.tfm.jherranzm.util.TFMSecurityManager;

public class ReceivedInvoicesRecyclerViewAdapter extends RecyclerView
        .Adapter<ReceivedInvoicesRecyclerViewAdapter
        .DataObjectHolder> {
    private final static String TAG = ReceivedInvoicesRecyclerViewAdapter.class.getCanonicalName();
    private final List<FileDataObject> mDataset;
    private static ReceivedInvoicesClickListener theClickListener;


    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        final TextView fileName;
        final CardView cardView;
        final Button buttonProcess;

        DataObjectHolder(View itemView) {
            super(itemView);

            Log.i(TAG, "DataObjectHolder...");

            cardView = itemView.findViewById(R.id.cardViewFileDataObject);
            fileName = itemView.findViewById(R.id.textViewFileName);
            buttonProcess = itemView.findViewById(R.id.buttonProcess);
            Log.i(TAG, "Adding Listener");
            //itemView.setOnClickListener(this);

            cardView.setCardBackgroundColor(Color.LTGRAY);
            buttonProcess.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {

            theClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(ReceivedInvoicesClickListener aClickListener) {
        ReceivedInvoicesRecyclerViewAdapter.theClickListener = aClickListener;
    }

    public ReceivedInvoicesRecyclerViewAdapter(List<FileDataObject> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        Log.i(TAG, "onCreateViewHolder...");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.received_invoice_recyclerview_item, parent, false);

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder...");
        holder.fileName.setText(String.format("File: %s", mDataset.get(position).getFileName()));

        if(mDataset.get(position).getStatus() == Constants.FILE_VALID) {
            holder.cardView.setCardBackgroundColor(Color.GREEN);
            holder.buttonProcess.setVisibility(View.INVISIBLE);
        }else if(mDataset.get(position).getStatus() == Constants.FILE_PENDING) {
            holder.cardView.setCardBackgroundColor(Color.LTGRAY);
            holder.buttonProcess.setVisibility(View.VISIBLE);
            holder.buttonProcess.setEnabled(TFMSecurityManager.getInstance().isUserLogged());
        }
    }

    public void addItem(FileDataObject dataObj, int index) {
        Log.i(TAG, "addItem...");
        mDataset.add(dataObj);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        Log.i(TAG, "deleteItem...");
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