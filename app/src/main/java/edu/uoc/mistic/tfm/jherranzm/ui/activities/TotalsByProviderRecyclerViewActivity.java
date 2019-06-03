package edu.uoc.mistic.tfm.jherranzm.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.Serializable;
import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.R;
import edu.uoc.mistic.tfm.jherranzm.model.TotalByProviderVO;
import edu.uoc.mistic.tfm.jherranzm.services.InvoiceDataService;
import edu.uoc.mistic.tfm.jherranzm.ui.adapters.TotalsByProviderRecyclerViewAdapter;

public class TotalsByProviderRecyclerViewActivity extends AppCompatActivity {

    private static final String TAG = "TotalsByProviderRVA";

    // Widgets
    private RecyclerView.Adapter mAdapter;

    private List<TotalByProviderVO> totals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.totals_by_provider_recycler_view);

        initView();

    }

    private void initView() {
        RecyclerView mRecyclerView;
        RecyclerView.LayoutManager mLayoutManager;

        mRecyclerView = findViewById(R.id.totals_by_provider_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        totals = InvoiceDataService.getTotalsByProvider(this);
        mAdapter = new TotalsByProviderRecyclerViewAdapter(totals);

        Button buttonShowGraph = findViewById(R.id.buttonShowGraph);

        buttonShowGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ChartActivity.class);
                intent.putExtra("totals", (Serializable) totals);
                startActivity(intent);
            }
        });

        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((TotalsByProviderRecyclerViewAdapter) mAdapter).setOnItemClickListener(
                new TotalsByProviderRecyclerViewAdapter.TotalsByProviderClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        Log.i(TAG, " Clicked on Item " + position);
                        Toast.makeText(getBaseContext(), "TIN : " + totals.get(position).taxIdentificationNumber, Toast.LENGTH_SHORT).show();
                        Log.i(TAG, " Clicked on Item " + totals.get(position).toString());


                    }
                });
    }
}
