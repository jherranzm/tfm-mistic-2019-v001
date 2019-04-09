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
        StringBuilder txtInvoices = new StringBuilder();
        if(invoices == null){
            // Do nothing
            Log.i(TAG, "No se han recibido facturas...");
        }else {
            Log.i(TAG, "Número de factures rebudes: " + invoices.size());
            for (int k = 0; k < invoices.size(); k++) {
                txtInvoices.append(invoices.get(k).getTaxIdentificationNumber()).append(CR_LF);
            }
        }

        txtInvoices.append(message);

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.editText3);
        textView.setText(txtInvoices.toString());
    }
}
