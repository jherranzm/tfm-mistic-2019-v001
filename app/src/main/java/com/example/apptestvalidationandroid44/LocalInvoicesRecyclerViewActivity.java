package com.example.apptestvalidationandroid44;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.apptestvalidationandroid44.model.FileDataObject;

import java.util.ArrayList;
import java.util.List;

public class LocalInvoicesRecyclerViewActivity extends AppCompatActivity {
    private Context mContext;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static String LOG_TAG = "LocalInvoicesRVA";

    private List<FileDataObject> signedInvoices;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_invoices_recycler_view);

        mContext = getApplicationContext();
        mRecyclerView = findViewById(R.id.local_invoices_rv);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        signedInvoices = (ArrayList<FileDataObject>)getIntent().getSerializableExtra(MainActivity.FILE_LIST);
        mAdapter = new LocalInvoicesRecyclerViewAdapter(signedInvoices);

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
        ((LocalInvoicesRecyclerViewAdapter) mAdapter).setOnItemClickListener(
                new LocalInvoicesRecyclerViewAdapter.LocalInvoicesClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        Log.i(LOG_TAG, " Clicked on Item " + position);
                        Toast.makeText(mContext, "Factura " + signedInvoices.get(position).getFileName(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
