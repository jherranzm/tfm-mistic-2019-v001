package com.example.apptestvalidationandroid44;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.apptestvalidationandroid44.model.TotalByProviderVO;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TotalsByProviderRecyclerViewAdapter extends RecyclerView
        .Adapter<TotalsByProviderRecyclerViewAdapter
        .DataObjectHolder> {
    private final static String TAG = "TotalsByProviderRVA";
    private List<TotalByProviderVO> mDataset;
    private static TotalsByProviderClickListener myClickListener;
    private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        TextView textViewTaxIdentificationNumber;
        TextView textViewCorporateName;
        TextView textViewTotalAmount;

        DataObjectHolder(View itemView) {
            super(itemView);
            textViewTaxIdentificationNumber = itemView.findViewById(R.id.textViewTaxItentificationNumber);
            textViewCorporateName = itemView.findViewById(R.id.corporateName);
            textViewTotalAmount = itemView.findViewById(R.id.textViewTotalAmount);
            Log.i(TAG, "Adding Listener");
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    void setOnItemClickListener(TotalsByProviderClickListener myClickListener) {
        TotalsByProviderRecyclerViewAdapter.myClickListener = myClickListener;
    }

    TotalsByProviderRecyclerViewAdapter(List<TotalByProviderVO> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.totals_by_provider_recycler_view_item, parent, false);

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {

        Log.i(TAG, String.format("Position : [%d], mDataset.get(%d) = %s", position, position, mDataset.get(position)));
        Log.i(TAG, String.format("Position : [%d], mDataset.get(%d).getTaxIdentificationNumber() = %s", position, position, mDataset.get(position).taxIdentificationNumber));

        holder.textViewTaxIdentificationNumber.setText(String.format(
                "TIN: %s", mDataset.get(position).taxIdentificationNumber));
        holder.textViewCorporateName.setText(String.format(
                "Company: %s", mDataset.get(position).corporateName));
        holder.textViewTotalAmount.setText(String.format(
                "Total: %f", mDataset.get(position).totalAmount));
    }

    public void addItem(TotalByProviderVO dataObj, int index) {
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

    public interface TotalsByProviderClickListener {
        void onItemClick(int position, View v);
    }
}