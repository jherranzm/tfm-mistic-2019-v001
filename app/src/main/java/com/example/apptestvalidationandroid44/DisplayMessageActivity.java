package com.example.apptestvalidationandroid44;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.apptestvalidationandroid44.model.Invoice;

import java.util.ArrayList;

public class DisplayMessageActivity extends AppCompatActivity {

    private final static String TAG = "DISPLAY_MESSAGE_ACT";
    private final static String CR_LF = "\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);


        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        ArrayList<Invoice> invoices = (ArrayList<Invoice>)intent.getSerializableExtra(MainActivity.INVOICE_LIST);
        Log.i(TAG, "Número de factures rebudes: " + invoices.size());
        String txtInvoices="";
        for(int k = 0; k<invoices.size(); k++){
            txtInvoices += invoices.get(k).getTaxIdentificationNumber() + CR_LF;
        }

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.editText3);
        textView.setText(txtInvoices+message);
    }
}
