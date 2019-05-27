package edu.uoc.mistic.tfm.jherranzm;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.model.TotalByProviderVO;

public class ChartActivity extends AppCompatActivity {

    private static final String TAG = ChartActivity.class.getSimpleName();
    private PieChart thePieChart;

    private List<TotalByProviderVO> totals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        Log.i(TAG, String.format("onCreate...[%s]", ""));

        thePieChart = findViewById(R.id.totals_by_provider_pie_chart);
        totals = (List<TotalByProviderVO>) getIntent().getSerializableExtra("totals");

        Description description = new Description();
        description.setText("Totals by Provider");
        description.setTextSize(32);
        description.setPosition(600, 100);

        thePieChart.setDescription(description);
        thePieChart.setHoleRadius(25f);
        thePieChart.setTransparentCircleAlpha(10);
        thePieChart.setCenterText("By Provider");
        thePieChart.setDrawEntryLabels(true);

        addDataSet();
    }

    private void addDataSet(){

        List<PieEntry> yEntries = new ArrayList<>();
        List<String> xEntries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        int k = 0;
        for(TotalByProviderVO vo : totals){
            yEntries.add(new PieEntry(
                    (new Double(vo.totalAmount)).floatValue(),
                    vo.taxIdentificationNumber +" - "+ vo.corporateName )
            );
            //xEntries.add(vo.corporateName);
        }
        //colors.add(R.color.colorPrimary);
        //colors.add(R.color.colorPrimaryDark);
        //colors.add(R.color.colorAccent);

        PieDataSet pieDataSet = new PieDataSet(yEntries, "Totals by Provider");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(22);
        pieDataSet.setColors(
                ContextCompat.getColor(this, R.color.red),
                ContextCompat.getColor(this, R.color.green),
                ContextCompat.getColor(this, R.color.yellow),
                ContextCompat.getColor(this, R.color.pink),
                ContextCompat.getColor(this, R.color.blue));


        PieData pieData = new PieData(pieDataSet);

        thePieChart.setData(pieData);
        thePieChart.invalidate();
        Log.d(TAG, String.format("width :%d", thePieChart.getWidth() / 2));
        Legend l = thePieChart.getLegend();
        l.setTextSize(12f);
    }
}
